package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.ReadOnlyStateChecker;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * Implements drag behaviour when items are dragged out of the
 * resource navigator.
 */
class CViewDragAdapter extends DragSourceAdapter {
	private final ISelectionProvider selectionProvider;
	private static final int typeMask = IResource.FOLDER | IResource.FILE;
	private TransferData lastDataType; 	
	private static final String CHECK_MOVE_TITLE = "Drag and Drop Problem"; //$NON-NLS-1$
	private static final String CHECK_DELETE_MESSAGE = "{0} is read only. Do you still wish to delete it?"; //$NON-NLS-1$


	/**
	 * Invoked when an action occurs. 
	 * Argument context is the Window which contains the UI from which this action was fired.
	 * This default implementation prints the name of this class and its label.
	 * @see DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragFinished(DragSourceEvent event) {
		LocalSelectionTransfer.getInstance().setSelection(null);

		if (event.doit == false)
			return;

		if (event.detail == DND.DROP_MOVE) {
			//never delete resources when dragging outside Eclipse. 
			//workaround for bug 30543.
			if (lastDataType != null && FileTransfer.getInstance().isSupportedType(lastDataType))
				return;
			
			IResource[] resources = getSelectedResources();	
			DragSource dragSource = (DragSource) event.widget;
			Control control = dragSource.getControl();
			Shell shell = control.getShell();
			ReadOnlyStateChecker checker;
			
			if (resources == null || resources.length == 0)
				return;
			
			checker = new ReadOnlyStateChecker(shell, CHECK_MOVE_TITLE, CHECK_DELETE_MESSAGE);
			resources = checker.checkReadOnlyResources(resources);		
			//delete the old elements
			for (int i = 0; i < resources.length; i++) {
				try {
					resources[i].delete(IResource.KEEP_HISTORY | IResource.FORCE, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		} else if (event.detail == DND.DROP_TARGET_MOVE) {
			IResource[] resources = getSelectedResources();

			// file moved for us by OS, no need to delete the resources, just
			// update the view
			if (resources == null)
				return;
			for (int i = 0; i < resources.length; i++) {
				try {
					resources[i].refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * @see DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragSetData(DragSourceEvent event) {
		IResource[] resources = getSelectedResources();
		
		if (resources == null || resources.length == 0)
			return;

		lastDataType = event.dataType;
		//use local selection transfer if possible
		if (LocalSelectionTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = LocalSelectionTransfer.getInstance().getSelection();
			return;
		}
		//use resource transfer if possible
		if (ResourceTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = resources;
			return;
		}
		//resort to a file transfer
		if (!FileTransfer.getInstance().isSupportedType(event.dataType))
			return;

		// Get the path of each file and set as the drag data
		final int length = resources.length;
		int actualLength = 0; 
		String[] fileNames = new String[length];
		for (int i = 0; i < length; i++) {
			IPath location = resources[i].getLocation();
			// location may be null. See bug 29491.
			if (location != null) 
				fileNames[actualLength++] = location.toOSString();
		}
		if (actualLength == 0)
			return;
		// was one or more of the locations null?
		if (actualLength < length) {
			String[] tempFileNames = fileNames;
			fileNames = new String[actualLength];
			for (int i = 0; i < actualLength; i++)
				fileNames[i] = tempFileNames[i];
		}
		event.data = fileNames;
	}

	/*
	 * @see DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragStart(DragSourceEvent event) {
		lastDataType = null;
		// Workaround for 1GEUS9V
		DragSource dragSource = (DragSource) event.widget;
		Control control = dragSource.getControl();
		if (control != control.getDisplay().getFocusControl()) {
			event.doit = false;
			return;
		}

		IStructuredSelection selection =
			(IStructuredSelection) selectionProvider.getSelection();
		for (Iterator i = selection.iterator(); i.hasNext();) {
			Object next = i.next();
			if (next instanceof IAdaptable) {
				next = ((IAdaptable) next).getAdapter(IResource.class);
			}
			if (!(next instanceof IFile || next instanceof IFolder)) {
				event.doit = false;
				return;
			}
		}
		if (selection.isEmpty()) {
			event.doit = false;
			return;
		}
		LocalSelectionTransfer.getInstance().setSelection(selection);
		event.doit = true;
	}
	


	private IResource[] getSelectedResources() {
		ISelection selection = selectionProvider.getSelection();
		List resources = new ArrayList();

		// Sanity checks
		if (selection == null || !(selection instanceof IStructuredSelection) || selection.isEmpty()) {
			return null;
		}

		IStructuredSelection structuredSelection = (IStructuredSelection) selection;

		// loop through list and look for matching items
		for (Iterator enum = structuredSelection.iterator(); enum.hasNext();) {
			Object object = enum.next();
			IResource resource = null;

			if (object instanceof IResource) {
				resource = (IResource) object;
			} else if (object instanceof IAdaptable) {
				resource = (IResource) ((IAdaptable) object).getAdapter(IResource.class);
			}
			if (resource != null && (resource.getType() & typeMask) != 0) {
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
