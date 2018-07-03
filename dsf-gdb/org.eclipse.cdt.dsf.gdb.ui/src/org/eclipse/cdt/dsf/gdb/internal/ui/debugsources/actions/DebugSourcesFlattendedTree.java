/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Jonah Graham- Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.actions;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.DebugSourcesLabelProvider;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.DebugSourcesMessages;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.DebugSourcesTreeContentProvider;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.IDebugSourcesImagesConst;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;

public class DebugSourcesFlattendedTree extends Action {
	private final TreeViewer viewer;
	private TreeViewerColumn[] viewerColumns;

	public DebugSourcesFlattendedTree(TreeViewer viewer, TreeViewerColumn[] viewerColumns) {
		super();
		this.viewer = viewer;
		this.viewerColumns = viewerColumns;
		if (viewer == null || viewer.getControl().isDisposed()) {
			setEnabled(false);
		}
		setText(DebugSourcesMessages.DebugSourcesFlattendedTree_name);
		setToolTipText(DebugSourcesMessages.DebugSourcesFlattendedTree_description);
		setImageDescriptor(GdbUIPlugin.imageDescriptorFromPlugin(GdbUIPlugin.PLUGIN_ID, IDebugSourcesImagesConst.IMG_FLAT_LAYOUT));
	}

	@Override
	public void run() {
		DebugSourcesTreeContentProvider contentProvider = (DebugSourcesTreeContentProvider)viewer.getContentProvider();
		contentProvider.setFlattenFoldersWithNoFiles(true);
		for (int i = 0; i < viewerColumns.length; i++) {
			viewerColumns[i].setLabelProvider(DebugSourcesLabelProvider.FLATTENED[i]);
		}
		viewer.refresh(true);
		viewer.expandAll();
	}

}