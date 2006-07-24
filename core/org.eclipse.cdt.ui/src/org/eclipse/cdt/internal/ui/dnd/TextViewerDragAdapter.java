/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dnd;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.ITextEditorExtension;

/**
 * Drag source adapter for text selections in ITextViewers.
 */
public class TextViewerDragAdapter extends DragSourceAdapter {

	/** The position category to be used to indicate a drag source selection */
	public final static String DRAG_SELECTION_CATEGORY= "dragSelectionCategory"; //$NON-NLS-1$
	/** The position updater for the drag selection position */
	private IPositionUpdater fPositionUpdater;
	/** The drag selection position */
	private Position fSelectionPosition;
	/** The text viewer allowing drag */
	private ITextViewer fViewer;
	/** The editor of the viewer (may be null) */
	private ITextEditorExtension fEditor;

	/**
	 * Create a new TextViewerDragAdapter.
	 * @param viewer the text viewer
	 */
	public TextViewerDragAdapter(ITextViewer viewer) {
		this(viewer, null);
	}

	/**
	 * Create a new TextViewerDragAdapter.
	 * @param viewer the text viewer
	 */
	public TextViewerDragAdapter(ITextViewer viewer, ITextEditorExtension editor) {
		fViewer= viewer;
		fEditor= editor;
	}
	/*
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragFinished(DragSourceEvent event) {
		IDocument doc= fViewer.getDocument();
		try {
			doc.removePositionCategory(DRAG_SELECTION_CATEGORY);
			doc.removePositionUpdater(fPositionUpdater);
		} catch (BadPositionCategoryException e1) {
			// cannot happen
		}
		if (event.doit && event.detail == DND.DROP_MOVE && isDocumentEditable()) {
			try {
				doc.replace(fSelectionPosition.offset, fSelectionPosition.length, null);
			} catch (BadLocationException e) {
				// ignore
			}
		}
		if (fViewer instanceof ITextViewerExtension) {
			((ITextViewerExtension)fViewer).getRewriteTarget().endCompoundChange();
		}
	}

	/*
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragSetData(DragSourceEvent event) {
		IDocument doc= fViewer.getDocument();
		try {
			event.data= doc.get(fSelectionPosition.offset, fSelectionPosition.length);
		} catch (BadLocationException e) {
			event.detail= DND.DROP_NONE;
			event.doit= false;
		}
	}

	/*
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragStart(DragSourceEvent event) {
		// disable text drag on GTK until bug 151197 is fixed
		if (Platform.WS_GTK.equals(Platform.getWS())) {
			event.doit = false;
			return;
		}
		/// convert screen coordinates to widget offest
		int offset= getOffsetAtLocation(event.x, event.y, false);
		// convert further to a document offset
		offset= getDocumentOffset(offset);
		Point selection= fViewer.getSelectedRange();
		if (selection != null && offset >= selection.x && offset < selection.x+selection.y) {
			fSelectionPosition= new Position(selection.x, selection.y);
			if (fViewer instanceof ITextViewerExtension) {
				((ITextViewerExtension)fViewer).getRewriteTarget().beginCompoundChange();
			}
			IDocument doc= fViewer.getDocument();
			try {
				// add the drag selection position
				// the position is used to delete the selection on a DROP_MOVE
				// and it can be used by the drop target to determine if it should
				// allow the drop (e.g. if drop location overlaps selection)
				doc.addPositionCategory(DRAG_SELECTION_CATEGORY);
				fPositionUpdater= new DefaultPositionUpdater(DRAG_SELECTION_CATEGORY);
				doc.addPositionUpdater(fPositionUpdater);
				doc.addPosition(DRAG_SELECTION_CATEGORY, fSelectionPosition);
			} catch (BadLocationException e) {
				// should not happen
			} catch (BadPositionCategoryException e) {
				// cannot happen
			}
			event.doit= true;
			// this has no effect?
			event.detail = DND.DROP_COPY;
			if (isDocumentEditable()) {
				event.detail |= DND.DROP_MOVE;
			}
		} else {
			event.doit= false;
			event.detail = DND.DROP_NONE;
		}
	}

	/**
	 * Convert mouse screen coordinates to a <code>StyledText</code> offset.
	 * @param x
	 * @param y
	 * @param absolute if true, coordinates are expected to be absolute
	 *        screen coordinates
	 * @return text offset
	 */
	private int getOffsetAtLocation(int x, int y, boolean absolute) {
		StyledText textWidget= fViewer.getTextWidget();
		StyledTextContent content= textWidget.getContent();
		Point location;
		if (absolute) {
			location= textWidget.toControl(x, y);
		} else {
			location= new Point(x ,y);
		}
		int line= (textWidget.getTopPixel() + location.y) / textWidget.getLineHeight();
		if (line >= content.getLineCount()) {
			return content.getCharCount();
		}
		int lineOffset= content.getOffsetAtLine(line);
		String lineText= content.getLine(line);
		Point endOfLine= textWidget.getLocationAtOffset(lineOffset + lineText.length());
		if (location.x >= endOfLine.x) {
			return lineOffset + lineText.length();
		}
		try {
			return textWidget.getOffsetAtLocation(location);
		} catch (IllegalArgumentException iae) {
			// we are expecting this
			return -1;
		}
	}

	/**
	 * Convert a widget offset to the corresponding document offset.
	 * @param widgetOffset
	 * @return document offset
	 */
	private int getDocumentOffset(int widgetOffset) {
		if (fViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5)fViewer;
			return extension.widgetOffset2ModelOffset(widgetOffset);
		} else {
			IRegion visible= fViewer.getVisibleRegion();
			if (widgetOffset > visible.getLength()) {
				return -1;
			}
			return widgetOffset + visible.getOffset();
		}
	}

	/**
	 * @return true if the document may be changed by the drag.
	 */
	private boolean isDocumentEditable() {
		if (fEditor != null) {
			return !fEditor.isEditorInputReadOnly();
		}
		return fViewer.isEditable();
	}

}
