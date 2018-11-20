/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Red Hat Inc. - convert to use with Automake editor
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.editors.automake;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class MakefileReconcilingStrategy implements IReconcilingStrategy {

	private int fLastRegionOffset;
	private ITextEditor fEditor;
	private IWorkingCopyManager fManager;
	private IDocumentProvider fDocumentProvider;
	private MakefileContentOutlinePage fOutliner;
	private IReconcilingParticipant fMakefileReconcilingParticipant;

	public MakefileReconcilingStrategy(MakefileEditor editor) {
		fOutliner = editor.getOutlinePage();
		fLastRegionOffset = Integer.MAX_VALUE;
		fEditor = editor;
		fManager = AutomakeEditorFactory.getDefault().getWorkingCopyManager();
		fDocumentProvider = AutomakeEditorFactory.getDefault().getAutomakefileDocumentProvider();
		fMakefileReconcilingParticipant = (IReconcilingParticipant) fEditor;
	}

	@Override
	public void setDocument(IDocument document) {
	}

	@Override
	public void reconcile(IRegion region) {
		// We use a trick to avoid running the reconciler multiple times
		// on a file when it gets changed. This is because this gets called
		// multiple times with different regions of the file, we do a
		// complete parse on the first region.
		if (region.getOffset() <= fLastRegionOffset) {
			reconcile();
		}
		fLastRegionOffset = region.getOffset();
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion region) {
		// FIXME: This seems to generate to much flashing in
		// the contentouline viewer.
		//reconcile();
	}

	private void reconcile() {
		try {
			IMakefile makefile = fManager.getWorkingCopy(fEditor.getEditorInput());
			if (makefile != null) {
				String content = fDocumentProvider.getDocument(fEditor.getEditorInput()).get();
				StringReader reader = new StringReader(content);
				try {
					makefile.parse(makefile.getFileURI(), reader);
				} catch (IOException e) {
				}

				fOutliner.update();
			}
		} finally {
			try {
				if (fMakefileReconcilingParticipant != null) {
					fMakefileReconcilingParticipant.reconciled();
				}
			} finally {
				//
			}
		}
	}
}
