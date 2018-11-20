/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.ui.texteditor.ITextEditor;

public class CReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
	private ITextEditor fEditor;
	private IWorkingCopyManager fManager;
	private IProgressMonitor fProgressMonitor;
	// used by tests
	protected boolean fInitialProcessDone;

	public CReconcilingStrategy(ITextEditor editor) {
		fEditor = editor;
		fManager = CUIPlugin.getDefault().getWorkingCopyManager();
	}

	@Override
	public void setDocument(IDocument document) {
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		// only called for incremental reconciler
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor = monitor;
	}

	@Override
	public void reconcile(IRegion region) {
		reconcile(false);
	}

	private void reconcile(final boolean initialReconcile) {
		boolean computeAST = fEditor instanceof ICReconcilingListener;
		IASTTranslationUnit ast = null;
		IWorkingCopy workingCopy = fManager.getWorkingCopy(fEditor.getEditorInput());
		if (workingCopy == null) {
			return;
		}
		boolean forced = false;
		try {
			// reconcile
			synchronized (workingCopy) {
				forced = workingCopy.isConsistent();
				ast = workingCopy.reconcile(computeAST, true, fProgressMonitor);
			}
		} catch (OperationCanceledException e) {
			// document was modified while parsing
		} catch (CModelException e) {
			IStatus status = new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.OK,
					"Error in CDT UI during reconcile", e); //$NON-NLS-1$
			CUIPlugin.log(status);
		} finally {
			if (computeAST) {
				IIndex index = null;
				if (ast != null) {
					index = ast.getIndex();
				}
				try {
					final boolean canceled = fProgressMonitor.isCanceled();
					if (ast == null || canceled) {
						((ICReconcilingListener) fEditor).reconciled(null, forced, fProgressMonitor);
					} else {
						((ASTTranslationUnit) ast).beginExclusiveAccess();
						try {
							((ICReconcilingListener) fEditor).reconciled(ast, forced, fProgressMonitor);
						} finally {
							((ASTTranslationUnit) ast).endExclusiveAccess();
						}
					}
					if (canceled) {
						aboutToBeReconciled();
					}
				} catch (Exception e) {
					IStatus status = new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.OK,
							"Error in CDT UI during reconcile", e); //$NON-NLS-1$
					CUIPlugin.log(status);
				} finally {
					if (index != null) {
						index.releaseReadLock();
					}
				}
			}
		}
	}

	@Override
	public void initialReconcile() {
		reconcile(true);
		fInitialProcessDone = true;
	}

	void aboutToBeReconciled() {
		if (fEditor instanceof ICReconcilingListener) {
			((ICReconcilingListener) fEditor).aboutToBeReconciled();
		}
	}
}
