/*******************************************************************************
 * Copyright (c) 2002, 2008 QNX Software Systems and others.
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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DragSourceEvent;

/**
 * CDTViewerDragAdapter
 */
public class CDTViewerDragAdapter extends DelegatingDragAdapter {

	private StructuredViewer fViewer;

	public CDTViewerDragAdapter(StructuredViewer viewer, TransferDragSourceListener[] listeners) {
		super(listeners);
		Assert.isNotNull(viewer);
		fViewer = viewer;
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
		if (selection.isEmpty()) {
			event.doit = false;
			return;
		}
		super.dragStart(event);
	}

}
