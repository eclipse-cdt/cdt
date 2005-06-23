/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dnd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.ReadOnlyStateChecker;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * A drag adapter that transfers the current selection as </code>
 * IResource</code>. Only those elements in the selection are part 
 * of the transfer which can be converted into an <code>IResource
 * </code>.
 */
public class ResourceTransferDragAdapter implements TransferDragSourceListener {
	private final ISelectionProvider provider;

	/**
	 * Creates a new ResourceTransferDragAdapter for the given selection provider.
	 *
	 * @param provider the selection provider to access the viewer's selection
	 */
	public ResourceTransferDragAdapter(ISelectionProvider provider) {
		super();
		this.provider = provider;
		Assert.isNotNull(provider);
	}

	public Transfer getTransfer() {
		return ResourceTransfer.getInstance();
	}

	public void dragStart(DragSourceEvent event) {
		event.doit = getSelectedResources().length > 0;
	}

	public void dragSetData(DragSourceEvent event) {
		event.data = getSelectedResources();
	}

	public void dragFinished(DragSourceEvent event) {
		if (event.doit && event.detail == DND.DROP_MOVE) {
			IResource[] resources = getSelectedResources();

			if (resources.length == 0)
				return;

			DragSource dragSource = (DragSource) event.widget;
			Control control = dragSource.getControl();
			Shell shell = control.getShell();
			String title = CUIMessages.getString("Drag.move.problem.title"); //$NON-NLS-1$
			String message = CUIMessages.getString("Drag.move.problem.message"); //$NON-NLS-1$

			ReadOnlyStateChecker checker = new ReadOnlyStateChecker(shell, title, message);

			resources = checker.checkReadOnlyResources(resources);

			// delete the old elements
			for (int i = 0; i < resources.length; ++i) {
				try {
					resources[i].delete(IResource.KEEP_HISTORY | IResource.FORCE, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private IResource[] getSelectedResources() {
		List resources = Collections.EMPTY_LIST;
		ISelection selection = provider.getSelection();

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection) selection;

			resources = new ArrayList(structured.size());

			for (Iterator iterator = structured.iterator(); iterator.hasNext();) {
				Object element = iterator.next();
				IResource resource = null;
				if (element instanceof IResource) {
					resource = (IResource)element;
				} else if (element instanceof IAdaptable) {
					IAdaptable adaptable = (IAdaptable) element;
					resource = (IResource) adaptable.getAdapter(IResource.class);
				}
				if (resource != null) {
					resources.add(resource);
				}
			}
		}

		IResource[] result = new IResource[resources.size()];
		resources.toArray(result);

		return result;
	}

}
