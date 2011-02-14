/********************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186128][refactoring] Move IProgressMonitor last in public base classes 
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Kevin Doyle (IBM) - [198007] Moving multiple folders allows moving to themselves
 * Kevin Doyle (IBM) - [160769] Move Resource dialog allows user to continue on invalid destination
 * Kevin Doyle (IBM) - [199324] [nls] Move dialog SystemMessages should be added/updated
 * Xuan Chen (IBM) - [160775] [api] rename (at least within a zip) blocks UI thread
 * Xuan Chen (IBM) - [209827] Update DStore command implementation to enable cancelation of archive operations
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Rupen Mardirossian (IBM)		-  [210682] Modified MoveRemoteFileJob.runInWorkspace to use SystemCopyDialog for collisions in move operations
 * David McKnight   (IBM)        - [224313] [api] Create RSE Events for MOVE and COPY holding both source and destination fields
 * David McKnight   (IBM)        - [224377] "open with" menu does not have "other" option
 * David Dykstal (IBM) [230821] fix IRemoteFileSubSystem API to be consistent with IFileService
 * David McKnight   (IBM)        - [240699] Problem with moving a file which has been opened in an editor
 ********************************************************************************/

package org.eclipse.rse.internal.files.ui.actions;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.files.ui.resources.SystemEditableRemoteFile;
import org.eclipse.rse.files.ui.resources.UniversalFileTransferUtility;
import org.eclipse.rse.internal.files.ui.Activator;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.internal.files.ui.ISystemFileConstants;
import org.eclipse.rse.internal.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.internal.ui.dialogs.CopyRunnable;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.SystemIFileProperties;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseCopyAction;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Move selected files and folders action.
 */
public class SystemMoveRemoteFileAction extends SystemCopyRemoteFileAction
       implements  IValidatorRemoteSelection
{
	private SystemMessage targetEqualsSrcMsg = null;
	private SystemMessage targetEqualsParentSrcMsg = null;
	private SystemMessage targetDescendsFromSrcMsg = null;
	private SystemMessage invalidFilterMsg = null;
	protected Vector movedFiles = new Vector();
	protected Vector movedFileNames = new Vector();

	private class MoveRemoteFileJob extends WorkspaceJob
	{
		
		/**
		 * RenameJob job.
		 * @param message text used as the title of the job
		 */
		public MoveRemoteFileJob(String message)
		{
			super(message);
			setUser(true);
		}

		public IStatus runInWorkspace(IProgressMonitor monitor) 
		{
			SystemMessage msg = getCopyingMessage();
			
			IStatus status = Status.OK_STATUS;
			
			//holds existing objects
			List existing = new ArrayList();
			//holds objects to be copied
			List toCopy = new ArrayList();
	        boolean overwrite = false;
			
		
	        int steps = oldObjects.length;
		    monitor.beginTask(msg.getLevelOneText(), steps);
		    copiedOk = true;
		    String oldName = null;
		    String newName = null;
		    Object oldObject = null;
		    newNames = new String[oldNames.length];
		    //go through all files to see if they exist
		    for (int idx=0; copiedOk && (idx<steps); idx++)
		    {
		    	oldName = oldNames[idx];
		     	oldObject = oldObjects[idx];
		        //monitor.subTask(getCopyingMessage(oldName).getLevelOneText());
		       	if(checkForCollision(getShell(), monitor, targetContainer, oldName))
		       	{
		       		existing.add(oldObject);
		       	}
		       	toCopy.add(oldObject);
		       	/*newName = checkForCollision(getShell(), monitor, targetContainer, oldObject, oldName);
		        if (newName == null)
		        	copiedOk = false;
		   	  	else
			        copiedOk = doCopy(targetContainer, oldObject, newName, monitor);
			    newNames[idx] = newName;
			    monitor.worked(1);
			    movedFileNames.add(oldName);*/ //remember the old name, in case we need it later.
		    }
	        //monitor.done();
		    
		    //SystemCopyDialog used here with all existing objects
			if(existing.size()>0)
			{
				CopyRunnable cr = new CopyRunnable(existing);
				Display.getDefault().syncExec(cr);
				overwrite = cr.getOk();
				if(!overwrite)
				{
					status = Status.CANCEL_STATUS;
				}
			}
			//Proceed with copy if user chose to overwrite or there were no copy collisions
			if(existing.size()==0 || overwrite)
			{
				try
				{	
					for (int idx=0; copiedOk && (idx<steps); idx++)
					{
						newName = oldNames[idx];
			       	  	oldObject = oldObjects[idx];
			       	  	monitor.subTask(getCopyingMessage(newName).getLevelOneText());
			       	  	copiedOk = doCopy(targetContainer, oldObject, newName, monitor);
			       	  	monitor.worked(1);
			       	  	newNames[idx] = newName;
			       	  	movedFileNames.add(newName);
			       	  	monitor.done();
					}  			
				}
				catch (SystemMessageException exc)
				{
					copiedOk = false;
					//If this operation is cancelled, need to display a proper message to the user.
					if (monitor.isCanceled() && movedFileNames.size() > 0)
					{
						//Get the moved file names
						String movedFileNamesList = (String)(movedFileNames.get(0));
						for (int i=1; i<(movedFileNames.size()); i++)
						{
							movedFileNamesList = movedFileNamesList + "\n" + (String)(movedFileNames.get(i)); //$NON-NLS-1$
						}
						String msgTxt = FileResources.FILEMSG_MOVE_INTERRUPTED;
						String msgDetails = NLS.bind(FileResources.FILEMSG_MOVE_INTERRUPTED_DETAILS, movedFileNamesList);
									
					SystemMessage thisMessage = new SimpleSystemMessage(Activator.PLUGIN_ID, 							
							ISystemFileConstants.FILEMSG_MOVE_INTERRUPTED,
							IStatus.ERROR, msgTxt, msgDetails);
					SystemMessageDialog.displayErrorMessage(shell, thisMessage);
				
						status = Status.CANCEL_STATUS;
					}
					else
					{
						SystemMessageDialog.displayErrorMessage(shell, exc.getSystemMessage());
					}
				}
				catch (Exception exc)
				{
					copiedOk = false;
					exc.printStackTrace();
				}
			}
			if (movedFiles.size() > 0)
			{
				copyComplete(ISystemRemoteChangeEvents.SYSTEM_REMOTE_OPERATION_MOVE);  //Need to reflect the views.
			}
	        
	        return status;
		}
	}	
	/**
	 * Constructor
	 */
	public SystemMoveRemoteFileAction(Shell shell) 
	{
		super(shell, MODE_MOVE);
  	    setHelp(RSEUIPlugin.HELPPREFIX+"actn0111"); //$NON-NLS-1$
  	    setDialogHelp(RSEUIPlugin.HELPPREFIX+"dmrf0000");  //$NON-NLS-1$
	}
 
    // --------------------------
    // PARENT METHOD OVERRIDES...
    // --------------------------
	/**
	 * Reset. This is a re-run of this action
	 */
	protected void reset()
	{
		movedFiles.clear();
		super.reset();
		
		//targetEqualsSrcMsg = null;
	}
	
	 public void run(IProgressMonitor monitor)
     throws java.lang.reflect.InvocationTargetException,
            java.lang.InterruptedException
     {
		 SystemMessage moveMessage = getCopyingMessage();
		 moveMessage.makeSubstitution(""); //$NON-NLS-1$
			MoveRemoteFileJob moveRemoteFileJob = new MoveRemoteFileJob(moveMessage.getLevelOneText());
			moveRemoteFileJob.schedule();
     }
    
	/**
	 * @param targetContainer will be the IRemoteFile folder selected to move into
	 * @param oldObject will be the IRemoteFile object currently being moved
	 * @param newName will be the new name to give the oldObject on move
	 * @param monitor Usually not needed
	 * @see SystemBaseCopyAction#doCopy(Object, Object, String, IProgressMonitor)
	 */
	protected boolean doCopy(Object targetContainer, Object oldObject, String newName, IProgressMonitor monitor) throws Exception {
		IRemoteFile targetFolder = (IRemoteFile) targetContainer;
		IRemoteFile srcFileOrFolder = (IRemoteFile) oldObject;

		IRemoteFileSubSystem ss = targetFolder.getParentRemoteFileSubSystem();

		ss.move(srcFileOrFolder, targetFolder, newName, monitor);
		String sep = targetFolder.getSeparator();
		String targetFolderName = targetFolder.getAbsolutePath();
		String resultPath = null;

		if (!targetFolderName.endsWith(sep))
			resultPath = targetFolderName + sep + newName;
		else
			resultPath = targetFolderName + newName;

		copiedFiles.addElement(resultPath);
		movedFiles.add(srcFileOrFolder);
		return true;
	}

	/**
	 * The user has selected a remote object. Return null if OK is to be enabled, or a SystemMessage
	 *  if it is not to be enabled. The message will be displayed on the message line.
	 */
	public SystemMessage isValid(IHost selectedConnection, Object[] selectedObjects, ISystemRemoteElementAdapter[] remoteAdaptersForSelectedObjects)
	{
		//if (selectedConnection != sourceConnection) {} // someday, but can't happen today.
		IRemoteFile[] files = getSelectedFiles();
		Object selectedObject = selectedObjects[0];
		if (!(selectedObject instanceof IRemoteFile || selectedObject instanceof ISystemFilterReference) || files == null) {
		  return null;
		}
		
		if (selectedObject instanceof IRemoteFile) {
			IRemoteFile selectedFolder = (IRemoteFile)selectedObject;
			String selectedFolderPath = selectedFolder.getAbsolutePath();
			
			for (int i = 0; i < files.length; i++) {
				IRemoteFile selectedFile = files[i];
				if (selectedFile != null && selectedFile.getParentRemoteFile() != null) {
					IRemoteFile selectedParentFile = selectedFile.getParentRemoteFile();
					
			        if (selectedFolderPath.equals(selectedParentFile.getAbsolutePath()))
			        {
			        	if (targetEqualsParentSrcMsg == null){
			        		targetEqualsParentSrcMsg = new SimpleSystemMessage(Activator.PLUGIN_ID, 
			        				ISystemFileConstants.FILEMSG_MOVE_TARGET_EQUALS_PARENT_OF_SOURCE,
			        				IStatus.ERROR, 
			        				FileResources.FILEMSG_MOVE_TARGET_EQUALS_PARENT_OF_SOURCE, 
			        				FileResources.FILEMSG_MOVE_TARGET_EQUALS_PARENT_OF_SOURCE_DETAILS);

			        	
			        	}
			            return targetEqualsParentSrcMsg;
			        }
			        else if (selectedFolderPath.equals(selectedFile.getAbsolutePath()))
			        {
			        	if (targetEqualsSrcMsg == null){
			        		targetEqualsSrcMsg = new SimpleSystemMessage(Activator.PLUGIN_ID, 
			        				ISystemFileConstants.FILEMSG_MOVE_TARGET_EQUALS_SOURCE,
			        				IStatus.ERROR, 
			        				FileResources.FILEMSG_MOVE_TARGET_EQUALS_SOURCE, 
			        				FileResources.FILEMSG_MOVE_TARGET_EQUALS_SOURCE_DETAILS);
			        	}
			            return targetEqualsSrcMsg;
			        }
			        else if (selectedFolder.isDescendantOf(selectedFile))
			        {
			        	if (targetDescendsFromSrcMsg == null){
				        		targetDescendsFromSrcMsg = new SimpleSystemMessage(Activator.PLUGIN_ID, 
				        				ISystemFileConstants.FILEMSG_MOVE_TARGET_DESCENDS_FROM_SOURCE,
				        				IStatus.ERROR, 
				        				FileResources.FILEMSG_MOVE_TARGET_DESCENDS_FROM_SOURCE, 
				        				FileResources.FILEMSG_MOVE_TARGET_DESCENDS_FROM_SOURCE_DETAILS);
			        		
			        	}
			        	return targetDescendsFromSrcMsg;
			        }
				}
			}
		} else if (selectedObject instanceof ISystemFilterReference) {
			ISystemFilterReference filter = (ISystemFilterReference) selectedObject;
			String[] filterStrings = filter.getReferencedFilter().getFilterStrings();
			String firstFilterString = filterStrings[0];
			// Check only first filter string as by convention we move files only
			// to the first filter string.  * and /* are invalid as they represent
			// Drives and Root Filters which we can't Move files to.
			if (firstFilterString.equals("*") || firstFilterString.equals("/*")) { //$NON-NLS-1$ //$NON-NLS-2$
				if (invalidFilterMsg == null) {
		        		invalidFilterMsg = new SimpleSystemMessage(Activator.PLUGIN_ID, 
		        				ISystemFileConstants.FILEMSG_MOVE_FILTER_NOT_VALID,
		        				IStatus.ERROR, 
		        				FileResources.FILEMSG_MOVE_FILTER_NOT_VALID, 
		        				FileResources.FILEMSG_MOVE_FILTER_NOT_VALID_DETAILS);

				}
				return invalidFilterMsg;
			}
		}
        return null;
	}   

	private void moveTempResource(IResource oldLocalResource, IResource newLocalResource, IRemoteFile newRemoteFile, IRemoteFileSubSystem ss)
	{
		if (oldLocalResource != null)
		{
			try
			{
				moveTempFileProperties(oldLocalResource, ss, newRemoteFile);
				oldLocalResource.move(newLocalResource.getFullPath(), true, null);

			}
			catch (Exception e)
			{
			}

		}
	}

	private void moveTempFileProperties(IResource oldLocalResource, IRemoteFileSubSystem ss, IRemoteFile newRemoteFile)
	{

		if (oldLocalResource instanceof IContainer)
		{
			IContainer localContainer = (IContainer) oldLocalResource;
			try
			{
				IResource[] members = localContainer.members();
				for (int i = 0; i < members.length; i++)
				{
					IResource member = members[i];
					IRemoteFile newChildRemoteFile = ss.getRemoteFileObject(newRemoteFile, member.getName(), new NullProgressMonitor());
					moveTempFileProperties(member, ss, newChildRemoteFile); //$NON-NLS-1$
				}
			}
			catch (Exception e)
			{
			}
		}
		else if (oldLocalResource instanceof IFile)
		{
			IFile localFile = (IFile)oldLocalResource;
			try
			{
				SystemIFileProperties properties = new SystemIFileProperties(localFile);
				properties.setRemoteFilePath(newRemoteFile.getAbsolutePath());

				Object editableObj = properties.getRemoteFileObject();
				if (editableObj != null)
				{
					SystemEditableRemoteFile editable = (SystemEditableRemoteFile)editableObj;
			
					// change the remote file regardless of whether it's open in an editor or not
					// there's an in-memory editable, so change the associated remote file
					editable.setRemoteFile(newRemoteFile);

				}
			}
			catch (Exception e)
			{
			}

		}

	}
	/**
	 * Called after all the copy/move operations end, be it successfully or not.
	 * Your opportunity to display completion or do post-copy selections/refreshes
	 */
	public void copyComplete(String operation) 
    {
    	// we want to do the super.copyComplete() to refresh the target, but first we must do refresh the 
    	//  source to reflect the deletion...

		// refresh all instances of the source parent, and all affected filters...
		ISubSystem fileSS = targetFolder.getParentRemoteFileSubSystem();

		if (operation == null){
			operation = ISystemRemoteChangeEvents.SYSTEM_REMOTE_OPERATION_MOVE;
		}
		
		// deal with editors
		for (int i = 0; i < movedFiles.size(); i++){
			
			IRemoteFile oldRemoteFile = (IRemoteFile)movedFiles.get(i);
			IRemoteFile newRemoteFile = null;
			try {
				newRemoteFile = ss.getRemoteFileObject((String)copiedFiles.get(i), new NullProgressMonitor());
			} catch (SystemMessageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IResource oldLocalResource = null;
			if (SystemRemoteEditManager.getInstance().doesRemoteEditProjectExist()){				
				oldLocalResource = UniversalFileTransferUtility.getTempFileFor(oldRemoteFile);
			}
	
			if (oldLocalResource != null){
				IResource newLocalResource = UniversalFileTransferUtility.getTempFileFor(newRemoteFile);			
				moveTempResource(oldLocalResource, newLocalResource, newRemoteFile, ss);
			}
		}
		
		Viewer originatingViewer = getViewer(); 
    	RSECorePlugin.getTheSystemRegistry().fireRemoteResourceChangeEvent(operation,
		   ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED, movedFiles, firstSelectionParent.getAbsolutePath(), fileSS, getOldAbsoluteNames(), originatingViewer);

    	super.copyComplete(operation);    	
    }
}