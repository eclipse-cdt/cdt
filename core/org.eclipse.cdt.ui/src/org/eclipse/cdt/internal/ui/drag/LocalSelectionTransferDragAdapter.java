/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.drag;

import org.eclipse.cdt.ui.CLocalSelectionTransfer;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;

public class LocalSelectionTransferDragAdapter implements TransferDragSourceListener {
	private final ISelectionProvider provider;
	private final CLocalSelectionTransfer transfer;

	public LocalSelectionTransferDragAdapter(ISelectionProvider provider) {
		super();
		this.provider = provider;
		this.transfer = CLocalSelectionTransfer.getInstance();
		Assert.isNotNull(provider);
		Assert.isNotNull(transfer);
	}

	/* (non-Javadoc)
	 * @see DragSourceListener#dragStart
	 */
	public void dragStart(DragSourceEvent event) {
		transfer.setSelection(provider.getSelection());

		event.doit = true;
	}

	/* (non-Javadoc)
	 * @see DragSourceListener#dragSetData
	 */
	public void dragSetData(DragSourceEvent event) {
		event.data = transfer.isSupportedType(event.dataType) ? transfer.getSelection() : null;
	}

	/* (non-Javadoc)
	 * @see DragSourceListener#dragFinished
	 */
	public void dragFinished(DragSourceEvent event) {
		transfer.setSelection(null);
	}

	/* (non-Javadoc)
	 * @see TransferDragSourceListener#getTransfer
	 */
	public Transfer getTransfer() {
		return transfer;
	}
}
