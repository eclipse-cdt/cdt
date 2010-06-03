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
import org.eclipse.cdt.codan.core.model.AbstractChecker;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Implementation of IChecker that works with C-Index of a file (but not AST)
 * 
 * Clients may extend this class.
 */
public abstract class AbstractCIndexChecker extends AbstractChecker implements ICIndexChecker {
	private IFile file;
	protected IIndex index;

	protected IFile getFile() {
		return file;
	}

	void  processFile(IFile file) throws CoreException, InterruptedException {
		// create translation unit and access index
		ICElement model = CoreModel.getDefault().create(file);
		if (!(model instanceof ITranslationUnit)) return; // not a C/C++ file
		ITranslationUnit tu = (ITranslationUnit) model;
		index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());
		// lock the index for read access
		index.acquireReadLock();
		try {
			// traverse the translation unit using the visitor pattern.
			this.file = file;
			processUnit(tu);
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

	@Override
	public boolean runInEditor() {
		return false;
	}
}
