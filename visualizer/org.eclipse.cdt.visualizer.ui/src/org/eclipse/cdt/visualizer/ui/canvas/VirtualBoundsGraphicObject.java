/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * Graphic object that can be used as a container for child objects. Each object
 * is sized and positioned in virtual units, and positioned relative to the parent
 * object's position. Setting the real (pixel) bounds of an object recursively sets
 * the bounds of any contained child objects, taking into account its virtual
 * bounds, compared to its parent.
 * @since 1.1
 */
public class VirtualBoundsGraphicObject extends GraphicObject {

	// --- members ---

	/**
	 * Holds the virtual position and size of graphical object.
	 * Position is relative to parent object
	 */
	protected Rectangle m_virtualBounds = new Rectangle(0, 0, 0, 0);

	/** List of children objects contained in this one */
	protected ArrayList<VirtualBoundsGraphicObject> m_childrenObjects = new ArrayList<>();

	/** Map of contained objects and their identifying labels. for quick look-up */
	protected HashMap<String, VirtualBoundsGraphicObject> m_childrenObjectsMap = new HashMap<>();

	/** Whether the container's boundaries should be drawn */
	protected boolean m_drawContainerBounds = true;

	/** Is the object selectable? */
	protected boolean m_selectable = false;

	/** Color to use when this object is Selected */
	protected Color m_selectedColor = null;

	/** Value for the margin in pixels */
	protected int m_childMargin = 0;

	/** Default for the margin in pixels */
	protected static final int MARGIN_PIXELS_DEFAULT = 1;

	// --- constructors/destructors ---

	/** Constructor */
	public VirtualBoundsGraphicObject() {
		// default is not selectable
		this(false, MARGIN_PIXELS_DEFAULT);
	}

	/** Alternate constructor */
	public VirtualBoundsGraphicObject(boolean selectable, int childMargin) {
		m_selectable = selectable;
		setDrawContainerBounds(true);
		m_childMargin = childMargin;
	}

	/** Dispose method - recursively dispose this object and children objects */
	@Override
	public void dispose() {
		super.dispose();
		if (m_childrenObjects != null) {
			for (VirtualBoundsGraphicObject o : m_childrenObjects) {
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
		return String.format("Class: %s, Real bounds: %s, Virtual bounds: %s" + ", Draw container bounds: %s ",
				this.getClass().getSimpleName(), this.getBounds().toString(), m_virtualBounds.toString(),
				m_drawContainerBounds);
	}

	// --- accessors ---

	/**
	 * Sets whether the container's boundary should be drawn / filled-in.
	 * If false, indicates child objects are displayed without showing
	 * the parent container.
	 */
	public void setDrawContainerBounds(boolean draw) {
		m_drawContainerBounds = draw;
	}

	/** Sets whether the object is selectable */
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

	/** Set the margin to be inserted between a parent and a child object,
	 * in pixels */
	public void setChildMargin(int margin) {
		m_childMargin = margin;
	}

	// --- methods ---

	/**
	 * Sets the absolute bounds (in pixels) of this container object. If it has
	 * children objects, recursively set their absolute bounds.
	 * Overridden to delegate to setBounds(int,int,int,int) in this class,
	 * rather than the base class.
	 */
	@Override
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
		for (VirtualBoundsGraphicObject o : m_childrenObjects) {
			o.setBounds(virtualToRealBounds(o.getVirtualBounds()));
		}
	}

	/**
	 * Set bounds of current object relative to its container.
	 * The x and y coordinate are relative to the parent container.
	 * The unit is arbitrary integer but have to be consistent between
	 * parent and children. Width and height must be greater than zero.
	 */
	public void setVirtualBounds(int[] bounds) {
		m_virtualBounds.x = bounds[0];
		m_virtualBounds.y = bounds[1];
		m_virtualBounds.width = bounds[2];
		m_virtualBounds.height = bounds[3];
		checkVirtualBounds();
	}

	/**
	 * Set bounds of current object relative to its container.
	 * The x and y coordinate are relative to the parent container.
	 * The unit is arbitrary integer but have to be consistent between
	 * parent and children. Width and height must be greater than zero.
	 */
	public void setVirtualBounds(Rectangle bounds) {
		m_virtualBounds = bounds;
		checkVirtualBounds();
	}

	/**
	 * Set bounds of current object relative to its container.
	 * The x and y coordinate are relative to the parent container.
	 * The unit is arbitrary integer but have to be consistent between
	 * parent and children. Width and height must be greater than zero.
	 */
	public void setVirtualBounds(int x, int y, int width, int height) {
		m_virtualBounds.x = x;
		m_virtualBounds.y = y;
		m_virtualBounds.width = width;
		m_virtualBounds.height = height;
		checkVirtualBounds();
	}

	/** Get the relative bounds of current object, relative to its parent container */
	public Rectangle getVirtualBounds() {
		return m_virtualBounds;
	}

	/** Performs a sanity check of the virtual bounds of this object */
	private void checkVirtualBounds() {
		if (m_virtualBounds.x < 0) {
			throw new IllegalArgumentException("Illegal x: " + m_virtualBounds.x);
		}
		if (m_virtualBounds.y < 0) {
			throw new IllegalArgumentException("Illegal y: " + m_virtualBounds.y);
		}
		if (m_virtualBounds.width <= 0) {
			throw new IllegalArgumentException("Illegal width: " + m_virtualBounds.width);
		}
		if (m_virtualBounds.height <= 0) {
			throw new IllegalArgumentException("Illegal height: " + m_virtualBounds.height);
		}
	}

	/**
	 * Returns the computed absolute (canvas) bounds of passed child object,
	 * considering its virtual bounds compared to its parent's (current object)
	 */
	public Rectangle virtualToRealBounds(Rectangle childsVirtualBounds) {
		float ox = 0.0f;
		float oy = 0.0f;
		float ow = 0.0f;
		float oh = 0.0f;

		ox = this.getBounds().x
				+ childsVirtualBounds.x * ((float) this.getBounds().width / (this.getVirtualBounds().width));
		oy = this.getBounds().y
				+ childsVirtualBounds.y * ((float) this.getBounds().height / this.getVirtualBounds().height);
		ow = ((float) childsVirtualBounds.width / this.getVirtualBounds().width) * this.getBounds().width;
		oh = ((float) childsVirtualBounds.height / this.getVirtualBounds().height) * this.getBounds().height;

		// add margin
		ox += m_childMargin;
		oy += m_childMargin;
		ow -= 2 * m_childMargin;
		oh -= 2 * m_childMargin;

		// make sure computed width and height are positive
		ow = (ow > 0) ? ow : 0.0f;
		oh = (oh > 0) ? oh : 0.0f;
		return new Rectangle(Math.round(ox), Math.round(oy), Math.round(ow), Math.round(oh));
	}

	/** Add children graphical object in this container. Provided label can be used to later retrieve object */
	public VirtualBoundsGraphicObject addChildObject(String label, VirtualBoundsGraphicObject obj) {
		m_childrenObjects.add(obj);
		m_childrenObjectsMap.put(label, obj);
		return obj;
	}

	/** Returns a list of child objects of a given derived class, optionally recursing through child objects */
	public ArrayList<VirtualBoundsGraphicObject> getChildObjects(Class<?> type, boolean recurse) {
		ArrayList<VirtualBoundsGraphicObject> objs = new ArrayList<>();

		for (VirtualBoundsGraphicObject o : this.getAllObjects(recurse)) {
			if (type.isInstance(o)) {
				objs.add(o);
			}
		}
		return objs;
	}

	/** Searches recursively for a child object matching a label.
	 Returns null if object is not found */
	public VirtualBoundsGraphicObject getObject(String label) {
		return getObject(label, true);
	}

	/** Searches for a child object matching a label. Recurse flag
	 controls whether the search is recursive. */
	public VirtualBoundsGraphicObject getObject(String label, boolean recurse) {
		if (m_childrenObjectsMap.containsKey(label)) {
			return m_childrenObjectsMap.get(label);
		} else if (recurse) {
			for (VirtualBoundsGraphicObject o : m_childrenObjects) {
				if (o.getObject(label) != null) {
					return o.getObject(label, true);
				}
			}
		}
		return null;
	}

	/** Gets all objects from this container. Optionally recurse to all sub-objects */
	public ArrayList<VirtualBoundsGraphicObject> getAllObjects(boolean recurse) {
		ArrayList<VirtualBoundsGraphicObject> list = new ArrayList<>();
		for (VirtualBoundsGraphicObject o : m_childrenObjects) {
			list.add(o);
			if (recurse) {
				list.addAll(o.getAllObjects(recurse));
			}
		}
		return list;
	}

	/** Returns a list of selectable objects */
	public List<VirtualBoundsGraphicObject> getSelectableObjects() {
		List<VirtualBoundsGraphicObject> list = new ArrayList<>();
		for (VirtualBoundsGraphicObject o : m_childrenObjects) {
			if (o.isSelectable()) {
				list.add(o);
			}
			list.addAll(o.getSelectableObjects());
		}
		return list;
	}

	/**
	 * Returns whether an immediate child of current object reports
	 * having decorations to display
	 */
	public boolean hasChildrenWithDecorations() {
		for (VirtualBoundsGraphicObject o : getAllObjects(false)) {
			if (o.hasDecorations()) {
				return true;
			}
		}
		return false;
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
		} else {
			// Paint decorations
			if (isVisible() && hasDecorations()) {
				paintDecorations(gc);
			}
		}

		// recursively paint children objects
		if (m_childrenObjects != null) {
			for (VirtualBoundsGraphicObject o : m_childrenObjects) {
				o.paint(gc, decorations);
			}
		}

		// Restore old state.
		if (m_foreground != null && oldForeground != null)
			gc.setForeground(oldForeground);
		if (m_background != null && oldBackground != null)
			gc.setBackground(oldBackground);
	}

	/**
	 * Invoked to allow element to paint itself on the viewer canvas.
	 */
	@Override
	public void paintContent(GC gc) {
		if (m_drawContainerBounds) {
			if (isSelected() && m_selectedColor != null) {
				gc.setForeground(m_selectedColor);
			} else {
				gc.setForeground(m_foreground);
			}
			gc.setBackground(m_background);
			gc.fillRectangle(m_bounds);
			super.paintContent(gc);
		}
	}

	/**
	 * Recursively checks if children objects have decorations to draw.
	 * If overridden in a derived type, this behavior should be preserved
	 * to ensure that children's decorations are drawn.
	 */
	@Override
	public boolean hasDecorations() {
		return hasChildrenWithDecorations();
	}
}
