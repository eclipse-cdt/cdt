/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.visualizer.ui.canvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Graphical object that can hold children objects. Each object is sized
 * and positioned in virtual units, and positioned relative to the parent object's
 * position.
 */
public class GraphicObjectContainer extends GraphicObject {

	// --- members ---

	/**
	 * Holds the virtual position and size of graphical object.
	 * Position is relative to parent object
	 */
	protected Rectangle m_virtualBounds = new Rectangle(0,0,0,0);

	/** List of children objects contained in this one */
	protected ArrayList<GraphicObjectContainer> m_childrenObjects = new ArrayList<GraphicObjectContainer>();

	/** Map of contained objects and their identifying labels. for quick look-up */
	protected HashMap<String, GraphicObjectContainer> m_childrenObjectsMap = new HashMap<String, GraphicObjectContainer>();

	/** Whether the container's boundaries should be drawn */
	protected boolean m_drawContainerBounds = true;

	/** Is the object selectable? */
	protected boolean m_selectable = false;

	/** Color to use when this object is Selected */
	protected Color m_selectedColor = null;

	/** Value for the margin in pixels */
	protected int m_marginPixels = 0;

	/** Default for the margin in pixels */
	protected static final int MARGIN_PIXELS_DEFAULT = 1;


	// --- constructors/destructors ---

	/** Constructor */
	public GraphicObjectContainer() {
		// default is not selectable
		this(false, MARGIN_PIXELS_DEFAULT);
	}

	/** Alternate constructor */
	public GraphicObjectContainer(boolean selectable, int margin) {
		m_selectable = selectable;
		setDrawContainerBounds(false);
		m_marginPixels = margin;
	}

	/** Dispose method - recursively dispose this object and children objects */
	@Override
	public void dispose() {
		super.dispose();
		if (m_childrenObjects != null) {
			for (GraphicObjectContainer o : m_childrenObjects) {
				o.dispose();
			}
			m_childrenObjects.clear();
			m_childrenObjects = null;
		}
		if (m_childrenObjectsMap != null) {
			m_childrenObjectsMap.clear();
			m_childrenObjectsMap = null;
		}
	}


	// --- Object methods ---

	/** Returns string representation. */
	@Override
	public String toString() {
		return String.format("Real bounds: %s Virtual bounds %s: Draw container: %s ",
			this.getClass().getSimpleName(),
			this.getBounds().toString(),
			m_virtualBounds.toString(),
			m_drawContainerBounds
		);
	}


	// --- accessors ---

	/**
	 * Sets if the container should be drawed / filled-in. Can be used
	 * to draw children objects without displaying their parent container
	 */
	public void setDrawContainerBounds(boolean draw) {
		m_drawContainerBounds = draw;
	}

	/** Marks this object as being selectable or not */
	public void setSelectable(boolean sel) {
		m_selectable = sel;
	}

	/** Returns whether the object is selectable */
	public boolean isSelectable() {
		return m_selectable;
	}

	/** Set the color used to highlight a selection */
	public void setSelectedColor(Color color) {
		m_selectedColor = color;
	}

	/** Get the color used to highlight a selection */
	public Color getSelectedColor() {
		return m_selectedColor;
	}

	// --- methods ---


	@Override
	// so that the overridden version of setBounds(int,int,int,int) will be called,
	// instead of the version in the base class
	public void setBounds(Rectangle bounds) {
		setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
	}


	/**
	 * Sets the absolute bounds (in pixels) of this container object. If it has
	 * children objects, recursively set their absolute bounds.
	 */
	@Override
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		for(GraphicObjectContainer o : m_childrenObjects) {
			// compute bounds of contained object considering its virtual size/position
			o.setBounds(virtualToRealBounds(o.getVirtualBounds()));
		}
	}

	/**
	 * Set bounds of current object relative to its container.
	 * The x and y coordinate are relative to the parent container.
	 * The unit is arbitrary integer but have to be consistent between
	 * parent and children.
	 */
	public void setVirtualBounds(int[] bounds) {
		m_virtualBounds.x = bounds[0];
		m_virtualBounds.y = bounds[1];
		m_virtualBounds.width = bounds[2];
		m_virtualBounds.height = bounds[3];
	}

	/**
	 * Set bounds of current object relative to its container.
	 * The x and y coordinate are relative to the parent container.
	 * The unit is arbitrary integer but have to be consistent between
	 * parent and children.
	 */
	public void setVirtualBounds(Rectangle bounds) {
		m_virtualBounds = bounds;
	}

	/** Get the relative bounds of current object, relative to its parent container */
	public Rectangle getVirtualBounds() {
		return m_virtualBounds;
	}

	/**
	 * Compute absolute (canvas) bounds of passed child object, considering its virtual
	 * bounds compared to its parent's (current object)
	 */
	public Rectangle virtualToRealBounds(Rectangle childsVirtualBounds) {
		float ox = 0.0f;
		float oy = 0.0f;
		float ow = 0.0f;
		float oh = 0.0f;

		ox = (float) this.getBounds().x + childsVirtualBounds.x * ((float) this.getBounds().width / (this.getVirtualBounds().width));
		oy = (float) this.getBounds().y + childsVirtualBounds.y * ((float) this.getBounds().height / this.getVirtualBounds().height);
		ow = ((float) childsVirtualBounds.width / this.getVirtualBounds().width) * this.getBounds().width;
		oh = ((float) childsVirtualBounds.height / this.getVirtualBounds().height) * this.getBounds().height;

		// add margin
		ox += m_marginPixels;
		oy += m_marginPixels;
		ow -= 2 * m_marginPixels;
		oh -= 2 * m_marginPixels;

		return new Rectangle(Math.round(ox), Math.round(oy), Math.round(ow), Math.round(oh));
	}


	/** Add children graphical object in this container. Provided label can be used to later retrieve object */
	public GraphicObjectContainer addChildObject(String label, GraphicObjectContainer obj) {
		m_childrenObjects.add(obj);
		m_childrenObjectsMap.put(label, obj);
		return obj;
	}

	/** Returns a list of child objects of a given derived class, optionally recursing through child objects */
	public ArrayList<GraphicObjectContainer> getChildObjects(Class<?> type, boolean recurse) {
		ArrayList<GraphicObjectContainer> objs = new ArrayList<GraphicObjectContainer>();

		for (GraphicObjectContainer o : this.getAllObjects(recurse)) {
			if(type.isInstance(o) ) {
				objs.add(o);
			}
		}
		return objs;
	}

	/** Recursively search contained objects for one matching a label */
	public GraphicObjectContainer getObject(String key) {
		if (m_childrenObjectsMap.containsKey(key)) {
			return m_childrenObjectsMap.get(key);
		}
		else {
			for(GraphicObjectContainer o : m_childrenObjects) {
				if (o.getObject(key) != null) {
					return o.getObject(key);
				}
			}
		}
		return null;
	}

	/** Get all objects from this container. Optionally recurse to all sub-objects */
	public ArrayList<GraphicObjectContainer> getAllObjects(boolean recurse) {
		ArrayList<GraphicObjectContainer> list = new ArrayList<GraphicObjectContainer>();
		//list.add(this);
		for (GraphicObjectContainer o : m_childrenObjects) {
			list.add(o);
			if (recurse) {
				list.addAll(o.getAllObjects(recurse));
			}
		}
		return list;
	}

	/** returns a list of selectable objects */
	public List<GraphicObjectContainer> getSelectableObjects() {
		List<GraphicObjectContainer> list = new ArrayList<GraphicObjectContainer>();
		for (GraphicObjectContainer o : m_childrenObjects) {
			if (o.isSelectable()) {
				list.add(o);
			}
			list.addAll(o.getSelectableObjects());
		}
		return list;
	}


	// --- paint methods ---

	/**
	 * Invoked to allow element to paint itself on the viewer canvas
	 * Also paints any children objects(s)
	 */
	@Override
	public void paint(GC gc, boolean decorations) {
		// Set GC to reflect object properties, if set.
		Color oldForeground = null;
		Color oldBackground = null;
		if (m_foreground != null) {
			oldForeground = gc.getForeground();
			gc.setForeground(m_foreground);
		}
		if (m_background != null) {
			oldBackground = gc.getBackground();
			gc.setBackground(m_background);
		}

		if (!decorations) {
			// Paint the object.
			if (isVisible()) {
				paintContent(gc);
			}
		}
		else {
			// Paint decorations
			if (isVisible() && hasDecorations()) {
				paintDecorations(gc);
			}
		}

		// recursively paint children objects
		if (m_childrenObjects != null) {
			for (GraphicObjectContainer o : m_childrenObjects) {
				o.paint(gc, decorations);
			}
		}

		// Restore old state.
		if (m_foreground != null) gc.setForeground(oldForeground);
		if (m_background != null) gc.setBackground(oldBackground);
	}

	/**
	 * Invoked to allow element to paint itself on the viewer canvas.
	 */
	@Override
	public void paintContent(GC gc) {
		if (m_drawContainerBounds) {
			if (isSelected()) {
				gc.setForeground(m_selectedColor);
			}
			else {
				gc.setForeground(m_foreground);
			}
			gc.setBackground(m_background);
			gc.fillRectangle(m_bounds);
			super.paintContent(gc);
		}
	}

	/** Draws a grid over a GraphicObjectContainer object, using virtual units */
	public void drawGrid(GC gc) {
		Rectangle realBounds;

		// draw vertical lines
		for (int x = m_virtualBounds.x; x <= m_virtualBounds.width - m_virtualBounds.x; x++) {
			realBounds = virtualToRealBounds(new Rectangle(x,m_virtualBounds.y,m_virtualBounds.width,m_virtualBounds.height));
			gc.drawLine(realBounds.x, realBounds.y, realBounds.x, realBounds.y + realBounds.height);
		}

		// draw horizontal lines
		for (int y = m_virtualBounds.y; y <= m_virtualBounds.height - m_virtualBounds.y; y++) {
			realBounds = virtualToRealBounds(new Rectangle(m_virtualBounds.y,y,m_virtualBounds.width,m_virtualBounds.height));
			gc.drawLine(realBounds.x, realBounds.y, realBounds.x + realBounds.width, realBounds.y);
		}
	}

}
