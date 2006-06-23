/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.text.makefile;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.editor.IReconcilingParticipant;
import org.eclipse.cdt.make.internal.ui.editor.MakefileContentOutlinePage;
import org.eclipse.cdt.make.internal.ui.editor.MakefileEditor;
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
		fOutliner= editor.getOutlinePage();
		fLastRegionOffset = Integer.MAX_VALUE;
		fEditor= editor;
		fManager= MakeUIPlugin.getDefault().getWorkingCopyManager();
		fDocumentProvider= MakeUIPlugin.getDefault().getMakefileDocumentProvider();
		if (fEditor instanceof IReconcilingParticipant) {
			fMakefileReconcilingParticipant= (IReconcilingParticipant)fEditor;
		}

	}
	
	/**
	 * @see IReconcilingStrategy#reconcile(document)
	 */
	public void setDocument(IDocument document) {
	}	


	/**
	 * @see IReconcilingStrategy#reconcile(region)
	 */
	public void reconcile(IRegion region) {
		// We use a trick to avoid running the reconciler multiple times
		// on a file when it gets changed. This is because this gets called
		// multiple times with different regions of the file, we do a 
		// complete parse on the first region.
		if(region.getOffset() <= fLastRegionOffset) {
			reconcile();
		}
		fLastRegionOffset = region.getOffset();
	}

	/**
	 * @see IReconcilingStrategy#reconcile(dirtyRegion, region)
	 */
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
					makefile.parse(makefile.getFileName(), reader);
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
