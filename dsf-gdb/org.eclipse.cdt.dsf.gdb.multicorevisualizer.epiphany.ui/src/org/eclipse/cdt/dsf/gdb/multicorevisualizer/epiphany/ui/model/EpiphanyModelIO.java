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

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.IVisualizerModelObject;

@SuppressWarnings("restriction")
public class EpiphanyModelIO implements Comparable<EpiphanyModelIO>, IVisualizerModelObject {
	
	public enum IOPosition {
		IO_NORTH,
		IO_EAST,
		IO_SOUTH,
		IO_WEST,
	}
	
	// --- members ---
	
	/** An IO block can be connected to something or not */
	protected Boolean m_connected = false; 
	
	// Is there a way to identify a specific IO block? For a single 
	// epiphany chip we might us N, S, E and W. But what if there are
	// many Epiphanys? 
	/** Position of the IO relative to Epiphany chip */
	protected IOPosition m_position = null;
	
	// --- constructors/destructors ---


	/** Constructor */
	public EpiphanyModelIO(boolean connected, IOPosition pos) {
		m_connected = connected;
		m_position = pos;
	}
	
	/** Constructor */
	public EpiphanyModelIO(boolean connected) {
		m_connected = connected;
	}
	
	public void dispose() {
		m_connected = null;
		m_position = null;
	}
	
	// --- Object methods ---

	/** Returns string representation. */
	@Override
	public String toString() {
		return "Epiphany IO:  connected ?: " + m_connected + ", IO position: " + m_position; //$NON-NLS-1$
	}
	
	
	// --- accessors ---	

	/** Returns if an IO block is connected or not */
	public boolean getConnected() {
		return m_connected;
	}

	/** Sets if an IO block is connected or not */
	public void setConnected (boolean connected) {
		m_connected = connected;
	}
	
	/** Gets the position of an IO block */
	public IOPosition getPosition() {
		return m_position;
	}

	/** Sets the position of an IO block */
	public void setPosition(IOPosition pos) {
		m_position = pos;
	}

	@Override
	public int getID() {
		// IO_NORTH = 0, IO_SOUTH = 1, ...
		return m_position.ordinal();
	}

	@Override
	public IVisualizerModelObject getParent() {
		// no parent
		return null;
	}

	
	// --- Comparable implementation ---
	
	
	@Override
	public int compareTo(IVisualizerModelObject o) {
		if (o != null) {
			if (o.getClass() == this.getClass()) {
				return compareTo((EpiphanyModelIO)o);
			}
		}
		return 1;
	}

	@Override
	public int compareTo(EpiphanyModelIO o) {
		int result = 0;
		if (o != null) {
			if (this.getID() < o.getID()) {
				result = -1;
			}
			else if (this.getID() > o.getID()) {
				result = 1;
			}
		}
		return result;
	}
	
	

}
