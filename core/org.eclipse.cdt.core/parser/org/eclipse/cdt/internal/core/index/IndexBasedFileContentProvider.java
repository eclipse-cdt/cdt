/*******************************************************************************
 * Copyright (c) 2005, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Anton Leherbauer (Wind River Systems)
 *     IBM Corporation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IFileNomination;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.internal.core.parser.IMacroDictionary;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent.FileVersion;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent.InclusionKind;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask;
import org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask.IndexFileContent;
import org.eclipse.core.runtime.CoreException;

/**
 * Code reader factory, that fakes code readers for header files already stored in the index.
 */
public final class IndexBasedFileContentProvider extends InternalFileContentProvider {
	private static final String GAP = "__gap__"; //$NON-NLS-1$

	private final IIndex fIndex;
	private int fLinkage;
	/** The fall-back code reader factory used in case a header file is not indexed */
	private final InternalFileContentProvider fFallBackFactory;
	private final ASTFilePathResolver fPathResolver;
	private final AbstractIndexerTask fRelatedIndexerTask;
	private long fFileSizeLimit= 0;
	private IIndexFile[] fContextToHeaderGap;
	private final Map<IIndexFileLocation, IFileNomination> fPragmaOnce= new HashMap<IIndexFileLocation, IFileNomination>();

	public IndexBasedFileContentProvider(IIndex index,
			ASTFilePathResolver pathResolver, int linkage, IncludeFileContentProvider fallbackFactory) {
		this(index, pathResolver, linkage, fallbackFactory, null);
	}

	public IndexBasedFileContentProvider(IIndex index, ASTFilePathResolver pathResolver, int linkage,
			IncludeFileContentProvider fallbackFactory, AbstractIndexerTask relatedIndexerTask) {
		fIndex= index;
		fFallBackFactory= (InternalFileContentProvider) fallbackFactory;
		fPathResolver= pathResolver;
		fRelatedIndexerTask= relatedIndexerTask;
		fLinkage= linkage;
	}

	public void setContextToHeaderGap(IIndexFile[] ctxToHeader) {
		fContextToHeaderGap= ctxToHeader;
	}
	
	public void setFileSizeLimit(long limit) {
		fFileSizeLimit= limit;
	}

	public void setLinkage(int linkageID) {
		fLinkage= linkageID;
	}
	
	@Override
	public void resetForTranslationUnit() {
		super.resetForTranslationUnit();
		fPragmaOnce.clear();
	}

	/** 
	 * Reports detection of pragma once semantics.
	 */
	@Override
	public void reportPragmaOnceSemantics(String filePath, IFileNomination nom) {
		fPragmaOnce.put(fPathResolver.resolveIncludeFile(filePath), nom);
	}

	/**
	 * Returns whether the given file has been included with pragma once semantics.
	 */
	@Override
	public IFileNomination isIncludedWithPragmaOnceSemantics(String filePath) {
		return fPragmaOnce.get(fPathResolver.resolveIncludeFile(filePath));
	}

	@Override
	public boolean getInclusionExists(String path) {
		return fPathResolver.doesIncludeFileExist(path); 
	}
	
	
	@Override
	public InternalFileContent getContentForInclusion(String path, IMacroDictionary macroDictionary) {
		IIndexFileLocation ifl= fPathResolver.resolveIncludeFile(path);
		if (ifl == null) {
			return null;
		}

		path= fPathResolver.getASTPath(ifl);
		try {
			IIndexFile file = selectIndexFile(macroDictionary, ifl);
			if (file != null) {
				try {
					List<IIndexFile> files= new ArrayList<IIndexFile>();
					List<IIndexMacro> macros= new ArrayList<IIndexMacro>();
					List<ICPPUsingDirective> directives= new ArrayList<ICPPUsingDirective>();
					Map<IIndexFileLocation, IFileNomination> newPragmaOnce= new HashMap<IIndexFileLocation, IFileNomination>();
					LinkedHashSet<IIndexFile> preLoaded= new LinkedHashSet<IIndexFile>();
					collectFileContent(file, null, newPragmaOnce, preLoaded, files, macros, directives, null);
					// Report pragma once inclusions, only if no exception was thrown.
					fPragmaOnce.putAll(newPragmaOnce);
					return new InternalFileContent(path, macros, directives, files, toList(preLoaded));
				} catch (DependsOnOutdatedFileException e) {
				}
			} 
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}

		// Skip large files
		if (fFileSizeLimit > 0 && fPathResolver.getFileSize(path) > fFileSizeLimit) {
			return new InternalFileContent(path, InclusionKind.SKIP_FILE);
		}

		if (fFallBackFactory != null) {
			InternalFileContent ifc= getContentForInclusion(ifl, path);
			if (ifc != null)
				ifc.setIsSource(fPathResolver.isSource(path));
			return ifc;
		}
		return null;
	}

	public List<String> toPathList(Collection<IIndexFileLocation> newPragmaOnce) {
		List<String> newPragmaOncePaths= new ArrayList<String>(newPragmaOnce.size());
		for (IIndexFileLocation l : newPragmaOnce) {
			newPragmaOncePaths.add(fPathResolver.getASTPath(l));
		}
		return newPragmaOncePaths;
	}

	public IIndexFile selectIndexFile(IMacroDictionary macroDictionary, IIndexFileLocation ifl)
			throws CoreException {
		if (fRelatedIndexerTask != null)
			return fRelatedIndexerTask.selectIndexFile(fLinkage, ifl, macroDictionary);
		
		for (IIndexFile file : fIndex.getFiles(fLinkage, ifl)) {
			if (macroDictionary.satisfies(file.getSignificantMacros()))
				return file;
		}
		return null;
	}

	@Override
	public InternalFileContent getContentForInclusion(IIndexFileLocation ifl, String astPath) {
		if (fFallBackFactory != null) {
			return fFallBackFactory.getContentForInclusion(ifl, astPath);
		}
		return null;
	}

	private boolean collectFileContent(IIndexFile file, IIndexFile stopAt,
			Map<IIndexFileLocation, IFileNomination> newPragmaOnce,
			LinkedHashSet<IIndexFile> preLoaded, List<IIndexFile> files,
			List<IIndexMacro> macros, List<ICPPUsingDirective> usingDirectives,
			Set<IIndexFile> preventRecursion) throws CoreException, DependsOnOutdatedFileException {
		if (file.equals(stopAt))
			return true;
		
		IIndexFileLocation ifl= file.getLocation();
		if (newPragmaOnce.containsKey(ifl))
			return false;
		if (file.hasPragmaOnceSemantics()) 
			newPragmaOnce.put(ifl, file);
		
		if (preventRecursion != null) {
			if (fPragmaOnce.containsKey(ifl)) 
				return false;
		} else {
			preventRecursion= new HashSet<IIndexFile>();
		}
		if (!preventRecursion.add(file))
			return false;

		final ICPPUsingDirective[] uds;
		final Object[] pds;
		if (fRelatedIndexerTask != null) {
			IndexFileContent content= fRelatedIndexerTask.getFileContent(fLinkage, ifl, file);
			uds= content.getUsingDirectives();
			pds= content.getPreprocessingDirectives();
		} else {
			uds= file.getUsingDirectives();
			pds= IndexFileContent.merge(file.getIncludes(), file.getMacros());
		}
		
		files.add(file);
		if (!file.hasPragmaOnceSemantics()) {
			preLoaded.add(file);
		}
		int udx= 0;
		for (Object d : pds) {
			if (d instanceof IIndexMacro) {
				macros.add((IIndexMacro) d);
			} else if (d instanceof IIndexInclude) {
				IIndexInclude inc= (IIndexInclude) d;
				IIndexFile includedFile= fIndex.resolveInclude((IIndexInclude) d);
				if (includedFile != null) {
					// Add in using directives that appear before the inclusion
					final int offset= inc.getNameOffset();
					for (; udx < uds.length && uds[udx].getPointOfDeclaration() <= offset; udx++) {
						usingDirectives.add(uds[udx]);
					}
					if (collectFileContent(includedFile, stopAt, newPragmaOnce, preLoaded, files, macros, usingDirectives, preventRecursion))
						return true;
				}
			}
		}
		// Add in remaining using directives
		for (; udx < uds.length; udx++) {
			usingDirectives.add(uds[udx]);
		}
		preventRecursion.remove(file);
		return false;
	}

	@Override
	public InternalFileContent getContentForContextToHeaderGap(String path,
			IMacroDictionary macroDictionary) throws DependsOnOutdatedFileException {
		if (fContextToHeaderGap == null) {
			return null;
		}
		
		try {
			IIndexFile contextFile= fContextToHeaderGap[0];
			IIndexFile targetFile = fContextToHeaderGap[1];
			if (contextFile == null || targetFile == null ||  contextFile == targetFile) 
				return null;

			Map<IIndexFileLocation, IFileNomination> newPragmaOnce= new HashMap<IIndexFileLocation, IFileNomination>();
			List<IIndexFile> filesIncluded= new ArrayList<IIndexFile>();
			ArrayList<IIndexMacro> macros= new ArrayList<IIndexMacro>();
			ArrayList<ICPPUsingDirective> directives= new ArrayList<ICPPUsingDirective>();
			LinkedHashSet<IIndexFile> preLoaded= new LinkedHashSet<IIndexFile>();
			if (!collectFileContent(contextFile, targetFile, newPragmaOnce, preLoaded,
					filesIncluded, macros, directives, new HashSet<IIndexFile>())) {
				return null;
			}

			// Report pragma once inclusions.
			fPragmaOnce.putAll(newPragmaOnce);
			return new InternalFileContent(GAP, macros, directives, filesIncluded, toList(preLoaded));
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	private List<FileVersion> toList(LinkedHashSet<IIndexFile> preLoaded) throws CoreException {
		List<FileVersion> result= new ArrayList<InternalFileContent.FileVersion>(preLoaded.size());
		for (IIndexFile file : preLoaded) {
			String path= fPathResolver.getASTPath(file.getLocation());
			result.add(new FileVersion(path, file.getSignificantMacros()));
		}
		return result;
	}

	public IIndexFile[] findIndexFiles(InternalFileContent fc) throws CoreException {
		IIndexFileLocation ifl = fPathResolver.resolveASTPath(fc.getFileLocation());
		if (ifl != null) {
			return fIndex.getFiles(fLinkage, ifl);
		}
		return IIndexFile.EMPTY_FILE_ARRAY;
	}
	
	@Override
	public String getContextPath() {
		if (fContextToHeaderGap != null)
			try {
				return fPathResolver.getASTPath(fContextToHeaderGap[0].getLocation());
			} catch (CoreException e) {
			}
		return null;
	}
}
