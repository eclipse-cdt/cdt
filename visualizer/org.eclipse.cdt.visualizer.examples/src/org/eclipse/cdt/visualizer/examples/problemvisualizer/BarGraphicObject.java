/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *     Xavier Raynaud (Kalray) - Bug 431690
 *******************************************************************************/
package org.eclipse.cdt.visualizer.examples.problemvisualizer;

import org.eclipse.cdt.visualizer.ui.canvas.VirtualBoundsGraphicObject;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * A class that draws a bar or a bar outline in the specified color.
 */
public class BarGraphicObject extends VirtualBoundsGraphicObject {

	/* The different colors to use for the different severities */
	private static final Color ERROR_OUTLINE_COLOR = Colors.DARK_RED;
	private static final Color WARNING_OUTLINE_COLOR = Colors.DARK_YELLOW;
	private static final Color INFO_OUTLINE_COLOR = Colors.DARK_BLUE;

	private String m_label;
	private int m_barPercent;

	public BarGraphicObject(int severity, int barPercent) {
		m_barPercent = barPercent;

		Color color = getColor(severity);
		setForeground(color);
		setBackground(color);
	}

	public void setLabel(String label) {
		m_label = label;
	}

	@Override
	public void paintContent(GC gc) {
		// draw outline of bar
		gc.drawRectangle(m_bounds);

		// figure-out the width that needs to be filled-in for this bar
		int barWidth = m_bounds.width * m_barPercent / 100;
		Rectangle fillIn = new Rectangle(m_bounds.x, m_bounds.y, barWidth, m_bounds.height);
		// fill-in bar
		gc.fillRectangle(fillIn);
	}

	@Override
	public boolean hasDecorations() {
		return true;
	}

	/** Invoked to allow element to paint decorations on top of anything drawn on it */
	@Override
	public void paintDecorations(GC gc) {
		if (m_bounds.height > 20) {
			gc.setForeground(Colors.BLACK);

			int text_indent = 6;
			int tx = m_bounds.x + m_bounds.width - text_indent;
			int ty = m_bounds.y + m_bounds.height - text_indent;
			GUIUtils.drawTextAligned(gc, m_label, m_bounds, tx, ty, false, false);
		}
	}

	private Color getColor(int severity) {
		switch (severity) {
		case IMarker.SEVERITY_ERROR:
			return ERROR_OUTLINE_COLOR;
		case IMarker.SEVERITY_WARNING:
			return WARNING_OUTLINE_COLOR;
		case IMarker.SEVERITY_INFO:
			return INFO_OUTLINE_COLOR;
		}
		return Colors.ORANGE;
	}
}