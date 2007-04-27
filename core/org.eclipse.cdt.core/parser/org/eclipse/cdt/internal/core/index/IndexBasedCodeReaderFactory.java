/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Andrew Ferguson (Symbian)
 * Anton Leherbauer (Wind River Systems)
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.IMacroCollector;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacro;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class IndexBasedCodeReaderFactory implements ICodeReaderFactory {
	public static interface CallbackHandler {
		boolean needToUpdate(IndexFileInfo fileInfo) throws CoreException;		
	}
	public static class IndexFileInfo {
		public final static int NOT_REQUESTED= 0;
		public final static int REQUESTED_IF_CONFIG_CHANGED= 1;
		public final static int REQUESTED= 2;
		
		private IndexFileInfo() {}
		private IMacro[] fMacros= null;

		public IIndexFile fFile= null;
		public int fRequested= 0;
	}
	private static class NeedToParseException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	private final static boolean CASE_SENSITIVE_FILES= !new File("a").equals(new File("A"));  //$NON-NLS-1$//$NON-NLS-2$
	private final static char[] EMPTY_CHARS = new char[0];

	private final IIndex index;
	private Map/*<IIndexFileLocation,FileInfo>*/ fileInfoCache;
	private Map/*<String,IIndexFileLocation>*/ iflCache;
	private List usedMacros = new ArrayList();
	private Set/*<FileInfo>*/ fIncluded= new HashSet();
	/** The fallback code reader factory used in case a header file is not indexed */
	private ICodeReaderFactory fFallBackFactory;
	private CallbackHandler fCallbackHandler;
	
	
	public IndexBasedCodeReaderFactory(IIndex index) {
		this(index, new HashMap/*<String,IIndexFileLocation>*/());
	}

	public IndexBasedCodeReaderFactory(IIndex index, ICodeReaderFactory fallbackFactory) {
		this(index, new HashMap/*<String,IIndexFileLocation>*/(), fallbackFactory);
	}

	public IndexBasedCodeReaderFactory(IIndex index, Map iflCache) {
		this(index, iflCache, null);
	}

	public IndexBasedCodeReaderFactory(IIndex index, Map iflCache, ICodeReaderFactory fallbackFactory) {
		this.index = index;
		this.fileInfoCache = new HashMap/*<IIndexFileLocation,FileInfo>*/();
		this.iflCache = iflCache;
		this.fFallBackFactory= fallbackFactory;
	}

	final protected Map getIFLCache() {
		return iflCache;
	}
	
	public int getUniqueIdentifier() {
		return 0;
	}

	public CodeReader createCodeReaderForTranslationUnit(String path) {
		return ParserUtil.createReader(path, null);
	}
	
	public CodeReader createCodeReaderForInclusion(IMacroCollector scanner, String path) {
		// if the file is in the index, we skip it
		File location= new File(path);
		String canonicalPath= path;
		if (!location.exists()) {
			return null;
		}
		if (!CASE_SENSITIVE_FILES) {
			try {
				canonicalPath= location.getCanonicalPath();
			}
			catch (IOException e) {
				// just use the original
			}
		}
		try {
			IIndexFileLocation incLocation = findLocation(canonicalPath);
			IndexFileInfo info= createInfo(incLocation, null);

			if (isIncluded(info)) {
				return new CodeReader(canonicalPath, EMPTY_CHARS);
			}

			// try to build macro dictionary off index
			if (info.fFile != null) {
				try {
					LinkedHashSet infos= new LinkedHashSet();
					getInfosForMacroDictionary(info, infos);
					for (Iterator iter = infos.iterator(); iter.hasNext();) {
						IndexFileInfo fi = (IndexFileInfo) iter.next();
						if (fi.fMacros == null) {
							assert fi.fFile != null;
							IIndexMacro[] macros= fi.fFile.getMacros();
							IMacro[] converted= new IMacro[macros.length];
							for (int i = 0; i < macros.length; i++) {
								IIndexMacro macro = macros[i];
								converted[i]= ((PDOMMacro)macro).getMacro();
							}
							fi.fMacros= converted;
						}
						for (int i = 0; i < fi.fMacros.length; ++i) {
							scanner.addDefinition(fi.fMacros[i]);
						}
						// record the macros we used.
						usedMacros.add(fi.fMacros);
					}
					return new CodeReader(canonicalPath, EMPTY_CHARS);
				} catch (NeedToParseException e) {
				}
			}
			setIncluded(info);
		}
		catch (CoreException e) {
			CCorePlugin.log(e);
			// still try to parse the file.
		}

		if (fFallBackFactory != null) {
			return fFallBackFactory.createCodeReaderForInclusion(scanner, canonicalPath);
		}
		return ParserUtil.createReader(canonicalPath, null);
	}

	/**
	 * Mark the given inclusion as included.
	 * @param info
	 */
	private void setIncluded(IndexFileInfo info) {
		fIncluded.add(info);
	}

	/**
	 * Test whether the given inclusion is already included.
	 * @param info
	 * @return <code>true</code> if the inclusion is already included.
	 */
	private boolean isIncluded(IndexFileInfo info) {
		return fIncluded.contains(info);
	}

	private IndexFileInfo createInfo(IIndexFileLocation location, IIndexFile file) throws CoreException {
		IndexFileInfo info= (IndexFileInfo) fileInfoCache.get(location);
		if (info == null) {
			info= new IndexFileInfo();			
			info.fFile= file == null ? index.getFile(location) : file;
			fileInfoCache.put(location, info);
		}
		return info;
	}
	
	private void getInfosForMacroDictionary(IndexFileInfo fileInfo, LinkedHashSet/*<FileInfo>*/ target) throws CoreException, NeedToParseException {
		if (!target.add(fileInfo)) {
			return;
		}
		if (isIncluded(fileInfo)) {
			return;
		}
		final IIndexFile file= fileInfo.fFile;
		if (file == null || 
				(fCallbackHandler != null && fCallbackHandler.needToUpdate(fileInfo))) {
			throw new NeedToParseException();
		}

		// Follow the includes
		IIndexInclude[] includeDirectives= file.getIncludes();
		for (int i = 0; i < includeDirectives.length; i++) {
			IIndexFile includedFile= index.resolveInclude(includeDirectives[i]);
			if (includedFile != null) {
				IndexFileInfo nextInfo= createInfo(includedFile.getLocation(), includedFile);
				getInfosForMacroDictionary(nextInfo, target);
			}
		}
		setIncluded(fileInfo);
	}
	
	public void clearMacroAttachements() {
		Iterator i = usedMacros.iterator();
		while (i.hasNext()) {
			IMacro[] macros = (IMacro[])i.next();
			for (int j = 0; j < macros.length; ++j) {
				if (macros[j] instanceof ObjectStyleMacro) {
					((ObjectStyleMacro)macros[j]).attachment = null;
				}
			}
		}
		usedMacros.clear();
		fIncluded.clear();
	}

	public ICodeReaderCache getCodeReaderCache() {
		// No need for cache here
		return null;
	}
	
	public IndexFileInfo createFileInfo(IIndexFileLocation location) throws CoreException {
		return createInfo(location, null);
	}
	
	public IIndexFileLocation findLocation(String absolutePath) {
		if(!iflCache.containsKey(absolutePath)) {
			iflCache.put(absolutePath, IndexLocationFactory.getIFLExpensive(absolutePath));
		}
		return (IIndexFileLocation) iflCache.get(absolutePath);
	}

	public void setCallbackHandler(CallbackHandler callbackHandler) {
		fCallbackHandler= callbackHandler;
	}
}
