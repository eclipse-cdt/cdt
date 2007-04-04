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

	private final static boolean CASE_SENSITIVE_FILES= !new File("a").equals(new File("A"));  //$NON-NLS-1$//$NON-NLS-2$
	private final IIndex index;
	private Map/*<IIndexFileLocation,FileInfo>*/ fileInfoCache;
	private Map/*<String,IIndexFileLocation>*/ iflCache;
	private List usedMacros = new ArrayList();
	private Set/*<FileInfo>*/ fIncluded= new HashSet();
	/** The fallback code reader factory used in case a header file is not indexed */
	private ICodeReaderFactory fFallBackFactory;
	
	private static final char[] EMPTY_CHARS = new char[0];
	
	private static class NeedToParseException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	public static class FileInfo {
		private FileInfo() {}
		public IIndexFile fFile= null;
		public IMacro[] fMacros= null;
//		public FileInfo[] fFileInfos= null;
		private boolean fRequested= false;
		
		public boolean isRequested() {
			return fRequested;
		}
		public void setRequested(boolean val) {
			fRequested= val;
		}
	}
	
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
			FileInfo info= createInfo(incLocation, null);

			if (isIncluded(info)) {
				return new CodeReader(canonicalPath, EMPTY_CHARS);
			}

			// try to build macro dictionary off index
			if (info.fFile != null) {
				try {
					LinkedHashSet infos= new LinkedHashSet();
					getInfosForMacroDictionary(info, infos);
					for (Iterator iter = infos.iterator(); iter.hasNext();) {
						FileInfo fi = (FileInfo) iter.next();
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
	private void setIncluded(FileInfo info) {
		fIncluded.add(info);
	}

	/**
	 * Test whether the given inclusion is already included.
	 * @param info
	 * @return <code>true</code> if the inclusion is already included.
	 */
	private boolean isIncluded(FileInfo info) {
		return fIncluded.contains(info);
	}

	private FileInfo createInfo(IIndexFileLocation location, IIndexFile file) throws CoreException {
		FileInfo info= (FileInfo) fileInfoCache.get(location);
		if (info == null) {
			info= new FileInfo();			
			info.fFile= file == null ? index.getFile(location) : file;
			fileInfoCache.put(location, info);
		}
		return info;
	}
	
	private void getInfosForMacroDictionary(FileInfo fileInfo, LinkedHashSet/*<FileInfo>*/ target) throws CoreException, NeedToParseException {
		if (!target.add(fileInfo)) {
			return;
		}
		if (isIncluded(fileInfo)) {
			return;
		}
		if (fileInfo.fFile == null || fileInfo.isRequested()) {
			throw new NeedToParseException();
		}

		// Follow the includes
		IIndexFile file= fileInfo.fFile;
		IIndexInclude[] includeDirectives= file.getIncludes();
		for (int i = 0; i < includeDirectives.length; i++) {
			IIndexFile includedFile= index.resolveInclude(includeDirectives[i]);
			if (includedFile != null) {
				FileInfo nextInfo= createInfo(includedFile.getLocation(), includedFile);
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
	
	public FileInfo createFileInfo(IIndexFileLocation location) throws CoreException {
		return createInfo(location, null);
	}
	
	public IIndexFileLocation findLocation(String absolutePath) {
		if(!iflCache.containsKey(absolutePath)) {
			iflCache.put(absolutePath, IndexLocationFactory.getIFLExpensive(absolutePath));
		}
		return (IIndexFileLocation) iflCache.get(absolutePath);
	}
}
