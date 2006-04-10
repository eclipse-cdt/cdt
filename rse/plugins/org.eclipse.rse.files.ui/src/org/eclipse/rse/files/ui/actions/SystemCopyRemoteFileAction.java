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
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.files.ui.dialogs.SystemSelectRemoteFileOrFolderDialog;
import org.eclipse.rse.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRemoteChangeEvents;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.util.ValidatorFileUniqueName;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.actions.SystemBaseCopyAction;
import org.eclipse.rse.ui.dialogs.SystemRenameSingleDialog;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;
import org.eclipse.rse.ui.validators.IValidatorRemoteSelection;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.SystemView;
import org.eclipse.swt.widgets.Shell;


/**
 * Copy selected files and folders action.
 */
public class SystemCopyRemoteFileAction extends SystemBaseCopyAction
       implements  ISystemMessages, IValidatorRemoteSelection
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
  	    setHelp(SystemPlugin.HELPPREFIX+"actn0110"); 
  	    setDialogHelp(SystemPlugin.HELPPREFIX+"dcrf0000"); 
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
		Iterator e= ((IStructuredSelection) selection).iterator();		
		while (enable && e.hasNext())
		{
			Object selectedObject = e.next();
			if (!(selectedObject instanceof IRemoteFile))
			  enable = false;
		}
		return enable;
	}
 
    // --------------------------
    // PARENT METHOD OVERRIDES...
    // --------------------------
    
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
			targetFileOrFolder = ss.getRemoteFileObject(targetFolder, oldName);
	
	
			//SystemPlugin.logInfo("CHECKING FOR COLLISION ON '"+srcFileOrFolder.getAbsolutePath() + "' IN '" +targetFolder.getAbsolutePath()+"'");
			//SystemPlugin.logInfo("...TARGET FILE: '"+tgtFileOrFolder.getAbsolutePath()+"'");  		
			//SystemPlugin.logInfo("...target.exists()? "+tgtFileOrFolder.exists());
			if (targetFileOrFolder.exists())
			{
			  //monitor.setVisible(false); wish we could!
	
		      // we no longer have to set the validator here... the common rename dialog we all now use queries the input
		      // object's system view adaptor for its name validator. See getNameValidator in SystemViewRemoteFileAdapter. phil
			  ValidatorFileUniqueName validator = null; // new ValidatorFileUniqueName(shell, targetFolder, srcFileOrFolder.isDirectory());
			  //SystemCollisionRenameDialog dlg = new SystemCollisionRenameDialog(shell, validator, oldName);
			  SystemRenameSingleDialog dlg = new SystemRenameSingleDialog(shell, true, targetFileOrFolder, validator); // true => copy-collision-mode
			  dlg.open();
			  if (!dlg.wasCancelled())
			    newName = dlg.getNewName();
			  else
			    newName = null;
			}
		} catch (SystemMessageException e) {
			SystemBasePlugin.logError("SystemCopyRemoteFileAction.checkForCollision()", e);
		}
		
		return newName;
	}

	
	/**
	 * @see SystemBaseCopyAction#doCopy(IProgressMonitor, Object, Object, String)
	 * @param monitor Usually not needed
	 * @param targetContainer will be the IRemoteFile folder selected to copy into
	 * @param oldObject will be the IRemoteFile object currently being copied
	 * @param newName will be the new name to give the oldObject on copy
	 */
	protected boolean doCopy(IProgressMonitor monitor, Object targetContainer, Object oldObject, String newName)
		throws Exception 
    {
   		targetFolder    = (IRemoteFile)targetContainer;   		   		
		IRemoteFile srcFileOrFolder = (IRemoteFile)oldObject;

		IHost targetConnection = targetFolder.getSystemConnection();
		IHost srcConnection    = srcFileOrFolder.getSystemConnection();
   	
		boolean ok = false;
   		if (targetConnection == srcConnection)
   		{
			ss = targetFolder.getParentRemoteFileSubSystem();
	
		
			ok = ss.copy(srcFileOrFolder, targetFolder, newName, null);
			if (!ok)
			{
			  SystemMessage msg = SystemPlugin.getPluginMessage(FILEMSG_COPY_FILE_FAILED);
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
			}
   		}
   		// DKM - for cross system copy
   		else
   		{
   			IRemoteFileSubSystem targetFS = targetFolder.getParentRemoteFileSubSystem();
   			IRemoteFileSubSystem srcFS    = srcFileOrFolder.getParentRemoteFileSubSystem();
   			String newPath = targetFolder.getAbsolutePath() + "/" + newName;
   			if (srcFileOrFolder.isFile())
   			{
   				SystemRemoteEditManager mgr = SystemRemoteEditManager.getDefault();
   				// if remote edit project doesn't exist, create it
   				if (!mgr.doesRemoteEditProjectExist())
   					mgr.getRemoteEditProject();
   				
   				StringBuffer path = new StringBuffer(mgr.getRemoteEditProjectLocation().makeAbsolute().toOSString());
				path = path.append("/" + srcFS.getSystemProfileName() + "/" + srcFS.getHostAliasName() + "/");

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

	   			srcFS.download(srcFileOrFolder, tempFile, null);	
		   		targetFS.upload(tempFile, newPath, null);	
   			}
   			else
   			{
   				
   				IRemoteFile newTargetFolder = targetFS.getRemoteFileObject(newPath);
   				targetFS.createFolder(newTargetFolder);
   				IRemoteFile[] children = srcFS.listFoldersAndFiles(srcFileOrFolder);
   				if (children != null)
   				{
	   				for (int i = 0; i < children.length; i++)
	   				{
	   					IRemoteFile child = children[i];
	   					monitor.subTask("copying " + child.getName());	
	   					doCopy(monitor, newTargetFolder, child, child.getName());	
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
		SystemSelectRemoteFileOrFolderDialog dlg = new SystemSelectRemoteFileOrFolderDialog(shell,dlgTitle,SystemSelectRemoteFileOrFolderDialog.FOLDER_MODE);
		dlg.setNeedsProgressMonitor(true);
		dlg.setMessage(getPromptString());
		dlg.setShowNewConnectionPrompt(false);
		dlg.setShowPropertySheet(true, false);
		firstSelection = getFirstSelectedFile();
		sourceConnection = firstSelection.getSystemConnection();
		dlg.setSystemConnection(sourceConnection);
		if (mode==MODE_MOVE)
		  dlg.setSelectionValidator(this);
		//SystemPlugin.logInfo("Calling getParentRemoteFile for '"+firstSelection.getAbsolutePath()+"'");
		firstSelectionParent = firstSelection.getParentRemoteFile();
		/*
		if (firstSelectionParent != null)
		  SystemPlugin.logInfo("Result of getParentRemoteFile: '"+firstSelectionParent.getAbsolutePath()+"'");
		else
		  SystemPlugin.logInfo("Result of getParentRemoteFile: null");
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
			if (!singleTitle.startsWith("Missing")) // TODO: remove test after next mri rev         	
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
		SystemSelectRemoteFileOrFolderDialog cpyDlg = (SystemSelectRemoteFileOrFolderDialog)dlg;		
		Object targetContainer = null;
		if (!cpyDlg.wasCancelled())
		   targetContainer = cpyDlg.getSelectedObject();
	    return targetContainer;
	}

	/**
	 * Called after all the copy/move operations end, be it successfully or not.
	 * Your opportunity to display completion or do post-copy selections/refreshes
	 */
	public void copyComplete() 
	{
		if (copiedFiles.size() == 0)
		  return;

		// refresh all instances of this parent, and all affected filters...
		ISubSystem fileSS = targetFolder.getParentRemoteFileSubSystem();
		Viewer originatingViewer = null;
		if (getViewer() instanceof Viewer)
		{		
		  originatingViewer = (Viewer)getViewer();
          if (!targetFolder.getAbsolutePath().equals(firstSelectionParent.getAbsolutePath()))
          {
          	  // we select the first instance of the target folder now so that the copied members will be selected in it
          	  //  after it is refreshed via the remote_resource_created event.
          	  if (originatingViewer instanceof SystemView)
          	  {
                 boolean selectedOk = ((SystemView)originatingViewer).selectRemoteObjects(targetFolder.getAbsolutePath(), fileSS, null);
                 //System.out.println(targetFolder.getAbsolutePath()+" selectedOK? " + selectedOk);
                 //if (selectedOk)
                 //  return;
          	  }
          }
		}
		SystemPlugin.getTheSystemRegistry().fireRemoteResourceChangeEvent(
		   ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED, copiedFiles, targetFolder.getAbsolutePath(), fileSS, null, originatingViewer);
		
		/* Old release 1.0 way...
		// did they copy to the same parent? Just refresh that parent, whatever it is...
        if (targetFolder.getAbsolutePath().equals(firstSelectionParent.getAbsolutePath()))
        {
		  Viewer v = getViewer();
		  if (v instanceof ISystemTree)
		  {
		    SystemRegistry sr = SystemPlugin.getTheSystemRegistry();
		  	ISystemTree tree = (ISystemTree)v;
		  	Object parent = tree.getSelectedParent();
		  	if (parent == null)
		  	  return;
		  	if (parent instanceof IRemoteFile)
		  	  // refresh parent in all views...
		      sr.fireEvent(
                 new org.eclipse.rse.model.SystemResourceChangeEvent(
                    parent,ISystemResourceChangeEvent.EVENT_REFRESH_REMOTE, null)
		      );
		  	else
		  	  // refresh parent in all views...
		      sr.fireEvent(
                 new org.eclipse.rse.model.SystemResourceChangeEvent(
                    parent,ISystemResourceChangeEvent.EVENT_REFRESH, null)
		      );
		    // select new files in this view only
		    sr.fireEvent((ISystemResourceChangeListener)v,
                 new org.eclipse.rse.model.SystemResourceChangeEvent(
                    copiedFiles,ISystemResourceChangeEvent.EVENT_SELECT_REMOTE, targetFolder)
		    );
		  }
        }
        // they copied somewhere else... a little more work...
		else
		{
			// refresh target folder in all views, but only select new files in this view...
			org.eclipse.rse.model.SystemResourceChangeEvent event = 
			  new org.eclipse.rse.model.SystemResourceChangeEvent(
			        targetFolder,ISystemResourceChangeEvent.EVENT_REFRESH_REMOTE, copiedFiles);
			event.setOriginatingViewer(getViewer());
		    sr.fireEvent(event);
		}
		*/
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
   	      IStructuredSelection selection = (IStructuredSelection)getSelection();
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