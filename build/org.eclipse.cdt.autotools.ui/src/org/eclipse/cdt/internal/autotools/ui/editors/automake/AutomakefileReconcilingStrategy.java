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
 *     Red Hat Inc. - Modified from MakefileReconcilingStrategy for Automake files
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class AutomakefileReconcilingStrategy implements IReconcilingStrategy {

	private int fLastRegionOffset;
	private ITextEditor fEditor;
	private IWorkingCopyManager fManager;
	private IDocumentProvider fDocumentProvider;
	private AutomakefileContentOutlinePage fOutliner;
	private IReconcilingParticipant fMakefileReconcilingParticipant;
	private AutomakeErrorHandler fErrorHandler;
	private IEditorInput input;

	public AutomakefileReconcilingStrategy(AutomakeEditor editor) {
		fOutliner = editor.getAutomakeOutlinePage();
		fLastRegionOffset = Integer.MAX_VALUE;
		fEditor = editor;
		input = fEditor.getEditorInput();
		fManager = AutomakeEditorFactory.getDefault().getWorkingCopyManager();
		fDocumentProvider = AutomakeEditorFactory.getDefault().getAutomakefileDocumentProvider();
		fErrorHandler = new AutomakeErrorHandler(input);
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
		// FIXME: This seems to generate too much flashing in
		// the contentoutline viewer.
		//reconcile();
	}

	private void reconcile() {
		try {
			IMakefile makefile = fManager.getWorkingCopy(fEditor.getEditorInput());
			if (makefile != null) {
				String content = fDocumentProvider.getDocument(input).get();
				StringReader reader = new StringReader(content);
				try {
					makefile.parse(makefile.getFileURI(), reader);
				} catch (IOException e) {
				}

				fOutliner.update();
				fErrorHandler.update(makefile);
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
