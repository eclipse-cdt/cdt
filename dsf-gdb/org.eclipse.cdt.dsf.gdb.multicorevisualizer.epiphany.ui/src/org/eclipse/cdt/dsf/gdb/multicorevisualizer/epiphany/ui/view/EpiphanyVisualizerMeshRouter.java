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

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.EpiphanyConstants;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class EpiphanyVisualizerMeshRouter extends EpiphanyVisualizerContainer 
{

	// --- members ---
	
	protected Integer m_load = null;
	

	// --- constructors/destructors ---
	
	/** Constructor */
	public EpiphanyVisualizerMeshRouter(int cpuId)
	{
		setId(cpuId); 
	}
	
	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
	}
	
	
	// --- accessors ---
	
	/** set the router's load */
	public void setLoad(int load) {
		m_load = load;
	}

	
	// --- Object methods ---
	
	/** Returns string representation. */
	@Override
	public String toString() {
		return super.toString() + "eCore id:" + getId() + ", Load: " +( m_load != null ? m_load.toString() : "undefined"  );		
	}
	
	
	// --- methods ---
	
	
	// --- paint methods ---
	
	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		if (!isVisible()) return; 
		
		super.paintContent(gc);
		Rectangle routerRel = new Rectangle(0, 0, 2, 2);
		// absolute bounds of router
		Rectangle routerAbs = relativeToAbsoluteBounds(routerRel);
		
		gc.setForeground(EpiphanyConstants.EV_COLOR_FOREGROUND);
		
		// set BG color according to load
		if (m_load < 25) {
			gc.setBackground(EpiphanyConstants.EV_COLOR_LOW_LOAD);
		}
		else if (m_load < 75) {
			gc.setBackground(EpiphanyConstants.EV_COLOR_MED_LOAD);
		}
		else { 
			gc.setBackground(EpiphanyConstants.EV_COLOR_HIGH_LOAD);
		}
		
		// Draw mesh router
		gc.fillOval(routerAbs.x + routerAbs.width/4, routerAbs.y + routerAbs.width/4, routerAbs.width/2, routerAbs.height/2);
		gc.drawOval(routerAbs.x + routerAbs.width/4, routerAbs.y + routerAbs.width/4, routerAbs.width/2, routerAbs.height/2);
		
		// Draw line to join router to its eCore
		gc.drawLine(routerAbs.x, routerAbs.y, routerAbs.x + routerAbs.width/4, routerAbs.y + routerAbs.height/4);
	}
	

	/** Returns true if object has decorations to paint. */
	@Override
	public boolean hasDecorations() {
		return false;
	}
	
	/** Invoked to allow element to paint decorations on top of anything drawn on it */
	@Override
	public void paintDecorations(GC gc) {

	}

}
