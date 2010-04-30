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
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TransferData;

/**
 * This abstract class provides a frame for {@link DropTargetListener} able to accept a
 * drop into a container area. A container area includes the container itself and
 * the items directly belonging to the container. A drop into the container area is
 * treated as a drop on the container itself. Also, some flexibility for adjusting
 * drop operation is provided.
 *
 * @see org.eclipse.swt.dnd.DropTargetListener
 *
 */
public abstract class AbstractContainerAreaDropAdapter implements TransferDropTargetListener {

	private int originallyRequestedOperation = DND.DROP_NONE;
	private Object lastDragOverTarget = null;
	private int lastDragOverOperation = DND.DROP_NONE;

	/**
	 * This method lets changing initial drag operation. The operation will be
	 * indicated by mouse cursor. This operation is passed in {@code event.detail}
	 * to {@code dragEnter} method.
	 *
	 * @param operation - incoming operation.
	 * @return changed operation. The return must be one of
	 *         {@link org.eclipse.swt.dnd.DND} operations.
	 *
	 * @see DropTargetListener#dragEnter(DropTargetEvent)
	 * @see DND#DROP_NONE
	 * @see DND#DROP_COPY
	 * @see DND#DROP_MOVE
	 * @see DND#DROP_LINK
	 * @see DND#DROP_DEFAULT
	 */
	protected abstract int dragEnterOperation(int operation);

	/**
	 * This method lets adjust drag operation over container area. The operation
	 * will be indicated by mouse cursor. This operation is passed in {@code
	 * event.detail} to {@code dragOver} method.
	 *
	 * @param operation - incoming operation.
	 * @param dropContainer - container where drop is going to be.
	 * @param dropTarget - drop target.
	 * @return changed operation. The return must be one of
	 *         {@link org.eclipse.swt.dnd.DND} operations.
	 *
	 *
	 * @see DropTargetListener#dragOver(DropTargetEvent)
	 * @see DND#DROP_NONE
	 * @see DND#DROP_COPY
	 * @see DND#DROP_MOVE
	 * @see DND#DROP_LINK
	 * @see DND#DROP_DEFAULT
	 */
	protected abstract int dragOverOperation(int operation, IContainer dropContainer, Object dropTarget);

	/**
	 * Implementation of the actual drop of {@code dropObject} to {@code dropContainer}.
	 *
	 * @param dropObject - object to drop.
	 * @param dropContainer - container where to drop the object.
	 * @param operation - drop operation.
	 *
	 * @see DropTargetListener#drop(DropTargetEvent)
	 */
	protected abstract void dropToContainer(Object dropObject, IContainer dropContainer,
		int operation);

	/**
	 * Defines if a transfer data is supported by the implementer.
	 *
	 * @see org.eclipse.swt.dnd.Transfer#isSupportedType
	 *
	 * @param transferData - data type to examine
	 * @return whether the given data type is supported by this listener
	 */
	public boolean isSupportedType(TransferData transferData) {
		return getTransfer().isSupportedType(transferData);
	}

	/**
	 * This function is called only the listener is able to handle provided data type.
	 * By default {@code isEnabled} returns {@code true} but subclasses are free to
	 * override.
	 *
	 * @param event - the information associated with the drag event.
	 * @return {@code true}
	 *
	 * @see DropTargetEvent
	 */
	public boolean isEnabled(DropTargetEvent event) {
		return true;
	}

	/**
	 * Implementation of {@code DropTargetListener#dragEnter} to support
	 * dropping into container area.
	 *
	 * @param event - the information associated with the drag event
	 *
	 * @see DropTargetEvent
	 */
	public void dragEnter(DropTargetEvent event) {
		lastDragOverTarget = null;
		lastDragOverOperation = DND.DROP_NONE;

		if (isSupportedType(event.currentDataType)) {
			originallyRequestedOperation = event.detail;
			event.detail = dragEnterOperation(originallyRequestedOperation);
		} else {
			event.detail = DND.DROP_NONE;
			originallyRequestedOperation = DND.DROP_NONE;
		}
	}

	/**
	 * Implementation of {@code DropTargetListener#dragOperationChanged} to support
	 * dropping into container area.
	 *
	 * @param event - the information associated with the drag event
	 *
	 * @see DropTargetEvent
	 */
	public void dragOperationChanged(DropTargetEvent event) {
		originallyRequestedOperation = event.detail;
		event.detail = dragOverOperationCached(originallyRequestedOperation,
			determineDropContainer(event), determineDropTarget(event));
	}

	/**
	 * Implementation of {@code DropTargetListener#dragOver} to support
	 * dropping into container area.
	 *
	 * @param event - the information associated with the drag event
	 *
	 * @see DropTargetEvent
	 */
	public void dragOver(DropTargetEvent event) {
		event.detail = dragOverOperationCached(originallyRequestedOperation,
			determineDropContainer(event), determineDropTarget(event));

		if (originallyRequestedOperation != DND.DROP_NONE) {
			// let user discover items even if event.detail is DND.DROP_NONE
			// since the original operation could be applicable to other items
			event.feedback = DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND;
			if (event.detail != DND.DROP_NONE) {
				event.feedback = event.feedback | DND.FEEDBACK_SELECT;
			}
		} else {
			event.feedback = DND.FEEDBACK_NONE;
		}
	}

	/**
	 * Implementation of {@code DropTargetListener#dragLeave} to support
	 * dropping into container area.
	 *
	 * @param event - the information associated with the drag event
	 *
	 * @see DropTargetEvent
	 */
	public void dragLeave(DropTargetEvent event) {
		// no action
	}

	/**
	 * Implementation of {@code DropTargetListener#dropAccept} to support
	 * dropping into container area.
	 *
	 * @param event - the information associated with the drag event
	 *
	 * @see DropTargetEvent
	 */
	public void dropAccept(DropTargetEvent event) {
		// no action
	}

	/**
	 * Implementation of {@code DropTargetListener#drop} to support
	 * dropping into container area.
	 *
	 * @param event - the information associated with the drag event
	 *
	 * @see DropTargetEvent
	 */
	public void drop(DropTargetEvent event) {
		IContainer dropContainer = determineDropContainer(event);
		if (dropContainer != null) {
			event.detail = dragOverOperationCached(event.detail, dropContainer, determineDropTarget(event));
			dropToContainer(event.data, dropContainer, event.detail);
		} else {
			event.detail = DND.DROP_NONE;
		}
	}

	/**
	 * This method provides caching of potentially long running and called on each
	 * mouse move {@link #dragOverOperation}.
	 *
	 * @param operation - incoming operation.
	 * @param dropContainer - container where drop is going to be.
	 * @param dropTarget - drop target.
	 * @return changed operation. The return must be one of
	 *         org.eclipse.swt.dnd.DND operations such as {@link DND#DROP_NONE},
	 *         {@link DND#DROP_COPY}, {@link DND#DROP_MOVE},
	 *         {@link DND#DROP_LINK}
	 *
	 *
	 * @see DropTargetListener#dragOver(DropTargetEvent)
	 */
	private int dragOverOperationCached(int operation, IContainer dropContainer, Object dropTarget) {
		if (dropTarget != lastDragOverTarget || operation != lastDragOverOperation) {
			lastDragOverOperation = dragOverOperation(operation, dropContainer, dropTarget);
			lastDragOverTarget = dropTarget;
		}
		return lastDragOverOperation;
	}

	/**
	 * Returns the drop target passed by {@code event.item}.
	 *
	 * @param event - the information associated with the drop event
	 */
	private static Object determineDropTarget(DropTargetEvent event) {
		return event.item == null ? null : event.item.getData();
	}

	/**
	 * Find which container area the mouse cursor is currently in.
	 *
	 * @param event - the information associated with the drop event
	 * @return the container of the current mouse location.
	 */
	public static IContainer determineDropContainer(DropTargetEvent event) {
		Object dropTarget = determineDropTarget(event);
		if (dropTarget instanceof IMakeTarget) {
			return ((IMakeTarget) dropTarget).getContainer();
		} else if (dropTarget instanceof IContainer) {
			return (IContainer) dropTarget;
		}
		return null;
	}

}
