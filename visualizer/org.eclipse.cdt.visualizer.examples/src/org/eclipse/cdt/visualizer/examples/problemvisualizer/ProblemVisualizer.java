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
 *     Marc Dumais (Ericsson) - Re-factored (bug 432908)
 *******************************************************************************/
package org.eclipse.cdt.visualizer.examples.problemvisualizer;

import java.util.List;

import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvas;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvasVisualizer;
import org.eclipse.cdt.visualizer.ui.canvas.VirtualBoundsGraphicObject;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.SelectionManager;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

public class ProblemVisualizer extends GraphicCanvasVisualizer {

	/** The predefined number of severities */
	private static final int NUM_SEVERITY = 3;

	private static final Color MAIN_BACKGROUND_COLOR = Colors.WHITE;
	private static final Color MAIN_FOREGROUND_COLOR = Colors.BLACK;
	/** Virtual bounds of the "box" that will contains the bars */
	private static final int[] BAR_CONTAINER_BOUNDS = { 0, 0, 1, 18 };
	private static final int BAR_VIRTUAL_WIDTH = 1;
	private static final int BAR_VIRTUAL_HEIGHT = 4;
	/** Virtual bounds of each of the bars, relative to their container */
	private static final int[][] BARS_VIRTUAL_BOUNDS = { { 0, 13, BAR_VIRTUAL_WIDTH, BAR_VIRTUAL_HEIGHT }, // infos
			{ 0, 7, BAR_VIRTUAL_WIDTH, BAR_VIRTUAL_HEIGHT }, // warnings
			{ 0, 1, BAR_VIRTUAL_WIDTH, BAR_VIRTUAL_HEIGHT } // errors
	};

	/** The canvas on which we'll draw our bars */
	private GraphicCanvas m_canvas;
	/** Graphic container object - will hold the 3 bars */
	private VirtualBoundsGraphicObject m_container = null;

	/**
	 * The model containing the data to be displayed.
	 * In this case, it is the number of the three
	 * different types of problem markers.
	 */
	private int[] m_markerCount = new int[NUM_SEVERITY];

	/** Labels for the different marker severity levels*/
	private String[] m_markerSeverityLabels = { Messages.ProblemCountVisualizer_Infos,
			Messages.ProblemCountVisualizer_Warnings, Messages.ProblemCountVisualizer_Errors, };

	public ProblemVisualizer() {
		super(Messages.ProblemCountVisualizer_Name, Messages.ProblemCountVisualizer_DisplayName,
				Messages.ProblemCountVisualizer_Description);
	}

	@Override
	public GraphicCanvas createCanvas(Composite parent) {
		m_canvas = new ResizableGraphicCanvas(this, parent);
		return m_canvas;
	}

	@Override
	protected void initializeCanvas(GraphicCanvas canvas) {
		m_canvas.setBackground(MAIN_BACKGROUND_COLOR);
		m_canvas.setForeground(MAIN_FOREGROUND_COLOR);
	}

	@Override
	public void disposeCanvas() {
		if (m_canvas != null) {
			m_canvas.dispose();
			m_canvas = null;
		}
	}

	@Override
	public void visualizerDeselected() {
	}

	@Override
	public void visualizerSelected() {
	}

	/**
	 * Actually create the graphics bars for the different severities.
	 * @param outline Should the bars be created, or the bar outline
	 * @return The bars to be drawn.
	 */
	private void createBars() {
		BarGraphicObject bar;
		// Graphic container that will contain the bars
		m_container = new VirtualBoundsGraphicObject();
		m_container.setVirtualBounds(BAR_CONTAINER_BOUNDS);
		// no need to draw the ontainer's bounds
		m_container.setDrawContainerBounds(false);

		// The inside of the bars use a proportional width with the maximum width and
		// the largest amount of markers for one severity.
		// Find the maximum marker count to dictate the width
		int maxCount = Math.max(m_markerCount[0], m_markerCount[1]);
		maxCount = Math.max(maxCount, m_markerCount[2]);
		if (maxCount == 0)
			maxCount = 1; // Set to anything but 0.  It will be multiplied by 0 and not matter.

		// go from high severity to low
		for (int severity = IMarker.SEVERITY_ERROR; severity >= IMarker.SEVERITY_INFO; severity--) {
			float barPercent = m_markerCount[severity] / (float) maxCount * 100.0f;
			bar = new BarGraphicObject(severity, Math.round(barPercent));
			bar.setVirtualBounds(BARS_VIRTUAL_BOUNDS[severity]);
			bar.setLabel(m_markerSeverityLabels[severity] + " " + m_markerCount[severity]); //$NON-NLS-1$
			m_container.addChildObject("bar" + severity, bar); //$NON-NLS-1$
		}

		// set real bounds on parent "container" object - real bounds of
		// bars will be recursively computed in proportion of their virtual
		// bounds, relative to their container
		m_container.setBounds(m_canvas.getBounds());
		// Add container object to canvas - when canvas draws the container,
		// the bars will automatically be drawn too, so no need to add them
		// to canvas.
		m_canvas.add(m_container);
	}

	/**
	 * Clear the marker count array.
	 */
	private void clearMarkerCount() {
		m_markerCount[IMarker.SEVERITY_ERROR] = 0;
		m_markerCount[IMarker.SEVERITY_WARNING] = 0;
		m_markerCount[IMarker.SEVERITY_INFO] = 0;
	}

	/**
	 * Add the count of problem markers for each severity for the
	 * specified resource.
	 */
	private void addToMarkerCount(IResource resource) {
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
					int severity = (Integer) attrValue;
					m_markerCount[severity]++;
				}
			} catch (CoreException e) {
			}
		}
	}

	/**
	 * Refresh the visualizer display based on the existing data.
	 */
	public void refresh() {
		m_canvas.clear();

		// First create the outline bars
		createBars();
		m_canvas.add(m_container);

		m_canvas.redraw();
	}

	@Override
	public int handlesSelection(ISelection selection) {
		List<Object> selections = SelectionUtils.getSelectedObjects(selection);

		// As long as we support at least one element of the selection
		// that is good enough
		for (Object sel : selections) {
			if (sel instanceof IResource) {
				return 2;
			}
		}

		return 0;
	}

	@Override
	public void workbenchSelectionChanged(ISelection selection) {
		clearMarkerCount();

		List<Object> selections = SelectionUtils.getSelectedObjects(selection);

		for (Object sel : selections) {
			if (sel instanceof IResource) {
				// Update the data
				addToMarkerCount((IResource) sel);
			}
		}

		refresh();
	}

	public SelectionManager getSelectionManager() {
		return m_selectionManager;
	}
}
