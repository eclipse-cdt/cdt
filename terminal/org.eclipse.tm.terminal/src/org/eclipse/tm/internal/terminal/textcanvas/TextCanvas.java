/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.textcanvas;


import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A cell oriented Canvas. Maintains a list of "cells".
 * It can either be vertically or horizontally scrolled.
 * The CellRenderer is responsible for painting the cell.
 */
public class TextCanvas extends GridCanvas {
	protected final ITextCanvasModel fCellCanvasModel;
	/** Renders the cells */
	private ILinelRenderer fCellRenderer;
	private boolean fScrollLock;
	private Point fDraggingStart;
	private Point fDraggingEnd;
	/**
	 * Create a new CellCanvas with the given SWT style bits.
	 * (SWT.H_SCROLL and SWT.V_SCROLL are automatically added).
	 */
	public TextCanvas(Composite parent, ITextCanvasModel model, int style) {
		super(parent, style | SWT.H_SCROLL | SWT.V_SCROLL);
		fCellCanvasModel=model;
		fCellCanvasModel.addCellCanvasModelListener(new ITextCanvasModelListener(){
			public void cellSizeChanged() {
				setCellWidth(fCellRenderer.getCellWidth());
				setCellHeight(fCellRenderer.getCellHeight());

				calculateGrid();
				
			}
			public void rangeChanged(int col, int line, int width, int height) {
				repaintRange(col,line,width,height);
			}
			public void dimensionsChanged(int cols, int rows) {
				calculateGrid();
			}
			public void terminalDataChanged() {
				if(isDisposed())
					return;
				scrollToEnd();
			}
		});
		addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				calculateGrid();
			}
		});
		addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
				fCellCanvasModel.setCursorEnabled(true);
			}
			public void focusLost(FocusEvent e) {
				fCellCanvasModel.setCursorEnabled(false);
			}});
		addMouseListener(new MouseListener(){
			public void mouseDoubleClick(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
				if(e.button==1) { // left button
					fDraggingStart=screenPointToCell(e.x, e.y);
					if((e.stateMask&SWT.SHIFT)!=0) {
						Point anchor=fCellCanvasModel.getSelectionAnchor();
						if(anchor!=null)
							fDraggingStart=anchor;
					} else {
						fCellCanvasModel.setSelectionAnchor(fDraggingStart);
					}
					fDraggingEnd=null;
				}
			}
			public void mouseUp(MouseEvent e) {				
				if(e.button==1) { // left button
					setSelection(screenPointToCell(e.x, e.y));
					fDraggingStart=null;
				}
			}
		});
		addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				if (fDraggingStart != null) {
					setSelection(screenPointToCell(e.x, e.y));
				}
			}
		});
	}

	void setSelection(Point p) {
		if (!p.equals(fDraggingEnd)) {
			fDraggingEnd = p;
			if (compare(p, fDraggingStart) < 0) {
				fCellCanvasModel.setSelection(p.y, fDraggingStart.y, p.x, fDraggingStart.x);
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
	public void setCellRenderer(ILinelRenderer cellRenderer) {
		fCellRenderer = cellRenderer;
		setCellWidth(fCellRenderer.getCellWidth());
		setCellHeight(fCellRenderer.getCellHeight());
	}
	public ILinelRenderer getCellRenderer() {
		return fCellRenderer;
	}
	private void calculateGrid() {
		setVirtualExtend(getCols()*getCellWidth(),getRows()*getCellHeight());
		// scroll to end
		scrollToEnd();
		// make sure the scroll area is correct:
		scrollY(getVerticalBar());
		scrollX(getHorizontalBar());

		updateViewRectangle();
		getParent().layout();
		redraw();
	}
	void scrollToEnd() {
		if(!fScrollLock) {
			int y=-(getRows()*getCellHeight()-getClientArea().height);
			Rectangle v=getViewRectangle();
			if(v.y!=y) {
				setVirtualOrigin(0,y);
			}
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
	 * @param scrollLock 
	 */
	public void setScrollLock(boolean scrollLock) {
		fScrollLock=scrollLock;
	}
	protected void repaintRange(int col, int line, int width, int height) {
		Point origin=cellToOriginOnScreen(col,line);
		Rectangle r=new Rectangle(origin.x,origin.y,width*getCellWidth(),height*getCellHeight());
		repaint(r);
	}
	protected void drawLine(GC gc, int line, int x, int y, int colFirst, int colLast) {
		fCellRenderer.drawLine(fCellCanvasModel, gc,line,x,y,colFirst, colLast);
		
	}
	protected void visibleCellRectangleChanged(int x, int y, int width, int height) {
		fCellCanvasModel.setVisibleRectangle(y,x,height,width);
		update();
	}
	protected int getCols() {
		return fCellCanvasModel.getTerminalText().getWidth();
	}
	protected int getRows() {
		return fCellCanvasModel.getTerminalText().getHeight();
	}
	public String getSelectionText() {
		// TODO -- create a hasSelectionMethod!
		return fCellCanvasModel.getSelectedText();
	}
	public void copy() {
		Clipboard clipboard = new Clipboard(getDisplay());
		clipboard.setContents(new Object[] { getSelectionText() }, new Transfer[] { TextTransfer.getInstance() });
		clipboard.dispose();
	}
	public void selectAll() {
		fCellCanvasModel.setSelection(0, fCellCanvasModel.getTerminalText().getHeight(), 0, fCellCanvasModel.getTerminalText().getWidth());
		fCellCanvasModel.setSelectionAnchor(new Point(0,0));
	}
	public boolean isEmpty() {
		return false;
	}
}

