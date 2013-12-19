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

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelMeshRouter.LinkDirection;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.IEpiphanyConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * Contains an Epiphany "CPU": one eCore, a mesh router and 8 mesh links
 */
public class EpiphanyVisualizerCPU extends EpiphanyVisualizerContainer
{

	// --- members ---
	
	// eCore within CPU container
	protected EpiphanyVisualizerEcore m_eCore;
		
	/** Mesh router associated to this node */
	protected EpiphanyVisualizerMeshRouter m_router = null;
	
	/** Mesh links associated to this CPU */
	protected ArrayList<EpiphanyVisualizerMeshLink> m_links = null;
	
	/** Map that permits finding the mesh links that goes in a given direction */
	protected Hashtable<LinkDirection, EpiphanyVisualizerMeshLink> m_linkMap = null; 
	
	
	// --- constructors/destructors ---
	
	/** Constructor */
	
	public EpiphanyVisualizerCPU(int id, IEpiphanyConstants eConstants, String progName, Color coreColor, boolean loadEnabled) {
		setId(id);
		setDrawBounds(false);
		
		// relative position and size of the eCore in the node container
		m_eCore = new EpiphanyVisualizerEcore(getId(), eConstants, progName, coreColor, loadEnabled);
		m_eCore.setRelativeBounds(eConstants.getEcoreBounds());

		m_eCore.setSelectable(true);
		addChildObject(m_eCore.toString(), m_eCore);
		
		// create and size mesh router
		m_router = new EpiphanyVisualizerMeshRouter(id);
		m_router.setRelativeBounds(eConstants.getRouterBounds());
		m_router.setSelectable(true);
		m_router.setVisible(loadEnabled);
		addChildObject(m_router.toString(), m_router);
		
		m_links = new ArrayList<EpiphanyVisualizerMeshLink>();
		
		// create and size mesh links in this eCore
		m_linkMap = new Hashtable<LinkDirection, EpiphanyVisualizerMeshLink>(); 
		EpiphanyVisualizerMeshLink l;
		int i = 0;
		
		for (LinkDirection d : LinkDirection.values()) {
			l = new EpiphanyVisualizerMeshLink(id, d, eConstants.getEpiphanyCpuLinksConnected()[id][i]);
			l.setRelativeBounds(eConstants.getMeshLinksBounds()[i]);

			// mesh links are selectable objects
			l.setSelectable(true);
			l.setVisible(loadEnabled);
			setLink(d, l);
			
			i++;
		}
	}
	
	
	/** Dispose method */
	public void dispose() {
		super.dispose();
		if (m_eCore != null) {
			m_eCore.dispose();
			m_eCore = null;
		}
		if (m_router != null) {
			m_router.dispose();
			m_router = null;
		}
		if (m_links != null) {
			for(EpiphanyVisualizerMeshLink l : m_links) {
				l.dispose();
			}
			m_links.clear();
			m_links = null;
		}
		if (m_linkMap != null) {
			m_linkMap.clear();
			m_linkMap = null;
		}
	}
	

	// --- Object methods ---
	
	/** Returns string representation. */
	@Override
	public String toString() {
		return super.toString() + ", eCore id: " + getId();		
	}

	// --- accessors ---
	
	
	/** Returns the Mesh Link object for a given direction  */
	public EpiphanyVisualizerMeshLink getLink(LinkDirection dir) {
		return m_linkMap.get(dir);
	}
	
	/**  */
	public void setLink(LinkDirection dir, EpiphanyVisualizerMeshLink link) {
		m_linkMap.put(dir, link);
		m_links.add(link);
		addChildObject(link.toString(), link);
	}
	
	/**
	 *  The links loads are in this order:
	 *  North-in, North-out, East-in, East-out, South-in, South-out, West-in, West-out  
	 */
	public void setLinksLoad(Integer[] loads) {
		for(int i = 0; i < loads.length; i++) {
			m_links.get(i).setLoad(loads[i]);
		}
	}
	
	/** Sets the load associated to router */
	public void setRouterLoad(Integer load) {
		m_router.setLoad(load);
	}
	
	/** When an Epiphany CPU is selected, reflect that by selecting its eCore */
	@Override
	public void setSelected(boolean sel) {
		if (m_eCore != null) {
			m_eCore.setSelected(sel);
		}
	}
	@Override 
	public boolean isSelected() {
		if (m_eCore != null) {
			return m_eCore.isSelected();
		}
		return false;
	}
	
	/**
	 *  The links loads are in this order:
	 *  North-in, North-out, East-in, East-out, South-in, South-out, West-in, West-out  
	 */
	public Integer[] getLinksLoad() {
		Integer[] loads = new Integer[8];
		
		for(int i = 0; i < m_links.size(); i++) {
			loads[i] = m_links.get(i).m_load;
		}
		return loads;
	}
	
	
	/** Sets the load associated to the current CPU */
	public void setCPULoad(int load) {
		m_eCore.getLoadIndicator().setLoad(load);
	}
	
	
	// --- methods ---
	
	// --- paint methods ---
	
	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		super.paintContent(gc);
	}
	
	/** Returns true if object has decorations to paint. */
	@Override
	public boolean hasDecorations() {
		return super.hasDecorations();
	}
	
	/** Invoked to allow element to paint decorations on top of anything drawn on it */
	@Override
	public void paintDecorations(GC gc) {
		super.paintDecorations(gc);
	}
	
}
