/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.drag;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Control;

/**
 * A delegating drag adapter negotiates between a set of <code>TransferDragSourceListener</code>s
 * On <code>dragStart</code> the adapter determines the listener to be used for any further
 * <code>drag*</code> callbacks.
 */
public class DelegatingDragAdapter implements DragSourceListener {
	private final ISelectionProvider provider;
	private final TransferDragSourceListener[] listeners;
	private final boolean[] actives;
	private TransferDragSourceListener selected;

	public DelegatingDragAdapter(
		ISelectionProvider provider,
		TransferDragSourceListener[] listeners) {
		super();
		Assert.isNotNull(provider);
		Assert.isNotNull(listeners);
		this.provider = provider;
		this.listeners = listeners;
		this.actives = new boolean[listeners.length];
		this.selected = null;
	}

	/* non Java-doc
	 * @see DragSourceListener
	 */
	public void dragStart(DragSourceEvent event) {
		selected = null;

		IStructuredSelection selection = (IStructuredSelection) provider.getSelection();

		if (selection.isEmpty()) {
			event.doit = false;
			return;
		}

		for (Iterator i = selection.iterator(); i.hasNext();) {
			Object next = i.next();
			IResource res = null;
			if (next instanceof IResource) {
				res = (IResource)next;
			} else if (next instanceof IAdaptable) {
				res = (IResource)((IAdaptable)next).getAdapter(IResource.class);
			}
			if (!(res instanceof IFile || res instanceof IFolder)) {
					event.doit = false;
					return;
			}
		}

		// Workaround for 1GEUS9V
		final DragSource dragSource = (DragSource) event.widget;
		final Control control = dragSource.getControl();

		if (control != control.getDisplay().getFocusControl()) {
			event.doit = false;
			return;
		}

		final Object saveData = event.data;
		final boolean saveDoit = event.doit;
		final int listenerCount = listeners.length;

		int transferCount = 0;

		for (int i = 0; i < listenerCount; ++i) {
			TransferDragSourceListener listener = listeners[i];

			event.data = saveData;
			event.doit = saveDoit;

			listener.dragStart(event);

			if (actives[i] = event.doit)
				++transferCount;
		}

		event.data = saveData;

		if (event.doit = (transferCount != 0)) {
			Transfer[] transferArray = new Transfer[transferCount];

			for (int i = listenerCount; --i >= 0;)
				if (actives[i])
					transferArray[--transferCount] = listeners[i].getTransfer();

			dragSource.setTransfer(transferArray);
		}
	}

	/* non Java-doc
	 * @see DragSourceListener
	 */
	public void dragSetData(DragSourceEvent event) {
		selected = getListener(event.dataType);

		if (selected != null)
			selected.dragSetData(event);
	}

	/* non Java-doc
	 * @see DragSourceListener
	 */
	public void dragFinished(DragSourceEvent event) {
		try {
			// If the user presses Escape then we get a dragFinished
			// without getting a dragSetData before.
			if (selected == null)
				selected = getListener(event.dataType);

			if (selected != null)
				selected.dragFinished(event);
		} finally {
			Arrays.fill(actives, false);
			selected = null;
		}
	}

	private TransferDragSourceListener getListener(TransferData type) {
		if (type != null) {
			for (int i = 0; i < actives.length; ++i) {
				if (actives[i]) {
					TransferDragSourceListener listener = listeners[i];

					if (listener.getTransfer().isSupportedType(type))
						return listener;
				}
			}
		}

		return null;
	}
}
