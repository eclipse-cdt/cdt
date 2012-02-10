/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;


/**
 * Monitors mouse down/move/up events on a specified control (e.g. a canvas)
 * and converts them to semantic events (click, double-click, select, drag, etc.)
 */
public class MouseMonitor
{
	// --- constants ---
	
	/** MouseEvent button ID for Left mouse button */
	public static final int LEFT_BUTTON = 1;
	
	/** MouseEvent button ID for Middle mouse button */
	public static final int MIDDLE_BUTTON = 2;
	
	/** MouseEvent button ID for Right mouse button */
	public static final int RIGHT_BUTTON = 3;
	
	
	/** Mouse drag state value */
	public static final int MOUSE_DRAG_BEGIN = 0;
	
	/** Mouse drag state value */
	public static final int MOUSE_DRAG = 1;

	/** Mouse drag state value */
	public static final int MOUSE_DRAG_END = 2;
	
	/** Distance mouse must move for a mouse-down to be treated as a drag */
	public static final int MOUSE_DRAG_HYSTERESIS = 4;


	// --- members ---
	
	/** Control being monitored */
	protected Control m_control = null;
	
	/** Mouse button listener */
	protected MouseListener m_mouseButtonListener = null;
	
	/** Mouse move listener */
	protected MouseMoveListener m_mouseMoveListener = null;
	
	/** Mouse enter/exit event listener */
	protected MouseTrackListener m_mouseTrackListener = null;
	
	/** Whether mouse button is down. */
	protected boolean m_mouseDown = false;
	
	/** Whether mouse is being dragged. */
	protected boolean m_mouseDrag = false;
	
	/** Mouse-down point. */
	protected Point m_mouseDownPoint = new Point(0,0);
	
	/** Last button down. */
	protected int m_mouseDownButton = 0;
	
	/** Current mouse drag point. */
	protected Point m_dragPoint = new Point(0,0);
	
	/** Drag region. */
	protected Rectangle m_dragRegion = new Rectangle(0,0,0,0);
	
	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public MouseMonitor() {
	}
	
	/** Constructor. */
	public MouseMonitor(Control control) {
		this();
		m_control = control;
		attach(m_control);
	}
	
	/** Dispose method. */
	public void dispose() {
		detach(m_control);
	}

	
	// --- init methods ---
	
	/** Attach event listeners to specified control. */
	protected void attach(Control control) {
		detach(m_control);
		control.addMouseListener(
			m_mouseButtonListener = new MouseListener() {
				public void mouseDown(MouseEvent e) {
					mouseDownHandler(e.button, e.x, e.y, e.stateMask);
				}
				public void mouseUp(MouseEvent e) {
					mouseUpHandler(e.button, e.x, e.y, e.stateMask);
				}
				public void mouseDoubleClick(MouseEvent e) {
					mouseDoubleClickHandler(e.button, e.x, e.y, e.stateMask);
				}
			}
		);
		control.addMouseMoveListener(
			m_mouseMoveListener = new MouseMoveListener() {
				public void mouseMove(MouseEvent e) {
					mouseMoveHandler(e.x, e.y, e.stateMask);
				}
			}
		);
		control.addMouseTrackListener(
			m_mouseTrackListener = new MouseTrackListener() {
				public void mouseEnter(MouseEvent e) {
					mouseEnterHandler(e.x, e.y);
				}
				public void mouseExit(MouseEvent e) {
					mouseExitHandler(e.x, e.y);
				}
				public void mouseHover(MouseEvent e) {
					mouseHoverHandler(e.x, e.y);
				}
			}
		);
	}
	
	/** Detach event listeners from specified control. */
	protected void detach(Control control) {
		if (control == null) return;
		if (m_control != null) {
			if (m_mouseButtonListener != null) {
				m_control.removeMouseListener(m_mouseButtonListener);
				m_mouseButtonListener = null;
			}
			if (m_mouseMoveListener != null) {
				m_control.removeMouseMoveListener(m_mouseMoveListener);
				m_mouseMoveListener = null;
			}
			if (m_mouseTrackListener != null) {
				m_control.removeMouseTrackListener(m_mouseTrackListener);
				m_mouseTrackListener = null;
			}
		}
	}
	
	
	// --- accessors ---
	
	/** Gets associated control */
	public Control getControl() {
		return m_control;
	}
	
	/** Sets associated control */
	public void setControl(Control control) {
		detach(m_control);
		m_control = control;
		attach(m_control);
	}
	
	/** Gets mouse down point of current drag, if any */
	public Point getMouseDownPoint() {
		return m_mouseDownPoint;
	}
	
	/** Gets current drag x,y point. */
	public Point getDragPoint() {
		return m_dragPoint;
	}
	
	/** Gets bounds of most recent drag region. */
	public Rectangle getDragRegion() {
		return m_dragRegion;
	}
	
	
	// --- utilities ---
	
	/** Returns true if either Shift key is down in mouse event modifier key mask. */
	public static boolean isShiftDown(int keys) {
		return ((keys & SWT.SHIFT) != 0);
	}
	
	/** Returns true if either Control key is down in mouse event modifier key mask. */
	public static boolean isControlDown(int keys) {
		return ((keys & SWT.CONTROL) != 0);
	}
	
	/** Returns true if either Alt key is down in mouse event modifier key mask. */
	public static boolean isAltDown(int keys) {
		return ((keys & SWT.ALT) != 0);
	}
	
	
	// --- methods ---
	
	/** Internal -- sets drag point */
	protected void setDragPoint(int x, int y) {
		m_dragPoint.x=x;
		m_dragPoint.y=y;
	}
	
	/** Internal -- sets drag region explicitly */
	protected void setDragRegion(int x, int y, int width, int height) {
		m_dragRegion.x=x;
		m_dragRegion.y=y;
		m_dragRegion.width=width;
		m_dragRegion.height=height;
	}
	
	/** Internal -- sets drag region from specified drag start/end points */
	protected void setDragRegionFromPoints(int x1, int y1, int x2, int y2) {
		if (x1 < x2) {
			m_dragRegion.x = x1;
			m_dragRegion.width = x2 - x1;
		}
		else {
			m_dragRegion.x = x2;
			m_dragRegion.width = x1 - x2;
		}
		if (y1 < y2) {
			m_dragRegion.y = y1;
			m_dragRegion.height = y2 - y1;
		}
		else {
			m_dragRegion.y = y2;
			m_dragRegion.height = y1 - y2;
		}
	}
	
	/** Invoked when mouse button is pressed */
	protected void mouseDownHandler(int button, int x, int y, int keys) {
		if (! m_mouseDown) {
			m_mouseDown = true;
			m_mouseDownPoint.x = x;
			m_mouseDownPoint.y = y;
			m_mouseDownButton = button;
			setDragPoint(x,y);
			setDragRegion(x,y,0,0);
		}
		mouseDown(button, x, y, keys);
	}
	
	/** Invoked when mouse is moved */
	protected void mouseMoveHandler(int x, int y, int keys) {
		if (m_mouseDown) {
			if (! m_mouseDrag) {
				// allow a small hysteresis before we start dragging, so clicks with a little movement don't cause drags
				int distance = Math.abs(x - m_mouseDownPoint.x) + Math.abs(y - m_mouseDownPoint.y);
				if (distance > MOUSE_DRAG_HYSTERESIS) {
					m_mouseDrag = true;
					
					// initialize mouse drag
					drag(m_mouseDownButton, m_mouseDownPoint.x, m_mouseDownPoint.y, keys, MOUSE_DRAG_BEGIN);
				}
			}
			if (m_mouseDrag) {
				// update mouse drag
				int dx = x - m_mouseDownPoint.x;
				int dy = y - m_mouseDownPoint.y;
				setDragPoint(x,y);
				setDragRegionFromPoints(m_mouseDownPoint.x, m_mouseDownPoint.y, x, y);
				drag(m_mouseDownButton, dx, dy, keys, MOUSE_DRAG);
			}
		}
		mouseMove(x, y, keys);
	}
	
	/** Invoked when mouse button is released */
	protected void mouseUpHandler(int button, int x, int y, int keys) {
		if (m_mouseDown) {
			if (m_mouseDrag) {
				// finish mouse drag
				int dx = x - m_mouseDownPoint.x;
				int dy = y - m_mouseDownPoint.y;
				setDragPoint(x,y);
				setDragRegionFromPoints(m_mouseDownPoint.x, m_mouseDownPoint.y, x, y);
				drag(m_mouseDownButton, dx, dy, keys, MOUSE_DRAG_END);
				m_mouseDrag = false;
			}
			else {
				if (button == RIGHT_BUTTON) {
					contextMenu(x, y, keys);
				}
				else {
					select(x, y, keys);
				}
			}
			m_mouseDown = false;
		}
		mouseUp(button, x, y, keys);
	}

	/** Invoked when mouse button is double-clicked */
	protected void mouseDoubleClickHandler(int button, int x, int y, int keys) {
		mouseDoubleClick(button, x, y, keys);
	}

	/** Invoked when mouse pointer enters control region */
	protected void mouseEnterHandler(int x, int y) {
		if (! m_mouseDown) {
			mouseEnter(x, y);
		}
	}

	/** Invoked when mouse pointer exits control region */
	protected void mouseExitHandler(int x, int y) {
		if (! m_mouseDown) {
			mouseExit(x, y);
		}
	}
	
	/** Invoked when mouse pointer hovers over control */
	protected void mouseHoverHandler(int x, int y) {
		if (! m_mouseDown) {
			mouseHover(x, y);
		}
	}
	
	
	// --- event handlers ---
	
	// These are intended to be overridden by derived types.
	// A user of this class need only override methods for events
	// that need to be tracked.
	
	/** Invoked when mouse button is pressed */
	public void mouseDown(int button, int x, int y, int keys) {}
	
	/** Invoked when mouse is moved */
	public void mouseMove(int x, int y, int keys) {}
	
	/** Invoked when mouse button is released */
	public void mouseUp(int button, int x, int y, int keys) {}
	
	/** Invoked for a selection click at the specified point. */
	public void select(int x, int y, int keys) {}
	
	/** Invoked for a context menu click at the specified point. */
	public void contextMenu(int x, int y, int keys) {}
	
	/** Invoked when mouse button is double-clicked */
	public void mouseDoubleClick(int button, int x, int y, int keys) {}
	
	/** Invoked when mouse is dragged (moved with mouse button down).
	 *  Drag state indicates stage of drag:
	 *  - MOUSE_DRAG_BEGIN -- dx, dy offset from initial mouse down point (initial mouse down)
	 *  - MOUSE_DRAG       -- dx, dy of intermediate drag offset (initial mouse down, then each mouse move)
	 *  - MOUSE_DRAG_END   -- dx, dy of final drag offset (mouse up)
	 *  The pattern of calls is always: BEGIN, DRAG(+), END.
	 */
	public void drag(int button, int x, int y, int keys, int dragState) {}
	
	/** Invoked when mouse pointer enters control region */
	public void mouseEnter(int x, int y) {}

	/** Invoked when mouse pointer exits control region */
	public void mouseExit(int x, int y) {}
	
	/** Invoked when mouse pointer hovers over control */
	public void mouseHover(int x, int y) {}

}
