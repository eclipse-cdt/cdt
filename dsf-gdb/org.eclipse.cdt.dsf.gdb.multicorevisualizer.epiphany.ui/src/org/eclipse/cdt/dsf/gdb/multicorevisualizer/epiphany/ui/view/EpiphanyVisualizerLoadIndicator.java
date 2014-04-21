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

public class EpiphanyVisualizerLoadIndicator extends EpiphanyVisualizerContainer 
{

	// --- members ---
	
	/** Load associated to this load indicator */
	protected Integer m_load = null;
	
	/** Is load monitoring enabled?   */
	protected boolean m_loadEnabled = false;
	

	// --- constructors/destructors ---

	/** Constructor */
	public EpiphanyVisualizerLoadIndicator(int id, boolean loadEnabled)
	{
		setId(id);
		m_loadEnabled = loadEnabled;
	}

	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
	}


	// --- Object methods ---
	
	/** Returns string representation. */
	@Override
	public String toString() {
		return super.toString() + ", Load: " +( m_load != null ? m_load.toString() : "undefined"  );		
	}
	
		
	// --- accessors ---
	
	/** set the router's load */
	public void setLoad(int load) {
		m_load = load;
		
		// Adjust load indicator's color according to current load
		if (m_load > 75) {
			setBackground(EpiphanyConstants.EV_COLOR_HIGH_LOAD_BG);
		}
		else if (m_load > 50) {
			setBackground(EpiphanyConstants.EV_COLOR_MED_LOAD_BG);
		}
		else {
			setBackground(EpiphanyConstants.EV_COLOR_LOW_LOAD_BG);
		}
	}
	

	// --- methods ---


	// --- paint methods ---

}
