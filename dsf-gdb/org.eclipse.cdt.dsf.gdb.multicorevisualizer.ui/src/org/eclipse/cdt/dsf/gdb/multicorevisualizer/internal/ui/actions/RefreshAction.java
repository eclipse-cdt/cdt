/*******************************************************************************
 * Copyright (c) 2012, 2014 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.actions;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view.MulticoreVisualizer;
import org.eclipse.cdt.visualizer.ui.VisualizerAction;
import org.eclipse.swt.SWT;

/** Action that refreshes the Visualizer canvas. */
public class RefreshAction extends VisualizerAction
{
	// --- members ---
	
	/** Visualizer instance we're associated with. */
	MulticoreVisualizer m_visualizer = null;
	
	
	// --- constructors/destructors ---

	/** Constructor. */
	public RefreshAction()
	{
		setText(MulticoreVisualizerUIPlugin.getString(
			"MulticoreVisualizer.actions.Refresh.text")); //$NON-NLS-1$
		setDescription(MulticoreVisualizerUIPlugin.getString(
			"MulticoreVisualizer.actions.Refresh.description")); //$NON-NLS-1$
		setAccelerator(SWT.F5);
	}
	
	/** Dispose method. */
	@Override
	public void dispose()
	{
		m_visualizer = null;
		super.dispose();
	}
	
	
	// --- init methods ---
	
	/** Initializes this action for the specified view. */
	public void init(MulticoreVisualizer visualizer)
	{
		m_visualizer = visualizer;
	}
	
	
	// --- methods ---
	
	/** Invoked when action is triggered. */
	@Override
	public void run() {
		if (m_visualizer != null)
			m_visualizer.refresh();
	}
}
