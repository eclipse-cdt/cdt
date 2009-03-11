/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186128][refactoring] Move IProgressMonitor last in public base classes
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Kevin Doyle (IBM) - [196588] Move Dialog doesn't show Archives
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * Xuan Chen (IBM) - [160775] [api] rename (at least within a zip) blocks UI thread
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Rupen Mardirossian (IBM)		-  [210682] created checkForCollision method that returns a boolean for SystemCopyDialog enhancement
 * David McKnight   (IBM)        - [224313] [api] Create RSE Events for MOVE and COPY holding both source and destination fields
 * David McKnight   (IBM)        - [224377] "open with" menu does not have "other" option
 * David Dykstal (IBM) [230821] fix IRemoteFileSubSystem API to be consistent with IFileService
 * David McKnight   (IBM)        - [261019] New File/Folder actions available in Work Offline mode
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui.actions;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.internal.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.services.clientserver.SystemEncodingUtil;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.util.ValidatorFileUniqueName;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemActionViewerFilter;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.actions.SystemBaseCopyAction;
import org.eclipse.rse.ui.dialogs.SystemRenameSingleDialog;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Copy selected files and folders action.
 */
public class SystemCopyRemoteFileAction extends SystemBaseCopyAction
implements  IValidatorRemoteSelection
{
	protected IRemoteFile targetFolder, targetFileOrFolder = null;
	protected IRemoteFile firstSelection = null;
	protected IRemoteFile firstSelectionParent = null;
	protected IRemoteFile[] files;
	protected Vector copiedFiles = new Vector();
	protected IHost sourceConnection;
	protected IRemoteFileSubSystem ss;

	/**
	 * Constructor
	 */
	public SystemCopyRemoteFileAction(Shell shell)
	{
		this(shell, MODE_COPY);
	}
	/**
	 * Constructor for subclass
	 */
	SystemCopyRemoteFileAction(Shell shell, int mode)
	{
		super(shell, mode);
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0110"); //$NON-NLS-1$
		setDialogHelp(RSEUIPlugin.HELPPREFIX+"dcrf0000");  //$NON-NLS-1$
	}

	/**
	 * Reset. This is a re-run of this action
	 */
	protected void reset()
	{
		//System.out.println("inside remote file copy reset()");
		super.reset();
		targetFolder = null;
		targetFileOrFolder = null;
		firstSelection = null;
		firstSelectionParent = null;
		files = null;
		copiedFiles = new Vector();
		sourceConnection = null;
		ss = null;
	}

	/**
	 * We override from parent to do unique checking...
	 * <p>
	 * We simply ensure every selected object is an IRemoteFile
	 * <p>
	 * @see SystemBaseAction#updateSelection(IStructuredSelection)
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = true;
		Iterator e = selection.iterator();
		while (enable && e.hasNext())
		{
			Object selectedObject = e.next();
			if (!(selectedObject instanceof IRemoteFile))
				enable = false;
			else {
				enable = !((IRemoteFile)selectedObject).getParentRemoteFileSubSystem().isOffline();
			}
		}
		return enable;
	}

	// --------------------------
	// PARENT METHOD OVERRIDES...
	// --------------------------
	public static class RenameRunnable implements Runnable
	{
		private IRemoteFile _targetFileOrFolder;
		private String _newName;
		public RenameRunnable(IRemoteFile targetFileOrFolder)
		{
			_targetFileOrFolder = targetFileOrFolder;
		}

		public void run() {
			ValidatorFileUniqueName validator = null;
			SystemRenameSingleDialog dlg = new SystemRenameSingleDialog(null, true, _targetFileOrFolder, validator); // true => copy-collision-mode

			dlg.open();
			if (!dlg.wasCancelled())
				_newName = dlg.getNewName();
			else
				_newName = null;
		}

		public String getNewName()
		{
			return _newName;
		}
	}


	/**
	 * @see SystemBaseCopyAction#checkForCollision(Shell, IProgressMonitor, Object, Object, String)
	 * @param shell Window to host dialog
	 * @param monitor Usually not needed
	 * @param targetContainer will be the IRemoteFile folder selected to copy into
	 * @param oldObject will be the IRemoteFile object currently being copied
	 * @param oldName will be the name of the IRemoteFile object currently being copied
	 */
	protected String checkForCollision(Shell shell, IProgressMonitor monitor,
			Object targetContainer, Object oldObject, String oldName)
	{
		String newName = oldName;

		try {



			targetFolder   = (IRemoteFile)targetContainer;
			ss = targetFolder.getParentRemoteFileSubSystem();
			targetFileOrFolder = ss.getRemoteFileObject(targetFolder, oldName, monitor);


			//RSEUIPlugin.logInfo("CHECKING FOR COLLISION ON '"+srcFileOrFolder.getAbsolutePath() + "' IN '" +targetFolder.getAbsolutePath()+"'");
			//RSEUIPlugin.logInfo("...TARGET FILE: '"+tgtFileOrFolder.getAbsolutePath()+"'");
			//RSEUIPlugin.logInfo("...target.exists()? "+tgtFileOrFolder.exists());
			if (targetFileOrFolder.exists())
			{
				//monitor.setVisible(false); wish we could!

				// we no longer have to set the validator here... the common rename dialog we all now use queries the input
				// object's system view adaptor for its name validator. See getNameValidator in SystemViewRemoteFileAdapter. phil
				// ValidatorFileUniqueName validator = null; // new
				// ValidatorFileUniqueName(shell, targetFolder,
				// srcFileOrFolder.isDirectory());
				//SystemCollisionRenameDialog dlg = new SystemCollisionRenameDialog(shell, validator, oldName);
				RenameRunnable rr = new RenameRunnable(targetFileOrFolder);
				Display.getDefault().syncExec(rr);
				newName = rr.getNewName();
			}
		} catch (SystemMessageException e) {
			SystemBasePlugin.logError("SystemCopyRemoteFileAction.checkForCollision()", e); //$NON-NLS-1$
		}

		return newName;
	}
	/**
	 * @see SystemBaseCopyAction#checkForCollision(Shell, IProgressMonitor, Object, Object, String)
	 * @param shell Window to host dialog
	 * @param monitor Usually not needed
	 * @param targetContainer will be the IRemoteFile folder selected to copy into
	 * @param oldName will be the name of the IRemoteFile object currently being copied
	 */
	protected boolean checkForCollision(Shell shell, IProgressMonitor monitor,
			Object targetContainer, String oldName)
	{
		try
		{
			targetFolder   = (IRemoteFile)targetContainer;
			ss = targetFolder.getParentRemoteFileSubSystem();
			targetFileOrFolder = ss.getRemoteFileObject(targetFolder, oldName, monitor);

			if (targetFileOrFolder.exists())
			{
				return true;
			}

		}
		catch (SystemMessageException e)
		{
			SystemBasePlugin.logError("SystemCopyRemoteFileAction.checkForCollision()", e); //$NON-NLS-1$
		}
		return false;

	}

	/**
	 * @param targetContainer will be the IRemoteFile folder selected to copy into
	 * @param oldObject will be the IRemoteFile object currently being copied
	 * @param newName will be the new name to give the oldObject on copy
	 * @param monitor Usually not needed
	 * @see SystemBaseCopyAction#doCopy(Object, Object, String, IProgressMonitor)
	 */
	protected boolean doCopy(Object targetContainer, Object oldObject, String newName, IProgressMonitor monitor)
	throws Exception
	{
		targetFolder    = (IRemoteFile)targetContainer;
		IRemoteFile srcFileOrFolder = (IRemoteFile)oldObject;

		IHost targetConnection = targetFolder.getHost();
		IHost srcConnection    = srcFileOrFolder.getHost();

		boolean ok = false;
		if (targetConnection == srcConnection)
		{
			ss = targetFolder.getParentRemoteFileSubSystem();
			ss.copy(srcFileOrFolder, targetFolder, newName, null);
			ok = true;
			String sep = targetFolder.getSeparator();
			String targetFolderName = targetFolder.getAbsolutePath();
			if (!targetFolderName.endsWith(sep))
				copiedFiles.addElement(targetFolderName+sep+newName);
			else
				copiedFiles.addElement(targetFolderName+newName);
		}
		// DKM - for cross system copy
		else
		{
			IRemoteFileSubSystem targetFS = targetFolder.getParentRemoteFileSubSystem();
			IRemoteFileSubSystem srcFS    = srcFileOrFolder.getParentRemoteFileSubSystem();
			String newPath = targetFolder.getAbsolutePath() + "/" + newName; //$NON-NLS-1$
			if (srcFileOrFolder.isFile())
			{
				SystemRemoteEditManager mgr = SystemRemoteEditManager.getInstance();
				// if remote edit project doesn't exist, create it
				if (!mgr.doesRemoteEditProjectExist())
					mgr.getRemoteEditProject();

				StringBuffer path = new StringBuffer(mgr.getRemoteEditProjectLocation().makeAbsolute().toOSString());
				path = path.append("/" + srcFS.getSystemProfileName() + "/" + srcFS.getHostAliasName() + "/"); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$

				String absolutePath = srcFileOrFolder.getAbsolutePath();


				int colonIndex = absolutePath.indexOf(IPath.DEVICE_SEPARATOR);

				if (colonIndex != -1)
				{
					if (colonIndex == 0)
					{
						absolutePath = absolutePath.substring(1);
					}
					else if (colonIndex == (absolutePath.length() - 1))
					{
						absolutePath = absolutePath.substring(0, colonIndex);
					}
					else
					{
						absolutePath = absolutePath.substring(0, colonIndex) + absolutePath.substring(colonIndex + 1);
					}
				}

				path = path.append(absolutePath);

				String tempFile = path.toString();
   
				srcFS.download(srcFileOrFolder, tempFile, SystemEncodingUtil.ENCODING_UTF_8, null);
				targetFS.upload(tempFile, SystemEncodingUtil.ENCODING_UTF_8, newPath, System.getProperty("file.encoding"), null);	 //$NON-NLS-1$
			}
			else
			{

				IRemoteFile newTargetFolder = targetFS.getRemoteFileObject(newPath, monitor);
				targetFS.createFolder(newTargetFolder, monitor);
				IRemoteFile[] children = srcFS.list(srcFileOrFolder, monitor);
				if (children != null)
				{
					for (int i = 0; i < children.length; i++)
					{
						IRemoteFile child = children[i];
						monitor.subTask("copying " + child.getName());	 //$NON-NLS-1$
						doCopy(newTargetFolder, child, child.getName(), monitor);
						monitor.worked(1);
					}
				}
			}
		}

		return ok;
	}


	/**
	 * Required parent class abstract method.
	 * Does not apply to us as we supply our own dialog for the copy-target
	 */
	protected SystemSimpleContentElement getTreeModel()
	{
		return null;
	}
	/**
	 * Required parent class abstract method.
	 * Does not apply to us as we supply our own dialog for the copy-target
	 */
	protected SystemSimpleContentElement getTreeInitialSelection()
	{
		return null;
	}

	/**
	 * @see SystemBaseCopyAction#getOldObjects()
	 * Returns an array of IRemoteFile objects
	 */
	protected Object[] getOldObjects()
	{
		return getSelectedFiles();
	}

	/**
	 * @see SystemBaseCopyAction#getOldNames()
	 */
	protected String[] getOldNames()
	{
		IRemoteFile[] files = getSelectedFiles();
		String[] names = new String[files.length];
		for (int idx=0; idx<files.length; idx++)
			names[idx] = files[idx].getName();
		return names;
	}


	/**
	 * @see SystemBaseCopyAction#getOldAbsoluteNames()
	 */
	protected String[] getOldAbsoluteNames()
	{
		IRemoteFile[] files = getSelectedFiles();
		String[] names = new String[files.length];
		for (int idx=0; idx<files.length; idx++)
			names[idx] = files[idx].getAbsolutePath();
		return names;
	}

	/**
	 * Override of parent.
	 * Return the dialog that will be used to prompt for the copy/move target location.
	 */
	protected Dialog createDialog(Shell shell)
	{
		++runCount;
		if (runCount > 1)
			reset();
		//return new SystemSimpleCopyDialog(parent, getPromptString(), mode, this, getTreeModel(), getTreeInitialSelection());
		String dlgTitle = (mode==MODE_COPY ? SystemResources.RESID_COPY_TITLE : SystemResources.RESID_MOVE_TITLE);

		firstSelection = getFirstSelectedFile();
		sourceConnection = firstSelection.getHost();
		SystemRemoteFolderDialog dlg = new SystemRemoteFolderDialog(shell, dlgTitle, sourceConnection);
		dlg.setNeedsProgressMonitor(true);
		dlg.setMessage(getPromptString());
		dlg.setShowPropertySheet(true, false);
		dlg.setDefaultSystemConnection(sourceConnection, true);

		//dlg.setSystemConnection(sourceConnection);
		if (mode==MODE_MOVE)
			dlg.setSelectionValidator(this);
		//RSEUIPlugin.logInfo("Calling getParentRemoteFile for '"+firstSelection.getAbsolutePath()+"'");
		firstSelectionParent = firstSelection.getParentRemoteFile();
		boolean supportsArchiveManagement = firstSelectionParent.getParentRemoteFileSubSystem().getParentRemoteFileSubSystemConfiguration().supportsArchiveManagement();
		if (supportsArchiveManagement)
		{
			// Set a new viewer filter
			SystemActionViewerFilter _filter = new SystemActionViewerFilter();
			Class[] types = {IRemoteFile.class};
			_filter.addFilterCriterion(types, "isDirectory", "true"); //$NON-NLS-1$  //$NON-NLS-2$
			_filter.addFilterCriterion(types, "isArchive", "true"); //$NON-NLS-1$  //$NON-NLS-2$
			dlg.setCustomViewerFilter(_filter);
		}
		/*
		if (firstSelectionParent != null)
		  RSEUIPlugin.logInfo("Result of getParentRemoteFile: '"+firstSelectionParent.getAbsolutePath()+"'");
		else
		  RSEUIPlugin.logInfo("Result of getParentRemoteFile: null");
		 */
		dlg.setPreSelection(firstSelectionParent);

		// our title now reflects multiple selection. If single change it.
		IStructuredSelection sel = getSelection();
		//System.out.println("size = "+sel.size());
		if (sel.size() == 1)
		{
			String singleTitle = null;
			if (mode == MODE_COPY)
				singleTitle = SystemResources.RESID_COPY_SINGLE_TITLE;
			else
				singleTitle = SystemResources.RESID_MOVE_SINGLE_TITLE;
			//System.out.println("..."+singleTitle);
			if (!singleTitle.startsWith("Missing")) // TODO: remove test after next mri rev         	 //$NON-NLS-1$
				dlg.setTitle(singleTitle);
		}
		return dlg;
	}

	/**
	 * Override this method if you supply your own copy/move target dialog.
	 * Return the user-selected target or null if cancelled
	 */
	protected Object getTargetContainer(Dialog dlg)
	{
		SystemRemoteFolderDialog cpyDlg = (SystemRemoteFolderDialog)dlg;
		Object targetContainer = null;
		if (!cpyDlg.wasCancelled())
		{
			targetContainer = cpyDlg.getSelectedObject();
			if (targetContainer instanceof ISystemFilterReference)
			{
				ISubSystem targetSubSystem = ((ISystemFilterReference)targetContainer).getSubSystem();
				ISubSystemConfiguration factory = targetSubSystem.getSubSystemConfiguration();
				if (factory.supportsDropInFilters())
				{
					targetContainer = targetSubSystem.getTargetForFilter((ISystemFilterReference)targetContainer);
				}
			}
		}
		return targetContainer;
	}

	private void invalidateFilterReferences(IRemoteFile targetFolder)
	{
		String path = targetFolder.getAbsolutePath();
		IRemoteFileSubSystem fileSS = targetFolder.getParentRemoteFileSubSystem();
		ISystemFilterPoolReferenceManager mgr = fileSS.getSystemFilterPoolReferenceManager();
		ISystemFilterPool[] pools = mgr.getReferencedSystemFilterPools();
		IProgressMonitor monitor = new NullProgressMonitor();
		for (int i = 0; i < pools.length; i++)
		{
			ISystemFilterPool pool = pools[i];
			ISystemFilter[] filters = pool.getSystemFilters();
			for (int f = 0; f < filters.length; f++)
			{
				String[] strs = filters[f].getFilterStrings();
				for (int s = 0; s < strs.length; s++)
				{
					String str = strs[s];
					int lastSep = str.lastIndexOf(fileSS.getSeparator());
					if (lastSep > 0)
					{
						str = str.substring(0, lastSep);
					}
					IRemoteFile par = null;
					try
					{
						par = fileSS.getRemoteFileObject(str, monitor);
					}
					catch (Exception e)
					{
					}

					if (par != null)
						str = par.getAbsolutePath();

					//if (StringCompare.compare(str, path, true))
					if (str.equals(path))
					{
						ISystemFilterReference ref = mgr.getSystemFilterReference(fileSS, filters[f]);
						ref.markStale(true);
					}
				}
			}
		}
	}

	/**
	 * Called after all the copy/move operations end, be it successfully or not.
	 * Your opportunity to display completion or do post-copy selections/refreshes
	 */
	public void copyComplete(String operation)
	{
		if (copiedFiles.size() == 0)
			return;

		// refresh all instances of this parent, and all affected filters...
		ISubSystem fileSS = targetFolder.getParentRemoteFileSubSystem();
		Viewer originatingViewer = getViewer();

		targetFolder.markStale(true);

		// invalidate filters
		invalidateFilterReferences(targetFolder);

		if (operation == null){
			operation = ISystemRemoteChangeEvents.SYSTEM_REMOTE_OPERATION_COPY;
		}

		
		RSECorePlugin.getTheSystemRegistry().fireRemoteResourceChangeEvent(operation,
				ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED, copiedFiles, targetFolder.getAbsolutePath(), fileSS, getOldAbsoluteNames(), originatingViewer);
	
	}

	// ------------------
	// PRIVATE METHODS...
	// ------------------

	/**
	 * Get the currently selected IRemoteFile objects
	 */
	protected IRemoteFile[] getSelectedFiles()
	{
		if (files == null)
		{
			IStructuredSelection selection = getSelection();
			files = new IRemoteFile[selection.size()];
			Iterator i = selection.iterator();
			int idx=0;
			while (i.hasNext())
			{
				files[idx++] = (IRemoteFile)i.next();
			}
		}
		return files;
	}
	/**
	 * Get the first selected file or folder
	 */
	protected IRemoteFile getFirstSelectedFile()
	{
		if (files == null)
			getSelectedFiles();
		if (files.length > 0)
			return files[0];
		else
			return null;
	}

	/**
	 * The user has selected a remote object. Return null if OK is to be enabled, or a SystemMessage
	 *  if it is not to be enabled. The message will be displayed on the message line.
	 * <p>
	 * This is overridden in SystemMoveRemoteFileAction
	 */
	public SystemMessage isValid(IHost selectedConnection, Object[] selectedObjects, ISystemRemoteElementAdapter[] remoteAdaptersForSelectedObjects)
	{
		return null;
	}

	

}