/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.visualizer.examples;

import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvas;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvasVisualizer;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicObject;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.SelectionManager;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class ProblemVisualizer extends GraphicCanvasVisualizer {

	private static final int MARGIN_WIDTH = 10;
	private static final int MARGIN_HEIGHT = 10;
	private static final int SPACING_HEIGHT = 40;
	private static final int NUM_SEVERITY = 3;

	private class BarGraphicObject extends GraphicObject {
		private boolean m_outline;
		
		public BarGraphicObject(int severity, int x, int y, int w, int h, boolean outline) {
			super(x, y, w, h);
			m_outline = outline;
			
			Color color = Colors.BLACK;
			
			switch (severity) {
			case IMarker.SEVERITY_ERROR:
				color = Colors.DARK_RED;
				break;
			case IMarker.SEVERITY_WARNING:
				color = Colors.DARK_YELLOW;
				break;
			case IMarker.SEVERITY_INFO:
				color = Colors.DARK_BLUE;
				break;
			}
			if (!m_outline) setBackground(color);
			setForeground(color);
		}
		
		@Override
		public void paintContent(GC gc) {
			if (m_outline) {
				gc.drawRectangle(m_bounds);
			} else {
				gc.fillRectangle(m_bounds);
			}
		}
	}
	
	
	private GraphicCanvas m_canvas;
	
	private int[] m_markerCount = new int[NUM_SEVERITY];
	
	public ProblemVisualizer() {
		super();
	}

	@Override
	public String getName() {
		return Messages.CounterVisualizer_Name;
	}
	
	@Override
	public String getDisplayName() {
		return Messages.CounterVisualizer_DisplayName;
	}
	
	@Override
	public String getDescription() {
		return Messages.CounterVisualizer_Description;
	}
	
	@Override
	public GraphicCanvas createCanvas(Composite parent) {
		m_canvas = new GraphicCanvas(parent);
		return m_canvas;
	}
	
	@Override
	protected void initializeCanvas(GraphicCanvas canvas) {
		// TODO Auto-generated method stub
		super.initializeCanvas(canvas);
	}
	
	@Override
	public int handlesSelection(ISelection selection) {
		Object sel = SelectionUtils.getSelectedObject(selection);

		if (sel instanceof IResource) {
			return 2;
		}
		
		return 0;
	}
	
	@Override
	public void visualizerDeselected() {
		// TODO Auto-generated method stub
		super.visualizerDeselected();
	}
	
	@Override
	public void visualizerSelected() {
		// TODO Auto-generated method stub
		super.visualizerSelected();
	}

	private BarGraphicObject[] getBars(boolean outline) {
		BarGraphicObject[] bars = new BarGraphicObject[3];
		
		Rectangle bounds = m_canvas.getBounds();

		int x = bounds.x + MARGIN_WIDTH;
		int y = bounds.y + MARGIN_HEIGHT;
		
		int spacing = SPACING_HEIGHT;
		int height = (bounds.height - 2 * MARGIN_HEIGHT - 2 * SPACING_HEIGHT) / 3;
		if (height <= 0) {
			spacing = 0;
			y = bounds.y;
			height = bounds.height / 3;
		}

		int maxWidth = bounds.width - 2 * MARGIN_WIDTH;

		if (outline) {
			bars[0] = new BarGraphicObject(IMarker.SEVERITY_ERROR, x, y, maxWidth, height, outline);
			
			y = y + height + spacing;
			bars[1] = new BarGraphicObject(IMarker.SEVERITY_WARNING, x, y, maxWidth, height, outline);

			y = y + height + spacing;
			bars[2] = new BarGraphicObject(IMarker.SEVERITY_INFO, x, y, maxWidth, height, outline);

		} else {
			// Find the maximum marker count to dictate the width
			int maxCount = Math.max(m_markerCount[0], m_markerCount[1]);
			maxCount = Math.max(maxCount, m_markerCount[2]);
			if (maxCount == 0) maxCount = 1; // Set to anything but 0.  It will be multiplied by 0 and not matter.

			int width = maxWidth * m_markerCount[IMarker.SEVERITY_ERROR] / maxCount;
			bars[0] = new BarGraphicObject(IMarker.SEVERITY_ERROR, x, y, width, height, outline);

			y = y + height + spacing;
			width = maxWidth * m_markerCount[IMarker.SEVERITY_WARNING] / maxCount;
			bars[1] = new BarGraphicObject(IMarker.SEVERITY_WARNING, x, y, width, height, outline);

			y = y + height + spacing;
			width = maxWidth * m_markerCount[IMarker.SEVERITY_INFO] / maxCount;
			bars[2] = new BarGraphicObject(IMarker.SEVERITY_INFO, x, y, width, height, outline);
		}
		
		return bars;
	}
	
	private void setMarkerCount(IResource resource) {
		m_markerCount[IMarker.SEVERITY_ERROR] = 0;
		m_markerCount[IMarker.SEVERITY_WARNING] = 0;
		m_markerCount[IMarker.SEVERITY_INFO] = 0;

		IMarker[] problems = null;
		try {
			problems = resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			return;
		}
		
		for (IMarker problem : problems) {
			try {
				Object attrValue = problem.getAttribute(IMarker.SEVERITY);
				if (attrValue != null && attrValue instanceof Integer) {
					int severity = (Integer)attrValue;
					m_markerCount[severity]++;
				}
			} catch (CoreException e) {
			}
		}
	}

	@Override
	public void workbenchSelectionChanged(ISelection selection) {
		m_canvas.clear();
		
		// First create the outline bars
		BarGraphicObject[] bars = getBars(true);
		for (BarGraphicObject bar : bars) {
			m_canvas.add(bar);
		}
		
		Object sel = SelectionUtils.getSelectedObject(selection);
		if (sel instanceof IResource) {
			// Now, create the inside bars

			setMarkerCount((IResource)sel);

			bars = getBars(false);
			for (BarGraphicObject bar : bars) {
				m_canvas.add(bar);
			}
		}
		
		m_canvas.redraw();
	}
	
	public SelectionManager getSelectionManager() {
		return m_selectionManager;
	}
	
}
