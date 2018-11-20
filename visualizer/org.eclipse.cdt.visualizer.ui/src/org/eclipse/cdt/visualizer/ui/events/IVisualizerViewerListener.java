/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
public interface IVisualizerViewerListener {
	// --- methods ---

	/** Invoked when VisualizerViewer's selected IVisualizer changes. */
	public void visualizerEvent(IVisualizerViewer source, VisualizerViewerEvent event);

}
