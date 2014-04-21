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
	public EpiphanyVisualizerMeshRouter(int cpuId)	{
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
		
		// Adjust Mesh router's color according to current load
		if (m_load < 25) {
			setBackground(EpiphanyConstants.EV_COLOR_LOW_LOAD_BG);
			setForeground(EpiphanyConstants.EV_COLOR_LOW_LOAD_FG);
		}
		else if (m_load < 60) {
			setBackground(EpiphanyConstants.EV_COLOR_MED_LOAD_BG);
			setForeground(EpiphanyConstants.EV_COLOR_MED_LOAD_FG);
		}
		else { 
			setBackground(EpiphanyConstants.EV_COLOR_HIGH_LOAD_BG);
			setForeground(EpiphanyConstants.EV_COLOR_HIGH_LOAD_FG);
		}
	}

	
	// --- Object methods ---
	
	/** Returns string representation. */
	@Override
	public String toString() {
		return super.toString() + "eCore id:" + getId() + ", Load: " +( m_load != null ? m_load.toString() : "undefined"  );		
	}
	
	
	// --- methods ---
	
	
	// --- paint methods ---
	
	/** Draw the eMesh router */
	@Override
	public void paintContent(GC gc) {
		// virtual bounds of router
		Rectangle routerVirt = new Rectangle(0, 0, 2, 2);
		// "real" bounds of router
		Rectangle routerReal = virtualToRealBounds(routerVirt);
		
		// Draw mesh router
		gc.fillOval(routerReal.x + routerReal.width/4, routerReal.y + routerReal.width/4, routerReal.width/2, routerReal.height/2);
		gc.drawOval(routerReal.x + routerReal.width/4, routerReal.y + routerReal.width/4, routerReal.width/2, routerReal.height/2);
		
		// Draw line to join router to its eCore
		gc.drawLine(routerReal.x, routerReal.y, routerReal.x + routerReal.width/4, routerReal.y + routerReal.height/4);
	}

}
