package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.MoveFilesAndFoldersOperation;
import org.eclipse.ui.actions.ReadOnlyStateChecker;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.part.PluginDropAdapter;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * Implements drop behaviour for drag and drop operations
 * that land on the resource navigator.
 */
class CViewDropAdapter extends PluginDropAdapter implements IOverwriteQuery {

	/**
	 * A flag indicating that the drop has been cancelled by the user.
	 */
	protected boolean isCanceled = false;
	/**
	 * A flag indicating that overwrites should always occur.
	 */
	protected boolean alwaysOverwrite = false;

	/**
	 * The last valid operation.
	 */
	private int lastValidOperation = DND.DROP_NONE;
	
	/*
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragEnter(DropTargetEvent event) {
	
		if (FileTransfer.getInstance().isSupportedType(event.currentDataType) &&
			event.detail == DND.DROP_DEFAULT) {
			// default to copy when dragging from outside Eclipse. Fixes bug 16308.
			event.detail = DND.DROP_COPY;
		}		
		
		super.dragEnter(event);
	}

	
	public void dropAccept(DropTargetEvent event){
		if (getCurrentOperation() == DND.DROP_MOVE){
			validateMove(event);
		}
	} 

	/**
	 * @param event
	 */
	private void validateMove(DropTargetEvent event) {
		ICElement currentContainer = null;

		Object currentTarget = getCurrentTarget();
		if (currentTarget instanceof ICElement){
			currentContainer =(ICElement) currentTarget;		
		} else {
			return;
		}
		
		if ((!((currentContainer instanceof ICContainer) ||
			  (currentContainer instanceof ICProject))) ||
			  currentContainer.isReadOnly()){
			event.detail = DND.DROP_NONE;
			return;
		} 
		
		ISelection sel = this.getViewer().getSelection();
		if (sel instanceof IStructuredSelection){
			StructuredSelection structSel = (StructuredSelection) sel;
			Iterator iter=structSel.iterator(); 
			while (iter.hasNext()){
				Object tempSelection = iter.next();
				if (tempSelection instanceof ICElement){
					
					if (tempSelection instanceof ICProject){
						event.detail = DND.DROP_NONE;
						break;
					}
					
					ICElement tempElement = (ICElement) tempSelection;
					ICElement tempElementParent = tempElement.getParent();
					
					if (tempElementParent.equals(currentContainer) ||
						tempElement.equals(currentContainer) ||
						tempElement.equals(currentContainer.getParent()) ||
						tempElement.isReadOnly()){
						event.detail = DND.DROP_NONE;
						break;
					}
				}
				else if (tempSelection instanceof IResource){
					
					if (tempSelection instanceof IProject){
						event.detail = DND.DROP_NONE;
						break;
					}
					
				
					IResource tempResource = (IResource) tempSelection;
					IResource tempResourceParent = tempResource.getParent();
					//Apples to apples...
					IResource resourceCurrentContainer = currentContainer.getResource();
				
					if (tempResourceParent.equals(resourceCurrentContainer) ||
						tempResource.equals(resourceCurrentContainer) ||
						tempResource.equals(resourceCurrentContainer.getParent()) ||
						tempResource.isReadOnly()){
						event.detail = DND.DROP_NONE;
						break;
					}
					
				}
			}
		}
		
	}


	/**
	 * Returns an error status with the given info.
	 */
	protected IStatus error(String message, Throwable exception) {
		return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, exception);
	}

	/**
	 * Returns an error status with the given info.
	 */
	protected IStatus error(String message) {
		return error(message, null);
	}

	/**
	 * Returns an error status, indicating why the given source
	 * could not be copied or moved.
	 */
	protected IStatus error(IResource source, String message) {
		if (getCurrentOperation() == DND.DROP_COPY) {
			return error("Can Not Copy", null); //$NON-NLS-1$
		}
		return error("Can Not Move", null); //$NON-NLS-1$
	}

	/**
	 * Returns the actual target of the drop, given the resource
	 * under the mouse.  If the mouse target is a file, then the drop actually 
	 * occurs in its parent.  If the drop location is before or after the
	 * mouse target, the target is also the parent.
	 */
	protected IContainer getActualTarget(IResource mouseTarget) {
		/* if cursor is before or after mouseTarget, set target to parent */
		if (getCurrentLocation() == LOCATION_BEFORE || getCurrentLocation() == LOCATION_AFTER) {
			return mouseTarget.getParent();
		}
		/* if cursor is on a file, return the parent */
		if (mouseTarget.getType() == IResource.FILE) {
			return mouseTarget.getParent();
		}
		/* otherwise the mouseTarget is the real target */
		return (IContainer)mouseTarget;
	}

	/**
	 * Returns the display
	 */
	protected Display getDisplay() {
		return getViewer().getControl().getDisplay();
	}

	/**
	 * Returns the shell
	 */
	protected Shell getShell() {
		return getViewer().getControl().getShell();
	}

	/**
	 * Returns an error status with the given info.
	 */
	protected IStatus info(String message) {
		return new Status(IStatus.INFO, PlatformUI.PLUGIN_ID, 0, message, null);
	}

	/**
	 * CViewDropAction constructor comment.
	 */
	public CViewDropAdapter(StructuredViewer viewer) {
		super(viewer);
	}

	/**
	 * Adds the given status to the list of problems.  Discards
	 * OK statuses.  If the status is a multi-status, only its children
	 * are added.
	 */
	protected void mergeStatus(MultiStatus status, IStatus toMerge) {
		if (!toMerge.isOK()) {
			status.merge(toMerge);
		}
	}

	/**
	 * Creates a status object from the given list of problems.
	 */
	protected IStatus multiStatus(List problems, String message) {
		IStatus[] children = new IStatus[problems.size()];
		problems.toArray(children);
		if (children.length == 1) {
			return children[0];
		}
		return new MultiStatus(PlatformUI.PLUGIN_ID, 0, children, message, null);
	}

	/**
	 * Returns an status indicating success.
	 */
	protected IStatus ok() {
		return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "Ok", null); //$NON-NLS-1$
	}

	/**
	 * Opens an error dialog if necessary.  Takes care of
	 * complex rules necessary for making the error dialog look nice.
	 */
	protected void openError(IStatus status) {
		if (status == null)
			return;

		String genericTitle = "Error"; //$NON-NLS-1$
		int codes = IStatus.ERROR | IStatus.WARNING;
	
		//simple case: one error, not a multistatus
		if (!status.isMultiStatus()) {
			ErrorDialog.openError(getShell(), genericTitle, null, status, codes);
			return;
		}

		//one error, single child of multistatus
		IStatus[] children = status.getChildren();
		if (children.length == 1) {
			ErrorDialog.openError(getShell(), status.getMessage(), null, children[0], codes);
			return;
		}
		//several problems
		ErrorDialog.openError(getShell(), genericTitle, null, status, codes);
	}

	/**
	 * Returns the resource selection from the LocalSelectionTransfer.
	 * 
	 * @return the resource selection from the LocalSelectionTransfer
	 */
	private static final int typeMask = IResource.FOLDER | IResource.FILE;

	private IResource[] getSelectedResources() {
		ISelection selection = LocalSelectionTransfer.getInstance().getSelection();
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
	/**
	 * Invoked when an action occurs. 
	 * Argument context is the Window which contains the UI from which this action was fired.
	 * This default implementation prints the name of this class and its label.
	 */
	public boolean performDrop(final Object data) {
		isCanceled = false;
		alwaysOverwrite = false;
		if (getCurrentTarget() == null || data == null) {
			return false;
		}
		boolean result = false;
		IStatus status = null;
		IResource[] resources = null;
		TransferData currentTransfer = getCurrentTransfer();
		if (LocalSelectionTransfer.getInstance().isSupportedType(currentTransfer)) {
			resources = getSelectedResources();
		} else if (ResourceTransfer.getInstance().isSupportedType(currentTransfer)) {
			resources = (IResource[]) data;
		} else if (FileTransfer.getInstance().isSupportedType(currentTransfer)) {
			status = performFileDrop(data);
			result = status.isOK();
		} else {
			result = super.performDrop(data);
		}
		if (resources != null) {
			if (getCurrentOperation() == DND.DROP_COPY) {
				status = performResourceCopy(getShell(), resources);
			} else {
				status = performResourceMove(resources);
			}
		}
		openError(status);
		
		
		return result;
	}

	/**
	 * Performs a drop using the FileTransfer transfer type.
	 */
	private IStatus performFileDrop(Object data) {
		MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 0, "ProblemI mporting", null); //$NON-NLS-1$
		mergeStatus(problems, validateTarget(getCurrentTarget(), getCurrentTransfer()));

		Object obj = getCurrentTarget();
		IResource res = null;
		if (obj instanceof IAdaptable) {
			res = (IResource)((IAdaptable) obj).getAdapter(IResource.class);
		}
		final IContainer target = getActualTarget(res);
		final String[] names = (String[]) data;
		// Run the import operation asynchronously. 
		// Otherwise the drag source (e.g., Windows Explorer) will be blocked 
		// while the operation executes. Fixes bug 16478.
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				getShell().forceActive();
				CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(getShell());
				operation.copyFiles(names, target);
			}
		});
		return problems;
	}

	/**
	 * Performs a resource copy
	 */
	private IStatus performResourceCopy(Shell shell, IResource[] sources) {
		MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 1, "Problems Moving", null); //$NON-NLS-1$
		mergeStatus(problems, validateTarget(getCurrentTarget(), getCurrentTransfer()));

		Object obj = getCurrentTarget();
		IResource res = null;
		if (obj instanceof IAdaptable) {
			res = (IResource)((IAdaptable) obj).getAdapter(IResource.class);
		}
		IContainer target = getActualTarget(res);
		CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(shell);
		operation.copyResources(sources, target);
		
		return problems;
	}

	/**
	 * Performs a resource move
	 */
	private IStatus performResourceMove(IResource[] sources) {
		MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 1, "Problems Moving", null); //$NON-NLS-1$
		mergeStatus(problems, validateTarget(getCurrentTarget(), getCurrentTransfer()));

		Object obj = getCurrentTarget();
		IResource res = null;
		if (obj instanceof IAdaptable) {
			res = (IResource)((IAdaptable) obj).getAdapter(IResource.class);
		}
		IContainer target = getActualTarget(res);
		ReadOnlyStateChecker checker = new ReadOnlyStateChecker(
			getShell(), 
			"Move Resource Action",			//$NON-NLS-1$
			"Move Resource Action");//$NON-NLS-1$	
		sources = checker.checkReadOnlyResources(sources);
		MoveFilesAndFoldersOperation operation = new MoveFilesAndFoldersOperation(getShell());
		operation.copyResources(sources, target);
		
		return problems;
	}

	/* (non-Javadoc)
	 * Method declared on IOverWriteQuery
	 */
	public String queryOverwrite(String pathString) {
		final String returnCode[] = {CANCEL};
		final String msg = pathString + " " + CUIPlugin.getResourceString("CViewDragNDrop.txt") ; //$NON-NLS-1$ //$NON-NLS-2$
		final String[] options = {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.CANCEL_LABEL};
		getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog dialog = new MessageDialog(getShell(), "Question", null, msg, MessageDialog.QUESTION, options, 0); //$NON-NLS-1$
				dialog.open();
				int returnVal = dialog.getReturnCode();
				String[] returnCodes = {YES, NO, ALL, CANCEL};
				returnCode[0] = returnVal < 0 ? CANCEL : returnCodes[returnVal];
			}
		});
		return returnCode[0];
	}

	/**
	 * This method is used to notify the action that some aspect of
	 * the drop operation has changed.
	 */
	public boolean validateDrop(Object target, int dragOperation, TransferData transferType) {
		if (dragOperation != DND.DROP_NONE) {
			lastValidOperation = dragOperation;
		}

		if (super.validateDrop(target, dragOperation, transferType)) {
			return true;
		}
		return validateTarget(target, transferType).isOK();
	}

	/**
	 * Ensures that the drop target meets certain criteria
	 */
	private IStatus validateTarget(Object target, TransferData transferType) {
	
		if (target instanceof IAdaptable) {
			IResource r = (IResource)((IAdaptable) target).getAdapter(IResource.class);
			if (r == null)
				return info("Target Must Be Resource"); //$NON-NLS-1$
			target = r;
		}

		if (!(target instanceof IResource)) {
			return info("Target Must Be Resource"); //$NON-NLS-1$
		}
		IResource resource = (IResource) target;
		if (!resource.isAccessible()) {
			return error("Can Not Drop Into Closed Project"); //$NON-NLS-1$
		}
		IContainer destination = getActualTarget(resource);
		if (destination.getType() == IResource.ROOT) {
			return error("Resources Can Not Be Siblings"); //$NON-NLS-1$
		}
		
		String message = null;
		// drag within Eclipse?
		if (LocalSelectionTransfer.getInstance().isSupportedType(transferType)) {
			IResource[] selectedResources = getSelectedResources();
			
			if (selectedResources == null)
				message = "Drop Operation Error Other"; //$NON-NLS-1$
			else {
				CopyFilesAndFoldersOperation operation;
				if (lastValidOperation == DND.DROP_COPY) {
					operation = new CopyFilesAndFoldersOperation(getShell());
				}
				else {
					operation = new MoveFilesAndFoldersOperation(getShell());
				}
				message = operation.validateDestination(destination, selectedResources);
			
			}
		} // file import?
		else if (FileTransfer.getInstance().isSupportedType(transferType)) {
			String[] sourceNames = (String[]) FileTransfer.getInstance().nativeToJava(transferType);
			if (sourceNames == null) {
				// source names will be null on Linux. Use empty names to do destination validation.
				// Fixes bug 29778
				sourceNames = new String[0];
			}				
			CopyFilesAndFoldersOperation copyOperation = new CopyFilesAndFoldersOperation(getShell());
			message = copyOperation.validateImportDestination(destination, sourceNames);
		}
	
		if (message != null) {
			return error(message);
		}
		
		return ok();
	}


}
