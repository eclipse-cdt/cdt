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
package org.eclipse.cdt.internal.ui;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * A painter for drawing visible characters for (invisible) whitespace 
 * characters.
 * 
 * @author anton.leherbauer@windriver.com
 */
public class InvisibleCharacterPainter implements IPainter, PaintListener {

	private static final char PILCROW_SIGN= '\u00b6';
	private static final char MIDDLE_DOT= '\u00b7';
	private static final char RIGHT_POINTING_DOUBLE_ANGLE= '\u00bb';
	private static final char LATIN_SMALL_LETTER_THORN= '\u00fe';
	
	/** Indicates whether this painter is active */
	private boolean fIsActive= false;
	/** The source viewer this painter is attached to */
	private ITextViewer fTextViewer;
	/** The viewer's widget */
	private StyledText fTextWidget;

	/**
	 * Creates a new painter for the given text viewer.
	 * @param textViewer  the text viewer the painter should be attached to
	 */
	public InvisibleCharacterPainter(ITextViewer textViewer) {
		super();
		fTextViewer= textViewer;
		fTextWidget= textViewer.getTextWidget();
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#dispose()
	 */
	public void dispose() {
		fTextViewer= null;
		fTextWidget= null;
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#paint(int)
	 */
	public void paint(int reason) {
		IDocument document= fTextViewer.getDocument();
		if (document == null) {
			deactivate(false);
			return;
		}
		if (!fIsActive) {
			fIsActive= true;
			fTextWidget.addPaintListener(this);
			redrawAll(true);
		} else if (reason == CONFIGURATION || reason == INTERNAL) {
			redrawAll(false);
		} else if (reason == TEXT_CHANGE) {
			// redraw current line only
			try {
				IRegion lineRegion =
					document.getLineInformationOfOffset(getDocumentOffset(fTextWidget.getCaretOffset()));
				int widgetOffset= getWidgetOffset(lineRegion.getOffset());
				int charCount= fTextWidget.getCharCount();
				int redrawLength= Math.min(lineRegion.getLength(), charCount - widgetOffset);
				if (widgetOffset >= 0 && redrawLength > 0) {
					fTextWidget.redrawRange(widgetOffset, redrawLength, true);
				}
			} catch (BadLocationException e) {
				// ignore
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#deactivate(boolean)
	 */
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive= false;
			fTextWidget.removePaintListener(this);
			if (redraw) {
				redrawAll(true);
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#setPositionManager(org.eclipse.jface.text.IPaintPositionManager)
	 */
	public void setPositionManager(IPaintPositionManager manager) {
		// no need for a position manager
	}

	/*
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent event) {
		if (fTextWidget != null) {
			handleDrawRequest(event.gc, event.x, event.y, event.width, event.height);
		}
	}

	/**
	 * Draw characters in view range.
	 * @param gc
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	private void handleDrawRequest(GC gc, int x, int y, int w, int h) {
		int lineCount= fTextWidget.getLineCount();
		int startLine= (y + fTextWidget.getTopPixel()) / fTextWidget.getLineHeight();
		int endLine= (y + h - 1 + fTextWidget.getTopPixel()) / fTextWidget.getLineHeight();
		if (startLine <= endLine && startLine < lineCount) {
			int startOffset= fTextWidget.getOffsetAtLine(startLine);
			int endOffset =
				endLine < lineCount - 1 ? fTextWidget.getOffsetAtLine(endLine + 1) : fTextWidget.getCharCount();
			handleDrawRequest(gc, startOffset, endOffset);
		}
	}

	/**
	 * Draw characters of content range.
	 * @param gc
	 * @param startOffset inclusive start index
	 * @param endOffset exclusive end index
	 */
	private void handleDrawRequest(GC gc, int startOffset, int endOffset) {
		StyledTextContent content= fTextWidget.getContent();
		int length= endOffset - startOffset;
		String text= content.getTextRange(startOffset, length);
		assert text.length() == length;
		StyleRange styleRange= null;
		Color fg= null;
		Point selection= fTextWidget.getSelection();
		StringBuffer visibleChar= new StringBuffer(20);
		for (int textOffset= 0; textOffset <= length; ++textOffset) {
			int delta= 0;
			if (textOffset < length) {
				delta= 1;
				char c= text.charAt(textOffset);
				switch (c) {
				case ' ' :
					visibleChar.append(MIDDLE_DOT);
					continue;
				case '\t' :
					visibleChar.append(RIGHT_POINTING_DOUBLE_ANGLE);
					break;
				case '\r' :
					visibleChar.append(LATIN_SMALL_LETTER_THORN);
					if (textOffset >= length - 1 || text.charAt(textOffset + 1) != '\n') {
						break;
					}
					continue;
				case '\n' :
					visibleChar.append(PILCROW_SIGN);
					break;
				default :
					delta= 0;
					break;
				}
			}
			if (visibleChar.length() > 0) {
				int widgetOffset= startOffset + textOffset - visibleChar.length() + delta;
				if (widgetOffset >= selection.x && widgetOffset < selection.y) {
					fg= fTextWidget.getSelectionForeground();
				} else if (styleRange == null || styleRange.start + styleRange.length <= widgetOffset) {
					styleRange= fTextWidget.getStyleRangeAtOffset(widgetOffset);
					if (styleRange == null || styleRange.foreground == null) {
						fg= fTextWidget.getForeground();
					} else {
						fg= styleRange.foreground;
					}
				}
				draw(gc, widgetOffset, visibleChar.toString(), fg);
				visibleChar.delete(0, visibleChar.length());
			}
		}
	}

	/**
	 * Redraw all of the text widgets visible content.
	 * @param redrawBackground  If true, clean background before painting text.
	 */
	private void redrawAll(boolean redrawBackground) {
		int startLine= fTextWidget.getTopPixel() / fTextWidget.getLineHeight();
		int startOffset= fTextWidget.getOffsetAtLine(startLine);
		int endLine= 1 + (fTextWidget.getTopPixel() + fTextWidget.getClientArea().height) / fTextWidget.getLineHeight();
		int endOffset;
		if (endLine >= fTextWidget.getLineCount()) {
			endOffset= fTextWidget.getCharCount();
		} else {
			endOffset= fTextWidget.getOffsetAtLine(endLine);
		}
		if (startOffset < endOffset) {
			// add 2 for line separator characters
			endOffset= Math.min(endOffset + 2, fTextWidget.getCharCount());
			int redrawOffset= startOffset;
			int redrawLength= endOffset - redrawOffset;
			fTextWidget.redrawRange(startOffset, redrawLength, redrawBackground);
		}
	}

	/**
	 * Draw character c at widget offset.
	 * @param gc
	 * @param offset the widget offset
	 * @param c the character to be drawn
	 * @param fg the foreground color
	 */
	private void draw(GC gc, int offset, String c, Color fg) {
		Point pos= fTextWidget.getLocationAtOffset(offset);
		gc.setForeground(fg);
		gc.drawString(c, pos.x, pos.y, true);
	}

	/**
	 * Convert a document offset to the corresponding widget offset.
	 * @param documentOffset
	 * @return widget offset
	 */
	private int getWidgetOffset(int documentOffset) {
		if (fTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5)fTextViewer;
			return extension.modelOffset2WidgetOffset(documentOffset);
		} else {
			IRegion visible= fTextViewer.getVisibleRegion();
			int widgetOffset= documentOffset - visible.getOffset();
			if (widgetOffset > visible.getLength()) {
				return -1;
			}
			return widgetOffset;
		}
	}

	/**
	 * Convert a widget offset to the corresponding document offset.
	 * @param widgetOffset
	 * @return document offset
	 */
	private int getDocumentOffset(int widgetOffset) {
		if (fTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5)fTextViewer;
			return extension.widgetOffset2ModelOffset(widgetOffset);
		} else {
			IRegion visible= fTextViewer.getVisibleRegion();
			if (widgetOffset > visible.getLength()) {
				return -1;
			}
			return widgetOffset + visible.getOffset();
		}
	}

}
