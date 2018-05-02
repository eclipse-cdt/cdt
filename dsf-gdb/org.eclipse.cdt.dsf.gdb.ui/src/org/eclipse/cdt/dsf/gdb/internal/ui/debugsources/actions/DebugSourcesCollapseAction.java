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
 * Baha El-Kassaby - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.actions;

import java.util.Optional;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.DebugSourcesMessages;
import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.IDebugSourcesImagesConst;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Action to collapse of the nodes of the Debug Sources tree
 */
public class DebugSourcesCollapseAction extends Action {

	private final TreeViewer viewer;

	public DebugSourcesCollapseAction(TreeViewer viewer) {
		super();
		this.viewer = viewer;
		if (viewer == null || viewer.getControl().isDisposed()) {
			setEnabled(false);
		}
		setText(DebugSourcesMessages.DebugSourcesCollapseAction_name);
		setToolTipText(DebugSourcesMessages.DebugSourcesCollapseAction_description);
		Optional<ImageDescriptor> descriptor = ResourceLocator.imageDescriptorFromBundle(GdbUIPlugin.PLUGIN_ID,
				IDebugSourcesImagesConst.IMG_COLLAPSE_DEBUG_SOURCES);
		descriptor.ifPresent(this::setImageDescriptor);
	}

	@Override
	public void run() {
		if (viewer != null) {
			viewer.collapseAll();
		}
	}
}
