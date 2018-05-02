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

public class DebugSourcesShowExistingFilesOnly extends Action {
	private final TreeViewer viewer;

	public DebugSourcesShowExistingFilesOnly(TreeViewer viewer) {
		super(null, IAction.AS_CHECK_BOX);
		this.viewer = viewer;
		if (viewer == null || viewer.getControl().isDisposed()) {
			setEnabled(false);
		}
		setText(DebugSourcesMessages.DebugSourcesShowExistingFilesOnly_name);
		setToolTipText(DebugSourcesMessages.DebugSourcesShowExistingFilesOnly_description);
		Optional<ImageDescriptor> descriptor = ResourceLocator.imageDescriptorFromBundle(GdbUIPlugin.PLUGIN_ID,
				IDebugSourcesImagesConst.IMG_SHOW_EXISTING_FILES_ONLY);
		descriptor.ifPresent(this::setImageDescriptor);
		if (viewer != null) {
			DebugSourcesTreeContentProvider contentProvider = (DebugSourcesTreeContentProvider) viewer
					.getContentProvider();
			setChecked(contentProvider.isShowExistingFilesOnly());
		}
	}

	@Override
	public void run() {
		DebugSourcesTreeContentProvider contentProvider = (DebugSourcesTreeContentProvider) viewer.getContentProvider();
		boolean showExistingFilesOnly = contentProvider.isShowExistingFilesOnly();
		showExistingFilesOnly = !showExistingFilesOnly;
		contentProvider.setShowExistingFilesOnly(showExistingFilesOnly);

		for (int i = 0; i < viewer.getTree().getColumnCount(); i++) {
			DebugSourcesLabelProvider labelProvider = (DebugSourcesLabelProvider) viewer.getLabelProvider(i);
			labelProvider.setShowExistingFilesOnly(showExistingFilesOnly);
		}

		setChecked(showExistingFilesOnly);
		viewer.refresh(true);
	}

}