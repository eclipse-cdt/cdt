/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.visualizer.examples.problemvisualizer;

import org.eclipse.cdt.visualizer.ui.canvas.GraphicObject;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

/**
 * A class that draws a bar or a bar outline in the specified color.
 */
public class BarGraphicObject extends GraphicObject {
	
	/* The different colors to use for the different severities */
	private static final Color ERROR_OUTLINE_COLOR = Colors.DARK_RED;
	private static final Color ERROR_INSIDE_COLOR = Colors.DARK_RED;
	private static final Color WARNING_OUTLINE_COLOR = Colors.DARK_YELLOW;
	private static final Color WARNING_INSIDE_COLOR = Colors.DARK_YELLOW;
	private static final Color INFO_OUTLINE_COLOR = Colors.DARK_BLUE;
	private static final Color INFO_INSIDE_COLOR = Colors.DARK_BLUE;

	private boolean m_outline;
	private String m_label;
	
	public BarGraphicObject(int severity, int x, int y, int w, int h, boolean outline) {
		super(x, y, w, h);
		m_outline = outline;
		
		Color color = getColor(severity);
		if (m_outline) {
			setForeground(color);
		} else {
			setBackground(color);
		}
	}
	
	public void setLabel(String label) {
		m_label = label;
	}
	
	@Override
	public void paintContent(GC gc) {
		if (m_outline) {
			gc.drawRectangle(m_bounds);
		} else {
			gc.fillRectangle(m_bounds);
		}
	}
	
	@Override
	public boolean hasDecorations() {
		// Only the outline bar has a label decoration.
		// We muse the the outline bar and not the inside one because
		// the inside bar may be too small
		return m_outline;
	}
	
	/** Invoked to allow element to paint decorations on top of anything drawn on it */
	@Override
	public void paintDecorations(GC gc) {
		if (m_bounds.height > 20) {
			gc.setForeground(Colors.BLACK);
			
			int text_indent = 6;
			int tx = m_bounds.x + m_bounds.width  - text_indent;
			int ty = m_bounds.y + m_bounds.height - text_indent;
			GUIUtils.drawTextAligned(gc, m_label, tx, ty, false, false);
		}
	}

	private Color getColor(int severity) {
		switch (severity) {
		case IMarker.SEVERITY_ERROR:
			if (m_outline) return ERROR_OUTLINE_COLOR;
			return ERROR_INSIDE_COLOR;
		case IMarker.SEVERITY_WARNING:
			if (m_outline) return WARNING_OUTLINE_COLOR;
			return WARNING_INSIDE_COLOR;
		case IMarker.SEVERITY_INFO:
			if (m_outline) return INFO_OUTLINE_COLOR;
			return INFO_INSIDE_COLOR;
		}
		return Colors.ORANGE;
	}
}