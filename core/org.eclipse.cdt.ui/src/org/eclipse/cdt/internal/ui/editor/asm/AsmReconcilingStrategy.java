/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor.asm;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IWorkingCopy;
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

/**
 * Reconciling strategy for assembly translation units.
 *
 * @since 5.0
 */
public class AsmReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	private ITextEditor fEditor;

	public AsmReconcilingStrategy(ITextEditor editor) {
		fEditor = editor;
	}

	private IProgressMonitor fProgressMonitor;

	/*
	 * @see IReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
	 */
	@Override
	public void reconcile(IRegion partition) {
		reconcile(false);
	}

	/*
	 * @see IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		// unused - non-incremental reconciler
	}

	/*
	 * @see IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
	 */
	@Override
	public void setDocument(IDocument document) {
		// no-op
	}

	/*
	 * @see IReconcilingStrategyExtension#setProgressMonitor(IProgressMonitor)
	 */
	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor = monitor;
	}

	/*
	 * @see IReconcilingStrategyExtension#initialReconcile()
	 */
	@Override
	public void initialReconcile() {
		reconcile(true);
	}

	private void reconcile(final boolean initialReconcile) {
		IWorkingCopyManager fManager = CUIPlugin.getDefault().getWorkingCopyManager();
		IWorkingCopy workingCopy = fManager.getWorkingCopy(fEditor.getEditorInput());
		if (workingCopy == null) {
			return;
		}
		try {
			// reconcile
			synchronized (workingCopy) {
				workingCopy.reconcile(false, true, fProgressMonitor);
			}
		} catch (OperationCanceledException oce) {
			// document was modified while parsing
		} catch (CModelException e) {
			IStatus status = new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.OK,
					"Error in CDT UI during reconcile", e); //$NON-NLS-1$
			CUIPlugin.log(status);
		}
	}

}
