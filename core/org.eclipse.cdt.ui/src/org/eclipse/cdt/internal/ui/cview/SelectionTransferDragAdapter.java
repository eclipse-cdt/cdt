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

package org.eclipse.cdt.internal.ui.cview;

import java.util.Iterator;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.ui.dnd.BasicSelectionTransferDragAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

public class SelectionTransferDragAdapter extends BasicSelectionTransferDragAdapter {

	public SelectionTransferDragAdapter(ISelectionProvider provider) {
		super(provider);
	}

	@Override
	protected boolean isDragable(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> iter = ((IStructuredSelection) selection).iterator(); iter.hasNext();) {
				Object element = iter.next();
				if (element instanceof ICElement) {
					if (!(element instanceof ISourceReference)) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}
}
