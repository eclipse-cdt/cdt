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
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class EpiphanyVisualizerIO extends EpiphanyVisualizerContainer 
{
	
	// --- members ---
	
	/** An IO block can be connected to something or not */
	protected Boolean m_connected = false; 
	
	/** Position of the IO relative to Epiphany chip */
	protected IOPosition m_position = null;

//	protected static final Color BG_COLOR_CONNECTED = Colors.WHITE;
//	protected static final Color BG_COLOR_DISCONNECTED = Colors.DARK_GRAY;
//	protected static final Color FG_COLOR = Colors.BLACK;

	

	// --- constructors/destructors ---
	
	/** Constructor */
	public EpiphanyVisualizerIO(EpiphanyModelIO.IOPosition position, boolean connected)
	{
		m_position = position;
		m_connected = connected;
		this.setSelectable(true);
		this.setForeground(Colors.WHITE);
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
		super.paintContent(gc);
		
		// relative bounds of IO
		Rectangle ioRel = null;
		// absolute bounds of IO
		Rectangle ioAbs= null;
		
		// use background color to reflect if IO is connected or not
		Color bg;
		if (m_connected) {
			bg = EpiphanyConstants.EV_COLOR_CONNECTED;
		}
		else {
			bg = EpiphanyConstants.EV_COLOR_DISCONNECTED;
		}
		
//		gc.setForeground(FG_COLOR);
		gc.setBackground(bg);
		
		// North and South IOs are horizontal
		if (m_position == IOPosition.IO_NORTH || m_position == IOPosition.IO_SOUTH) {
			ioRel = new Rectangle(m_relativeObj.getX() + 3, 0, m_relativeObj.getWidth() - 7, m_relativeObj.getHeight());
		}
		// East and West IOs are vertical
		else if (m_position == IOPosition.IO_EAST || m_position == IOPosition.IO_WEST) {
			ioRel = new Rectangle(0, m_relativeObj.getY() + 3, m_relativeObj.getWidth(), m_relativeObj.getHeight() - 7);
		}
		
		ioAbs = relativeToAbsoluteBounds(ioRel);
		
		
		gc.fillRectangle(ioAbs);
		gc.drawRectangle(ioAbs);
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
