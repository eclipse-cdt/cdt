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


import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelIO;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelIO.IOPosition;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.EpiphanyConstants;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class EpiphanyVisualizerIO extends EpiphanyVisualizerContainer 
{
	
	// --- members ---
	
	/** An IO block can be connected to something or not */
	protected Boolean m_connected = false; 
	
	/** Position of the IO relative to Epiphany chip */
	protected IOPosition m_position = null;


	// --- constructors/destructors ---
	
	/** Constructor */
	public EpiphanyVisualizerIO(EpiphanyModelIO.IOPosition position, boolean connected)
	{
		m_position = position;
		m_connected = connected;
		setSelectable(true);
		setForeground(EpiphanyConstants.EV_COLOR_FOREGROUND);
		if (m_connected) {
			setBackground(EpiphanyConstants.EV_COLOR_CONNECTED);
		}
		else {
			setBackground(EpiphanyConstants.EV_COLOR_DISCONNECTED);
		}
		
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
		return super.toString() + ", Position: " + m_position +", Connected: " + m_connected;		
	}
	
	
	// --- accessors ---
	
	
	// --- methods ---
	
	
	// --- paint methods ---
	
	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		
		// virtual bounds of IO
		Rectangle ioVirt = null;
		// absolute bounds of IO
		Rectangle ioReal= null;
		
		// North and South IOs are horizontal
		if (m_position == IOPosition.IO_NORTH || m_position == IOPosition.IO_SOUTH) {
			ioVirt = new Rectangle(m_virtualBounds.x + 3, 0, m_virtualBounds.width - 7, m_virtualBounds.height);
		}
		// East and West IOs are vertical
		else if (m_position == IOPosition.IO_EAST || m_position == IOPosition.IO_WEST) {
			ioVirt = new Rectangle(0, m_virtualBounds.y + 3, m_virtualBounds.width, m_virtualBounds.height - 7);
		}
		
		ioReal = virtualToRealBounds(ioVirt);
		
		gc.fillRectangle(ioReal);
		gc.drawRectangle(ioReal);
	}

}
