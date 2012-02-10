/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.events;

import org.eclipse.cdt.visualizer.ui.IVisualizerViewer;


// ---------------------------------------------------------------------------
// IVisualizerViewerListener
// ---------------------------------------------------------------------------

/**
 * IVisualizerViewer event listener.
 */
public interface IVisualizerViewerListener
{
	// --- methods ---
	
	/** Invoked when VisualizerViewer's selected IVisualizer changes. */
	public void visualizerEvent(IVisualizerViewer source, VisualizerViewerEvent event);
	
}
