/*******************************************************************************
 * Copyright (c) 2012, 2014 Tilera Corporation and others.
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
 *     Xavier Raynaud <xavier.raynaud@kalray.eu> - fix #428424
 *******************************************************************************/

package org.eclipse.cdt.visualizer.examples.sourcegraph;

import org.eclipse.cdt.visualizer.examples.VisualizerExamplesPlugin;
import org.eclipse.cdt.visualizer.ui.Visualizer;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.ScrollPanel;
import org.eclipse.cdt.visualizer.ui.util.SelectionManager;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

//---------------------------------------------------------------------------
// SourceGraphVisualizer
//---------------------------------------------------------------------------

public class SourceGraphVisualizer extends Visualizer {
	// --- constants ---

	/** Eclipse ID for this view */
	public static final String ECLIPSE_ID = "org.eclipse.cdt.visualizer.examples.sourcegraph"; //$NON-NLS-1$

	// --- members ---

	/** ScrollPanel container for visualizer control. */
	ScrollPanel m_scrollPanel = null;

	/** visualizer control (downcast reference) */
	SourceGraphControl m_sourceGraphControl = null;

	// --- constructors/destructors ---

	/** Constructor. */
	public SourceGraphVisualizer() {
		super(VisualizerExamplesPlugin.getString("SourceGraphVisualizer.name"), //$NON-NLS-1$
				VisualizerExamplesPlugin.getString("SourceGraphVisualizer.displayName"), //$NON-NLS-1$
				VisualizerExamplesPlugin.getString("SourceGraphVisualizer.description") //$NON-NLS-1$
		);
	}

	/** Dispose method. */
	@Override
	public void dispose() {
		super.dispose();
	}

	// --- control management ---

	/** Creates and returns visualizer control on specified parent. */
	@Override
	public Control createControl(Composite parent) {
		if (m_sourceGraphControl == null) {

			m_scrollPanel = new ScrollPanel(parent);
			m_scrollPanel.setBackground(Colors.BLACK);

			m_sourceGraphControl = new SourceGraphControl(m_scrollPanel);
			m_scrollPanel.setContent(m_sourceGraphControl);

			// source graph control sets its own height based on the graph size
			m_scrollPanel.setAutoResizeHeight(false);
			// auto-resize to fit scrollpanel width
			m_scrollPanel.setAutoResizeWidth(true);

			setControl(m_scrollPanel);
		}
		return getControl();
	}

	/** Invoked when visualizer control should be disposed. */
	@Override
	public void disposeControl() {
		if (m_sourceGraphControl != null) {
			setControl(null);
			m_sourceGraphControl.dispose();
			m_scrollPanel.dispose();
			m_sourceGraphControl = null;
			m_scrollPanel = null;
		}
	}

	// --- visualizer events ---

	@Override
	public void visualizerDeselected() {
	}

	@Override
	public void visualizerSelected() {
	}

	// --- update methods ---

	/**
	 * Refresh the visualizer display based on the existing data.
	 */
	public void refresh() {
		m_sourceGraphControl.refresh();
	}

	// --- selection handling ---

	/** Invoked when selection changes, to determine whether this
	 *  visualizer knows how to display the current selection.
	 */
	@Override
	public int handlesSelection(ISelection selection) {
		Object s = SelectionUtils.getSelectedObject(selection);
		if (s instanceof TextSelection)
			return 1;
		return 0;
	}

	/** Invoked when workbench selection changes and this visualizer
	 *  is selected to display it.
	 */
	@Override
	public void workbenchSelectionChanged(ISelection selection) {
		Object o = SelectionUtils.getSelectedObject(selection);
		if (o instanceof TextSelection) {
			String text = ((TextSelection) o).getText();
			m_sourceGraphControl.setSourceText(text);
		} else {
			m_sourceGraphControl.setSourceText(""); //$NON-NLS-1$
		}
	}

	public SelectionManager getSelectionManager() {
		return m_selectionManager;
	}

	// --- menu/toolbar management ---

	/** Invoked when visualizer is selected, to populate the toolbar. */
	@Override
	public void populateToolBar(IToolBarManager toolBarManager) {
	}

	/** Invoked when visualizer is selected, to populate the toolbar's menu. */
	@Override
	public void populateMenu(IMenuManager menuManager) {
	}

	// --- context menu handling ---

	/** Invoked when visualizer view's context menu is invoked, to populate it. */
	@Override
	public void populateContextMenu(IMenuManager menuManager) {
	}

}
