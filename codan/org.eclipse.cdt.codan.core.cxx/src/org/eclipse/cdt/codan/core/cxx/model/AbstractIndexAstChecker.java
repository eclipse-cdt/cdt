/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.model;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.cxx.Activator;
import org.eclipse.cdt.codan.core.model.AbstractCheckerWithProblemPreferences;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.cdt.codan.core.model.IRunnableInEditorChecker;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Convenience implementation of checker that work on index based ast of a c/c++
 * program.
 * 
 * Clients may extend this class.
 */
public abstract class AbstractIndexAstChecker extends AbstractCheckerWithProblemPreferences implements ICAstChecker,
		IRunnableInEditorChecker {
	private IFile file;
	private ICodanCommentMap commentmap;

	protected IFile getFile() {
		return file;
	}

	protected IProject getProject() {
		return file == null ? null : file.getProject();
	}

	void processFile(IFile file) throws CoreException, InterruptedException {
		commentmap = null;
		IASTTranslationUnit ast = CxxModelsCache.getInstance().getAst(file);
		if (ast == null)
			return;
		// lock the index for read access
		IIndex index = CxxModelsCache.getInstance().getIndex(file);
		index.acquireReadLock();
		try {
			// traverse the ast using the visitor pattern.
			this.file = file;
			processAst(ast);
		} finally {
			this.file = null;
			index.releaseReadLock();
		}
	}

	public synchronized boolean processResource(IResource resource) {
		if (!shouldProduceProblems(resource))
			return false;
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			try {
				processFile(file);
			} catch (CoreException e) {
				CodanCorePlugin.log(e);
			} catch (InterruptedException e) {
				// ignore
			}
			return false;
		}
		return true;
	}


	public void reportProblem(String id, IASTNode astNode, Object... args) {
		IProblemLocation loc = getProblemLocation(astNode);
		if (loc!=null) reportProblem(id, loc, args);
	}
	public void reportProblem(IProblem problem, IASTNode astNode, Object... args) {
		IProblemLocation loc = getProblemLocation(astNode);
		if (loc!=null) reportProblem(problem, loc, args);
	}

	@SuppressWarnings("restriction")
	protected IProblemLocation getProblemLocation(IASTNode astNode) {
		IASTFileLocation astLocation = astNode.getFileLocation();
		IPath location = new Path(astLocation.getFileName());
		IFile astFile = ResourceLookup.selectFileForLocation(location, getProject());
		if (astFile == null) {
			astFile = file;
		}
		if (astFile == null) {
			Activator.log("Cannot resolve location: " + location); //$NON-NLS-1$
			return null;
		}
		return getProblemLocation(astNode, astLocation, astFile);
	}

	private IProblemLocation getProblemLocation(IASTNode astNode, IASTFileLocation astLocation, IFile astFile) {
		int line = astLocation.getStartingLineNumber();
		IProblemLocationFactory locFactory = getRuntime().getProblemLocationFactory();
		if (hasMacroLocation(astNode) && astNode instanceof IASTName) {
			IASTImageLocation imageLocation = ((IASTName) astNode).getImageLocation();
			if (imageLocation != null) {
				int start = imageLocation.getNodeOffset();
				int end = start + imageLocation.getNodeLength();
				return locFactory.createProblemLocation(astFile, start, end, line);
			}
		}
		if (line == astLocation.getEndingLineNumber()) {
			return locFactory.createProblemLocation(astFile, astLocation.getNodeOffset(),
					astLocation.getNodeOffset() + astLocation.getNodeLength(), line);
		}
		return locFactory.createProblemLocation(astFile, line);
	}

	private boolean hasMacroLocation(IASTNode astNode) {
		return astNode.getNodeLocations().length == 1 && astNode.getNodeLocations()[0] instanceof IASTMacroExpansionLocation;
	}

	@Override
	public boolean runInEditor() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.model.IRunnableInEditorChecker#processModel
	 * (java.lang.Object)
	 */
	@SuppressWarnings("restriction")
	public synchronized void processModel(Object model) {
		if (model instanceof IASTTranslationUnit) {
			CxxModelsCache.getInstance().clearCash();
			IASTTranslationUnit ast = (IASTTranslationUnit) model;
			IPath location = new Path(ast.getFilePath());
			IFile astFile = ResourceLookup.selectFileForLocation(location, getProject());
			file = astFile;
			commentmap = null;
			processAst(ast);
		}
	}

	protected ICodanCommentMap getCommentMap() {
		if (commentmap == null) {
			try {
				CxxModelsCache cxxcache = CxxModelsCache.getInstance();
				synchronized (cxxcache) {
					IASTTranslationUnit ast = cxxcache.getAst(getFile());
					commentmap = cxxcache.getCommentedNodeMap(ast);
					return commentmap;
				}

			} catch (Exception e) {
				Activator.log(e);
			}
		}
		return commentmap;
	}
}
