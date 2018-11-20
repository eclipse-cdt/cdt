/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.cdt.internal.ui.text.spelling.CSpellingReconcileStrategy;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Reconciling strategy for C/C++ code. This is a composite strategy containing
 * the regular C/C++ model reconciler and the comment spelling strategy.
 */
public class CCompositeReconcilingStrategy extends CompositeReconcilingStrategy {
	private ITextEditor fEditor;
	private CReconcilingStrategy fCStrategy;

	/**
	 * Creates a new C/C++ reconciling strategy.
	 *
	 * @param viewer the source viewer
	 * @param editor the editor of the strategy's reconciler
	 * @param documentPartitioning the document partitioning this strategy uses for configuration
	 */
	public CCompositeReconcilingStrategy(ISourceViewer viewer, ITextEditor editor, String documentPartitioning) {
		fEditor = editor;
		fCStrategy = new CReconcilingStrategy(editor);
		setReconcilingStrategies(
				new IReconcilingStrategy[] { fCStrategy, new CSpellingReconcileStrategy(viewer, editor) });
	}

	/**
	 * Returns the problem requestor for the editor's input element.
	 *
	 * @return the problem requestor for the editor's input element
	 */
	private IProblemRequestorExtension getProblemRequestorExtension() {
		IDocumentProvider p = fEditor.getDocumentProvider();
		if (p == null) {
			return null;
		}
		IAnnotationModel m = p.getAnnotationModel(fEditor.getEditorInput());
		if (m instanceof IProblemRequestorExtension)
			return (IProblemRequestorExtension) m;
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.CompositeReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		IProblemRequestorExtension e = getProblemRequestorExtension();
		if (e != null) {
			try {
				e.beginReportingSequence();
				super.reconcile(dirtyRegion, subRegion);
			} finally {
				e.endReportingSequence();
			}
		} else {
			super.reconcile(dirtyRegion, subRegion);
		}
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.CompositeReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
	 */
	@Override
	public void reconcile(IRegion partition) {
		IProblemRequestorExtension e = getProblemRequestorExtension();
		if (e != null) {
			try {
				e.beginReportingSequence();
				super.reconcile(partition);
			} finally {
				e.endReportingSequence();
			}
		} else {
			super.reconcile(partition);
		}
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.CompositeReconcilingStrategy#initialReconcile()
	 */
	@Override
	public void initialReconcile() {
		IProblemRequestorExtension e = getProblemRequestorExtension();
		if (e != null) {
			try {
				e.beginReportingSequence();
				super.initialReconcile();
			} finally {
				e.endReportingSequence();
			}
		} else {
			super.initialReconcile();
		}
	}

	/**
	 * Called before reconciling is started.
	 */
	public void aboutToBeReconciled() {
		fCStrategy.aboutToBeReconciled();
	}
}
