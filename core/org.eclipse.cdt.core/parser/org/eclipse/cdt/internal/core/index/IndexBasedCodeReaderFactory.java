/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *    Anton Leherbauer (Wind River Systems)
 *    IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.parser.scanner2.IIndexBasedCodeReaderFactory;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacro;
import org.eclipse.core.runtime.CoreException;

/**
 * Code reader factory, that fakes code readers for header files already stored in the 
 * index.
 */
public final class IndexBasedCodeReaderFactory implements IIndexBasedCodeReaderFactory {
	private static final class NeedToParseException extends Exception {}
	private final static char[] EMPTY_CHARS = new char[0];

	private final IIndex fIndex;
	private int fLinkage;
	private Set fIncludedFiles= new HashSet();
	/** The fall-back code reader factory used in case a header file is not indexed */
	private final ICodeReaderFactory fFallBackFactory;
	private final ASTFilePathResolver fPathResolver;
	private final AbstractIndexerTask fRelatedIndexerTask;
	
	public IndexBasedCodeReaderFactory(IIndex index, ASTFilePathResolver pathResolver, int linkage) {
		this(index, pathResolver, linkage, null);
	}

	public IndexBasedCodeReaderFactory(IIndex index, ASTFilePathResolver pathResolver, int linkage, ICodeReaderFactory fallbackFactory) {
		this(index, pathResolver, linkage, fallbackFactory, null);
	}

	public IndexBasedCodeReaderFactory(IIndex index, ASTFilePathResolver pathResolver, int linkage,
			ICodeReaderFactory fallbackFactory, AbstractIndexerTask relatedIndexerTask) {
		fIndex= index;
		fFallBackFactory= fallbackFactory;
		fPathResolver= pathResolver;
		fRelatedIndexerTask= relatedIndexerTask;
		fLinkage= linkage;
	}

	public int getUniqueIdentifier() {
		return 0;
	}

	public CodeReader createCodeReaderForTranslationUnit(String path) {
		return ParserUtil.createReader(path, null);
	}
	
	public CodeReader createCodeReaderForInclusion(IMacroCollector scanner, String path) {
		IIndexFileLocation ifl= fPathResolver.resolveIncludeFile(path);
		if (ifl == null) {
			return null;
		}
		path= fPathResolver.getASTPath(ifl);
		
		// include files once, only.
		if (!fIncludedFiles.add(ifl)) {
			return new CodeReader(path, EMPTY_CHARS);
		}
		
		try {
			IIndexFile file= fIndex.getFile(fLinkage, ifl);
			if (file != null) {
				try {
					LinkedHashMap macroMap= new LinkedHashMap();
					collectMacros(file, macroMap, false);
					for (Iterator iterator = macroMap.entrySet().iterator(); iterator.hasNext();) {
						Map.Entry entry = (Map.Entry) iterator.next();
						IIndexFileLocation includedIFL = (IIndexFileLocation) entry.getKey();
						IMacro[] macros = (IMacro[]) entry.getValue();
						for (int i = 0; i < macros.length; ++i) {
							scanner.addDefinition(macros[i]);
						}
						fIncludedFiles.add(includedIFL);
					}
					return new CodeReader(path, EMPTY_CHARS);
				}
				catch (NeedToParseException e) {
				}
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e);
		}

		if (fFallBackFactory != null) {
			return fFallBackFactory.createCodeReaderForInclusion(scanner, path);
		}
		return ParserUtil.createReader(path, null);
	}

	public boolean hasFileBeenIncludedInCurrentTranslationUnit(String path) {
		IIndexFileLocation ifl= fPathResolver.resolveASTPath(path);
		return fIncludedFiles.contains(ifl);
	}
	
	private void collectMacros(IIndexFile file, LinkedHashMap macroMap, boolean checkIncluded) throws CoreException, NeedToParseException {
		IIndexFileLocation ifl= file.getLocation();
		if (macroMap.containsKey(ifl) || (checkIncluded && fIncludedFiles.contains(ifl))) {
			return;
		}
		IMacro[] converted;
		if (fRelatedIndexerTask != null) {
			converted= fRelatedIndexerTask.getConvertedMacros(fLinkage, ifl);
			if (converted == null) {
				throw new NeedToParseException();
			}
		}
		else {
			IIndexMacro[] macros= file.getMacros();
			converted= new IMacro[macros.length];
			for (int i = 0; i < macros.length; i++) {
				IIndexMacro macro = macros[i];
				converted[i]= ((PDOMMacro)macro).getMacro();
			}
		}
		macroMap.put(ifl, converted); // prevent recursion
		
		// follow the includes
		IIndexInclude[] includeDirectives= file.getIncludes();
		for (int i = 0; i < includeDirectives.length; i++) {
			final IIndexInclude indexInclude = includeDirectives[i];
			IIndexFile includedFile= fIndex.resolveInclude(indexInclude);
			if (includedFile != null) {
				collectMacros(includedFile, macroMap, true);
			}
		}
	}

	public void cleanupAfterTranslationUnit() {
		fIncludedFiles.clear();
	}

	public ICodeReaderCache getCodeReaderCache() {
		return null;
	}
	
	public void setLinkage(int linkageID) {
		fLinkage= linkageID;
	}
}
