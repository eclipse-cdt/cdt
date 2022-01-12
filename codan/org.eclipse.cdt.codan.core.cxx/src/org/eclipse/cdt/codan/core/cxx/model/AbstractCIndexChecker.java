/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.model;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Implementation of IChecker that works with C-Index of a file (but not AST)
 *
 * Clients may extend this class.
 */
public abstract class AbstractCIndexChecker extends AbstractCElementChecker {
	private IFile file;
	protected IIndex index;

	protected IFile getFile() {
		return file;
	}

	@Override
	protected void processTranslationUnitUnlocked(ITranslationUnit tu) {
		try {
			index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());
			// lock the index for read access
			index.acquireReadLock();
			try {
				// traverse the translation unit using the visitor pattern.
				this.file = tu.getFile();
				processUnit(tu);
			} finally {
				this.file = null;
				index.releaseReadLock();
			}
		} catch (CoreException e) {
			CodanCorePlugin.log(e);
		} catch (InterruptedException e) {
			// ignore
		}
	}
}
