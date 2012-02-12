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

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

//----------------------------------------------------------------------------
// VisualizerCPU
//----------------------------------------------------------------------------

/** Represents single CPU. */
public class VisualizerCPU
	implements Comparable<VisualizerCPU>
{
	// --- members ---
	
	/** ID of this core. */
	public int m_id;
	
	/** List of cores */
	protected ArrayList<VisualizerCore> m_cores;
	
	/** Lookup table for cores. */
	protected Hashtable<Integer, VisualizerCore> m_coreMap;
	
	
	// --- constructors/destructors ---
	
	/** Constructor */
	public VisualizerCPU(int id) {
		m_id = id;
		m_cores = new ArrayList<VisualizerCore>();
		m_coreMap = new Hashtable<Integer, VisualizerCore>();
	}
	
	/** Dispose method */
	public void dispose() {
		if (m_cores != null) {
			for (VisualizerCore core : m_cores) {
				core.dispose();
			}
			m_coreMap.clear();
			m_coreMap = null;
			m_cores.clear();
			m_cores = null;
		}
	}
	
	
	// --- Object methods ---
	
	/** Returns string representation. */
	@Override
	public String toString() {
		return "CPU:" + m_id; //$NON-NLS-1$
	}
	
	
	// --- accessors ---
	
	/** Gets ID of this CPU. */
	public int getID() {
		return m_id;
	}
	
	
	// --- methods ---
	
	/** Gets number of cores. */
	public int getCoreCount() {
		return m_cores.size();
	}
	
	/** Gets cores. */
	public List<VisualizerCore> getCores() {
		return m_cores;
	}
	
	/** Gets core with specified ID. */
	public VisualizerCore getCore(int id) {
		return m_coreMap.get(id);
	}
	
	/** Adds core. */
	public VisualizerCore addCore(VisualizerCore core) {
		m_cores.add(core);
		m_coreMap.put(core.getID(), core);
		return core;
	}

	/** Removes core. */
	public void removeCore(VisualizerCore core) {
		m_cores.remove(core);
		m_coreMap.remove(core.getID());
	}

	
	/** Sorts cores, cpus, etc. by IDs. */
	public void sort() {
		Collections.sort(m_cores);
	}

	
	// --- Comparable implementation ---
	
	/** Compares this item to the specified item. */
	@Override
	public int compareTo(VisualizerCPU o) {
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
	
}
