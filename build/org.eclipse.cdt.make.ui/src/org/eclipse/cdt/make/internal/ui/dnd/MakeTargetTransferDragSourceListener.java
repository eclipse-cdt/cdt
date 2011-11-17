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

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

/**
 * {@code MakeTargetTransferDragSourceListener} supports dragging of selected
 * make targets from Make Target View. {@link TextTransfer} is used as the
 * transfer agent and provides for drag/drop and copy/paste between different
 * eclipse sessions.
 *
 * @see AbstractSelectionDragAdapter
 * @see org.eclipse.swt.dnd.DragSourceListener
 */
public class MakeTargetTransferDragSourceListener extends AbstractSelectionDragAdapter {

	/**
	 * Constructor setting selection provider.
	 * @param provider - selection provider.
	 */
	public MakeTargetTransferDragSourceListener(ISelectionProvider provider) {
		super(provider);
	}

	/**
	 * @return the {@link Transfer} type that this listener can accept a
	 * drop operation for.
	 */
	@Override
	public Transfer getTransfer() {
		return MakeTargetTransfer.getInstance();
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
	 * A custom action executed during drag initialization.
	 *
	 * @param selection - the selected elements to be dragged.
	 */
	@Override
	public void dragInit(ISelection selection) {
		// no special action is required
	}

	/**
	 * Prepare the selection to be passed via drag and drop actions.
	 *
	 * @param selection - the selected elements to be dragged.
	 * @return selected make targets converted to {@link MakeTargetTransferData}
	 *         in order to be transfered.
	 */
	@Override
	public Object prepareDataForTransfer(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			MakeTargetTransferData makeTargetTransferData = new MakeTargetTransferData();
			for (Object selectionItem : ((IStructuredSelection)selection).toList()) {
				if (selectionItem instanceof IMakeTarget) {
					makeTargetTransferData.addMakeTarget((IMakeTarget)selectionItem);
				}
			}
			return makeTargetTransferData;
		}
		return null;
	}

	/**
	 * A custom action to finish the drag.
	 */
	@Override
	public void dragDone() {
		// no special action is required
	}

}
