/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 ********************************************************************************/

package org.eclipse.rse.internal.files.ui.actions;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseCopyAction;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;

/**
 * Move selected files and folders action.
 */
public class SystemMoveRemoteFileAction extends SystemCopyRemoteFileAction
       implements  IValidatorRemoteSelection
{
	private SystemMessage targetEqualsSrcMsg = null;
	private SystemMessage targetDescendsFromSrcMsg = null;
	private SystemMessage invalidFilterMsg = null;
	protected Vector movedFiles = new Vector();

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
    
	/**
	 * @param targetContainer will be the IRemoteFile folder selected to move into
	 * @param oldObject will be the IRemoteFile object currently being moved
	 * @param newName will be the new name to give the oldObject on move
	 * @param monitor Usually not needed
	 * @see SystemBaseCopyAction#doCopy(Object, Object, String, IProgressMonitor)
	 */
	protected boolean doCopy(Object targetContainer, Object oldObject, String newName, IProgressMonitor monitor)
		throws Exception 
    {
		IRemoteFile targetFolder    = (IRemoteFile)targetContainer;
		IRemoteFile srcFileOrFolder = (IRemoteFile)oldObject;

		IRemoteFileSubSystem ss = targetFolder.getParentRemoteFileSubSystem();
		
		boolean ok = false;
		
		ok = ss.move(srcFileOrFolder, targetFolder, newName, monitor);
		if (!ok)
		{
		  SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_MOVE_FILE_FAILED);
		  msg.makeSubstitution(srcFileOrFolder.getName());
		  throw new SystemMessageException(msg); 
		}
		else
		{
		   String sep = targetFolder.getSeparator();
		   String targetFolderName = targetFolder.getAbsolutePath();
		   String resultPath = null;
		   
		   if (!targetFolderName.endsWith(sep))		     
			   resultPath = targetFolderName+sep+newName;
		   else
			   resultPath = targetFolderName+newName;

		   copiedFiles.addElement(resultPath);
		   movedFiles.add(srcFileOrFolder);
		}

		return ok;
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
			        	if (targetEqualsSrcMsg == null)
			              targetEqualsSrcMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_MOVE_TARGET_EQUALS_SOURCE);
			            return targetEqualsSrcMsg;
			        }
			        else if (selectedFolderPath.equals(selectedFile.getAbsolutePath()))
			        {
			        	if (targetEqualsSrcMsg == null)
			              targetEqualsSrcMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_MOVE_TARGET_EQUALS_SOURCE); // todo: different msg
			            return targetEqualsSrcMsg;
			        }
			        else if (selectedFolder.isDescendantOf(selectedFile))
			        {
			        	if (targetDescendsFromSrcMsg == null)
			        	 targetDescendsFromSrcMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_MOVE_TARGET_DESCENDS_FROM_SOURCE);
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
					invalidFilterMsg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_MOVE_FILTER_NOT_VALID);
				}
				return invalidFilterMsg;
			}
		}
        return null;
	}   

	/**
	 * Called after all the copy/move operations end, be it successfully or not.
	 * Your opportunity to display completion or do post-copy selections/refreshes
	 */
	public void copyComplete() 
    {
    	// we want to do the super.copyComplete() to refresh the target, but first we must do refresh the 
    	//  source to reflect the deletion...

		// refresh all instances of the source parent, and all affected filters...
		ISubSystem fileSS = targetFolder.getParentRemoteFileSubSystem();
		//RSECorePlugin.getTheSystemRegistry().fireRemoteResourceChangeEvent(
		  // ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED, copiedFiles, firstSelectionParent.getAbsolutePath(), fileSS, null, null);
    	RSECorePlugin.getTheSystemRegistry().fireRemoteResourceChangeEvent(
		   ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED, movedFiles, firstSelectionParent.getAbsolutePath(), fileSS, null, null);

    	
    	/* old release 1.0 way of doing it...
		Viewer v = getViewer();
		if (v instanceof ISystemTree)
		{
		    SystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		  	ISystemTree tree = (ISystemTree)v;
		  	Object parent = tree.getSelectedParent();
		  	if (parent != null)
		  	{
		  	   if (parent instanceof IRemoteFile)
		  	   {
		  	   	 //System.out.println("Firing REFRESH_REMOTE");
		         sr.fireEvent(
                   new org.eclipse.rse.ui.model.impl.SystemResourceChangeEvent(
                      parent,ISystemResourceChangeEvent.EVENT_REFRESH_REMOTE, null) );
		  	   }
		  	   else  
		  	   {
		  	   	 //System.out.println("MOVE OPERATION: Firing REFRESH");
		  	   	 // FIRST REFRESH EXPANDED FILTER
		         sr.fireEvent(
                   new org.eclipse.rse.ui.model.impl.SystemResourceChangeEvent(
                      parent,ISystemResourceChangeEvent.EVENT_REFRESH, null) );
                 // NEXT REFRESH ALL OTHER PLACES THAT MIGHT BE SHOWING THE SOURCE FOLDER
		         sr.fireEvent(
                   new org.eclipse.rse.ui.model.impl.SystemResourceChangeEvent(
                      firstSelectionParent,ISystemResourceChangeEvent.EVENT_REFRESH_REMOTE, null) );                 
		  	   }
		  	}
		  	else
		  	  RSEUIPlugin.logWarning("Hmm, selected parent is null on a move operation!");
		}*/
    	super.copyComplete();    	
    }
}