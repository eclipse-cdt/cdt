package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * Implements drag behaviour when items are dragged out of the
 * resource navigator.
 */
class CViewDragAdapter extends DragSourceAdapter {
	private final ISelectionProvider selectionProvider;

	/**
	 * Invoked when an action occurs. 
	 * Argument context is the Window which contains the UI from which this action was fired.
	 * This default implementation prints the name of this class and its label.
	 * @see IAction#run
	 */
	public void dragFinished(DragSourceEvent event) {
		if (event.doit && event.detail == DND.DROP_MOVE) {
			// delete the old elements

			IResource[] resources = getSelectedResources();

			for (int i = 0; i < resources.length; ++i) {
				try {
					resources[i].delete(true, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Returns the data to be transferred in a drag and drop
	 * operation.
	 */
	public void dragSetData(DragSourceEvent event) {
		if (ResourceTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = getSelectedResources();
		} else if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
			// get the path of each file and set as the drag data
			IResource[] resources = getSelectedResources();
			int length = resources.length;
			String[] fileNames = new String[length];

			for (int i = 0; i < length; ++i)
				fileNames[i] = resources[i].getLocation().toOSString();

			event.data = fileNames;
		} else if (LocalSelectionTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = LocalSelectionTransfer.getInstance().getSelection();
		}
	}

	/**
	 * All selection must be files or folders.
	 */
	public void dragStart(DragSourceEvent event) {
		// Workaround for 1GEUS9V
		DragSource dragSource = (DragSource) event.widget;
		Control control = dragSource.getControl();

		if (control != control.getDisplay().getFocusControl())
			event.doit = false;
		else
			LocalSelectionTransfer.getInstance().setSelection(selectionProvider.getSelection());
	}

	private static final int typeMask = IResource.FOLDER | IResource.FILE;

	private IResource[] getSelectedResources() {
		ISelection selection = selectionProvider.getSelection();
		List resources = new ArrayList();

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			// loop through list and look for matching items
			for (Iterator enum = structuredSelection.iterator(); enum.hasNext();) {
				Object object = enum.next();
				IResource resource = null;

				if (object instanceof IResource)
					resource = (IResource) object;
				else if (object instanceof IAdaptable)
					resource = (IResource) ((IAdaptable) object).getAdapter(IResource.class);

				if (resource != null && (resource.getType() & typeMask) != 0)
					resources.add(resource);
			}
		}

		IResource[] result = new IResource[resources.size()];
		resources.toArray(result);

		return result;
	}

	/**
	 * CViewDragAction constructor.
	 */
	public CViewDragAdapter(ISelectionProvider provider) {
		super();
		selectionProvider = provider;
	}
}
