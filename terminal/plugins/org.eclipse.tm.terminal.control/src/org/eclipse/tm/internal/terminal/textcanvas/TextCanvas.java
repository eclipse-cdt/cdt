/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Michael Scharf (Wind River) - [240098] The cursor should not blink when the terminal is disconnected
 * Uwe Stieber (Wind River) - [281328] The very first few characters might be missing in the terminal control if opened and connected programmatically
 * Martin Oberhuber (Wind River) - [294327] After logging in, the remote prompt is hidden
 * Anton Leherbauer (Wind River) - [294468] Fix scroller and text line rendering
 * Uwe Stieber (Wind River) - [205486] Fix ScrollLock always moving to line 1
 * Anton Leherbauer (Wind River) - [219589] Copy an entire line selection
 * Anton Leherbauer (Wind River) - [196465] Resizing Terminal changes Scroller location
 * Anton Leherbauer (Wind River) - [324608] Terminal has strange scrolling behaviour
 * Martin Oberhuber (Wind River) - [265352][api] Allow setting fonts programmatically
 * Anton Leherbauer (Wind River) - [434749] UnhandledEventLoopException when copying to clipboard while the selection is empty
 * Davy Landman (CWI) - [475267][api] Allow custom mouse listeners
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.textcanvas;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.internal.terminal.control.ITerminalMouseListener;

/**
 * A cell oriented Canvas. Maintains a list of "cells".
 * It can either be vertically or horizontally scrolled.
 * The CellRenderer is responsible for painting the cell.
 */
public class TextCanvas extends GridCanvas {
	protected final ITextCanvasModel fCellCanvasModel;
	/** Renders the cells */
	private final ILinelRenderer fCellRenderer;
	private boolean fScrollLock;
	private Point fDraggingStart;
	private Point fDraggingEnd;
	private boolean fHasSelection;
	private ResizeListener fResizeListener;
	private final List<ITerminalMouseListener> fMouseListeners;

	// The minSize is meant to determine the minimum size of the backing store
	// (grid) into which remote data is rendered. If the viewport is smaller
	// than that minimum size, the backing store size remains at the minSize,
	// and a scrollbar is shown instead. In reality, this has the following
	// issues or effects today:
	//  (a) Bug 281328: For very early data coming in before the widget is
	//      realized, the minSize determines into what initial grid that is
	//      rendered. See also @link{#addResizeHandler(ResizeListener)}.
	//  (b) Bug 294468: Since we have redraw and size computation problems
	//      with horizontal scrollers, for now the minColumns must be small
	//      enough to avoid a horizontal scroller appearing in most cases.
	//  (b) Bug 294327: since we have problems with the vertical scroller
	//      showing the correct location, minLines must be small enough
	//      to avoid a vertical scroller or new data may be rendered off-screen.
	// As a compromise, we have been working with a 20x4 since the Terminal
	// inception, though many users would want a 80x24 minSize and backing
	// store. Pros and cons of the small minsize:
	//   + consistent "remote size==viewport size", vi works as expected
	//   - dumb terminals which expect 80x24 render garbled on small viewport.
	// If bug 294468 were resolved, an 80 wide minSize would be preferrable
	// since it allows switching the terminal viewport small/large as needed,
	// without destroying the backing store. For a complete solution,
	// Bug 196462 tracks the request for a user-defined fixed-widow-size-mode.
	private int fMinColumns = 80;
	private int fMinLines = 4;
	private boolean fCursorEnabled;
	private boolean fResizing;

	/**
	 * Create a new CellCanvas with the given SWT style bits.
	 * (SWT.H_SCROLL and SWT.V_SCROLL are automatically added).
	 */
	public TextCanvas(Composite parent, ITextCanvasModel model, int style, ILinelRenderer cellRenderer) {
		super(parent, style | SWT.H_SCROLL | SWT.V_SCROLL);
		fCellRenderer = cellRenderer;
		setCellWidth(fCellRenderer.getCellWidth());
		setCellHeight(fCellRenderer.getCellHeight());
		fCellCanvasModel = model;
		fCellCanvasModel.addCellCanvasModelListener(new ITextCanvasModelListener() {
			@Override
			public void rangeChanged(int col, int line, int width, int height) {
				if (isDisposed())
					return;
				repaintRange(col, line, width, height);
			}

			@Override
			public void dimensionsChanged(int cols, int rows) {
				if (isDisposed())
					return;
				calculateGrid();
			}

			@Override
			public void terminalDataChanged() {
				if (isDisposed())
					return;

				// scroll to end (unless scroll lock is active)
				if (!fResizing) {
					calculateGrid();
					scrollToEnd();
				}
			}
		});
		// let the cursor blink if the text canvas gets the focus...
		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				fCellCanvasModel.setCursorEnabled(fCursorEnabled);
			}

			@Override
			public void focusLost(FocusEvent e) {
				fCellCanvasModel.setCursorEnabled(false);
			}
		});
		fMouseListeners = new ArrayList<>();
		addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (fMouseListeners.size() > 0) {
					Point pt = screenPointToCell(e.x, e.y);
					if (pt != null) {
						for (ITerminalMouseListener l : fMouseListeners) {
							l.mouseDoubleClick(fCellCanvasModel.getTerminalText(), pt.y, pt.x, e.button);
						}
					}
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) { // left button
					fDraggingStart = screenPointToCell(e.x, e.y);
					fHasSelection = false;
					if ((e.stateMask & SWT.SHIFT) != 0) {
						Point anchor = fCellCanvasModel.getSelectionAnchor();
						if (anchor != null)
							fDraggingStart = anchor;
					} else {
						fCellCanvasModel.setSelectionAnchor(fDraggingStart);
					}
					fDraggingEnd = null;
				}
				if (fMouseListeners.size() > 0) {
					Point pt = screenPointToCell(e.x, e.y);
					if (pt != null) {
						for (ITerminalMouseListener l : fMouseListeners) {
							l.mouseDown(fCellCanvasModel.getTerminalText(), pt.y, pt.x, e.button);
						}
					}
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (e.button == 1) { // left button
					updateHasSelection(e);
					if (fHasSelection)
						setSelection(screenPointToCell(e.x, e.y));
					else
						fCellCanvasModel.setSelection(-1, -1, -1, -1);
					fDraggingStart = null;
				}
				if (fMouseListeners.size() > 0) {
					Point pt = screenPointToCell(e.x, e.y);
					if (pt != null) {
						for (ITerminalMouseListener l : fMouseListeners) {
							l.mouseUp(fCellCanvasModel.getTerminalText(), pt.y, pt.x, e.button);
						}
					}
				}
			}
		});
		addMouseMoveListener(e -> {
			if (fDraggingStart != null) {
				updateHasSelection(e);
				setSelection(screenPointToCell(e.x, e.y));
			}
		});
		serVerticalBarVisible(true);
		setHorizontalBarVisible(false);
	}

	/**
	 * The user has to drag the mouse to at least one character to make a selection.
	 * Once this is done, even a one char selection is OK.
	 *
	 * @param e
	 */
	private void updateHasSelection(MouseEvent e) {
		if (fDraggingStart != null) {
			Point p = screenPointToCell(e.x, e.y);
			if (fDraggingStart.x != p.x || fDraggingStart.y != p.y)
				fHasSelection = true;
		}
	}

	void setSelection(Point p) {
		if (fDraggingStart != null && !p.equals(fDraggingEnd)) {
			fDraggingEnd = p;
			if (compare(p, fDraggingStart) < 0) {
				// bug 219589 - make sure selection start coordinates are non-negative
				int startColumn = Math.max(0, p.x);
				int startRow = Math.max(p.y, 0);
				fCellCanvasModel.setSelection(startRow, fDraggingStart.y, startColumn, fDraggingStart.x);
			} else {
				fCellCanvasModel.setSelection(fDraggingStart.y, p.y, fDraggingStart.x, p.x);

			}
		}
	}

	int compare(Point p1, Point p2) {
		if (p1.equals(p2))
			return 0;
		if (p1.y == p2.y) {
			if (p1.x > p2.x)
				return 1;
			else
				return -1;
		}
		if (p1.y > p2.y) {
			return 1;
		} else {
			return -1;
		}
	}

	public ILinelRenderer getCellRenderer() {
		return fCellRenderer;
	}

	public int getMinColumns() {
		return fMinColumns;
	}

	public void setMinColumns(int minColumns) {
		fMinColumns = minColumns;
	}

	public int getMinLines() {
		return fMinLines;
	}

	public void setMinLines(int minLines) {
		fMinLines = minLines;
	}

	protected void onResize(boolean init) {
		if (fResizeListener != null) {
			Rectangle bonds = getClientArea();
			int cellHeight = getCellHeight();
			int cellWidth = getCellWidth();
			int lines = bonds.height / cellHeight;
			int columns = bonds.width / cellWidth;
			// when the view is minimised, its size is set to 0
			// we don't sent this to the terminal!
			if ((lines > 0 && columns > 0) || init) {
				if (columns < fMinColumns) {
					if (!isHorizontalBarVisble()) {
						setHorizontalBarVisible(true);
						bonds = getClientArea();
						lines = bonds.height / cellHeight;
					}
					columns = fMinColumns;
				} else if (columns >= fMinColumns && isHorizontalBarVisble()) {
					setHorizontalBarVisible(false);
					bonds = getClientArea();
					lines = bonds.height / cellHeight;
					columns = bonds.width / cellWidth;
				}
				if (lines < fMinLines)
					lines = fMinLines;
				fResizeListener.sizeChanged(lines, columns);
			}
		}
		super.onResize();
		calculateGrid();
	}

	@Override
	protected void onResize() {
		fResizing = true;
		try {
			onResize(false);
		} finally {
			fResizing = false;
		}
	}

	private void calculateGrid() {
		Rectangle virtualBounds = getVirtualBounds();
		setRedraw(false);
		try {
			setVirtualExtend(getCols() * getCellWidth(), getRows() * getCellHeight());
			getParent().layout();
			if (fResizing) {
				// scroll to end if view port was near last line
				Rectangle viewRect = getViewRectangle();
				if (virtualBounds.height - (viewRect.y + viewRect.height) < getCellHeight() * 2)
					scrollToEnd();
			}
		} finally {
			setRedraw(true);
		}
	}

	void scrollToEnd() {
		if (!fScrollLock) {
			int y = -(getRows() * getCellHeight() - getClientArea().height);
			if (y > 0) {
				y = 0;
			}
			Rectangle v = getViewRectangle();
			if (v.y != -y) {
				setVirtualOrigin(v.x, y);
			}
			// make sure the scroll area is correct:
			scrollY(getVerticalBar());
			scrollX(getHorizontalBar());
		}
	}

	/**
	 *
	 * @return true if the cursor should be shown on output....
	 */
	public boolean isScrollLock() {
		return fScrollLock;
	}

	/**
	 * If set then if the size changes
	 */
	public void setScrollLock(boolean scrollLock) {
		fScrollLock = scrollLock;
	}

	protected void repaintRange(int col, int line, int width, int height) {
		Point origin = cellToOriginOnScreen(col, line);
		Rectangle r = new Rectangle(origin.x, origin.y, width * getCellWidth(), height * getCellHeight());
		repaint(r);
	}

	@Override
	protected void drawLine(GC gc, int line, int x, int y, int colFirst, int colLast) {
		fCellRenderer.drawLine(fCellCanvasModel, gc, line, x, y, colFirst, colLast);
	}

	@Override
	protected Color getTerminalBackgroundColor() {
		return fCellRenderer.getDefaultBackgroundColor();
	}

	@Override
	protected void visibleCellRectangleChanged(int x, int y, int width, int height) {
		fCellCanvasModel.setVisibleRectangle(y, x, height, width);
		update();
	}

	@Override
	protected int getCols() {
		return fCellCanvasModel.getTerminalText().getWidth();
	}

	@Override
	protected int getRows() {
		return fCellCanvasModel.getTerminalText().getHeight();
	}

	public String getSelectionText() {
		// TODO -- create a hasSelectionMethod!
		return fCellCanvasModel.getSelectedText();
	}

	public void copy() {
		String selectionText = getSelectionText();
		if (selectionText != null && selectionText.length() > 0) {
			Clipboard clipboard = new Clipboard(getDisplay());
			clipboard.setContents(new Object[] { selectionText }, new Transfer[] { TextTransfer.getInstance() });
			clipboard.dispose();
		}
	}

	public void selectAll() {
		fCellCanvasModel.setSelection(0, fCellCanvasModel.getTerminalText().getHeight(), 0,
				fCellCanvasModel.getTerminalText().getWidth());
		fCellCanvasModel.setSelectionAnchor(new Point(0, 0));
	}

	/**
	 * @since 4.1
	 */
	public void clearSelection() {
		fCellCanvasModel.setSelection(-1, -1, -1, -1);
	}

	/**
	 * Collect and return all text present in the widget.
	 *
	 * <p>Individual lines of the returned text are separated by '\n'.
	 *
	 * <p>The method is primarily designed for test automation. Tests need
	 * to check what happens in a terminal (e.g. if and how a CDT Debugger
	 * Console reacts in a GDB session) and this method allows to read the
	 * text present in the terminal.
	 *
	 * @since 4.4
	 */
	public String getAllText() {
		return fCellCanvasModel.getAllText();
	}

	public boolean isEmpty() {
		return false;
	}

	/**
	 * Gets notified when the visible size of the terminal changes.
	 * This should update the model!
	 *
	 */
	public interface ResizeListener {
		void sizeChanged(int lines, int columns);
	}

	/**
	 * @param listener this listener gets notified, when the size of
	 * the widget changed. It should change the dimensions of the underlying
	 * terminaldata
	 */
	public void addResizeHandler(ResizeListener listener) {
		if (fResizeListener != null)
			throw new IllegalArgumentException("There can be at most one listener at the moment!"); //$NON-NLS-1$
		fResizeListener = listener;

		// Bug 281328: [terminal] The very first few characters might be missing in
		//             the terminal control if opened and connected programmatically
		//
		// In case the terminal had not been visible yet or is too small (less than one
		// line visible), the terminal should have a minimum size to avoid RuntimeExceptions.
		Rectangle bonds = getClientArea();
		if (bonds.height < getCellHeight() || bonds.width < getCellWidth()) {
			//Widget not realized yet, or minimized to < 1 item:
			//Just tell the listener our min size
			fResizeListener.sizeChanged(getMinLines(), getMinColumns());
		} else {
			//Widget realized: compute actual size and force telling the listener
			onResize(true);
		}
	}

	/**
	 * Notify about a change of the global Font Preference.
	 * @deprecated Use {@link #updateFont(String)}
	 */
	@Deprecated
	public void onFontChange() {
		fCellRenderer.onFontChange();
		setCellWidth(fCellRenderer.getCellWidth());
		setCellHeight(fCellRenderer.getCellHeight());
		calculateGrid();
	}

	public void updateFont(String fontName) {
		fCellRenderer.updateFont(fontName);
		setCellWidth(fCellRenderer.getCellWidth());
		setCellHeight(fCellRenderer.getCellHeight());
		calculateGrid();
	}

	public void setInvertedColors(boolean invert) {
		fCellRenderer.setInvertedColors(invert);
		redraw();
	}

	/**
	 * @return true if the cursor is enabled (blinking). By default the cursor is not enabled.
	 */
	public boolean isCursorEnabled() {
		return fCursorEnabled;
	}

	/**
	 * @param enabled enabling means that the cursor blinks
	 */
	public void setCursorEnabled(boolean enabled) {
		if (enabled != fCursorEnabled) {
			fCursorEnabled = enabled;
			fCellCanvasModel.setCursorEnabled(fCursorEnabled);
		}

	}

	public void addTerminalMouseListener(final ITerminalMouseListener listener) {
		fMouseListeners.add(listener);
	}

	public void removeTerminalMouseListener(ITerminalMouseListener listener) {
		fMouseListeners.remove(listener);
	}
}
