/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.IIndexBasedCodeReaderFactory;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeFileContent.InclusionKind;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask;
import org.eclipse.core.runtime.CoreException;

/**
 * Code reader factory, that fakes code readers for header files already stored in the 
 * index.
 */
public final class IndexBasedCodeReaderFactory implements IIndexBasedCodeReaderFactory {
	private static final class NeedToParseException extends Exception {}

	private final IIndex fIndex;
	private int fLinkage;
	private Set<IIndexFileLocation> fIncludedFiles= new HashSet<IIndexFileLocation>();
	/** The fall-back code reader factory used in case a header file is not indexed */
	private final ICodeReaderFactory fFallBackFactory;
	private final ASTFilePathResolver fPathResolver;
	private final AbstractIndexerTask fRelatedIndexerTask;
	
	public IndexBasedCodeReaderFactory(IIndex index, ASTFilePathResolver pathResolver, int linkage, 
			ICodeReaderFactory fallbackFactory) {
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
		if (fFallBackFactory != null) {
			return fFallBackFactory.createCodeReaderForTranslationUnit(path);
		}
		return ParserUtil.createReader(path, null);
	}

	public CodeReader createCodeReaderForInclusion(String path) {
		if (fFallBackFactory != null) {
			return fFallBackFactory.createCodeReaderForInclusion(path);
		}
		return ParserUtil.createReader(path, null);
	}

	public IncludeFileContent getContentForInclusion(String path) {
		IIndexFileLocation ifl= fPathResolver.resolveIncludeFile(path);
		if (ifl == null) {
			return null;
		}
		path= fPathResolver.getASTPath(ifl);
		
		// include files once, only.
		if (!fIncludedFiles.add(ifl)) {
			return new IncludeFileContent(path, InclusionKind.SKIP_FILE);
		}
		
		try {
			IIndexFile file= fIndex.getFile(fLinkage, ifl);
			if (file != null) {
				try {
					LinkedHashMap<IIndexFileLocation, IIndexMacro[]> macroMap= new LinkedHashMap<IIndexFileLocation, IIndexMacro[]>();
					collectMacros(file, macroMap, false);
					ArrayList<IIndexMacro> allMacros= new ArrayList<IIndexMacro>();
					for (Map.Entry<IIndexFileLocation,IIndexMacro[]> entry : macroMap.entrySet()) {
						allMacros.addAll(Arrays.asList(entry.getValue()));
						fIncludedFiles.add(entry.getKey());
					}
					return new IncludeFileContent(path, allMacros);
				}
				catch (NeedToParseException e) {
				}
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e);
		}

		CodeReader codeReader= createCodeReaderForInclusion(path);
		if (codeReader != null) {
			return new IncludeFileContent(codeReader);
		}
		return null;
	}

	public boolean hasFileBeenIncludedInCurrentTranslationUnit(String path) {
		IIndexFileLocation ifl= fPathResolver.resolveASTPath(path);
		return fIncludedFiles.contains(ifl);
	}
	
	private void collectMacros(IIndexFile file, LinkedHashMap<IIndexFileLocation, IIndexMacro[]> macroMap, boolean checkIncluded) throws CoreException, NeedToParseException {
		IIndexFileLocation ifl= file.getLocation();
		if (macroMap.containsKey(ifl) || (checkIncluded && fIncludedFiles.contains(ifl))) {
			return;
		}
		IIndexMacro[] converted;
		if (fRelatedIndexerTask != null) {
			converted= fRelatedIndexerTask.getConvertedMacros(fLinkage, ifl);
			if (converted == null) {
				throw new NeedToParseException();
			}
		}
		else {
			converted= file.getMacros();
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
