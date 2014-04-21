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

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.IVisualizerModelObject;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerLoadInfo;


/** Represents single Epiphany mesh router node. */
@SuppressWarnings("restriction")
public class EpiphanyModelMeshRouter implements Comparable<EpiphanyModelMeshRouter>, IVisualizerModelObject
{	
	public enum LinkDirection {
		LINK_NORTH_OUT,
		LINK_NORTH_IN,
		LINK_EAST_OUT,
		LINK_EAST_IN,
		LINK_SOUTH_OUT,
		LINK_SOUTH_IN,
		LINK_WEST_OUT,
		LINK_WEST_IN
	}
	
	/** 
	 * Represent one Epiphany mesh network link. 
	 * Attributes: link direction, link connected state
	 * and link load information 
	 */
	private class EpiphanyLink {
//		protected LinkDirection m_direction = null;
		protected Boolean m_linkConnected = null;
		protected VisualizerLoadInfo m_loadInfo = null;
		
		public EpiphanyLink(/*LinkDirection direction,*/ boolean connected) {
//			m_direction = direction;
			m_linkConnected = connected;
			// default starting load of zero
			m_loadInfo = new VisualizerLoadInfo(0);
		}
		
		/** Set VisualizerLoadInfo object associated to this emesh link */
		public void setLoad(VisualizerLoadInfo load) {
			m_loadInfo = load;
		}
		
		/** Get the numerical value of the load for this emesh link */
		public int getLoad() {
			return m_loadInfo.getLoad();
		}
		
		/** Set the enesh link connected state */
		public void setLinkConnected(boolean con) {
			m_linkConnected = con;
		}
		
		/** Get the enesh link connected state */
		public Boolean getLinkConnected() {
			return m_linkConnected;
		}
		
		/** Get the emesh link direction */
//		public LinkDirection getLinkdirection() {
//			return m_direction;
//		}
		
	}
	
	// --- members ---
	
	/** ID of this core - same id as it's eCore?  */
	protected int m_id;
	
	/** parent object */
	protected IVisualizerModelObject m_parent;
	
	/**  */
	protected ArrayList<EpiphanyLink> m_links;
	
	/**  */
	protected Hashtable<LinkDirection, EpiphanyLink> m_linkMap; 

	
	// --- constructors/destructors ---
	
	/** Constructor */
	public EpiphanyModelMeshRouter(IVisualizerModelObject parent, int id) {
		m_id = id;
		m_parent = parent;
		m_links = new ArrayList<EpiphanyLink>();
		m_linkMap = new Hashtable<LinkDirection, EpiphanyLink>();
		
		// create default links
		for (LinkDirection direction : LinkDirection.values()) {
			// by default we assume all links are connected
			EpiphanyLink link = new EpiphanyLink(/*direction,*/ true);
			m_links.add(link);
			m_linkMap.put(direction, link);
		}
	}
	
	/** Dispose method */
	public void dispose() {
		if (m_linkMap != null) {
			m_linkMap.clear();
			m_linkMap = null;
		}
		if (m_links != null) {
			m_links.clear();
			m_links = null;
		}
	}
	
	
	// --- Object methods ---
	
	/** Returns string representation. */
	@Override
	public String toString() {
		// print link info?
		return "Mesh router for node:" + m_id; //$NON-NLS-1$
	}
	
	
	// --- accessors ---	
	
	@Override
	public int getID() {
		return m_id;
	}

	@Override
	public IVisualizerModelObject getParent() {
		return m_parent;
	}
	
	
	/** 
	 * There is no separate load for the router - rather we provide
	 * a metric based on the load of the router's links. 
	 * Options:
	 *  Average load of all links? (* this one is implemented now)
	 *  Load of the worst link? (maybe would make more sense)
	 *  Something else? 
	 */
	public Integer getLoad() {
		Integer total = 0;
		int numLoads = m_links.size();
		
		if (numLoads == 0) {
			return null;
		}
		
		for(EpiphanyLink link : m_links) {
			total += link.getLoad();
		}
		return total / numLoads;
	}
	
	/** 
	 * Set the loads for the individual mesh links for this node
	 * The link loads should be passed in the following order:
	 * North-in, North-out, East-in, East-out, South-in, South-out, West-in, West-out
	 */
	public void setLinksLoads(VisualizerLoadInfo[] loads) {
		for (int i = 0; i < loads.length; i++) {
			m_links.get(i).setLoad(loads[i]);
		}
	}
	
	/** 
	 * Returns an array of loads, one for each link of this mesh router
	 * The links loads are in this order:
	 * North-in, North-out, East-in, East-out, South-in, South-out, West-in, West-out
	 */
	public Integer[] getLinksLoad() {
		Integer[] loads = new Integer[8];
		for(int i = 0; i < m_links.size(); i++) {
			loads[i] = m_links.get(i).getLoad();
		}
		
		return loads;
	}
	
	/**  */
	public void setLinkConnected(LinkDirection dir, boolean conn) {
		m_linkMap.get(dir).setLinkConnected(conn);
	}
	
	/**  */
	public boolean getLinkConnected(LinkDirection dir) {
		return m_linkMap.get(dir).getLinkConnected();
	}
	
	/** Sets the load of a single link */
	public void setLinkLoad(LinkDirection dir, VisualizerLoadInfo load) {
		m_linkMap.get(dir).setLoad(load);
	}
	
	/** Gets the load of a single link */
	public Integer getLinkLoad(LinkDirection direction) {
		return m_linkMap.get(direction).getLoad();
	}
	

	// --- Comparable implementation ---

	@Override
	public int compareTo(EpiphanyModelMeshRouter o) {
		int result = 0;
		if (o != null) {
			if (m_id < o.m_id) {
				result = -1;
			}
			else if (m_id > o.m_id) {
				result = 1;
			}
		}
		return result;
	}
	
	@Override
	public int compareTo(IVisualizerModelObject o) {
		if (o != null) {
			if (o.getClass() == this.getClass()) {
				return compareTo((EpiphanyModelMeshRouter)o);
			}
		}
		return 1;
	}

}
