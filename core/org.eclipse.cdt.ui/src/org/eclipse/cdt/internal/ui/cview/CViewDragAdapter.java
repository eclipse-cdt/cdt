package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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

/**
 * Implements drag behaviour when items are dragged out of the
 * resource navigator.
 */
class CViewDragAdapter extends DragSourceAdapter {
	ISelectionProvider selectionProvider;

	/**
	 * Invoked when an action occurs. 
	 * Argument context is the Window which contains the UI from which this action was fired.
	 * This default implementation prints the name of this class and its label.
	 * @see IAction#run
	 */
	public void dragFinished(DragSourceEvent event) {
		if (event.doit && event.detail == DND.DROP_MOVE) {
			//delete the old elements
			final int typeMask = IResource.FOLDER | IResource.FILE;
			IResource[] resources = getSelectedResources(typeMask);
			if (resources == null)
				return;
			for (int i = 0; i < resources.length; i++) {
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
		final int typeMask = IResource.FILE | IResource.FOLDER;
		IResource[] resources = getSelectedResources(typeMask);
		if (resources == null || resources.length == 0)
			return;

		//use resource transfer if possible
		if (ResourceTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = resources;
			return;
		}
	
		//resort to a file transfer
		if (!FileTransfer.getInstance().isSupportedType(event.dataType))
			return;

		// Get the path of each file and set as the drag data
		final int len = resources.length;
		String[] fileNames = new String[len];
		for (int i = 0, length = len; i < length; i++) {
			fileNames[i] = resources[i].getLocation().toOSString();
		}
		event.data = fileNames;
	}

	/**
	 * All selection must be files or folders.
	 */
	public void dragStart(DragSourceEvent event) {

		// Workaround for 1GEUS9V
		DragSource dragSource = (DragSource)event.widget;
		Control control = dragSource.getControl();
		if (control != control.getDisplay().getFocusControl()){
			event.doit = false;
			return;
		}
	
		IStructuredSelection selection = (IStructuredSelection)selectionProvider.getSelection();
		for (Iterator i = selection.iterator(); i.hasNext();) {
			Object next = i.next();
			IResource res = null;
			if (next instanceof ICElement) {
				ICElement celement = (ICElement)next;
				try {
					res = celement.getResource();
				} catch (CModelException e) {
				}
			}
			if (res == null) {
				event.doit = false;
				return;
			}
		}
		event.doit = true;
	}

	protected IResource[] getSelectedResources(int resourceTypes) {
		List resources = new ArrayList();
		IResource[] result = new IResource[0];

		ISelection selection = selectionProvider.getSelection();
		if (!(selection instanceof IStructuredSelection) || selection.isEmpty()) {
			return null;
		}
		IStructuredSelection structuredSelection = (IStructuredSelection)selection;
		if (structuredSelection == null)
			return null;

		// loop through list and look for matching items
		Iterator enum = structuredSelection.iterator();
		while (enum.hasNext()) {
			Object obj = enum.next();
			if (obj instanceof ICElement) {
				try {
					IResource res = ((ICElement)obj).getUnderlyingResource();
					if (res != null) {
						if ((res.getType() & resourceTypes) == res.getType()) {
							resources.add(res);
						}
					}
				} catch (CModelException e) {
				}
			}
		}
		result = new IResource[resources.size()];
		resources.toArray(result);
		return result;
	}

	/**
	 * CViewDragAction constructor comment.
	 */
	public CViewDragAdapter(ISelectionProvider provider) {
		selectionProvider = provider;
	}
}
