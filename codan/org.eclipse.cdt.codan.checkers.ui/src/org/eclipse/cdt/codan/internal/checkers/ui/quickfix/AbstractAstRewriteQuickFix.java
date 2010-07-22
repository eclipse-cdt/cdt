/*******************************************************************************
 * Copyright (c) 2010 Alena Laskavaia
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;

public abstract class AbstractAstRewriteQuickFix extends
		AbstractCodanCMarkerResolution {
	@Override
	public void apply(final IMarker marker, IDocument document) {
		try {
			openEditor(marker).doSave(new NullProgressMonitor());
			IIndex index;
			try {
				index = getIndexFromMarker(marker);
			} catch (CoreException e) {
				e.printStackTrace();
				CheckersUiActivator.log(e);
				return;
			}
			// lock the index for read access
			try {
				index.acquireReadLock();
			} catch (InterruptedException e) {
				return;
			}
			try {
				modifyAST(index, marker);
			} finally {
				index.releaseReadLock();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param ast
	 * @param astName
	 * @param r
	 */
	public abstract void modifyAST(IIndex index, IMarker marker);
}
