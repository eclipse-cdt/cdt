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
		public BarGraphicObject(int severity, int x, int y, int w, int h) {
			super(x, y, w, h);
			
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
			setBackground(color);
			setForeground(color);
		}
		
		@Override
		public void paintContent(GC gc) {
			gc.fillRectangle(m_bounds);
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

	private BarGraphicObject[] getBars() {
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

		// Find the maximum marker count to dictate the width
		int maxCount = Math.max(m_markerCount[0], m_markerCount[1]);
		maxCount = Math.max(maxCount, m_markerCount[2]);
		int maxWidth = bounds.width - 2 * MARGIN_WIDTH;
		if (maxCount == 0) maxCount = maxWidth;

		int count = m_markerCount[IMarker.SEVERITY_ERROR];
		if (count == 0) count = 1;
		int width = maxWidth * count / maxCount;
		bars[0] = new BarGraphicObject(IMarker.SEVERITY_ERROR, x, y, width, height);

		y = y + height + spacing;
		count = m_markerCount[IMarker.SEVERITY_WARNING];
		if (count == 0) count = 1;
		width = maxWidth * count / maxCount;
		bars[1] = new BarGraphicObject(IMarker.SEVERITY_WARNING, x, y, width, height);
		
		y = y + height + spacing;
		count = m_markerCount[IMarker.SEVERITY_INFO];
		if (count == 0) count = 1;
		width = maxWidth * count / maxCount;
		bars[2] = new BarGraphicObject(IMarker.SEVERITY_INFO, x, y, width, height);
		
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
		
		Object sel = SelectionUtils.getSelectedObject(selection);

		if (sel instanceof IResource) {
			m_canvas.setBackground(Colors.WHITE);

			setMarkerCount((IResource)sel);

			BarGraphicObject[] bars = getBars();

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
