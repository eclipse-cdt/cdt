/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.view;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.EpiphanyConstants;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.MulticoreVisualizerGraphicObject;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicObject;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

@SuppressWarnings("restriction")
public class EpiphanyVisualizerContainer extends MulticoreVisualizerGraphicObject 
{

	// --- members ---
	
	/** Object Id */
	protected Integer m_id = null;
	
	/** holds the relative position of graphical object, from it's parent container */
	protected GraphicObject m_relativeObj; 

	/** List of children objects contained in this one */
	protected ArrayList<EpiphanyVisualizerContainer> m_containedObjects = null;
	
	/** Map of contained objects and their identifying labels */
	protected Hashtable<String, EpiphanyVisualizerContainer> m_containedObjectsMap = null;
	
	/** Whether the boundaries of this container object should be drawn */
	protected boolean m_drawContainerBorder = true;
	
	/** Is the object selectable? */
	protected boolean m_selectable = false;
	
	/** Value for the margin in pixels */
	protected int m_marginPixels = 0;
	
	/** Default for the margin in pixels */
	protected static final int MARGIN_PIXELS_DEFAULT = 1;
		

	// --- constructors/destructors ---

	/** Constructor */
	public EpiphanyVisualizerContainer()
	{
		// use defaults for selectable and margin
		this(false, MARGIN_PIXELS_DEFAULT);
	}

	/** Alternate constructor */
	public EpiphanyVisualizerContainer(boolean selectable, int margin) {
		m_selectable = selectable;
		m_relativeObj = new GraphicObject();
		m_containedObjects = new ArrayList<EpiphanyVisualizerContainer>();
		m_containedObjectsMap = new Hashtable<String, EpiphanyVisualizerContainer>();
		setDrawBounds(false);
		
		m_marginPixels = margin;
	}
	
	
	/** Dispose method - recursively dispose this object and children objects */
	@Override
	public void dispose() {
		super.dispose();
		if (m_relativeObj != null) {
			m_relativeObj.dispose();
		}
		if (m_containedObjects != null) {
			for (EpiphanyVisualizerContainer o : m_containedObjects) {
				o.dispose();
			}
			m_containedObjects.clear();
			m_containedObjects = null;
		}
		if (m_containedObjectsMap != null) {
			m_containedObjectsMap.clear();
			m_containedObjectsMap = null;
		}
	}

	// --- Object methods ---
	
	/** Returns string representation. */
	@Override
	public String toString() {
		return String.format("ObjId: %s, %s - Absolute bounds: %s Relative bounds %s: Draw border: %s ", 
				m_id != null ? m_id : "n/a", 
				this.getClass().getSimpleName(),
				this.getBounds().toString(),
				m_relativeObj.getBounds().toString(),
				m_drawContainerBorder
			); 
	}

	
	// --- accessors ---
	
	/** Sets if the container's outer bounds should be drawed / filled-in. Default is "true" */
	protected void setDrawBounds(boolean draw) {
		m_drawContainerBorder = draw;
	}
	
	/** Marks this object as being selectable or not */
	protected void setSelectable(boolean sel) {
		m_selectable = sel;
	}
	
	/** Returns whether the object is selectable */
	protected boolean isSelectable() {
		return m_selectable;
	}
	
	/** Set object id */
	protected void setId(int id) {
		m_id = id;
	}

	/** Get object id */
	protected Integer getId() {
		return m_id;
	}
	
	
	// --- methods ---

	
	@Override
	public void setBounds(Rectangle bounds) {
		// so that the overridden version of setBounds(int,int,int,int) will be called
		setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
	
	/** Set the absolute bounds (in pixels) of this object and contained objects, recursively */
	@Override
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		for(EpiphanyVisualizerContainer o : m_containedObjects) {
			// compute bounds of contained object considering its relative size/position
			o.setBounds(relativeToAbsoluteBounds(o.getRelativeBounds()));
		}
	}
	
	/** 
	 * Set bounds of current object relative to its container. 
	 * Note: must use the same scale as container
	 */
	public void setRelativeBounds(int[] bounds) {
		m_relativeObj.setBounds(bounds[0], bounds[1], bounds[2], bounds[3]);
	}
//	public void setRelativeBounds(Rectangle bounds) {
//		m_relativeObj.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
//	}
	
	/** 
	 * Compute absolute (canvas) bounds of passed child object, considering its relative 
	 * bounds compared to its parent's (current object) 
	 */
	public Rectangle relativeToAbsoluteBounds(Rectangle childsRelBounds) {
		float ox = 0.0f;
		float oy = 0.0f;
		float ow = 0.0f;
		float oh = 0.0f;

		ox = (float) this.getBounds().x + childsRelBounds.x * ((float) this.getBounds().width / (this.getRelativeBounds().width));
		oy = (float) this.getBounds().y + childsRelBounds.y * ((float) this.getBounds().height / this.getRelativeBounds().height);
		ow = ((float) childsRelBounds.width / this.getRelativeBounds().width) * this.getBounds().width;
		oh = ((float) childsRelBounds.height / this.getRelativeBounds().height) * this.getBounds().height;

		// add margin
		ox += m_marginPixels;
		oy += m_marginPixels;
		ow -= 2 * m_marginPixels;
		oh -= 2 * m_marginPixels;

		return new Rectangle(Math.round(ox), Math.round(oy), Math.round(ow), Math.round(oh));
	}

	
	/** Add children graphical object in this container */
	public EpiphanyVisualizerContainer addChildObject(String label, EpiphanyVisualizerContainer obj) {		
		if (obj != null) {
			m_containedObjects.add(obj);
			m_containedObjectsMap.put(label, obj);
		}
		return obj;
	}
	
	/** Returns a list of EV canvas objects of a given class, optionally recursing through child objects */
	public ArrayList<Object> getChildObjects(Class<?> type, boolean recurse) {
		ArrayList<Object> objs = new ArrayList<Object>();
		
		for (Object o : this.getAllObjects(recurse)) {
			if(type.isInstance(o) ) {
				objs.add(o);
			}
		}
		return objs;
	}
	
	/** Recursively search contained objects for one matching String key */
	public EpiphanyVisualizerContainer getObject(String key) {
		if (m_containedObjectsMap.containsKey(key)) {
			return m_containedObjectsMap.get(key);
		}
		else {
			for(EpiphanyVisualizerContainer o : m_containedObjects) {
				if (o.getObject(key) != null) {
					return o.getObject(key);
				}
			}
		}
		return null;
	}
	
	
	
	/** Get all objects from this container. Optionally recurse to all sub-objects */
	public ArrayList<EpiphanyVisualizerContainer> getAllObjects(boolean recurse) {
		ArrayList<EpiphanyVisualizerContainer> list = new ArrayList<EpiphanyVisualizerContainer>();
		for (EpiphanyVisualizerContainer o : m_containedObjects) {
			list.add(o);
			if (recurse) {
				list.addAll(o.getAllObjects(recurse));
			}
		}
		return list;
	}
	
	/** Get the relative bounds of current object, relative to its container */
	public Rectangle getRelativeBounds() {
		return m_relativeObj.getBounds();
	}

	/** returns a list of selectable objects */
	public List<EpiphanyVisualizerContainer> getSelectableObjects() {
		List<EpiphanyVisualizerContainer> list = new ArrayList<EpiphanyVisualizerContainer>();
		for (EpiphanyVisualizerContainer o : m_containedObjects) {
			if (o.isSelectable()) {
				list.add(o);
			}
			list.addAll(o.getSelectableObjects());
		}
		return list;
	}
	
	// --- paint methods ---

	/** 
	 * Invoked to allow element to paint itself on the viewer canvas.
	 * Also recursively triggers the painting of contained objects 
	 */
	@Override
	public void paintContent(GC gc) {
		if (m_drawContainerBorder) {
			if (isSelected()) {
				gc.setBackground(EpiphanyConstants.EV_COLOR_SELECTED);
			}
			else {
				gc.setBackground(this.getBackground() != null ? this.getBackground() : EpiphanyConstants.EV_COLOR_BACKGROUND);
			}
			gc.setForeground(this.getForeground() != null ? this.getForeground() : EpiphanyConstants.EV_COLOR_FOREGROUND );

			gc.fillRectangle(m_bounds);
			gc.drawRectangle(m_bounds);
		}
		
		if (m_containedObjects != null) {
			for (EpiphanyVisualizerContainer o : m_containedObjects) {
				o.paintContent(gc);
			}
		}
	}
	
	/** Draws a grid over an EpiphanyVisualizerContainer object, using relative units - useful for debugging */
	public void drawGrid(GC gc) {
		gc.setForeground(EpiphanyConstants.EV_COLOR_GRID);
		gc.setBackground(EpiphanyConstants.EV_COLOR_GRID);
				
		int relX = m_relativeObj.getX();
		int relY = m_relativeObj.getY();
		int relW = m_relativeObj.getWidth();
		int relH = m_relativeObj.getHeight();

		// vertical lines
		for (int x = relX; x <= relW - relX; x++) {
			Rectangle absBounds = relativeToAbsoluteBounds(new Rectangle(x,relY,relW,relH));
			gc.drawLine(absBounds.x, absBounds.y, absBounds.x, absBounds.y + absBounds.height);
		}
				
		// horizontal lines
		for (int y = relY; y <= relH - relY; y++) {
			Rectangle absBounds = relativeToAbsoluteBounds(new Rectangle(relX,y,relW,relH));
			gc.drawLine(absBounds.x, absBounds.y, absBounds.x + absBounds.width, absBounds.y);
		}
	}

	/** Returns true if object has decorations to paint. */
	@Override
	public boolean hasDecorations() {
		return true;
	}

	/** Invoked to allow element to paint decorations on top of anything drawn on it */
	@Override
	public void paintDecorations(GC gc) {
		if (m_containedObjects != null) {
			for (MulticoreVisualizerGraphicObject o : m_containedObjects) {
				o.paintDecorations(gc);
			}
		}
	}
}
