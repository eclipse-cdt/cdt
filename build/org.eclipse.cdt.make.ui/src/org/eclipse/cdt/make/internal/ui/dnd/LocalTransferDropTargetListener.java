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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;

/**
 * {@code LocalTransferDropTargetListener} supports dropping of dragged selection to
 * Make Target View. {@link LocalSelectionTransfer} is used as a transfer agent.
 *
 * @see AbstractContainerAreaDropAdapter
 * @see org.eclipse.swt.dnd.DropTargetListener
 */
public class LocalTransferDropTargetListener extends AbstractContainerAreaDropAdapter {

	Viewer fViewer;

	/**
	 * Constructor setting a viewer such as TreeViewer.
	 *
	 * @param viewer - to provide shell for UI interaction.
	 */
	public LocalTransferDropTargetListener(Viewer viewer) {
		fViewer = viewer;
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
	 * Get selection from {@link LocalSelectionTransfer}.
	 *
	 * @return selection as {@link IStructuredSelection}.
	 */
	private IStructuredSelection getSelection() {
		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
		if (selection instanceof IStructuredSelection) {
			return (IStructuredSelection) selection;
		}
		return null;
	}

	/**
	 * Initial drag operation. Adjusted to be the least of user initiated
	 * operation and best supported operation for a given selection. The
	 * operation will be indicated by mouse cursor.
	 *
	 * @param operation - incoming operation.
	 * @return changed operation.
	 */
	@Override
	public int dragEnterOperation(int operation) {
		int bestOperation = determineBestOperation(getSelection(), null);
		if (bestOperation > operation) {
			bestOperation = operation;
		}
		return bestOperation;
	}

	/**
	 * Operation on dragging over target . Adjusted to be the least of user
	 * initiated operation and best supported operation for a given selection
	 * considering drop container. The operation will be indicated by mouse
	 * cursor. Note that drop on itself is not allowed here.
	 *
	 * @param operation - incoming operation.
	 * @param dropContainer - container where drop is going to be.
	 * @return changed operation.
	 */
	@Override
	public int dragOverOperation(int operation, IContainer dropContainer, Object dropTarget) {
		int bestOperation = DND.DROP_NONE;
		IStructuredSelection selection = getSelection();
		if (dropContainer != null && selection != null && !selection.toList().contains(dropTarget)) {
			bestOperation = determineBestOperation(selection, dropContainer);
			if (bestOperation > operation) {
				bestOperation = operation;
			}
		}
		return bestOperation;
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
		if (dropObject instanceof IStructuredSelection && dropContainer != null) {
			IMakeTarget[] makeTargets = prepareMakeTargetsFromSelection((IStructuredSelection)dropObject, dropContainer);
			MakeTargetDndUtil.copyTargets(makeTargets, dropContainer, operation, fViewer.getControl().getShell());
		}
	}

	/**
	 * Check if this item is a file or convertible to file.
	 *
	 * @param element - an item to examine.
	 * @return true if convertible to file.
	 */
	private static boolean isConvertibleToFile(Object element) {
		if (element instanceof IAdaptable) {
			IAdaptable a = (IAdaptable)element;
			if (a.getAdapter(IFile.class)!=null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculate which operations are available for dropping the selection to
	 * the target. {@code IMakeTarget} can be moved but files can only be
	 * copied. The selection should contain only {@code IMakeTarget} or items
	 * convertible to files.
	 *
	 * @param selection - data being dragged.
	 * @param dropContainer - container where drop is targeted.
	 *
	 * @return the most advanced operation available. The return must be one of
	 *         {@link org.eclipse.swt.dnd.DND} operations.
	 *
	 * @see DND#DROP_NONE
	 * @see DND#DROP_COPY
	 * @see DND#DROP_MOVE
	 * @see DND#DROP_LINK
	 */
	private int determineBestOperation(IStructuredSelection selection, IContainer dropContainer) {
		int bestOperation = DND.DROP_NONE;
		if (selection != null) {
			List<?> items = selection.toList();
			for (Object item : items) {
				if (item instanceof IMakeTarget) {
					// Looking for a target which is not being moving to itself
					IContainer container = ((IMakeTarget)item).getContainer();
					// dropContainer==null means disregard the container
					if (dropContainer==null || !dropContainer.equals(container)) {
						if (bestOperation < DND.DROP_MOVE) {
							bestOperation = DND.DROP_MOVE;
						}
					} else if (dropContainer.equals(container)) {
						// Allow to copy/duplicate targets into the same folder
						if (bestOperation < DND.DROP_COPY) {
							bestOperation = DND.DROP_COPY;
						}
					}
				} else if (isConvertibleToFile(item)) {
					// Files can be copied
					if (bestOperation < DND.DROP_COPY) {
						bestOperation = DND.DROP_COPY;
					}
				} else {
					// if any item is not drop-able selection is not drop-able
					bestOperation = DND.DROP_NONE;
					break;
				}
			}
		}
		return bestOperation;
	}

	/**
	 * Provide the list of make targets made out of selected elements. This method assumes
	 *  {@code IMakeTarget} or items adaptable to files in the selection.
	 *
	 * @param selection - selected items.
	 * @param dropContainer - container where make targets will belong to.
	 * @return an array of {@code IMakeTarget}s.
	 */
	private static IMakeTarget[] prepareMakeTargetsFromSelection(IStructuredSelection selection, IContainer dropContainer) {
		List<?> elements = selection.toList();
		List<IMakeTarget> makeTargetsList= new ArrayList<IMakeTarget>(elements.size());
		for (Object element : elements) {
			if (element instanceof IMakeTarget) {
				makeTargetsList.add((IMakeTarget)element);
				continue;
			} else if (isConvertibleToFile(element)) {
				IAdaptable a = (IAdaptable)element;
				IFile file = (IFile)a.getAdapter(IFile.class);
				String fileName = file.getName();
				String fileLocation = file.getLocation().toString();

				if (fileName!=null) {
					try {
						String buildCommand = MakeTargetDndUtil.getProjectBuildCommand(dropContainer.getProject());
						IMakeTarget makeTarget = MakeTargetDndUtil.createMakeTarget(fileName, fileLocation,
								buildCommand, dropContainer);
						makeTargetsList.add(makeTarget);
					} catch (CoreException e) {
						// log any problem then ignore it
						MakeUIPlugin.log(e);
					}
				}
			}
		}
		if (makeTargetsList.size()>0) {
			return makeTargetsList.toArray(new IMakeTarget[makeTargetsList.size()]);
		}
		return null;
	}

}
