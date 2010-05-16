/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
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
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IRunnableInEditorChecker;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
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
public abstract class AbstractIndexAstChecker extends AbstractCheckerWithProblemPreferences implements
		ICAstChecker, IRunnableInEditorChecker {
	private IFile file;

	protected IFile getFile() {
		return file;
	}

	protected IProject getProject() {
		return file == null ? null : file.getProject();
	}

	void processFile(IFile file) throws CoreException, InterruptedException {
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

	@SuppressWarnings("restriction")
	public void reportProblem(String id, IASTNode astNode, Object... args) {
		IASTFileLocation astLocation = astNode.getFileLocation();
		IPath location = new Path(astLocation.getFileName());
		IFile astFile = ResourceLookup.selectFileForLocation(location,
				getProject());
		if (astFile == null) {
			astFile = file;
		}
		if (astFile == null) {
			Activator.log("Cannot resolve location: " + location); //$NON-NLS-1$
			return;
		}
		IProblemLocation loc;
		int line = astLocation.getStartingLineNumber();
		if (line == astLocation.getEndingLineNumber())
			loc = getRuntime().getProblemLocationFactory()
					.createProblemLocation(
							astFile,
							astLocation.getNodeOffset(),
							astLocation.getNodeOffset()
									+ astLocation.getNodeLength(), line);
		else
			loc = getRuntime().getProblemLocationFactory()
					.createProblemLocation(astFile, line);
		getProblemReporter().reportProblem(id, loc, args);
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
			IASTTranslationUnit ast = (IASTTranslationUnit) model;
			IPath location = new Path(ast.getFilePath());
			IFile astFile = ResourceLookup.selectFileForLocation(location,
					getProject());
			file = astFile;
			processAst(ast);
		}
	}
}
