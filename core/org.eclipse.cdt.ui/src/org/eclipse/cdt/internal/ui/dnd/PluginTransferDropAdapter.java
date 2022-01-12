/*******************************************************************************
 * Copyright (c) 2002, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.dnd;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.part.PluginDropAdapter;
import org.eclipse.ui.part.PluginTransfer;

/**
 * PluginTransferDropAdapter
 * Enable DND to support PluginTransfer type.
 * The actual DND operation is a call back to the
 * org.eclipse.ui.dropActions delegate.
 *
 */

public class PluginTransferDropAdapter extends PluginDropAdapter implements TransferDropTargetListener {

	public PluginTransferDropAdapter(StructuredViewer viewer) {
		super(viewer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.dnd.TransferDropTargetListener#getTransfer()
	 */
	@Override
	public Transfer getTransfer() {
		return PluginTransfer.getInstance();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.dnd.TransferDropTargetListener#isEnabled(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	@Override
	public boolean isEnabled(DropTargetEvent event) {
		Object target = event.item != null ? event.item.getData() : null;
		if (target == null) {
			return false;
		}
		return target instanceof ICElement || target instanceof IResource;
	}
}
