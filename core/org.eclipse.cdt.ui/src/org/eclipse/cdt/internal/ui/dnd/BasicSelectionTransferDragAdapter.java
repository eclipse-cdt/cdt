/*******************************************************************************
 * Copyright (c) 2002, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX Software Systems - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.dnd;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;

public class BasicSelectionTransferDragAdapter extends DragSourceAdapter implements TransferDragSourceListener {
	
	private ISelectionProvider fProvider;
	
	public BasicSelectionTransferDragAdapter(ISelectionProvider provider) {
		Assert.isNotNull(provider);
		fProvider= provider;
	}

	/**
	 * @see TransferDragSourceListener#getTransfer
	 */
	public Transfer getTransfer() {
		return LocalSelectionTransfer.getTransfer();
	}
	
	/* non Java-doc
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart
	 */
	@Override
	public void dragStart(DragSourceEvent event) {
		ISelection selection= fProvider.getSelection();
		LocalSelectionTransfer.getTransfer().setSelection(selection);
		LocalSelectionTransfer.getTransfer().setSelectionSetTime(event.time & 0xFFFFFFFFL);
		event.doit= isDragable(selection);
	}
	
	/**
	 * Checks if the elements contained in the given selection can
	 * be dragged.
	 * <p>
	 * Subclasses may override.
	 * 
	 * @param selection containing the elements to be dragged
	 */
	protected boolean isDragable(ISelection selection) {
		return true;
	}


	/* non Java-doc
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData
	 */		
	@Override
	public void dragSetData(DragSourceEvent event) {
		// For consistency set the data to the selection even though
		// the selection is provided by the LocalSelectionTransfer
		// to the drop target adapter.
		event.data= LocalSelectionTransfer.getTransfer().getSelection();
	}


	/* non Java-doc
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished
	 */	
	@Override
	public void dragFinished(DragSourceEvent event) {
		// Make sure we don't have to do any remaining work
		Assert.isTrue(event.detail != DND.DROP_MOVE);
		LocalSelectionTransfer.getTransfer().setSelection(null);
		LocalSelectionTransfer.getTransfer().setSelectionSetTime(0);
	}	
}
