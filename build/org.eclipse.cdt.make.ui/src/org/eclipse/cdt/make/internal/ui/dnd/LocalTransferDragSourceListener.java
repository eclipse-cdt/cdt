/*******************************************************************************
 * Copyright (c) 2008, 2009 Andrew Gvozdev.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev (Quoin Inc.) - Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.dnd;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.dnd.Transfer;

/**
 * {@code LocalTransferDragSourceListener} supports dragging of selected objects from
 * Make Target View. {@link LocalSelectionTransfer} is used as a transfer agent.
 *
 * @see AbstractSelectionDragAdapter
 * @see org.eclipse.swt.dnd.DragSourceListener
 */
public class LocalTransferDragSourceListener extends AbstractSelectionDragAdapter {

	/**
	 * Constructor setting selection provider.
	 * @param provider - selection provider.
	 */
	public LocalTransferDragSourceListener(ISelectionProvider provider) {
		super(provider);
	}


	/**
	 * @return the {@link Transfer} type that this listener can accept a
	 * drop operation for.
	 */
	@Override
	public Transfer getTransfer() {
		return LocalSelectionTransfer.getTransfer();
	}

	/**
	 * Checks if the elements contained in the given selection can be dragged.
	 *
	 * @param selection - the selected elements to be dragged.
	 * @return {@code true} if the selection can be dragged.
	 */
	@Override
	public boolean isDragable(ISelection selection) {
		return MakeTargetDndUtil.isDragable(selection);
	}

	/**
	 * Pick the selected elements to be passed by {@link LocalSelectionTransfer}.
	 *
	 * @param selection - the selected elements to be dragged.
	 */
	@Override
	public void dragInit(ISelection selection) {
		LocalSelectionTransfer.getTransfer().setSelection(selection);
	}

	/**
	 * Provide the selection to attach to the drag event.
	 *
	 * @param selection - not used. The selection is taken from {@link LocalSelectionTransfer}.
	 * @return data to be passed.
	 */
	@Override
	public Object prepareDataForTransfer(ISelection selection) {
		return LocalSelectionTransfer.getTransfer().getSelection();
	}

	/**
	 * Cleanup upon the completion of the drop.
	 */
	@Override
	public void dragDone() {
		LocalSelectionTransfer.getTransfer().setSelection(null);
		LocalSelectionTransfer.getTransfer().setSelectionSetTime(0);
	}
}
