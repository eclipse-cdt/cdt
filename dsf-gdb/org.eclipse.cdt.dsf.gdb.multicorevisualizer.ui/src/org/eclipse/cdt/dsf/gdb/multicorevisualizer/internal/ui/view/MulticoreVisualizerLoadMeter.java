/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (Bug 396268)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @since 1.1
 */
public class MulticoreVisualizerLoadMeter extends MulticoreVisualizerGraphicObject {

	// --- members ---
	
	/** Is this load meter enabled? */
	protected boolean m_enabled = false;
	
	/** The current CPU/core load */
	protected Integer m_currentLoad = null;
	
	/** the high load water-mark */
	protected Integer m_highLoadWatermark = null;
	
	/** second rectangle, that will be displayed to show the load */
	protected Rectangle m_loadRect = null;
	
	/** to display the high load water-mark */
	protected Rectangle m_highWatermarkRect = null;
	
	/** Switch that permits to hide the load meter when not in overload */
	protected Boolean m_showOnlyIfOverload = false;
	
	/** Default overload threshold */
	protected int m_overloadThreshold = 75;
	
	/** Permits to have the load meter use the same BG color as its parent */
	protected Color m_parentBgColor = null;

	
	// --- constructors/destructors ---
	
	/** Constructor */
	public MulticoreVisualizerLoadMeter() {
		
	}
	public MulticoreVisualizerLoadMeter(Integer load) {
		m_currentLoad = (load != null) ? load : 0;
	}
	
	/** Constructor witch includes the high load water-mark */
	public MulticoreVisualizerLoadMeter(Integer load, Integer highWatermark) {
		this(load);
		m_highLoadWatermark = highWatermark;
	}

	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
	}

	
	// --- accessors ---
	
	public void setEnabled (boolean enabled) {
		m_enabled = enabled;
	}
	
	public boolean getEnabled() {
		return m_enabled;
	}
	
	public void setLoad(Integer load) {
		m_currentLoad = load;
	}
	
	public void setHighLoadWatermark(Integer wm) {
		m_highLoadWatermark = wm;
	}
	
	public void setOverloadThreshold (int t) {
		m_overloadThreshold = t;
	}
	
	public void setShowyOnlyIfOverload (Boolean o) {
		m_showOnlyIfOverload = o;
	}
	
	public void setParentBgColor(Color c) {
		m_parentBgColor = c;
	}

	// --- paint methods ---
	
	/** get a color that corresponds to the current load */
	private Color getLoadColor() {		
		if(m_currentLoad < m_overloadThreshold) {
			return IMulticoreVisualizerConstants.COLOR_LOAD_LOADBAR_NORMAL;
		}
		else {
			return IMulticoreVisualizerConstants.COLOR_LOAD_LOADBAR_OVERLOAD;
		}
	}

	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		
		if (!m_enabled || m_currentLoad == null) {
			return;
		}
		
		if (m_currentLoad < m_overloadThreshold && m_showOnlyIfOverload) 
			return;
		
		// Show meter only if there is enough space
		if (m_bounds.height < 30) {
			return;
		}
		
		if (m_parentBgColor == null) {
			// use default bg color
			m_parentBgColor = IMulticoreVisualizerConstants.COLOR_LOAD_UNDERBAR_BG_DEFAULT;
		}

		// Display complete-length load bar
		gc.setForeground(IMulticoreVisualizerConstants.COLOR_LOAD_UNDERBAR_FG);
		gc.setBackground(m_parentBgColor);
		gc.fillRectangle(m_bounds);
		gc.drawRectangle(m_bounds);

		// Create/display shorter bar over to show current load
		int x,y,w,h;
		x = m_bounds.x;
		y = (int) (m_bounds.y + m_bounds.height * ((100.0f - m_currentLoad) / 100.0f));
		w = m_bounds.width;
		h = (int)  (m_bounds.height - m_bounds.height * ((100.0f - m_currentLoad) / 100.0f));

		m_loadRect = new Rectangle(x, y, w, h);
		gc.setBackground(getLoadColor());
		gc.fillRectangle(m_loadRect);
		gc.drawRectangle(m_loadRect);

		// Display high water-mark, if defined
		if ( m_highLoadWatermark != null) {
			x = m_bounds.x - 5;
			y = (int) (m_bounds.y + m_bounds.height * ((100.0f - m_highLoadWatermark) / 100.0f));
			w = m_bounds.width + 7;
			h = 2;

			m_highWatermarkRect = new Rectangle(x, y, w, h);
			gc.setBackground(Colors.BLACK);
			gc.setForeground(Colors.DARK_RED);
			gc.fillRectangle(m_highWatermarkRect);
			gc.drawRectangle(m_highWatermarkRect);
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
		if (m_currentLoad == null)
			return;
		if (m_currentLoad < m_overloadThreshold && m_showOnlyIfOverload) 
			return;
		// Show load text only if there is enough space
		if (m_bounds.height > 50) {
			// Display load in text above the load monitor bar
			gc.setForeground(IMulticoreVisualizerConstants.COLOR_LOAD_TEXT);
			int tx = m_bounds.x;
			int ty = m_bounds.y;
			GUIUtils.drawTextAligned(gc, Integer.toString(m_currentLoad), tx, ty, true, false);
		}
	}

}
