/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.ui.dnd;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.MoveFilesAndFoldersOperation;
import org.eclipse.ui.actions.ReadOnlyStateChecker;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * ResourceTransferDropAdapter
 */
public class ResourceTransferDropAdapter extends CDTViewerDropAdapter implements TransferDropTargetListener {

	/**
	 * @param viewer
	 */
	public ResourceTransferDropAdapter(StructuredViewer viewer) {
		super(viewer, DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.drag.TransferDropTargetListener#getTransfer()
	 */
	public Transfer getTransfer() {
		return ResourceTransfer.getInstance();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.drag.TransferDropTargetListener#isEnabled(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public boolean isEnabled(DropTargetEvent event) {
		Object target= event.item != null ? event.item.getData() : null;
		if (target == null) {
			return false;
		}
		return target instanceof ICElement || target instanceof IResource;
	}

	public void validateDrop(Object target, DropTargetEvent event, int op) {
		IContainer destination = getDestination(target);
		if (destination == null) {
			event.detail = DND.DROP_NONE;
		} else {
			IResource[] selectedResources = getSelectedResources();
			if (selectedResources.length == 0) {
				event.detail = DND.DROP_NONE;
			} else {
				if (op == DND.DROP_COPY) {
					CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(getShell());
					if (operation.validateDestination(destination, selectedResources) == null) {
						event.detail = op;
					}
				} else {
					MoveFilesAndFoldersOperation operation = new MoveFilesAndFoldersOperation(getShell());
					if (operation.validateDestination(destination, selectedResources) == null) {
						event.detail = op;
					}
				}
			}
		}
	}

	public void drop(Object dropTarget, final DropTargetEvent event) {
		int op= event.detail;
		
		event.detail= DND.DROP_NONE;
		final Object data= event.data;
		if (data == null || !(data instanceof IResource[]))
			return;
		
		final IContainer target= getDestination(dropTarget);
		if (target == null) {
			return;
		}
		IResource[] sources = (IResource[])data;
		if (op == DND.DROP_COPY) {
			CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(getShell());
			operation.copyResources(sources, target);
		} else  {
			ReadOnlyStateChecker checker = new ReadOnlyStateChecker(
				getShell(), 
				"Move Resource Action",	//$NON-NLS-1$
				"Move Resource Action");//$NON-NLS-1$	
			sources = checker.checkReadOnlyResources(sources);
			MoveFilesAndFoldersOperation operation = new MoveFilesAndFoldersOperation(getShell());
			operation.copyResources(sources, target);
		}
	}

	private IContainer getDestination(Object dropTarget) {
		if (dropTarget instanceof IContainer) {
			return (IContainer)dropTarget;
		} else if (dropTarget instanceof ICElement) {
			return getDestination(((ICElement)dropTarget).getResource());
		}
		return null;
	}

	/**
	 * Returns the resource selection from the LocalSelectionTransfer.
	 * 
	 * @return the resource selection from the LocalSelectionTransfer
	 */
	private IResource[] getSelectedResources() {
		ArrayList selectedResources = new ArrayList();
		
		ISelection selection = LocalSelectionTransfer.getInstance()
		.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			for (Iterator i = ssel.iterator(); i.hasNext();) {
				Object o = i.next();
				if (o instanceof IResource) {
					selectedResources.add(o);
				}
				else if (o instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) o;
					IResource r = (IResource) a.getAdapter(IResource.class);
					if (r != null) {
						selectedResources.add(r);
					}
				}
			}
		}
		return (IResource[]) selectedResources.toArray(new IResource[selectedResources.size()]);
	}

	/**
	 * Returns the shell
	 */
	protected Shell getShell() {
		return getViewer().getControl().getShell();
	}

}
