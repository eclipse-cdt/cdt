/*******************************************************************************
 * Copyright (c) 2018, 2019 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Jonah Graham- Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.actions;

import java.util.Optional;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.DebugSourcesLabelProvider;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.DebugSourcesMessages;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.DebugSourcesTreeContentProvider;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.IDebugSourcesImagesConst;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.TreeViewer;

public class DebugSourcesFlattendedTree extends Action {
	private final TreeViewer viewer;

	public DebugSourcesFlattendedTree(TreeViewer viewer) {
		super(null, IAction.AS_RADIO_BUTTON);
		this.viewer = viewer;
		if (viewer == null || viewer.getControl().isDisposed()) {
			setEnabled(false);
		}
		setText(DebugSourcesMessages.DebugSourcesFlattendedTree_name);
		setToolTipText(DebugSourcesMessages.DebugSourcesFlattendedTree_description);
		Optional<ImageDescriptor> descriptor = ResourceLocator.imageDescriptorFromBundle(GdbUIPlugin.PLUGIN_ID,
				IDebugSourcesImagesConst.IMG_FLAT_LAYOUT);
		descriptor.ifPresent(this::setImageDescriptor);
		if (viewer != null) {
			DebugSourcesTreeContentProvider contentProvider = (DebugSourcesTreeContentProvider) viewer
					.getContentProvider();
			setChecked(contentProvider.isFlattenFoldersWithNoFiles());
		}
	}

	@Override
	public void run() {
		if (isChecked()) {
			DebugSourcesTreeContentProvider contentProvider = (DebugSourcesTreeContentProvider) viewer
					.getContentProvider();
			contentProvider.setFlattenFoldersWithNoFiles(true);
			for (int i = 0; i < viewer.getTree().getColumnCount(); i++) {
				DebugSourcesLabelProvider labelProvider = (DebugSourcesLabelProvider) viewer.getLabelProvider(i);
				labelProvider.setFlattenFoldersWithNoFiles(true);
			}
			viewer.refresh(true);
		}
	}

}