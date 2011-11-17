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
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;

/**
 * {@code MakeTargetTransferDropTargetListener} handles drop of make targets
 * onto Make Target View. {@link MakeTargetTransfer} is used as the transfer agent and
 * provides for drag/drop and copy/paste between different eclipse sessions.
 *
 * @see AbstractContainerAreaDropAdapter
 * @see org.eclipse.swt.dnd.DropTargetListener
 */
public class MakeTargetTransferDropTargetListener extends AbstractContainerAreaDropAdapter {

	private final Viewer fViewer;

	/**
	 * Constructor setting a viewer such as TreeViewer to pull selection from later on.
	 * @param viewer - the viewer providing shell for UI.
	 */
	public MakeTargetTransferDropTargetListener(Viewer viewer) {
		fViewer = viewer;
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
	 * Initial drag operation. Only {@link DND#DROP_COPY} is supported for
	 * dropping files to Make Target View, same as for {@code dragOverOperation}.
	 *
	 * @param operation - incoming operation.
	 * @return changed operation.
	 */
	@Override
	public int dragEnterOperation(int operation) {
		return dragOverOperation(operation, null, null);
	}

	/**
	 * Operation of dragging over a drop target. Only {@link DND#DROP_COPY} is
	 * supported for dropping files to Make Target View.
	 *
	 * @param operation - incoming operation.
	 * @return changed operation.
	 */
	@Override
	public int dragOverOperation(int operation, IContainer dropContainer, Object dropTarget) {
		// This class is intended only for drag/drop between eclipse instances,
		// so DND_COPY always set and we don't bother checking if the target is the source
		if (operation!=DND.DROP_NONE) {
			return DND.DROP_COPY;
		}
		return operation;
	}

	/**
	 * Implementation of the actual drop of {@code dropObject} to {@code dropContainer}.
	 *
	 * @param dropObject - object to drop.
	 * @param dropContainer - container where to drop the object.
	 * @param operation - drop operation.
	 */
	@Override
	public void dropToContainer(Object dropObject, IContainer dropContainer, int operation) {
		if (dropObject instanceof MakeTargetTransferData && dropContainer != null) {
			createTransferTargetsUI((MakeTargetTransferData)dropObject, dropContainer,
				operation, fViewer.getControl().getShell());
		}
	}

	/**
	 * Creates make targets received by {@link MakeTargetTransfer} in Make Target View.
	 *
	 * @param makeTargetTransferData - incoming data.
	 * @param dropContainer - a container where the targets are being created.
	 * @param operation - drop/paste operation.
	 * @param shell - a shell to display warnings to the user.
	 */
	public static void createTransferTargetsUI(MakeTargetTransferData makeTargetTransferData, IContainer dropContainer,
		int operation, Shell shell) {
		IMakeTarget[] makeTargets = makeTargetTransferData.createMakeTargets(dropContainer.getProject());
		MakeTargetDndUtil.copyTargets(makeTargets, dropContainer, operation, shell);
	}

}
