/********************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David Dykstal (IBM) - [188718] fix error messages showing up as info messages on wizard page
 * Xuan Chen        (IBM)        - [209828] Need to move the Create operation to a job.
 ********************************************************************************/

package org.eclipse.rse.internal.files.ui.wizards;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.rse.services.files.RemoteFileSecurityException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.wizards.AbstractSystemWizard;


public class SystemNewFolderWizard 
                  extends AbstractSystemWizard 
{	
	
	private SystemNewFolderWizardMainPage mainPage;
	//protected IRemoteFile myObject;
	//protected IStructuredSelection selection;
  
    private static final String CLASSNAME = "SystemNewFolderWizard";    //$NON-NLS-1$

    private class CreateNewFolderJob extends WorkspaceJob
	{
		IRemoteFile parentFolder = null;
		String      name = null;
		String      absName = null;
		String      message = null;
		
		/**
		 * CreateNewFileJob job.
		 * @param message text used as the title of the job
		 */
		public CreateNewFolderJob(IRemoteFile parentFolder, String name, String absName, String message)
		{
			super(message);
			this.parentFolder = parentFolder;
			this.name = name;
			this.absName = absName;
			this.message = message;
			setUser(true);
		}

		public IStatus runInWorkspace(IProgressMonitor monitor) 
		{
			boolean ok = true;
			IStatus status = Status.OK_STATUS;
			SystemMessage msg; 
			IRemoteFileSubSystem rfss = parentFolder.getParentRemoteFileSubSystem(); 
			
            // ok, proceed with actual creation...
            IRemoteFile newFolder = null;         
            try 
            {    	
                IRemoteFile newFolderPath = rfss.getRemoteFileObject(absName, monitor); 
                newFolder = rfss.createFolder(newFolderPath, monitor);
            } 
            catch (RemoteFileIOException exc ) 
            {	
            	ok = false;
            	SystemBasePlugin.logDebugMessage(CLASSNAME+ ":", " Creating remote file "+ absName + " failed with RemoteFileIOException " );  	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            	msg = (RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_CREATE_FOLDER_FAILED_EXIST)).makeSubstitution(absName);
            	SystemMessageDialog.displayErrorMessage(null, msg);
            } 
            catch (RemoteFileSecurityException e)  
            {
            	ok = false;
            	msg = (RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_CREATE_FOLDER_FAILED)).makeSubstitution(absName);
            	SystemBasePlugin.logDebugMessage(CLASSNAME+ ":", " Creating remote folder "+ absName + " failed with RemoteFileSecurityException ");  	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            	SystemMessageDialog.displayErrorMessage(null, msg);                                               
            } 
            catch (SystemMessageException exc)
			{
            	ok = false;
            	if (monitor.isCanceled())
            	{
            		status = Status.CANCEL_STATUS;
            	}
				SystemMessageDialog.displayErrorMessage(null, exc.getSystemMessage());
			}
		          
		   if (ok) 
		   {
			   SystemNewFileWizard.updateGUI(parentFolder, newFolder, getViewer(), isInputAFilter(), getSelectedFilterReference());
		   }
		
		   return status;
		}

	}
    
    /**
     * Constructor
     */	
	public SystemNewFolderWizard()
	{
	   	super(FileResources.RESID_NEWFOLDER_TITLE,
			//  RSEUIPlugin.getDefault().getImageDescriptorFromIDE("wizban/newfolder_wiz.gif")
	 	      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWFOLDERWIZARD_ID)
	 	      );		      
	}
	
	/**
	 * Creates the wizard pages.
	 * This method is an override from the parent AbstractSystemWizard class.
	 */
	public void addPages()
	{
	   try {
	      mainPage = createMainPage();	        
	      addPage(mainPage);
	      //super.addPages();
	   } catch (Exception exc)
	   {
	   	 SystemBasePlugin.logError("New File: Error in createPages: ",exc); //$NON-NLS-1$
	   }
	} 

	/**
	 * Creates the wizard's main page. 
	 * This method is an override from the parent class.
	 */
	protected SystemNewFolderWizardMainPage createMainPage()
	{
		IRemoteFile[] parentFolders = null;
		SystemMessage errMsg = null;
		try {
			parentFolders = getParentFolders();
		} catch (SystemMessageException exc)
		{
			// hmm, this means there are no valid folders so we are in error mode.
			errMsg = exc.getSystemMessage();
		}
   	    mainPage = new SystemNewFolderWizardMainPage(this, parentFolders); 
   	    if (errMsg != null)
   	      mainPage.setErrorMessage(errMsg);
   	    return mainPage;
	}     

	/**
	 * Completes processing of the wizard. If this
	 * method returns true, the wizard will close;
	 * otherwise, it will stay active.
	 * This method is an override from the parent Wizard class.
	 *
	 * @return whether the wizard finished successfully
	 */
	public boolean performFinish()
	{
		boolean ok = true;
		if (mainPage.performFinish())
		{
			SystemMessage msg = null;
            IRemoteFile parentFolder = mainPage.getParentFolder();
			String name = mainPage.getfolderName();
			String absName = SystemNewFileWizard.getNewAbsoluteName(parentFolder, name);
            if (!parentFolder.exists())
            {
            	/* Be nice to do this someday...
			    msg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_FOLDER_NOTFOUND_WANTTOCREATE);
			    msg.makeSubstitution(parentFolder.getAbsolutePath());            	
			    SystemMessageDialog msgDlg = new SystemMessageDialog(getShell(), msg);
			    if (msgDlg.openQuestionNoException())
			    {
			  	   try {
			  	     parentFolder = rfss.createFolder(parentFolder);
			  	   } 
			  	   catch (RemoteFileIOException exc)
			  	   {
                     RSEUIPlugin.logDebugMessage(CLASSNAME+ ":", " Creating remote folder "+ absName + " failed with RemoteFileIOException " );  	
                     msg = (RSEUIPlugin.getPluginMessage(FILEMSG_CREATE_FOLDER_FAILED_EXIST)).makeSubstitution(parentFolder.getAbsolutePath());
	                 mainPage.setMessage(msg);	
	                 return false;		  	   	 
			  	   }
			  	   catch (RemoteFileSecurityException exc)
			  	   {
                     RSEUIPlugin.logDebugMessage(CLASSNAME+ ":", " Creating remote folder "+ absName + " failed with RemoteFileSecurityException " );  	
                     msg = (RSEUIPlugin.getPluginMessage(FILEMSG_CREATE_FOLDER_FAILED)).makeSubstitution(parentFolder.getAbsolutePath());
	                 mainPage.setMessage(msg);	
	                 return false;		  	   	 
			  	   }
			    }
			    else
			    */
			    {
			  	   msg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_FOLDER_NOTFOUND);
			       msg.makeSubstitution(parentFolder.getAbsolutePath());            	
			       mainPage.setMessage(msg);
			       return false;
			    }
            }
            // if input is a filter, then we need to test if the new file will meet the filtering criteria of 
            //  that filter...
            if (isInputAFilter()) 
            {
            	if (!meetsFilterCriteria(getSelectedFilterReference(), parentFolder, absName))
            	  return false;
            }
            
            // ok, proceed with actual creation...
            SystemMessage createMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CREATEFOLDERGENERIC_PROGRESS); 
            createMessage.makeSubstitution(name); 
            CreateNewFolderJob createNewFolderJob = new CreateNewFolderJob(parentFolder, name, absName,  createMessage.getLevelOneText());
            createNewFolderJob.schedule();
            
		}
		return ok;
 
	}
	/**
	 * Test if the new file/folder will meet the filtering criteria of the selected filter.
	 * For folders, since we do not support subsetting by folder names, we simply need to test if any
	 *  of the filter strings in the filter has showSubDirs() set to true.
	 */
	protected boolean meetsFilterCriteria(ISystemFilterReference selectedFilterRef, IRemoteFile parentFolder, String newAbsName)
	{
		boolean meets = false;
		ISystemFilter filter = selectedFilterRef.getReferencedFilter();
    	String[] strings = filter.getFilterStrings();
    	if (strings != null)
    	{
    	  for (int idx=0; !meets && (idx<strings.length); idx++)
    	  {
    	  	String filterString = strings[idx];    	  	
    	  	if (filterString.indexOf("/ns") == -1) // if this filter string allows folders (no /ns switch) then we have a match! //$NON-NLS-1$
    	  	{
    	  		if (strings.length > 0 && !filterString.startsWith(parentFolder.getAbsolutePath()))
	    	  	{
    	  		    if (!filterString.equals("./*")) //DKM - ./ will always meet //$NON-NLS-1$
    	  		    {
    	  		        meets = false; 	   
    	  		    }
    	  		    else
    	  		    {
    	  		        meets = true;
    	  		    }
	    	  	}
	    	  	else
	    	  	{    
	    	  	    meets = true;
	    	  	}
    	  	}
    	  }
    	}
		if (!meets)
		{
			SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_CREATE_RESOURCE_NOTVISIBLE);
			SystemMessageDialog msgDlg = new SystemMessageDialog(getShell(), msg);
			if (msgDlg.openQuestionNoException()) // ask user if they want to proceed
			  meets = true; // they do, so pretend it meets the criteria
		}
    	return meets;
	}
	
	/**
	 * Return true if input is a system filter reference, versus a folder
	 */
	protected boolean isInputAFilter()
	{
		return (getInputObject() instanceof ISystemFilterReference);
	}
	/**
	 * Get the selected filter, or null if a filter is not selected.
	 */
	protected ISystemFilterReference getSelectedFilterReference()
	{
		if (isInputAFilter())
		  return (ISystemFilterReference)getInputObject();
		else
		  return null;
	}
	
	/**
	 * Deduce the parent remote folder to hold the new folder, by examining the current selection
	 */
	protected IRemoteFile[] getParentFolders() throws SystemMessageException
	{
		Object input = getInputObject();
		if (input instanceof IRemoteFile)
		  return new IRemoteFile[] {(IRemoteFile)input};
		else if (input instanceof ISystemFilterReference)
		{
		  return SystemNewFileWizard.getParentFolders((ISystemFilterReference)input);
		}
		else
		  return null;
	}
	

} // end class