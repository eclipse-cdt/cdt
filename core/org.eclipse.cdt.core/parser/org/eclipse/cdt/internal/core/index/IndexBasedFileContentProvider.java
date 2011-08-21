/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IFileContentKey;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent.InclusionKind;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask;
import org.eclipse.cdt.internal.core.pdom.AbstractIndexerTask.IndexFileContent;
import org.eclipse.core.runtime.CoreException;

/**
 * Code reader factory, that fakes code readers for header files already stored in the 
 * index.
 */
public final class IndexBasedFileContentProvider extends InternalFileContentProvider {
	private static final class NeedToParseException extends Exception {}
	private static final String GAP = "__gap__"; //$NON-NLS-1$

	private final IIndex fIndex;
	private int fLinkage;
	private final Set<IFileContentKey> fIncludedFiles= new HashSet<IFileContentKey>();
	private final Map<IIndexFileLocation, IFileContentKey> fIncludedFilesMap =
			new HashMap<IIndexFileLocation, IFileContentKey>();
	/** The fall-back code reader factory used in case a header file is not indexed */
	private final InternalFileContentProvider fFallBackFactory;
	private final ASTFilePathResolver fPathResolver;
	private final AbstractIndexerTask fRelatedIndexerTask;
	private boolean fSupportFillGapFromContextToHeader;
	private long fFileSizeLimit= 0;
	
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

	public void setSupportFillGapFromContextToHeader(boolean val) {
		fSupportFillGapFromContextToHeader= val;
	}
	
	public void setFileSizeLimit(long limit) {
		fFileSizeLimit= limit;
	}

	public void setLinkage(int linkageID) {
		fLinkage= linkageID;
	}

	public void cleanupAfterTranslationUnit() {
		fIncludedFiles.clear();
		fIncludedFilesMap.clear();
	}
	
	@Override
	public boolean getInclusionExists(String path) {
		return fPathResolver.doesIncludeFileExist(path); 
	}
	
	@Override
	public void reportTranslationUnitFile(String path, Map<String, String> relevantMacros,
			String includeGuardMacro) {
		IIndexFileLocation ifl= fPathResolver.resolveASTPath(path);
		IFileContentKey fileKey = new FileContentKey(ifl, relevantMacros, includeGuardMacro);
		fIncludedFiles.add(fileKey);
		fIncludedFilesMap.put(ifl, fileKey);
	}

	@Override
	public Boolean hasFileBeenIncludedInCurrentTranslationUnit(String path,
			Map<String, String> macroDictionary) {
		IIndexFileLocation ifl= fPathResolver.resolveASTPath(path);
		IFileContentKey fileKey = fIncludedFilesMap.get(ifl);
		if (fileKey == null) {
			return false;
		}
		Map<String, String> fileRelevantMacros = fileKey.getRelevantMacros();
		if (fileRelevantMacros == null) {
			return true;
		}
		Map<String, String> relevantMacros = new HashMap<String, String>(fileRelevantMacros.size());
		for (String key : fileRelevantMacros.keySet()) {
			String value = key.equals(fileKey.getIncludeGuardMacro()) ?
					null : macroDictionary.get(key); 
			relevantMacros.put(key, value);
		}
		fileKey = new FileContentKey(ifl, relevantMacros, fileKey.getIncludeGuardMacro());
		return fIncludedFiles.contains(fileKey);
	}
	
	@Override
	public InternalFileContent getContentForInclusion(String path, Map<String, String> macroDictionary) {
		IIndexFileLocation ifl= fPathResolver.resolveIncludeFile(path);
		if (ifl == null) {
			return null;
		}
		path= fPathResolver.getASTPath(ifl);
		
		try {
			IIndexFile file= fIndex.getFile(fLinkage, ifl, macroDictionary);
			if (file != null) {
				try {
					List<IIndexFile> files= new ArrayList<IIndexFile>();
					List<IIndexMacro> macros= new ArrayList<IIndexMacro>();
					List<ICPPUsingDirective> directives= new ArrayList<ICPPUsingDirective>();
					Set<IFileContentKey> fileKeys= new HashSet<IFileContentKey>();
					collectFileContent(file, fileKeys, files, macros, directives, false);
					// Add included files only if no exception was thrown.
					fIncludedFiles.addAll(fileKeys);
					for (IFileContentKey key : fileKeys) {
						fIncludedFilesMap.put(key.getLocation(), key);
					}
					return new InternalFileContent(path, macros, directives, files);
				} catch (NeedToParseException e) {
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}

		// TODO(197989): Without the following three lines IndexBugsTests.testIncludeGuardsOutsideOfHeader_Bug167100
		// test fails, but it's not clear how to make this code work with realistic relevant macros
		// since they are not yet known at this point.
//		IFileContentKey key = new FileContentKey(ifl, macroDictionary, null);
//		fIncludedFiles.add(key);
//		fIncludedFilesMap.put(key.getLocation(), key);

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

	@Override
	public InternalFileContent getContentForInclusion(IIndexFileLocation ifl, String astPath) {
		if (fFallBackFactory != null) {
			return fFallBackFactory.getContentForInclusion(ifl, astPath);
		}
		return null;
	}

	private void collectFileContent(IIndexFile file, Set<IFileContentKey> fileKeys,
			List<IIndexFile> files, List<IIndexMacro> macros,
			List<ICPPUsingDirective> usingDirectives, boolean checkIncluded)
			throws CoreException, NeedToParseException {
		IIndexFileLocation ifl= file.getLocation();
		IFileContentKey key = file.getContentKey();
		if (!fileKeys.add(key) || (checkIncluded && fIncludedFiles.contains(key))) {
			return;
		}
		IndexFileContent content;
		if (fRelatedIndexerTask != null) {
			content= fRelatedIndexerTask.getFileContent(fLinkage, ifl, key.getRelevantMacros());
			if (content == null) {
				throw new NeedToParseException();
			}
		} else {
			content= new IndexFileContent();
			content.setPreprocessorDirectives(file.getIncludes(), file.getMacros());
			content.setUsingDirectives(file.getUsingDirectives());
		}
		
		files.add(file);
		usingDirectives.addAll(Arrays.asList(content.getUsingDirectives()));
		Object[] dirs= content.getPreprocessingDirectives();
		for (Object d : dirs) {
			if (d instanceof IIndexMacro) {
				macros.add((IIndexMacro) d);
			} else if (d instanceof IIndexInclude) {
				IIndexFile includedFile= fIndex.resolveInclude((IIndexInclude) d);
				if (includedFile != null) {
					collectFileContent(includedFile, fileKeys, files, macros, usingDirectives, true);
				}
			}
		}
	}

	@Override
	public InternalFileContent getContentForContextToHeaderGap(String path,
			Map<String, String> macroDictionary) {
		if (!fSupportFillGapFromContextToHeader) {
			return null;
		}
		
		IIndexFileLocation ifl= fPathResolver.resolveASTPath(path);
		if (ifl == null) {
			return null;
		}
		
		try {
			IIndexFile targetFile= fIndex.getFile(fLinkage, ifl, macroDictionary);
			if (targetFile == null) {
				return null;
			}
			
			IIndexFile contextFile= findContext(targetFile);
			if (contextFile == targetFile || contextFile == null) {
				return null;
			}
			
			HashSet<IIndexFile> filesIncluded= new HashSet<IIndexFile>();
			ArrayList<IIndexMacro> macros= new ArrayList<IIndexMacro>();
			ArrayList<ICPPUsingDirective> directives= new ArrayList<ICPPUsingDirective>();
			if (!collectFileContentForGap(contextFile, ifl, filesIncluded, macros, directives)) {
				return null;
			}

			// mark the files in the gap as included
			for (IIndexFile file : filesIncluded) {
				IFileContentKey key = file.getContentKey();
				fIncludedFiles.add(key);
				fIncludedFilesMap.put(key.getLocation(), key);
			}
			return new InternalFileContent(GAP, macros, directives, new ArrayList<IIndexFile>(filesIncluded));
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	private IIndexFile findContext(IIndexFile file) throws CoreException {
		final HashSet<IIndexFile> ifiles= new HashSet<IIndexFile>();
		ifiles.add(file);
		IIndexInclude include= file.getParsedInContext();
		while (include != null) {
			final IIndexFile context= include.getIncludedBy();
			if (!ifiles.add(context)) {
				return file;
			}
			file= context;
			include= context.getParsedInContext();
		}
		return file;
	}

	private boolean collectFileContentForGap(IIndexFile from, IIndexFileLocation to,
			Set<IIndexFile> filesIncluded, List<IIndexMacro> macros,
			List<ICPPUsingDirective> directives) throws CoreException {
		final IIndexFileLocation ifl= from.getLocation();
		if (ifl.equals(to)) {
			return true;
		}

		if (fIncludedFiles.contains(from.getContentKey()) || !filesIncluded.add(from)) {
			return false;
		}

		final IIndexInclude[] ids= from.getIncludes();
		final IIndexMacro[] ms= from.getMacros();
		final Object[] dirs= IndexFileContent.merge(ids, ms);
		IIndexInclude success= null;
		for (Object d : dirs) {
			if (d instanceof IIndexMacro) {
				macros.add((IIndexMacro) d);
			} else if (d instanceof IIndexInclude) {
				IIndexFile includedFile= fIndex.resolveInclude((IIndexInclude) d);
				if (includedFile != null) {
					if (collectFileContentForGap(includedFile, to, filesIncluded, macros, directives)) {
						success= (IIndexInclude) d;
						break;
					}
				}
			}
		}

		final ICPPUsingDirective[] uds= from.getUsingDirectives();
		if (success == null) {
			directives.addAll(Arrays.asList(uds));
			return false;
		}
			
		final int offset= success.getNameOffset();
		for (ICPPUsingDirective ud : uds) {
			if (ud.getPointOfDeclaration() > offset)
				break;
			directives.add(ud);
		}
		return true;
	}

	public IIndexFile findIndexFile(InternalFileContent fc) throws CoreException {
		IIndexFileLocation ifl = fPathResolver.resolveASTPath(fc.getFileLocation());
		if (ifl != null) {
			return fIndex.getFile(fLinkage, ifl);
		}
		return null;
	}
}
