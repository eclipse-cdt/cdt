/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Marc-Andre Laperle - Extracted Util class from ToggleSourceHeaderAction
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;

/**
 * A collection of static methods for finding the source file corresponding to a header
 * and vice versa.
 */
public final class SourceHeaderPartnerFinder {
	
	private static class Counter {
		public int fCount;
	}
	
	private SourceHeaderPartnerFinder() {
	}
	
	/**
	 * Compute the partner file for a translation unit.
	 * The partner file is the corresponding source or header file
	 * based on heuristics.
	 *
	 * @since 4.0
	 */
	private static class PartnerFileComputer implements ASTRunnable {
		PartnerFileVisitor fVisitor = null;
		
		@Override
		public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
			if (ast != null && ast.getIndex() != null) {
				fVisitor = new PartnerFileVisitor();
				ast.accept(fVisitor);
			}
			return Status.OK_STATUS;
		}

		public IPath getPartnerFileLocation() {
			if (fVisitor != null) {
				return fVisitor.getPartnerFileLocation();
			}
			
			return null;
		}
	}
	
	private static class PartnerFileVisitor extends ASTVisitor {
		/** 
		 * When this many times the same partner file is hit, 
		 * we are confident enough to take it.
		 */
		private static final int CONFIDENCE_LIMIT = 15;
		/** 
		 * When this many times no match was found in the index, 
		 * we suspect that we won't get a good partner.
		 */
		private static final int SUSPECT_LIMIT = 15;

		private IIndex fIndex;
		private IPath fFilePath;
		private Map<IPath, Counter> fMap;
		/** The confidence level == number of hits */
		private int fConfidence;
		/** Suspect level == number of no index matches */
		private int fSuspect;
		/** The current favorite partner file */
		private IPath fFavoriteLocation;
		
		{
			shouldVisitDeclarators= true;
			shouldVisitTranslationUnit = true;
		}

		public PartnerFileVisitor() {
			fMap= new HashMap<IPath, Counter>();
		}

		@Override
		public int visit(IASTTranslationUnit tu) {
			fIndex= tu.getIndex();
			if (fIndex == null) {
				return PROCESS_ABORT;	
			}
			
			fFilePath= Path.fromOSString(tu.getFilePath());
			return super.visit(tu);
		}

		public IPath getPartnerFileLocation() {
			return fFavoriteLocation;
		}

		/*
		 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclarator)
		 */
		@Override
		public int visit(IASTDeclarator declarator) {
			if (declarator instanceof IASTFunctionDeclarator) {
				IASTName name= declarator.getName();
				if (name != null && declarator.getNestedDeclarator() == null) {
					IBinding binding= name.resolveBinding();
					if (binding != null && !(binding instanceof IProblemBinding)) {
						boolean isDefinition= name.isDefinition();
						final IIndexName[] partnerNames;
						try {
							if (isDefinition) {
								partnerNames= fIndex.findNames(binding,
										IIndex.FIND_DECLARATIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
							} else {
								partnerNames= fIndex.findNames(binding,
										IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
							}
							if (partnerNames.length == 0) {
								++fSuspect;
								if (fSuspect == SUSPECT_LIMIT) {
									fFavoriteLocation= null;
									return PROCESS_ABORT;
								}
							}
							for (int i= 0; i < partnerNames.length; i++) {
								IIndexName partnerName= partnerNames[i];
								IASTFileLocation partnerLocation= partnerName.getFileLocation();
								if (partnerLocation != null) {
									IPath partnerFileLocation= Path.fromOSString(partnerLocation.getFileName());
									if (!fFilePath.equals(partnerFileLocation)) {
										addPotentialPartnerFileLocation(partnerFileLocation);
										if (fConfidence == CONFIDENCE_LIMIT) {
											return PROCESS_ABORT;
										}
									}
								}
							}
						} catch (CoreException exc) {
							CUIPlugin.log(exc.getStatus());
						}
					}
				}
			}
			return PROCESS_SKIP;
		}

		private void addPotentialPartnerFileLocation(IPath partnerFileLocation) {
			Counter counter= fMap.get(partnerFileLocation);
			if (counter == null) {
				counter= new Counter();
				fMap.put(partnerFileLocation, counter);
			}
			++counter.fCount;
			if (counter.fCount > fConfidence) {
				fConfidence= counter.fCount;
				fFavoriteLocation= partnerFileLocation;
			}
		}
	}
	
	/**
	 * Finds a file in the given resource container for the given basename.
	 * 
	 * @param container
	 * @param basename
	 * @return a matching {@link IFile} or <code>null</code>, if no matching file was found
	 */
	private static IFile findInContainer(IContainer container, final String basename) {
		final IFile[] result= { null };
		IResourceProxyVisitor visitor= new IResourceProxyVisitor() {
			@Override
			public boolean visit(IResourceProxy proxy) throws CoreException {
				if (result[0] != null) {
					return false;
				}
				if (!proxy.isAccessible()) {
					return false;
				}
				if (proxy.getType() == IResource.FILE && proxy.getName().equals(basename)) {
					result[0]= (IFile)proxy.requestResource();
					return false;
				}
				return true;
			}};
		try {
			container.accept(visitor, 0);
		} catch (CoreException e) {
			// Ignore
		}
		return result[0];
	}
	
	private static IContentType[] getPartnerContentTypes(String contentTypeId) {
		IContentTypeManager mgr= Platform.getContentTypeManager();
		if (contentTypeId.equals(CCorePlugin.CONTENT_TYPE_CHEADER)) {
			return new IContentType[] {
					mgr.getContentType(CCorePlugin.CONTENT_TYPE_CSOURCE),
					mgr.getContentType(CCorePlugin.CONTENT_TYPE_CXXSOURCE)
			};
		}
		if (contentTypeId.equals(CCorePlugin.CONTENT_TYPE_CSOURCE)) {
			return new IContentType[] {
					mgr.getContentType(CCorePlugin.CONTENT_TYPE_CHEADER),
					mgr.getContentType(CCorePlugin.CONTENT_TYPE_CXXHEADER)
			};
		}
		if (contentTypeId.equals(CCorePlugin.CONTENT_TYPE_CXXHEADER)) {
			return new IContentType[] {
					mgr.getContentType(CCorePlugin.CONTENT_TYPE_CXXSOURCE),
					mgr.getContentType(CCorePlugin.CONTENT_TYPE_CSOURCE)
			};
		}
		if (contentTypeId.equals(CCorePlugin.CONTENT_TYPE_CXXSOURCE)) {
			return new IContentType[] {
					mgr.getContentType(CCorePlugin.CONTENT_TYPE_CXXHEADER),
					mgr.getContentType(CCorePlugin.CONTENT_TYPE_CHEADER)
			};
		}
		return new IContentType[0];
	}
	
	/**
	 * Finds a partner translation unit based on filename/extension matching.
	 * 
	 * @param a partner translation unit or <code>null</code>
	 */
	private static ITranslationUnit getPartnerFileFromFilename(ITranslationUnit tu) {
		IPath sourceFileLocation= tu.getLocation();
		if (sourceFileLocation == null) {
			return null;
		}
		IPath partnerBasePath= sourceFileLocation.removeFileExtension();
		IContentType[] contentTypes= getPartnerContentTypes(tu.getContentTypeId());
		HashSet<String> extensionsTried= new HashSet<String>();
		for (int j = 0; j < contentTypes.length; j++) {
			IContentType contentType= contentTypes[j];
			String[] partnerExtensions;
			partnerExtensions= contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
			for (int i= 0; i < partnerExtensions.length; i++) {
				String ext= partnerExtensions[i];
				if (extensionsTried.add(ext)) {
					String partnerFileBasename= partnerBasePath.addFileExtension(ext).lastSegment();
					
					IFile partnerFile= null;
					IResource resource = tu.getResource();
					IContainer container = resource != null ? resource.getParent() : null;
					while (container != null && partnerFile == null && !(container instanceof IWorkspaceRoot)) {
						partnerFile= findInContainer(container, partnerFileBasename);
						container = container.getParent();
					}

					if (partnerFile != null) {
						ITranslationUnit partnerUnit= (ITranslationUnit) CoreModel.getDefault().create(partnerFile);
						if (partnerUnit != null) {
							return partnerUnit;
						}
					}
					// External translation unit - try in same directory
					if (resource == null) {
						IPath partnerFileLoation= partnerBasePath.removeLastSegments(1).append(partnerFileBasename);
						ITranslationUnit partnerUnit= CoreModel.getDefault().createTranslationUnitFrom(
								tu.getCProject(), partnerFileLoation);
						if (partnerUnit != null) {
							return partnerUnit;
						}
					}
				}
			}
		}
		return null;
	}

	public static ITranslationUnit getPartnerTranslationUnit(ITranslationUnit tu) {
		ITranslationUnit partnerUnit= getPartnerFileFromFilename(tu);

		if (partnerUnit == null) {
			// Search partner file based on definition/declaration association
			IProgressMonitor monitor= new NullProgressMonitor();
			PartnerFileComputer computer= new PartnerFileComputer();
			ASTProvider.getASTProvider().runOnAST(tu, ASTProvider.WAIT_ACTIVE_ONLY, monitor, computer);
			partnerUnit = createTranslationUnit(computer.getPartnerFileLocation(), tu.getCProject());
		}
		return partnerUnit;
	}
	
	public static ITranslationUnit getPartnerTranslationUnit(ITranslationUnit tu,
			RefactoringASTCache astCache) throws CoreException {
		ITranslationUnit partnerUnit= getPartnerFileFromFilename(tu);

		if (partnerUnit == null) {
			// Search partner file based on definition/declaration association
			IProgressMonitor monitor= new NullProgressMonitor();
			IASTTranslationUnit ast = astCache.getAST(tu, monitor);
			PartnerFileVisitor visitor = new PartnerFileVisitor();
			ast.accept(visitor);
			partnerUnit = createTranslationUnit(visitor.getPartnerFileLocation(), tu.getCProject());
		}
		return partnerUnit;
	}
	
	private static ITranslationUnit createTranslationUnit(IPath partnerFileLoation, ICProject cProject) {
		ITranslationUnit partnerUnit = null;
		if (partnerFileLoation != null) {
			partnerUnit= (ITranslationUnit) CoreModel.getDefault().create(partnerFileLoation);
			if (partnerUnit == null) {
				partnerUnit= CoreModel.getDefault().createTranslationUnitFrom(cProject.getCProject(),
						partnerFileLoation);
			}
		}
		return partnerUnit;
	}
}
