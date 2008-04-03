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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.internal.core.parser.scanner.IIndexBasedCodeReaderFactory;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeFileResolutionCache;
import org.eclipse.cdt.internal.core.parser.scanner.IncludeFileContent.InclusionKind;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask;
import org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask.FileContent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Code reader factory, that fakes code readers for header files already stored in the 
 * index.
 */
public final class IndexBasedCodeReaderFactory implements IIndexBasedCodeReaderFactory, IAdaptable {
	private static final class NeedToParseException extends Exception {}

	private final IIndex fIndex;
	private int fLinkage;
	private Set<IIndexFileLocation> fIncludedFiles= new HashSet<IIndexFileLocation>();
	/** The fall-back code reader factory used in case a header file is not indexed */
	private final ICodeReaderFactory fFallBackFactory;
	private final ASTFilePathResolver fPathResolver;
	private final AbstractIndexerTask fRelatedIndexerTask;
	private final IncludeFileResolutionCache fIncludeFileResolutionCache;
	
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
		fIncludeFileResolutionCache= new IncludeFileResolutionCache(1024);
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
					LinkedHashMap<IIndexFileLocation, FileContent> fileContentMap= new LinkedHashMap<IIndexFileLocation, FileContent>();
					List<IIndexFile> files= new ArrayList<IIndexFile>();
					collectFileContent(file, fileContentMap, files, false);
					ArrayList<IIndexMacro> allMacros= new ArrayList<IIndexMacro>();
					ArrayList<ICPPUsingDirective> allDirectives= new ArrayList<ICPPUsingDirective>();
					for (Map.Entry<IIndexFileLocation,FileContent> entry : fileContentMap.entrySet()) {
						final FileContent content= entry.getValue();
						allMacros.addAll(Arrays.asList(content.fMacros));
						allDirectives.addAll(Arrays.asList(content.fDirectives));
						fIncludedFiles.add(entry.getKey());
					}
					return new IncludeFileContent(path, allMacros, allDirectives, files);
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
	
	private void collectFileContent(IIndexFile file, Map<IIndexFileLocation, FileContent> macroMap, List<IIndexFile> files, boolean checkIncluded) throws CoreException, NeedToParseException {
		IIndexFileLocation ifl= file.getLocation();
		if (macroMap.containsKey(ifl) || (checkIncluded && fIncludedFiles.contains(ifl))) {
			return;
		}
		FileContent content;
		if (fRelatedIndexerTask != null) {
			content= fRelatedIndexerTask.getFileContent(fLinkage, ifl);
			if (content == null) {
				throw new NeedToParseException();
			}
		}
		else {
			content= new FileContent();
			content.fMacros= file.getMacros();
			content.fDirectives= file.getUsingDirectives();
		}
		macroMap.put(ifl, content); // prevent recursion
		files.add(file);
		
		// follow the includes
		IIndexInclude[] includeDirectives= file.getIncludes();
		for (int i = 0; i < includeDirectives.length; i++) {
			final IIndexInclude indexInclude = includeDirectives[i];
			IIndexFile includedFile= fIndex.resolveInclude(indexInclude);
			if (includedFile != null) {
				collectFileContent(includedFile, macroMap, files, true);
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

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(fIncludeFileResolutionCache)) {
			return fIncludeFileResolutionCache;
		}
		return null;
	}
}
