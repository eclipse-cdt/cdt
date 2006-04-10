/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.files.ui.actions;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRemoteChangeEvents;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.actions.SystemBaseCopyAction;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;


/**
 * Move selected files and folders action.
 */
public class SystemMoveRemoteFileAction extends SystemCopyRemoteFileAction
       implements  ISystemMessages, IValidatorRemoteSelection
{
	private SystemMessage targetEqualsSrcMsg = null;
	private SystemMessage targetDescendsFromSrcMsg = null;
	protected Vector movedFiles = new Vector();

	/**
	 * Constructor
	 */
	public SystemMoveRemoteFileAction(Shell shell) 
	{
		super(shell, MODE_MOVE);
  	    setHelp(SystemPlugin.HELPPREFIX+"actn0111");
  	    setDialogHelp(SystemPlugin.HELPPREFIX+"dmrf0000"); 
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
	 * @see SystemBaseCopyAction#doCopy(IProgressMonitor, Object, Object, String)
	 * @param monitor Usually not needed
	 * @param targetContainer will be the IRemoteFile folder selected to move into
	 * @param oldObject will be the IRemoteFile object currently being moved
	 * @param newName will be the new name to give the oldObject on move
	 */
	protected boolean doCopy(IProgressMonitor monitor, Object targetContainer, Object oldObject, String newName)
		throws Exception 
    {
		IRemoteFile targetFolder    = (IRemoteFile)targetContainer;
		IRemoteFile srcFileOrFolder = (IRemoteFile)oldObject;

		IRemoteFileSubSystem ss = targetFolder.getParentRemoteFileSubSystem();
		
		boolean ok = false;
		
		ok = ss.move(srcFileOrFolder, targetFolder, newName, monitor);
		if (!ok)
		{
		  SystemMessage msg = SystemPlugin.getPluginMessage(FILEMSG_MOVE_FILE_FAILED);
		  msg.makeSubstitution(srcFileOrFolder.getName());
		  throw new SystemMessageException(msg); 
		}
		else
		{
		   String sep = targetFolder.getSeparator();
		   String targetFolderName = targetFolder.getAbsolutePath();
		   if (!targetFolderName.endsWith(sep))		     
		     copiedFiles.addElement(targetFolderName+sep+newName);
		   else
		     copiedFiles.addElement(targetFolderName+newName);
		     
		   movedFiles.addElement(srcFileOrFolder);  
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
		Object selectedObject = selectedObjects[0];
		if (!(selectedObject instanceof IRemoteFile))
		  return null;
		IRemoteFile selectedFolder = (IRemoteFile)selectedObject;
        if (selectedFolder.getAbsolutePath().equals(firstSelectionParent.getAbsolutePath()))
        {
        	if (targetEqualsSrcMsg == null)
              targetEqualsSrcMsg = SystemPlugin.getPluginMessage(FILEMSG_MOVE_TARGET_EQUALS_SOURCE);
            return targetEqualsSrcMsg;
        }
        else if (selectedFolder.getAbsolutePath().equals(firstSelection.getAbsolutePath()))
        {
        	if (targetEqualsSrcMsg == null)
              targetEqualsSrcMsg = SystemPlugin.getPluginMessage(FILEMSG_MOVE_TARGET_EQUALS_SOURCE); // todo: different msg
            return targetEqualsSrcMsg;
        }
        else if (selectedFolder.isDescendantOf(firstSelection))
        {
        	if (targetDescendsFromSrcMsg == null)
        	 targetDescendsFromSrcMsg = SystemPlugin.getPluginMessage(FILEMSG_MOVE_TARGET_DESCENDS_FROM_SOUCE);
        	return targetDescendsFromSrcMsg;
        }
        else
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
		//SystemPlugin.getTheSystemRegistry().fireRemoteResourceChangeEvent(
		  // ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED, copiedFiles, firstSelectionParent.getAbsolutePath(), fileSS, null, null);
    	SystemPlugin.getTheSystemRegistry().fireRemoteResourceChangeEvent(
		   ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED, movedFiles, firstSelectionParent.getAbsolutePath(), fileSS, null, null);
		           
    	/* old release 1.0 way of doing it...
		Viewer v = getViewer();
		if (v instanceof ISystemTree)
		{
		    SystemRegistry sr = SystemPlugin.getTheSystemRegistry();
		  	ISystemTree tree = (ISystemTree)v;
		  	Object parent = tree.getSelectedParent();
		  	if (parent != null)
		  	{
		  	   if (parent instanceof IRemoteFile)
		  	   {
		  	   	 //System.out.println("Firing REFRESH_REMOTE");
		         sr.fireEvent(
                   new com.ibm.etools.systems.model.impl.SystemResourceChangeEvent(
                      parent,ISystemResourceChangeEvent.EVENT_REFRESH_REMOTE, null) );
		  	   }
		  	   else  
		  	   {
		  	   	 //System.out.println("MOVE OPERATION: Firing REFRESH");
		  	   	 // FIRST REFRESH EXPANDED FILTER
		         sr.fireEvent(
                   new com.ibm.etools.systems.model.impl.SystemResourceChangeEvent(
                      parent,ISystemResourceChangeEvent.EVENT_REFRESH, null) );
                 // NEXT REFRESH ALL OTHER PLACES THAT MIGHT BE SHOWING THE SOURCE FOLDER
		         sr.fireEvent(
                   new com.ibm.etools.systems.model.impl.SystemResourceChangeEvent(
                      firstSelectionParent,ISystemResourceChangeEvent.EVENT_REFRESH_REMOTE, null) );                 
		  	   }
		  	}
		  	else
		  	  SystemPlugin.logWarning("Hmm, selected parent is null on a move operation!");
		}*/
    	super.copyComplete();    	
    }
}