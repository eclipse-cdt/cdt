/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * A painter for configurable background painting a range of text lines.
 * Replicates also the functionality of the 
 * {@link org.eclipse.jface.text.CursorLinePainter}
 * because only one {@link LineBackgroundListener} is allowed
 * per {@link StyledText} widget.
 * 
 * @author anton.leherbauer@windriver.com
 * 
 * @since 4.0
 */
public class LineBackgroundPainter implements IPainter, LineBackgroundListener {

	/** The default position type for untyped positions */
	private static final String DEFAULT_TYPE= "__default__"; //$NON-NLS-1$
	/** The position type for the cursor line position */
	private static final String CURSOR_LINE_TYPE= "__cursor_line__"; //$NON-NLS-1$

	/** Manager for position changes */
	private IPaintPositionManager fPositionManager;
	/** Indicates whether this painter is active */
	private boolean fIsActive= false;
	/** The text viewer this painter is associated with */
	private ITextViewer fTextViewer;
	/** The viewer's widget */
	private StyledText fTextWidget;
	/** Text positions (cursor line position is always at index 0 */
	private List<Position> fPositions= new ArrayList<Position>();
	/** Cached text positions */
	private List<Position> fLastPositions= new ArrayList<Position>();
	/** Temporary changed positions */
	private List<Position> fChangedPositions= new ArrayList<Position>();
	/** Cursor line position */
	private Position fCursorLine= new TypedPosition(0, 0, CURSOR_LINE_TYPE);
	/** Saved cursor line position */
	private Position fLastCursorLine= new Position(0, 0);
	/** Enablement of the cursor line highlighting */
	private boolean fCursorLineEnabled;
	/** Whether cursor line highlighting is active */
	private boolean fCursorLineActive;
	/** Map of position type to color */
	private Map<String, Color> fColorMap= new HashMap<String, Color>();

	/**
	 * Creates a new painter for the given text viewer.
	 * @param textViewer
	 */
	public LineBackgroundPainter(ITextViewer textViewer) {
		super();
		fTextViewer= textViewer;
		fTextWidget= textViewer.getTextWidget();
		fPositions.add(fCursorLine);
		fLastPositions.add(fLastCursorLine);
	}

	/**
	 * Sets the color in which to draw the background of the given position type.
	 * 
	 * @param positionType  the position type for which to specify the background color
	 * @param color  the color in which to draw the background of the given position type
	 */
	public void setBackgroundColor(String positionType, Color color) {
		fColorMap.put(positionType, color);
	}

	/**
	 * Sets the color in which to draw the background of the cursor line.
	 * 
	 * @param cursorLineColor the color in which to draw the background of the cursor line
	 */
	public void setCursorLineColor(Color cursorLineColor) {
		fColorMap.put(CURSOR_LINE_TYPE, cursorLineColor);
	}

	/**
	 * Sets the color in which to draw the background of untyped positions.
	 * 
	 * @param color  the color in which to draw the background of untyped positions
	 */
	public void setDefaultColor(Color color) {
		fColorMap.put(DEFAULT_TYPE, color);
	}

	/**
	 * Enable/disable cursor line highlighting.
	 * 
	 * @param enable
	 */
	public void enableCursorLine(boolean enable) {
		fCursorLineEnabled= enable;
		fCursorLineActive= enable;
		if (fCursorLineActive) {
			updateCursorLine();
		}
	}

	/**
	 * Set highlight positions. It is assumed that all positions
	 * are up-to-date with respect to the text viewer document.
	 * 
	 * @param positions a list of <code>Position</code>s
	 */
	public void setHighlightPositions(List<Position> positions) {
		boolean isActive= fIsActive;
		deactivate(isActive);
		fPositions.clear();
		fPositions.add(fCursorLine);
		fPositions.addAll(positions);
		if (isActive) {
			activate(true);
		}
	}

	/**
	 * Add highlight positions. It is assumed that all positions
	 * are up-to-date with respect to the text viewer document.
	 * 
	 * @param positions a list of <code>Position</code>s
	 */
	public void addHighlightPositions(List<Position> positions) {
		boolean isActive= fIsActive;
		deactivate(isActive);
		fPositions.addAll(positions);
		if (isActive) {
			activate(true);
		}
	}

	/**
	 * Remove highlight positions by identity.
	 * 
	 * @param positions a list of <code>Position</code>s
	 */
	public void removeHighlightPositions(List<Position> positions) {
		boolean isActive= fIsActive;
		deactivate(isActive);
		fPositions.removeAll(positions);
		if (isActive) {
			activate(true);
		}
	}

	/**
	 * Replace given highlight positions in one step.
	 * 
	 * @param removePositions a list of <code>Position</code>s to remove
	 * @param addPositions a list of <code>Position</code>s to add
	 */
	public void replaceHighlightPositions(List<Position> removePositions, List<Position> addPositions) {
		boolean isActive= fIsActive;
		deactivate(isActive);
		fPositions.removeAll(removePositions);
		fPositions.addAll(addPositions);
		if (isActive) {
			activate(true);
		}
	}

	/**
	 * Trigger redraw of managed positions.
	 */
	public void redraw() {
		if(fIsActive) {
			fTextWidget.redraw();
		}
	}

	/**
	 * Manage all positions.
	 * @param positions
	 */
	private void managePositions(List<Position> positions) {
		if (fPositionManager == null) {
			return;
		}
		int sz= fPositions.size();
		for (int i= 0; i < sz; ++i) {
			Position position= positions.get(i);
			fPositionManager.managePosition(position);
		}
	}

	/**
	 * Unmanage all positions.
	 * @param positions
	 */
	private void unmanagePositions(List<Position> positions) {
		if (fPositionManager == null) {
			return;
		}
		int sz= fPositions.size();
		for (int i= 0; i < sz; ++i) {
			Position position= positions.get(i);
			fPositionManager.unmanagePosition(position);
		}
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#dispose()
	 */
	public void dispose() {
		// no deactivate!
		fIsActive= false;
		fTextViewer= null;
		fTextWidget= null;
		fCursorLine= null;
		fLastCursorLine= null;
		fPositions= null;
		fLastPositions= null;
		fChangedPositions= null;
		fColorMap= null;
	}

	/**
	 * Query whether this painter is already disposed.
	 * @return <code>true</code> if the painter is disposed
	 */
	public boolean isDisposed() {
		return fTextViewer == null;
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
		activate(false);

		if (fCursorLineEnabled) {
			// check selection
			StyledText textWidget= fTextViewer.getTextWidget();
			Point selection= textWidget.getSelection();
			int startLine= textWidget.getLineAtOffset(selection.x);
			int endLine= textWidget.getLineAtOffset(selection.y);
			if (startLine != endLine) {
				redrawPositions(Collections.singletonList(fLastCursorLine));
				fCursorLineActive= false;
			} else {
				fCursorLineActive= true;
			}
			if (fCursorLineActive) {
				// redraw in case of text changes prior to update of current cursor line
				if (!fLastCursorLine.equals(fCursorLine)) {
					redrawPositions(Collections.singletonList(fLastCursorLine));
					fLastCursorLine.offset= fCursorLine.offset;
					fLastCursorLine.length= fCursorLine.length;
				}
				updateCursorLine();
			}
		}
		List<Position> changedPositions= getChangedPositions();
		if (changedPositions != null) {
			redrawPositions(changedPositions);
			updatePositions();
			redrawPositions(changedPositions);
		}
	}

	/**
	 * Activate the painter.
	 * @param redraw
	 */
	private void activate(boolean redraw) {
		if (!fIsActive) {
			fIsActive= true;
			fTextWidget.addLineBackgroundListener(this);
			if (redraw) {
				if (fCursorLineActive) {
					updateCursorLine();
				}
				updatePositions();
				redrawPositions(fPositions);
			}
			managePositions(fPositions);
		}
	}

	/**
	 * Copy positions from the current position list to the last position list.
	 */
	private void updatePositions() {
		int sz= fPositions.size();
		for (int i= 0; i < sz; ++i) {
			Position position= fPositions.get(i);
			Position copy;
			if (i == fLastPositions.size()) {
				copy= new Position(position.offset, position.length);
				copy.isDeleted= position.isDeleted;
				fLastPositions.add(copy);
			} else {
				copy= fLastPositions.get(i);
				copy.offset= position.offset;
				copy.length= position.length;
				copy.isDeleted= position.isDeleted;
			}
			position.isDeleted= false;
		}
		int diff= fLastPositions.size() - sz;
		while (diff > 0) {
			--diff;
			fLastPositions.remove(sz + diff);
		}
	}

	/**
	 * Check which positions have changed since last redraw.
	 * @return a list of changed positions or <code>null</code> if none changed.
	 */
	private List<Position> getChangedPositions() {
		if (fLastPositions.size() != fPositions.size()) {
			return fLastPositions;
		}
		List<Position> changedPositions= null;
		for (int i= 0, sz= fPositions.size(); i < sz; ++i) {
			Position previous= fLastPositions.get(i);
			Position current= fPositions.get(i);
			if (!previous.equals(current)) {
				if (changedPositions == null) {
					changedPositions= fChangedPositions;
					changedPositions.clear();
				}
				changedPositions.add(previous);
			}
		}
		return changedPositions;
	}

	/**
	 * Trigger redraw of given text positions.
	 * 
	 * @param positions
	 */
	private void redrawPositions(List<Position> positions) {
		// TextViewer.getTopIndexStartOffset is buggy
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=174419
//		final int minOffset= fTextViewer.getTopIndexStartOffset();
		final int minOffset= getTopIndexStartOffset();
		final int maxOffset= fTextViewer.getBottomIndexEndOffset()+3;
		Rectangle clientArea= fTextWidget.getClientArea();
		int width= clientArea.width + fTextWidget.getHorizontalPixel();
		int lineHeight= fTextWidget.getLineHeight();
		for (int i= 0, sz= positions.size(); i < sz; ++i) {
			Position position= positions.get(i);
			// if the position that is about to be drawn was deleted then we can't
			if (position.isDeleted()) {
				continue;
			}
			// check if position overlaps with visible area
			if (!position.overlapsWith(minOffset, maxOffset - minOffset + 1)) {
				continue;
			}
			int widgetOffset= getWidgetOffset(position.offset);
			if (widgetOffset < 0 || widgetOffset > fTextWidget.getCharCount()) {
				continue;
			}
			// TLETODO [performance] SyledText.getLocationAtOffset() is very expensive
			Point upperLeft= fTextWidget.getLocationAtOffset(widgetOffset);
			int upperY= Math.max(Math.min(upperLeft.y, clientArea.height), 0);
			int height;
			if (position.length == 0) {
				height= lineHeight;
			} else {
				int widgetEndOffset= Math.min(widgetOffset + position.length, fTextWidget.getCharCount());
				Point lowerRight= fTextWidget.getLocationAtOffset(widgetEndOffset);
				int lowerY= Math.min(lowerRight.y + lineHeight, clientArea.height);
				height= lowerY - upperY;
			}
			if (height > 0) {
				fTextWidget.redraw(0, upperY, width, height, false);
			}
		}
	}

	/**
	 * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=174419
	 * @return the offset of the topmost visible line
	 * @see ITextViewer#getTopIndexStartOffset()
	 */
	private int getTopIndexStartOffset() {
		if (fTextWidget != null) {
			int top= fTextWidget.getTopIndex();
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=183653
			top= fTextWidget.getOffsetAtLine(Math.min(fTextWidget.getLineCount() - 1, top));
			if (top >= 0) {
				return getDocumentOffset(top);
			}
		}
		return -1;
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#deactivate(boolean)
	 */
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive= false;
			fTextWidget.removeLineBackgroundListener(this);
			if (redraw) {
				redrawPositions(fLastPositions);
			}
			unmanagePositions(fPositions);
		}
	}

	/*
	 * @see org.eclipse.jface.text.IPainter#setPositionManager(org.eclipse.jface.text.IPaintPositionManager)
	 */
	public void setPositionManager(IPaintPositionManager manager) {
		fPositionManager= manager;
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
		}
		IRegion visible= fTextViewer.getVisibleRegion();
		int widgetOffset= documentOffset - visible.getOffset();
		if (widgetOffset > visible.getLength()) {
			return -1;
		}
		return widgetOffset;
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
		}
		IRegion visible= fTextViewer.getVisibleRegion();
		if (widgetOffset > visible.getLength()) {
			return -1;
		}
		return widgetOffset + visible.getOffset();
	}

	/*
	 * @see org.eclipse.swt.custom.LineBackgroundListener#lineGetBackground(org.eclipse.swt.custom.LineBackgroundEvent)
	 */
	public void lineGetBackground(LineBackgroundEvent event) {
		if (fTextWidget != null) {
			Position match= findIncludingPosition(getDocumentOffset(event.lineOffset));
			if (match != null) {
				Color color= getColorForPosition(match);
				if (color != null) {
					event.lineBackground= color;
				}
			}
		}
	}

	/**
	 * Get the color associated with given position.
	 * @param position
	 * @return the color associated with the position type
	 */
	private Color getColorForPosition(Position position) {
		if (position == fCursorLine) {
			if (fCursorLine.length == 0) {
				return fColorMap.get(CURSOR_LINE_TYPE);
			}
		} else {
			if (position instanceof TypedPosition) {
				String type= ((TypedPosition)position).getType();
				return fColorMap.get(type);
			}
			return fColorMap.get(DEFAULT_TYPE);
		}
		return null;
	}

	/**
	 * Find position which includes the (document-)offset.
	 * @param offset
	 * @return the first position including the offset or <code>null</code>.
	 */
	private Position findIncludingPosition(int offset) {
		// TLETODO [performance] Use binary search?
		for (int i= fCursorLineActive ? 0 : 1, sz= fPositions.size(); i < sz; ++i) {
			Position position= fPositions.get(i);
			if (position.offset == offset || position.includes(offset)) {
				return position;
			}
		}
		return null;
	}

	/**
	 * Updates the position of the cursor line.
	 */
	private void updateCursorLine() {
		try {
			IDocument document= fTextViewer.getDocument();
			if (document != null) {
				int lineNumber= document.getLineOfOffset(getDocumentOffset(fTextWidget.getCaretOffset()));
	
				fCursorLine.isDeleted= false;
				fCursorLine.offset= document.getLineOffset(lineNumber);
				fCursorLine.length= 0;
			}
		} catch (BadLocationException e) {
			// gracefully ignored
		}
	}
}
