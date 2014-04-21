/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - Initial API and implementation (borrowed from MulticoreVisualizerStatusBar)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.view;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.EpiphanyConstants;
import org.eclipse.cdt.visualizer.ui.plugin.CDTVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;

public class EpiphanyVisualizerStatusBar extends EpiphanyVisualizerContainer {
	
	// --- members ---

	/** message to display in status bar */
	protected String m_statusMessage = null;	

	// --- constructors/destructors ---

	/** Constructor */
	public EpiphanyVisualizerStatusBar() {
		setForeground(EpiphanyConstants.EV_COLOR_STATUSBAR_TEXT);
		setBackground(EpiphanyConstants.EV_COLOR_STATUSBAR_BG);
	}


	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
		m_statusMessage = null;
	}


	// --- accessors ---

	public void setMessage(String message) {
		m_statusMessage = message;
	}

	// --- paint methods ---
	
	@Override
	public void paintContent(GC gc) {
		// Draw status bar
		gc.fillRectangle(m_bounds);
	}
	
	@Override
	public boolean hasDecorations() {
		return true;
	}
	
	@Override
	public void paintDecorations(GC gc) {
		// Adjust font according to available space
		Font oldFont = gc.getFont();
		if (this.getHeight() < 12) {
			gc.setFont(CDTVisualizerUIPlugin.getResources().getFont("Luxi Sans", 5));
		}
		else if (this.getHeight() < 20) {
			gc.setFont(CDTVisualizerUIPlugin.getResources().getFont("Luxi Sans", 7));
		}
		else if (this.getHeight() < 30) {
			gc.setFont(CDTVisualizerUIPlugin.getResources().getFont("Luxi Sans", 10));
		}
		else {
			gc.setFont(CDTVisualizerUIPlugin.getResources().getFont("Luxi Sans", 14));
		}

		int tx = m_bounds.x + 10;
		int ty = m_bounds.y + m_bounds.height;
		// Display text
		if(m_statusMessage != null) {
			GUIUtils.drawTextAligned(gc, m_statusMessage, tx, ty, true, false);
		}
		
		// restore original font size
		gc.setFont(oldFont);
	}
}
