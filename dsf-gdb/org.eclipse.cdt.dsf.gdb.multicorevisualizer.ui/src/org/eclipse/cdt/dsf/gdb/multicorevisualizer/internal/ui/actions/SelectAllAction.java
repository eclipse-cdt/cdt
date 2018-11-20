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
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.MulticoreVisualizer;
import org.eclipse.cdt.visualizer.ui.VisualizerAction;
import org.eclipse.swt.SWT;

/** Select All action for Visualizer context menu. */
public class SelectAllAction extends VisualizerAction {
	// --- members ---

	/** Visualizer instance we're associated with. */
	MulticoreVisualizer m_visualizer = null;

	// --- constructors/destructors ---

	/** Constructor. */
	public SelectAllAction() {
		setText(MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SelectAll.text")); //$NON-NLS-1$
		setDescription(MulticoreVisualizerUIPlugin.getString("MulticoreVisualizer.actions.SelectAll.description")); //$NON-NLS-1$
		setAccelerator(SWT.CTRL + 'A');
	}

	/** Dispose method. */
	@Override
	public void dispose() {
		m_visualizer = null;
		super.dispose();
	}

	// --- init methods ---

	/** Initializes this action for the specified view. */
	public void init(MulticoreVisualizer visualizer) {
		m_visualizer = visualizer;
	}

	// --- methods ---

	/** Invoked when action is triggered. */
	@Override
	public void run() {
		if (m_visualizer != null)
			m_visualizer.selectAll();
	}
}
