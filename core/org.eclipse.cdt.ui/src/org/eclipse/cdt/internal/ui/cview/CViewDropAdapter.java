package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CopyResourceAction;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.part.PluginDropAdapter;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
;

/**
 * Implements drop behaviour for drag and drop operations
 * that land on the resource navigator.
 */
class CViewDropAdapter extends PluginDropAdapter implements IOverwriteQuery {
	/**
	 * The time the mouse first started hovering over the current target
	 */
	protected long hoverStart = 0;
	/**
	 * The amount of time to hover over a tree item before expanding it
	 */
	protected static final long hoverThreshold = 1500;

	/**
	 * A flag indicating that the drop has been cancelled by the user.
	 */
	protected boolean isCanceled = false;
	/**
	 * A flag indicating that overwrites should always occur.
	 */
	protected boolean alwaysOverwrite = false;

	/**
	 * Copies or moves the <code>source</code> file to the given <code>
	 * destination</code>.  If overwrite is true, any resource that
	 * already exists at the destination will be deleted before the
	 * copy/move occurs.
	 */
	protected IStatus doCopy(final IResource source, final IPath destination, final boolean overwrite) {
		final boolean copy = getCurrentOperation() == DND.DROP_COPY;
		final IStatus[] result = new IStatus[] { ok()};
		try {
			new ProgressMonitorDialog(getShell()).run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					try {
						if (overwrite) {
							//delete the destination
							IResource oldResource = source.getWorkspace().getRoot().findMember(destination);
							if (oldResource.exists()) {
								oldResource.delete(true, null);
							}
						}
						if (copy) {
							IPath newName = destination;
							if (source.getWorkspace().getRoot().exists(destination))
								newName = CopyResourceAction.getNewNameFor(destination, source.getWorkspace());
							if (newName != null) {
								source.copy(newName, false, monitor);
							}
						} else {
							source.move(destination, false, monitor);
						}
					} catch (CoreException e) {
						result[0] = e.getStatus();
					}
				}
			});
		} catch (InvocationTargetException e) {
			//implementation doesn't throw this
		} catch (InterruptedException e) {
		}
		return result[0];
	}

	/**
	 * Copies the source into the target container.  Returns a status object
	 * indicating success or failure.
	 */
	protected IStatus dragAndDropCopy(IContainer target, IResource source) {
		if (isCanceled) {
			return ok();
		}
		if (getCurrentOperation() != DND.DROP_COPY && (source.equals(target) || source.getParent().equals(target))) {
			return info("Same Source And Destination"); //$NON-NLS-1$
		}
		if (source.getFullPath().isPrefixOf(target.getFullPath())) {
			return error(source, "destination A SubFolder"); //$NON-NLS-1$
		}
		IPath destination = target.getFullPath().append(source.getName());

		IStatus result = doCopy(source, destination, false);
		if (result.getCode() == IResourceStatus.PATH_OCCUPIED) {
			if (alwaysOverwrite) {
				return doCopy(source, destination, true);
			}
			String query = queryOverwrite(destination.toString());
			if (query == YES) {
			   return doCopy(source, destination, true);
			}
			if (query == CANCEL) {
				isCanceled = true;
				return ok();
			}
			if (query == ALL) {
				alwaysOverwrite = true;
				return doCopy(source, destination, true);
			}
			if (query == NO) {
				return ok();
			}
		}
		return result;
	}

	/**
	 * Performs an import of the given file into the provided
	 * container.  Returns a status indicating if the import was successful.
	 */
	protected IStatus dragAndDropImport(IContainer target, String filePath) {
		File toImport = new File(filePath);
		if (target.getLocation().equals(toImport)) {
			return info(("DropAdapter.canNotDropOntoSelf")); //$NON-NLS-1$
		}
		ImportOperation op = 
			new ImportOperation(target.getFullPath(), new File(toImport.getParent()), FileSystemStructureProvider.INSTANCE, this, Arrays.asList(new File[] {toImport})); 
		op.setCreateContainerStructure(false);
		try {
			new ProgressMonitorDialog(getShell()).run(true, true, op);
		} catch (InterruptedException e) {
			return info("Cancelled"); //$NON-NLS-1$
		} catch (InvocationTargetException e) {
			return error("Drop Operation Error", e.getTargetException());  //$NON-NLS-1$
		}
		return op.getStatus();
	}

	/**
	 * The mouse has moved over the drop target.  If the
	 * target item has changed, notify the action and check
	 * that it is still enabled.
	 */
	public void dragOver(DropTargetEvent event) {
		try {
			//this method implements the UI behaviour that when the user hovers 
			//over an unexpanded tree item long enough, it will auto-expand.
			Object oldTarget = getCurrentTarget();
			super.dragOver(event);
			if (oldTarget != getCurrentTarget()) {
				hoverStart = System.currentTimeMillis();
			} else {
				//if we've been hovering over this item awhile, expand it.
				if (hoverStart > 0 && (System.currentTimeMillis() - hoverStart) > hoverThreshold) {
					expandSelection((TreeItem) event.item);
					hoverStart = 0;
				}
			}
		} catch (Throwable t) {
			handleException(t, event);
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
		} else {
			return error("Can Not Move", null); //$NON-NLS-1$
		}
	}

	/**
	 * Expands the selection of the given tree viewer.
	 */
	protected void expandSelection(TreeItem selection) {
		if (selection == null)
			return;
		if (!selection.getExpanded()) {
			TreeViewer treeViewer = (TreeViewer) getViewer();
			treeViewer.expandToLevel(selection.getData(), 1);
		}
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
		} else {
			return new MultiStatus(PlatformUI.PLUGIN_ID, 0, children, message, null);
		}
	}

	/**
	 * Returns an status indicating success.
	 */
	protected IStatus ok() {
		return new Status(Status.OK, PlatformUI.PLUGIN_ID, 0, "Ok", null); //$NON-NLS-1$
	}

	/**
	 * Opens an error dialog if necessary.  Takes care of
	 * complex rules necessary for making the error dialog look nice.
	 */
	protected void openError(IStatus status) {
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
	 * Invoked when an action occurs. 
	 * Argument context is the Window which contains the UI from which this action was fired.
	 * This default implementation prints the name of this class and its label.
	 * @see IAction#run
	 */
	public boolean performDrop(Object data) {
		isCanceled = false;
		alwaysOverwrite = false;
		if (getCurrentTarget() == null || data == null) {
			return false;
		}
		TransferData currentTransfer = getCurrentTransfer();
		if (ResourceTransfer.getInstance().isSupportedType(currentTransfer)) {
			return performResourceDrop(data);
		}
		if (FileTransfer.getInstance().isSupportedType(currentTransfer)) {
			return performFileDrop(data);
		}
		return super.performDrop(data);
	}

	/**
	 * Performs a drop using the FileTransfer transfer type.
	 */
	protected boolean performFileDrop(Object data) {
		MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 0, "Problem Importing", null); //$NON-NLS-1$
		mergeStatus(problems, validateTarget(getCurrentTarget()));

		Object obj = getCurrentTarget();
		IResource res = null;
		if (obj instanceof ICElement) {
			res = ((ICElement)obj).getUnderlyingResource();
		}
		IContainer targetResource = getActualTarget(res);
		String[] names = (String[]) data;
		for (int i = 0; i < names.length; i++) {
			mergeStatus(problems, dragAndDropImport(targetResource, names[i]));
		}
		openError(problems);
		return problems.isOK();
	}

	/**
	 * Performs a drop using the ResourceTransfer transfer type.
	 */
	protected boolean performResourceDrop(Object data) {
		MultiStatus problems = new MultiStatus(PlatformUI.PLUGIN_ID, 1, "Problems Moving", null); //$NON-NLS-1$
		mergeStatus(problems, validateTarget(getCurrentTarget()));

		Object obj = getCurrentTarget();
		IResource res = null;
		if (obj instanceof ICElement) {
			res = ((ICElement)obj).getUnderlyingResource();
		}
		IContainer targetResource = getActualTarget(res);
		IResource[] sources = (IResource[]) data;
		for (int i = 0; i < sources.length; i++) {
			mergeStatus(problems, dragAndDropCopy(targetResource, sources[i]));
		}
		openError(problems);

		//always return false because we don't want the source to clean up
		return false;
	}

	/* (non-Javadoc)
	 * Method declared on IOverWriteQuery
	 */
	public String queryOverwrite(String pathString) {
		final String returnCode[] = {CANCEL};
		final String msg = pathString + " " + CUIPlugin.getResourceString("CViewDragNDrop.txt") ; //$NON-NLS-1$
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
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		if (super.validateDrop(target, operation, transferType)) {
			return true;
		}
		return validateTarget(target).isOK();
	}

	/**
	 * Ensures that the drop target meets certain criteria
	 */
	protected IStatus validateTarget(Object target) {
		if (target instanceof ICElement) {
			IResource r = ((ICElement)target).getUnderlyingResource();
			if (r == null)
				return info("Target Must Be Resource"); //$NON-NLS-1$
			target = r;
		}
		if (!(target instanceof IResource)) {
			return info("Target Must Be Resource"); //$NON-NLS-1$
		}
		IResource resource = (IResource)target;
		if (!resource.isAccessible()) {
			return error("Can Not Drop Into Closed Project"); //$NON-NLS-1$
		}
		IContainer destination = getActualTarget(resource);
		if (destination.getType() == IResource.ROOT) {
			return error("Resources Can Not Be Siblings"); //$NON-NLS-2$
		}
		return ok();
	}
}
