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

import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvas;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class ResizableGraphicCanvas extends GraphicCanvas {
	private final ProblemVisualizer m_problemVisualizer;

	public ResizableGraphicCanvas(ProblemVisualizer problemVisualizer, Composite parent) {
		super(parent);
		m_problemVisualizer = problemVisualizer;
	}
	
	@Override
	public void resized(Rectangle bounds) {
		m_problemVisualizer.refresh();
	}
}