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
import org.eclipse.cdt.visualizer.ui.canvas.VirtualBoundsGraphicObject;

public class EpiphanyVisualizerContainer extends VirtualBoundsGraphicObject 
{

	// --- members ---
	
	/** Object Id */
	protected Integer m_id = null;
			

	// --- constructors/destructors ---

	/** Constructor */
	public EpiphanyVisualizerContainer()
	{
		setForeground(EpiphanyConstants.EV_COLOR_FOREGROUND);
		setBackground(EpiphanyConstants.EV_COLOR_BACKGROUND);
		setSelectedColor(EpiphanyConstants.EV_COLOR_SELECTED);
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
		return String.format("ObjId: %s, %s ", 
				m_id != null ? m_id : "n/a", 
				super.toString()
			); 
	}

	// --- accessors ---

	/** Set object id */
	protected void setId(int id) {
		m_id = id;
	}

	/** Get object id */
	protected Integer getId() {
		return m_id;
	}
	
	
	// --- methods ---

	
	// --- paint methods ---	

}
