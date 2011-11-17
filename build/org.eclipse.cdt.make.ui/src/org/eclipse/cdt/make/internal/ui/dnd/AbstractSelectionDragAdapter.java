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

import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TransferData;

/**
 * This abstract class provides a convenience frame for DragSourceListener to support
 * dragging of selected items in a view.
 * <p>
 * There is similar implementation in plugin {@code org.eclipse.cdt.ui} -
 * {@code org.eclipse.cdt.internal.ui.dnd.BasicSelectionTransferDragAdapter}.
 *
 * @see org.eclipse.swt.dnd.DropTargetListener
 *
 */
public abstract class AbstractSelectionDragAdapter implements TransferDragSourceListener {
	private final ISelectionProvider fProvider;

	/**
	 * Constructor setting selection provider.
	 * @param provider - selection provider.
	 */
	public AbstractSelectionDragAdapter(ISelectionProvider provider) {
		fProvider= provider;
	}

	/**
	 * Checks if the elements contained in the given selection can be dragged.
	 * If {@code false} {@link #dragStart} method won't initiate the drag.
	 *
	 * @param selection - the selected elements to be dragged.
	 * @return {@code true} if the selection can be dragged.
	 */
	protected abstract boolean isDragable(ISelection selection);

	/**
	 * A custom action executed during drag initialization. Executed from
	 * {@link #dragStart}.
	 *
	 * @param selection - the selected elements to be dragged.
	 */
	protected abstract void dragInit(ISelection selection);

	/**
	 * Prepare the selection to be passed via {@code Transfer} agent. Used by
	 * {@link #dragSetData}.
	 *
	 * @param selection - the selected elements to be dragged.
	 * @return data to be passed. Can be any type the specific transfer supports.
	 */
	protected abstract Object prepareDataForTransfer(ISelection selection);

	/**
	 * A custom action to gracefully finish the drag. Executed by {@link #dragFinished}.
	 */
	protected abstract void dragDone();

	/**
	 * Defines if a transfer data is supported by the implementer.
	 *
	 * @see org.eclipse.swt.dnd.Transfer#isSupportedType
	 *
	 * @param transferData - data type to examine
	 * @return {@code true} if the given data type is supported by this listener
	 */
	public boolean isSupportedType(TransferData transferData) {
		return getTransfer().isSupportedType(transferData);
	}

	/**
	 * Start of the action to drag the widget.
	 *
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart
	 *
	 * @param event the information associated with the drag event
	 */
	@Override
	public void dragStart(DragSourceEvent event) {
		ISelection selection= fProvider.getSelection();
		if (isDragable(selection)) {
			dragInit(selection);
		} else {
			dragDone();
			event.doit = false;
		}
	}


	/**
	 * Attach to the event the data from the drag source.
	 *
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData
	 * @see org.eclipse.swt.dnd.DragSourceEvent
	 *
	 * @param event the information associated with the drag event
	 */
	@Override
	public void dragSetData(DragSourceEvent event) {
		// Define data type so a listener could examine it with isSupportedType().
		// The selection is not passed using event by the LocalSelectionTransfer internally.
		if (isSupportedType(event.dataType)) {
			event.data= prepareDataForTransfer(fProvider.getSelection());
		}
	}


	/**
	 * The function is called upon the completion of the drop. Cleanup is performed.
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished
	 *
	 * @param event the information associated with the drag event
	 */
	@Override
	public void dragFinished(DragSourceEvent event) {
		dragDone();
		event.doit = false;
	}
}
