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
package org.eclipse.cdt.codan.ui;

import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Generic class for codan marker resolution (for quick fix). Use as a base
 * class for codanMarkerResolution extension. To add specific icon and
 * description client class should additionally implement
 * {@link IMarkerResolution2}
 */
public abstract class AbstarctCodanCMarkerResolution implements IMarkerResolution {
	/**
	 * Get position offset from marker. If CHAR_START attribute is not set for
	 * marker, line and document would be used.
	 * 
	 * @param marker
	 * @param doc
	 * @return
	 */
	public int getOffset(IMarker marker, IDocument doc) {
		int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
		int position;
		if (charStart > 0) {
			position = charStart;
		} else {
			int line = marker.getAttribute(IMarker.LINE_NUMBER, -1) - 1;
			try {
				position = doc.getLineOffset(line);
			} catch (BadLocationException e) {
				return -1;
			}
		}
		return position;
	}

	/**
	 * Runs this resolution.
	 * 
	 * @param marker
	 *            the marker to resolve
	 */
	public void run(IMarker marker) {
		IEditorPart editorPart;
        try {
	        editorPart = CodanEditorUtility.openInEditor(marker);
        } catch (PartInitException e) {
	        CodanUIActivator.log(e);
	        return;
        }
		if (editorPart instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) editorPart;
			IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			apply(marker, doc);
		}
	}

	/**
	 * Apply marker resolution for given marker in given open document.
	 * 
	 * @param marker
	 * @param document
	 */
	public abstract void apply(IMarker marker, IDocument document);

	/**
	 * Override is extra checks is required to determine appicablity of marker resolution
	 * @param marker
	 * @return
	 */
	public boolean isApplicable(IMarker marker) {
		return true;
	}
}
