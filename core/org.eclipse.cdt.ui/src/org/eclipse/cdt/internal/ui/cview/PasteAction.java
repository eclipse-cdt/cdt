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
package org.eclipse.cdt.internal.ui.cview;

import java.util.List;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.actions.CopyProjectOperation;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Standard action for pasting resources on the clipboard to the selected resource's location.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
/*package*/
public class PasteAction extends SelectionListenerAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".PasteAction"; //$NON-NLS-1$

	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;

	/**
	 * System clipboard
	 */
	Clipboard clipboard;

	/**
	 * Creates a new action.
	 *
	 * @param shell the shell for any dialogs
	 */
	public PasteAction(Shell shell, Clipboard clipboard) {
		super(CViewMessages.getString("PasteAction.title")); //$NON-NLS-1$
		Assert.isNotNull(shell);
		Assert.isNotNull(clipboard);
		this.shell = shell;
		this.clipboard = clipboard;
		setToolTipText(CViewMessages.getString("PasteAction.toolTip")); //$NON-NLS-1$
		setId(PasteAction.ID);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ICHelpContextIds.PASTE_ACTION);
	}
	/**
	 * Returns the actual target of the paste action. Returns null
	 * if no valid target is selected.
	 * 
	 * @return the actual target of the paste action
	 */
	private IResource getTarget() {
		List selectedResources = getSelectedResources();

		for (int i = 0; i < selectedResources.size(); i++) {
			IResource resource = (IResource) selectedResources.get(i);

			if (resource instanceof IProject && !((IProject) resource).isOpen())
				return null;
			if (resource.getType() == IResource.FILE)
				resource = resource.getParent();
			if (resource != null)
				return resource;
		}
		return null;
	}
	/**
	 * Returns whether any of the given resources are linked resources.
	 * 
	 * @param resources resource to check for linked type. may be null
	 * @return true=one or more resources are linked. false=none of the 
	 * 	resources are linked
	 */
	private boolean isLinked(IResource[] resources) {
		if (resources != null) {
			for (int i = 0; i < resources.length; i++) {
				if (resources[i].isLinked()) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Implementation of method defined on <code>IAction</code>.
	 */
	public void run() {
		// try a resource transfer
		ResourceTransfer resTransfer = ResourceTransfer.getInstance();
		IResource[] resourceData = (IResource[]) clipboard.getContents(resTransfer);

		if (resourceData != null && resourceData.length > 0) {
			if (resourceData[0].getType() == IResource.PROJECT) {
				// enablement checks for all projects
				for (int i = 0; i < resourceData.length; i++) {
					CopyProjectOperation operation = new CopyProjectOperation(this.shell);
					operation.copyProject((IProject) resourceData[i]);
				}
			} else {
				// enablement should ensure that we always have access to a container
				IContainer container = getContainer();

				CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(this.shell);
				operation.copyResources(resourceData, container);
			}
			return;
		}

		// try a file transfer
		FileTransfer fileTransfer = FileTransfer.getInstance();
		String[] fileData = (String[]) clipboard.getContents(fileTransfer);

		if (fileData != null) {
			// enablement should ensure that we always have access to a container
			IContainer container = getContainer();

			CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(this.shell);
			operation.copyFiles(fileData, container);
		}
	}
	/**
	 * Returns the container to hold the pasted resources.
	 */
	private IContainer getContainer() {
		List selection = getSelectedResources();
		if (selection.get(0) instanceof IFile)
			return ((IFile) selection.get(0)).getParent();
		return (IContainer) selection.get(0);
	}
	/**
	 * The <code>PasteAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method enables this action if 
	 * a resource compatible with what is on the clipboard is selected.
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!super.updateSelection(selection))
			return false;

		final IResource[][] clipboardData = new IResource[1][];
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				// clipboard must have resources or files
				ResourceTransfer resTransfer = ResourceTransfer.getInstance();
				clipboardData[0] = (IResource[])clipboard.getContents(resTransfer);
			}
		});
		IResource[] resourceData = clipboardData[0];
		boolean isProjectRes = resourceData != null
			&& resourceData.length > 0
			&& resourceData[0].getType() == IResource.PROJECT;
                                                                                                                             
		if (isProjectRes) {
			for (int i = 0; i < resourceData.length; i++) {
				// make sure all resource data are open projects
				// can paste open projects regardless of selection
				if (resourceData[i].getType() != IResource.PROJECT || ((IProject) resourceData[i]).isOpen() == false)
					return false;
			}
			return true;
		}

		// can paste files and folders to a single selection (project must be open)
		// or multiple file selection with the same parent
		if (getSelectedNonResources().size() > 0)
			return false;

		// targetResource is null if no valid target is selected or 
		// selection is empty	
		IResource targetResource = getTarget();
		if (targetResource == null)
			return false;

		// can paste files and folders to a single selection (file, folder,
		// open project) or multiple file selection with the same parent
		List selectedResources = getSelectedResources();
		if (selectedResources.size() > 1) {
			// if more than one resource is selected the selection has 
			// to be all files with the same parent
			for (int i = 0; i < selectedResources.size(); i++) {
				IResource resource = (IResource) selectedResources.get(i);
				if (resource.getType() != IResource.FILE)
					return false;
				if (!targetResource.equals(resource.getParent()))
					return false;
			}
		}

		if (resourceData != null) {
			// linked resources can only be pasted into projects
			if (isLinked(resourceData) && targetResource.getType() != IResource.PROJECT)
				return false;

			if (targetResource.getType() == IResource.FOLDER) {
				// don't try to copy folder to self
				for (int i = 0; i < resourceData.length; i++) {
					if (targetResource.equals(resourceData[i]))
						return false;
				}
			}
			return true;
		}

		TransferData[] transfers = clipboard.getAvailableTypes();
		FileTransfer fileTransfer = FileTransfer.getInstance();
		for (int i = 0; i < transfers.length; i++) {
			if (fileTransfer.isSupportedType(transfers[i]))
				return true;
		}
		return false;
	}
}
