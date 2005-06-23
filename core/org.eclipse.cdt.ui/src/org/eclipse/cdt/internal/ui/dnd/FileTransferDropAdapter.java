/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.dnd;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;

/**
 * FileTransferDropAdapter
 */
public class FileTransferDropAdapter extends CDTViewerDropAdapter implements TransferDropTargetListener {

	public FileTransferDropAdapter(AbstractTreeViewer viewer) {
		super(viewer, DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND);
	}

	//---- TransferDropTargetListener interface ---------------------------------------
	
	public Transfer getTransfer() {
		return FileTransfer.getInstance();
	}
	
	public boolean isEnabled(DropTargetEvent event) {
		Object target= event.item != null ? event.item.getData() : null;
		if (target == null) {
			return false;
		}
		return target instanceof ICElement || target instanceof IResource;
	}

	//---- Actual DND -----------------------------------------------------------------
	
	public void validateDrop(Object target, DropTargetEvent event, int operation) {
		event.detail= DND.DROP_NONE;
		boolean isContainer = false;
		if (target instanceof IContainer) {
			isContainer = true;
		} else if (target instanceof IAdaptable) {
			target = ((IAdaptable)target).getAdapter(IResource.class);
			isContainer = target instanceof IContainer;
		}
		if (isContainer) {
			IContainer container= (IContainer)target;
			if (container.isAccessible()) {
				ResourceAttributes attributes = container.getResourceAttributes();
				if (attributes != null && !attributes.isReadOnly()) {
					event.detail= DND.DROP_COPY;
				}
			}
		}
	}

	public void drop(Object dropTarget, final DropTargetEvent event) {
		int operation= event.detail;
		
		event.detail= DND.DROP_NONE;
		final Object data= event.data;
		if (data == null || !(data instanceof String[]) || operation != DND.DROP_COPY)
			return;
		
		final IContainer target= getActualTarget(dropTarget);
		if (target == null)
			return;
		
		// Run the import operation asynchronously. 
		// Otherwise the drag source (e.g., Windows Explorer) will be blocked 
		// while the operation executes. Fixes bug 35796.
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				getShell().forceActive();
				new CopyFilesAndFoldersOperation(getShell()).copyFiles((String[]) data, target);
				// Import always performs a copy.
				event.detail= DND.DROP_COPY;
			}
		});
	}
	
	private IContainer getActualTarget(Object dropTarget) {
		if (dropTarget instanceof IContainer) {
			return (IContainer)dropTarget;
		} else if (dropTarget instanceof ICElement) {
			return getActualTarget(((ICElement)dropTarget).getResource());
		}
		return null;
	}
	
	Shell getShell() {
		return getViewer().getControl().getShell();
	}

}
