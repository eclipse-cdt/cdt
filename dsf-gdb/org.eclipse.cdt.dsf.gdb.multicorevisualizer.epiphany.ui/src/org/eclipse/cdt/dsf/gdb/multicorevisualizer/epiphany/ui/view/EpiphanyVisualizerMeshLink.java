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

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelMeshRouter;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.model.EpiphanyModelMeshRouter.LinkDirection;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.EpiphanyConstants;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class EpiphanyVisualizerMeshLink extends	EpiphanyVisualizerContainer 
{

	// --- members ---
	
	/** Direction of the link */
	protected EpiphanyModelMeshRouter.LinkDirection m_direction = null;
	
	/** Is the link connected to something? */
	protected Boolean m_connected = null;
	
	/** Is the link horizontal? else vertical */
	protected boolean m_isHorizontal = false; 
	
	/** Load associated to this link */
	protected Integer m_load = null;
	
	/** Width of the body of the arrow in pixels*/
	protected static final int LINK_ARROW_WIDTH = 2;
	

	// --- constructors/destructors ---
	
	/** Constructor */
	public EpiphanyVisualizerMeshLink(int cpuId, EpiphanyModelMeshRouter.LinkDirection direction, boolean connected)
	{
		setId(cpuId);
		m_direction  = direction;
		m_connected = connected;
		
		if (direction == LinkDirection.LINK_EAST_IN || 
			direction == LinkDirection.LINK_EAST_OUT ||
			direction == LinkDirection.LINK_WEST_IN ||
			direction == LinkDirection.LINK_WEST_OUT ) 
		{
			m_isHorizontal = true;
		}
		else {
			m_isHorizontal = false;
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
		return super.toString() + "eCore id:" + getId() + ", Direction: " + m_direction + ", Connected: " + m_connected + ", Load: " +( m_load != null ? m_load.toString() : "undefined"  );
	}
	
	
	// --- accessors ---
	
	public void setLoad(int load) {
		m_load = load;
	}

	
	// --- methods ---
	
	
	// --- paint methods ---
	
	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		if (!isVisible()) return;
		
		// TODO: once scaling is implemented, convert pixel-based computations below to relative-based computations
		super.paintContent(gc);

		// absolute bounds of the body of the arrow
		Rectangle linkAbs = null;
		
		// (x,y) coordinates of the 3 points that make an arrow head (triangle)
		int[] arrowPointArray = new int [6];
		
		if (!m_connected) {
			gc.setBackground(EpiphanyConstants.EV_COLOR_DISCONNECTED);
			gc.setForeground(EpiphanyConstants.EV_COLOR_DISCONNECTED);
		}
		else {
			if (m_load > 75) {
				gc.setBackground(EpiphanyConstants.EV_COLOR_HIGH_LOAD);
				gc.setForeground(EpiphanyConstants.EV_COLOR_HIGH_LOAD);
			}
			else if(m_load > 25) {
				gc.setBackground(EpiphanyConstants.EV_COLOR_MED_LOAD);
				gc.setForeground(EpiphanyConstants.EV_COLOR_MED_LOAD);
			}
			else {
				gc.setBackground(EpiphanyConstants.EV_COLOR_LOW_LOAD);
				gc.setForeground(EpiphanyConstants.EV_COLOR_LOW_LOAD);
			}
		}
		
		// horizontal arrow?
		if (m_isHorizontal) {			
			// arrow points left
			if (m_direction == LinkDirection.LINK_EAST_IN || m_direction == LinkDirection.LINK_WEST_OUT) {
				// point 1
				arrowPointArray[0] = this.getX() ;
				arrowPointArray[1] = this.getY() + this.getHeight() / 2;
				// point 2
				arrowPointArray[2] = this.getX() + this.getWidth() / 6;
				arrowPointArray[3] = this.getY() + this.getHeight() / 4;
				// point 3
				arrowPointArray[4] = this.getX() + this.getWidth() / 6;
				arrowPointArray[5] = this.getY() + this.getHeight() * 3 / 4;
				
				// figure-out where to display the body of the arrow
				linkAbs = new Rectangle(this.getX() + this.getWidth() / 6 , this.getY() + this.getHeight()/2 - 1, this.getWidth() - this.getWidth() / 6, LINK_ARROW_WIDTH);
			}
			// arrow points right
			else {
				// point 1
				arrowPointArray[0] = this.getX() + this.getWidth();
				arrowPointArray[1] = this.getY() + this.getHeight() / 2;
				// point 2
				arrowPointArray[2] = this.getX() + this.getWidth() * 5 / 6;
				arrowPointArray[3] = this.getY() + this.getHeight() / 4;
				// point 3
				arrowPointArray[4] = this.getX() + this.getWidth() * 5 / 6;
				arrowPointArray[5] = this.getY() + this.getHeight() * 3 / 4;
				
				// figure-out where to display the body of the arrow
				linkAbs = new Rectangle(this.getX(), this.getY() + this.getHeight()/2 - 1, this.getWidth() - this.getWidth() / 6, LINK_ARROW_WIDTH);
			}
		}
		// vertical arrow?
		else {			
			// arrow points up
			if (m_direction == LinkDirection.LINK_NORTH_OUT || m_direction == LinkDirection.LINK_SOUTH_IN) {
				// point 1
				arrowPointArray[0] = this.getX() + this.getWidth() / 2;
				arrowPointArray[1] = this.getY();
				// point 2
				arrowPointArray[2] = this.getX() + this.getWidth() / 4;
				arrowPointArray[3] = this.getY() + this.getHeight() / 6;
				// point 3
				arrowPointArray[4] = this.getX() + this.getWidth() * 3 / 4;
				arrowPointArray[5] = this.getY() + this.getHeight() / 6;
				
				// figure-out where to display the body of the arrow
				linkAbs = new Rectangle(this.getX() + this.getWidth()/2 - 1, this.getY() + this.getHeight() / 6, LINK_ARROW_WIDTH, this.getHeight() -  this.getHeight() / 6);
			}
			// arrow points down
			else {
				// point 1
				arrowPointArray[0] = this.getX() + this.getWidth() / 2;
				arrowPointArray[1] = this.getY() + this.getHeight();
				// point 2
				arrowPointArray[2] = this.getX() + this.getWidth() / 4;
				arrowPointArray[3] = this.getY() + this.getHeight() * 5 / 6;
				// point 3
				arrowPointArray[4] = this.getX() + this.getWidth() * 3 / 4;
				arrowPointArray[5] = this.getY() + this.getHeight() * 5 / 6;
				
				// figure-out where to display the body of the arrow
				linkAbs = new Rectangle(this.getX() + this.getWidth()/2 - 1, this.getY(), LINK_ARROW_WIDTH, this.getHeight() - this.getHeight() / 6);
			}
		}
		
		// draw arrow body
		gc.fillRectangle(linkAbs);
		gc.drawRectangle(linkAbs);
		
		// draw arrow pointy end
		gc.fillPolygon(arrowPointArray);
		gc.drawPolygon(arrowPointArray);

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
