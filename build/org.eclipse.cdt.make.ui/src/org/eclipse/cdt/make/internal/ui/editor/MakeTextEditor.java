/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.StatusTextEditor;

public class MakeTextEditor extends StatusTextEditor {
	public final static String MAKE_COMMENT = "make_comment"; //$NON-NLS-1$
	public final static String MAKE_KEYWORD = "make_keyword"; //$NON-NLS-1$
	public final static String MAKE_MACRO_VAR = "macro_var"; //$NON-NLS-1$
	public final static String MAKE_META_DATA = "meta_data"; //$NON-NLS-1$

	private boolean presentationState;

	public MakeTextEditor() {
		super();
		initializeEditor();
	}

	/**
	 * @see AbstractTextEditor#init(IEditorSite, IEditorInput)
	 */
	protected void initializeEditor() {

		setSourceViewerConfiguration(new MakeEditorConfiguration(new MakeColorManager()));
		setRangeIndicator(new DefaultRangeIndicator());
		setEditorContextMenuId("#MakeEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#MakeRulerContext"); //$NON-NLS-1$

	}

	public boolean getPresentationState() {
		return presentationState;
	}

	public void setPresentationState(boolean newState) {
		ISourceViewer srcViewer = getSourceViewer();
		if (null == srcViewer)
			return;
		presentationState = newState;
		if (newState)
			srcViewer.resetVisibleRegion();
		else {
			IDocument document = getDocumentProvider().getDocument(getEditorInput());
			int nVisibleRegionLength;
			// Collect old information
			for (int offset = 0; offset < document.getLength();) {
				try {
					ITypedRegion region = document.getPartition(offset);
					if (region.getType().equals(MakePartitionScanner.MAKE_INTERNAL)) {
						nVisibleRegionLength = region.getOffset();
						srcViewer = getSourceViewer();
						if (null != srcViewer)
							srcViewer.setVisibleRegion(0, nVisibleRegionLength);
						break;
					}
					offset += Math.max(region.getLength(), 1);
				} catch (BadLocationException e) {
					break;
				}
			}
		}
	}

	/**
	 * @see org.eclipse.ui.texteditor.ITextEditor#selectAndReveal(int, int)
	 */
	public void selectAndReveal(int offset, int length) {
		super.selectAndReveal(offset, length);
		// Update visible region because text could be updated
		// This is not the best place for that, at least not very 
		// straightforward. However keep it there for a meantime.
		setPresentationState(getPresentationState());
	}

}
