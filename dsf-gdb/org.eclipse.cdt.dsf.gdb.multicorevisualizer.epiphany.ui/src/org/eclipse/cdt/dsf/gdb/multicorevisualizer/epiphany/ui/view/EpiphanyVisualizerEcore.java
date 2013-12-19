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
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui.utils.IEpiphanyConstants;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.IMulticoreVisualizerConstants;
import org.eclipse.cdt.visualizer.ui.plugin.CDTVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

@SuppressWarnings("restriction")
public class EpiphanyVisualizerEcore extends EpiphanyVisualizerContainer {

	// --- members ---
	/** Id to show users */
	protected String m_label;
	
	/** to draw to eCore on a sub-square of this */
	protected EpiphanyVisualizerContainer m_core;
	
	/** The load indicator associated to this eCore */
	protected EpiphanyVisualizerLoadIndicator m_loadIndicator;
	
	/** Name of the program running on this core */
	protected String m_programName = null;
	
	/** Color the core is meant to be painted in */
	protected Color m_coreColor = null;
	
	/** load monitoring enabled? */
	protected boolean m_loadEnabled = false;
	
//	protected static final Color BG_COLOR = Colors.GRAY;
//	protected static final Color FG_COLOR = Colors.BLACK;
//	protected static final Color TEXT_COLOR = Colors.BLACK;


	// --- constructors/destructors ---

	/** Constructor */
	public EpiphanyVisualizerEcore(int id, IEpiphanyConstants eConstants, String progName, Color coreColor, boolean loadEnabled)
	{
		setId(id);
		m_label = eConstants.getLabelFromId(id);
		m_programName = progName;
		m_coreColor = coreColor;
		m_loadEnabled = loadEnabled;
		
		m_core = new EpiphanyVisualizerContainer();
//		int[] ecoreBounds = {1, 1, 2, 2};
		int[] ecoreBoundsNoLoad = {1, 1, 4, 4};
		// bigger core square?
		int[] ecoreBounds = {0, 0, 3, 3};
		int[] loadIndicatorBounds = {2, 2, 1, 1};
		
		if (m_loadEnabled) {
			m_core.setRelativeBounds(ecoreBounds);
		}
		else {
			m_core.setRelativeBounds(ecoreBoundsNoLoad);
		}
		
		m_core.setDrawBounds(true);
		addChildObject("core_" + getId() , m_core);
		
		
		m_loadIndicator = new EpiphanyVisualizerLoadIndicator(getId(), m_loadEnabled);
		m_loadIndicator.setRelativeBounds(loadIndicatorBounds);
		addChildObject("load_indicator_" + getId(), m_loadIndicator);
	}

	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
		if (m_core != null) {
			m_core.dispose();
			m_core = null;
		}
		if (m_loadIndicator != null) {
			m_loadIndicator.dispose();
			m_loadIndicator = null;
		}
	}

	
	// --- Object methods ---
	
	/** Returns string representation. */
	@Override
	public String toString() {
		return super.toString() + ", id: " + getId() + ", program name: " + m_programName;		
	}

	// --- accessors ---
	public EpiphanyVisualizerLoadIndicator getLoadIndicator() {
		return m_loadIndicator;
	}
	
	/** Get the name of the program running on this eCore */
	public String getProgramName() {
		return m_programName;
	}
	
	/** Get the label associated to this eCore  */
	public String getLabel() {
		return m_label;
	}


	// --- methods ---


	// --- paint methods ---

	
	@Override
	public void paintContent(GC gc) {
		if (m_coreColor != null) {
			m_core.setBackground(m_coreColor);
		}
		else {
			m_core.setBackground(EpiphanyConstants.EV_COLOR_ECORE);
		}
		
		if (isSelected()) {
			m_core.setForeground(EpiphanyConstants.EV_COLOR_SELECTED);
		}
		else {
			m_core.setForeground(EpiphanyConstants.EV_COLOR_FOREGROUND);
		}
		
		super.paintContent(gc);
	}


	/** Returns true if object has decorations to paint. */
	@Override
	public boolean hasDecorations() {
		return true;
	}

	/** Invoked to allow element to paint decorations on top of anything drawn on it */
	@Override
	public void paintDecorations(GC gc) {
		// figure-out where to display the eCore id
		// relative bounds of eCore id text
		
		Rectangle eCoreId = new Rectangle(0, 0, 1, 1);
		
		if(m_loadEnabled) {
			eCoreId = new Rectangle(0, 0, 1, 1);
		}
		else {
			eCoreId = new Rectangle(1, 1, 1, 1);
		}
		
		// absolute bounds of eCore id text
		Rectangle eCoreAbsId = relativeToAbsoluteBounds(eCoreId);
		
		// Adjust font depending on available space
		Font oldFont = gc.getFont();
		if (eCoreAbsId.height > 15) {
			gc.setFont(CDTVisualizerUIPlugin.getResources().getFont("Luxi Sans", 12));
		}
		else /* if (eCoreAbsId.height  8) */ { 
			gc.setFont(CDTVisualizerUIPlugin.getResources().getFont("Luxi Sans", 7));
		}
		
		if (eCoreAbsId.height > 8) {
			gc.setForeground(EpiphanyConstants.EV_COLOR_ECORE_TEXT);
			gc.setBackground(IMulticoreVisualizerConstants.COLOR_CPU_BG);
			
			int text_indent_x = 5;
			int text_indent_y = -3;
			int tx = eCoreAbsId.x + eCoreAbsId.width  - text_indent_x;
			int ty = eCoreAbsId.y + eCoreAbsId.height - text_indent_y;
			GUIUtils.drawTextAligned(gc, m_label, tx, ty, true, false);
		}
		// restore original font size
		gc.setFont(oldFont);
	}
}
