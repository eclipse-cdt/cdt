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
package org.eclipse.cdt.codan.core.model;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Alena
 * 
 */
public abstract class AbstractIndexAstChecker extends AbstractChecker implements
		ICAstChecker {
	private IFile file;

	public IFile getFile() {
		return file;
	}

	void processFile(IFile file) throws CoreException, InterruptedException {
		this.file = file;
		// create translation unit and access index
		ITranslationUnit tu = (ITranslationUnit) CoreModel.getDefault().create(
				file);
		if (tu == null)
			return; // not a C/C++ file
		IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());
		// lock the index for read access
		index.acquireReadLock();
		try {
			// create index based ast
			IASTTranslationUnit ast = tu.getAST(index,
					ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			// traverse the ast using the visitor pattern.
			processAst(ast);
		} finally {
			index.releaseReadLock();
		}
	}

	public boolean processResource(IResource resource) {
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
}
