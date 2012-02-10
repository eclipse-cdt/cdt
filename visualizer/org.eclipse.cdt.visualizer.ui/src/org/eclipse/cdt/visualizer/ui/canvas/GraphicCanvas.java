/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.canvas;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;


// ---------------------------------------------------------------------------
// GraphicCanvas
// ---------------------------------------------------------------------------

/**
 * Viewer canvas -- base class for canvas that displays a collection
 * of persistent, repositionable graphic objects.
 * 
 * Note: painting is done in order objects were added,
 * so objects added last are drawn "on top" of others.
 * Use raise/lower methods to change the object z-ordering, if needed.
 */
public class GraphicCanvas extends BufferedCanvas
{
	// --- members ---
	
	/** Viewer elements. */
	protected ArrayList<IGraphicObject> m_objects = null;
	
	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public GraphicCanvas(Composite parent) {
		super(parent);
		m_objects = new ArrayList<IGraphicObject>();
	}
	
	/** Dispose method. */
	public void dispose() {
		if (m_objects != null) {
			m_objects.clear();
			m_objects = null;
		}
		super.dispose();
	}
	
	
	// --- object management methods ---

	/** Removes all elements */
	public void clear() {
		m_objects.clear();
	}
	
	/** Adds an element */
	public IGraphicObject add(IGraphicObject element) {
		if (! m_objects.contains(element)) {
			m_objects.add(element);
		}
		return element;
	}
	
	/** Removes an element */
	public void remove(IGraphicObject element) {
		m_objects.remove(element);
	}
	
	/** Raises an element to top of repaint z-ordering */
	public void raiseToFront(IGraphicObject element) {
		if (m_objects.contains(element)) {
			m_objects.remove(element);
			m_objects.add(element);
		}
	}
	
	/** Lowers an element to bottom of repaint z-ordering */
	public void lowerToBack(IGraphicObject element) {
		if (m_objects.contains(element)) {
			m_objects.remove(element);
			m_objects.add(0, element);
		}
	}

	
	// --- painting methods ---
	
	/** Paints elements on canvas. */
	public void paintCanvas(GC gc) {
		// paint background first
		clearCanvas(gc);
		
		// we paint object list from start to end,
		// so end of the list is "top" in z-ordering

		// allow objects to draw themselves
		for (IGraphicObject gobj : m_objects) {
			gobj.paint(gc, false);
		}

		// allow objects to paint any "decorations" on top of other stuff
		for (IGraphicObject gobj : m_objects) {
			if (gobj.hasDecorations())
				gobj.paint(gc, true);
		}
	}
	
	
	// --- point-to-object accessors ---
	
	/** Returns first graphic object found under specified point */
	public IGraphicObject getGraphicObject(int x, int y) {
		return getGraphicObject(null, x, y);
	}

	/** Returns first graphic object found under specified point.
	 *  If type argument is non-null, returns first object assignable to specified type.
	 */
	public IGraphicObject getGraphicObject(Class<?> type, int x, int y) {
		IGraphicObject result = null;
		
		// note: have to search list in reverse order we draw it,
		// so we hit items "on top" in the z-ordering first
		int count = (m_objects == null) ? 0 : m_objects.size();
		for (int i=count-1; i>=0; i--) {
			IGraphicObject gobj = m_objects.get(i);
			if (gobj.contains(x, y)) {
				if (type != null) {
					Class<?> objType = gobj.getClass();
					if (! type.isAssignableFrom(objType)) continue;
				}
				result = gobj;
				break;
			}
		}
		
		return result;
	}
	
	// --- model data accessors ---
	
	/** Returns graphic object (if any) that has specified data value. */
	public IGraphicObject getGraphicObjectFor(Object value) {
		IGraphicObject result = null;
		for (IGraphicObject gobj : m_objects) {
			if (gobj.getData() == value) {
				result = gobj;
				break;
			}
		}
		return result;
	}
	
	/** Returns data value (if any) for the specified graphic element. */
	public Object getDataFor(IGraphicObject IGraphicObject) {
		return (IGraphicObject == null) ? null : IGraphicObject.getData();
	}
}
