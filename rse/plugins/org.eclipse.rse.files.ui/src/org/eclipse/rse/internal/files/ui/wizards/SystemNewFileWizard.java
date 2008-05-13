/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 * Rupen Mardirossian	(IBM) - [187530] Commented out line 192, in order to stop logging of SystemMessageException
 * Martin Oberhuber (Wind River) - [204669] Fix ftp path concatenation on systems using backslash separator
 * Xuan Chen        (IBM)        - [209828] Need to move the Create operation to a job.
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * David McKnight   (IBM)        - [223204] [cleanup] fix broken nls strings in files.ui and others
 * David Dykstal (IBM) - [231841] Incorrect create messages being used. Cannot use NLS.bind if no substitution variables
 *******************************************************************************/

package org.eclipse.rse.internal.files.ui.wizards;

import java.util.Vector;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.files.ui.Activator;
import org.eclipse.rse.internal.files.ui.FileResources;
import org.eclipse.rse.internal.files.ui.ISystemFileConstants;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.rse.services.files.RemoteFileSecurityException;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.ISystemTree;
import org.eclipse.rse.ui.wizards.AbstractSystemWizard;


public class SystemNewFileWizard 
                  extends AbstractSystemWizard 
{	
	
	private SystemNewFileWizardMainPage mainPage;
	//protected IRemoteFile parentFolder;
	//protected IStructuredSelection selection;
  
    private static final String CLASSNAME = "SystemNewFileWizard";    //$NON-NLS-1$

	private class CreateNewFileJob extends WorkspaceJob
	{
		IRemoteFile parentFolder = null;
		String      name = null;
		String      absName = null;
		String      message = null;
		
		/**
		 * CreateNewFileJob job.
		 * @param message text used as the title of the job
		 */
		public CreateNewFileJob(IRemoteFile parentFolder, String name, String absName, String message)
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
            IRemoteFile newFile = null;         
            try 
            {    	
                IRemoteFile newFilePath = rfss.getRemoteFileObject(parentFolder, name, monitor); 
                newFile = rfss.createFile(newFilePath, monitor);
            } 
            catch (RemoteFileIOException exc ) 
            {	
            	ok = false;
            	SystemBasePlugin.logDebugMessage(CLASSNAME+ ":", " Creating remote file "+ absName + " failed with RemoteFileIOException " );  	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            	String msgTxt = FileResources.FILEMSG_CREATE_FILE_FAILED;
            	String msgDetails = FileResources.FILEMSG_CREATE_FILE_FAILED_DETAILS;
            	
            	msg = new SimpleSystemMessage(Activator.PLUGIN_ID, 
            			ISystemFileConstants.FILEMSG_CREATE_FILE_FAILED,
            			IStatus.ERROR, msgTxt, msgDetails);
            	SystemMessageDialog.displayErrorMessage(null, msg);
            } 
            catch (RemoteFileSecurityException e)  
            {
            	ok = false;
            	String msgTxt = FileResources.FILEMSG_CREATE_FILE_FAILED;
            	String msgDetails = FileResources.FILEMSG_CREATE_FILE_FAILED_DETAILS;
            	
            	msg = new SimpleSystemMessage(Activator.PLUGIN_ID, 
            			ISystemFileConstants.FILEMSG_CREATE_FILE_FAILED,
            			IStatus.ERROR, msgTxt, msgDetails);
            	

            	SystemBasePlugin.logDebugMessage(CLASSNAME+ ":", " Creating remote file "+ absName + " failed with RemoteFileSecurityException ");  	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
			   updateGUI(parentFolder, newFile, getViewer(), isInputAFilter(), getSelectedFilterReference());
		   }
		
		   return status;
		}

	}
	
    /**
     * Constructor
     */	
	public SystemNewFileWizard()
	{
	   	super(FileResources.RESID_NEWFILE_TITLE,
//		      RSEUIPlugin.getDefault().getImageDescriptorFromIDE("wizban/newfile_wiz.gif")
		    //  RSEUIPlugin.getDefault().getImageDescriptor("wizban/newfile_wiz.gif")

			  RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWFILEWIZARD_ID));		      
	}
	
	/**
	 * Creates the wizard pages.
	 * This method is an override from the parent Wizard class.
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
	protected SystemNewFileWizardMainPage createMainPage()
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
   	    mainPage = new SystemNewFileWizardMainPage(this, parentFolders); 
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
			String name = mainPage.getfileName();          
			String absName = getNewAbsoluteName(parentFolder, name);
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
			       String msgTxt = NLS.bind(FileResources.FILEMSG_FOLDER_NOTFOUND, parentFolder.getAbsolutePath());			    	
			  	   msg = new SimpleSystemMessage(Activator.PLUGIN_ID, 
			  			   ISystemFileConstants.FILEMSG_FILE_NOTFOUND,
			  			   IStatus.ERROR, msgTxt);
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
            
            String msgTxt = NLS.bind(FileResources.MSG_CREATEFILEGENERIC_PROGRESS, name);
            CreateNewFileJob createNewFileJob = new CreateNewFileJob(parentFolder, name, absName,  msgTxt);
            createNewFileJob.schedule();
		}
		return ok;
	}
	
	/**
	 * Create a new absolute name from the parent folder, and the new name
	 */
	protected static String getNewAbsoluteName(IRemoteFile parentFolder, String newName)
	{
		String newAbsName = null;
        char sep = parentFolder.getSeparatorChar();		
        String parentFolderPath = parentFolder.getAbsolutePath();
        
        // hack by Mike to allow virtual files and folders.
        if (parentFolder instanceof IVirtualRemoteFile)
        {
        	sep = '/';             	
        }
        else if (parentFolder.isArchive())
        {
        	sep = '/';
        	parentFolderPath = parentFolderPath + ArchiveHandlerManager.VIRTUAL_SEPARATOR;
        }
        
        // hack by Phil to fix bug when trying to create file inside root "/"... it
        //  tried to create "//file.ext".           	
       	if ((parentFolderPath.length()==1) && (parentFolderPath.charAt(0)=='/') &&
            (parentFolderPath.charAt(0)==sep))
       	  newAbsName = sep + newName; 
        else
	      newAbsName = parentFolderPath + sep + newName; 
	    return newAbsName;
	}
	
	/**
	 * Test if the new file/folder will meet the filtering criteria of the selected filter
	 */
	protected boolean meetsFilterCriteria(ISystemFilterReference selectedFilterRef, IRemoteFile parentFolder, String newAbsName)
	{
		IRemoteFileSubSystem parentSubSystem = (IRemoteFileSubSystem)selectedFilterRef.getSubSystem();
		
		ISystemFilter filter = selectedFilterRef.getReferencedFilter();
		
		// if the filter is "*", this represents the Drives filter on Windows
		// we can not create a file directly under it since it doesn't actually represent a container
		// if we create a new file or folder by right clicking on this filter, the parent folder defaults to the first drive
		// that shows up when this filter is resolved. Hence we ignore this filter from the filter matching criteria
		String[] strings = filter.getFilterStrings();
		
		if (strings != null) {
	      		
	      	for (int idx = 0; idx < strings.length; idx++) {
	      		
	      		if (strings[idx].equals("*")) { //$NON-NLS-1$
	      			return true;
	      		}
	      	}
		}		
	
		boolean meets = parentSubSystem.doesFilterMatch(filter, newAbsName);

		if (!meets)
		{
			String msgTxt = FileResources.FILEMSG_CREATE_RESOURCE_NOTVISIBLE;
			String msgDetails = FileResources.FILEMSG_CREATE_RESOURCE_NOTVISIBLE_DETAILS;
			
			SystemMessage msg = new SimpleSystemMessage(Activator.PLUGIN_ID, 
					ISystemFileConstants.FILEMSG_CREATE_RESOURCE_NOTVISIBLE,
					IStatus.ERROR, msgTxt, msgDetails);
			SystemMessageDialog msgDlg = new SystemMessageDialog(getShell(), msg);
			if (msgDlg.openQuestionNoException()) // ask user if they want to proceed
			  meets = true; // they do, so pretend it meets the criteria
		}
		return meets;
	}
	

	/**
	 * Called after all a successful create operation to update the GUI in an intuitive way:
	 * <ul>
	 *   <li>The parent folder is refreshed in all views
	 *   <li>The newly created file or folder is selected in current view
	 * </ul>
	 */
	protected static void updateGUI(IRemoteFile parentFolder, IRemoteFile newFileOrFolder, Viewer viewer,  
	                                  boolean isInputAFilter, ISystemFilterReference selectedFilterRef)
	{
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		if (selectedFilterRef != null)
		{
			selectedFilterRef.markStale(true);
		}
		
		// invalidate filters that reference this object
		// TODO: we shouldn't have to do this. Presumably step 0 below should take care of it.
		sr.invalidateFiltersFor(newFileOrFolder, parentFolder.getParentRemoteFileSubSystem());
		
     	// step 0: refresh all affected filters...
    	ISubSystem fileSS = newFileOrFolder.getParentRemoteFileSubSystem();
    	sr.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED, newFileOrFolder, parentFolder, fileSS, null, viewer);
    	
    	/*
		// step 1: refresh all occurrences of the parent folder
		SystemResourceChangeEvent event = new SystemResourceChangeEvent(
                parentFolder,ISystemResourceChangeEvent.EVENT_REFRESH_REMOTE, newFileOrFolder);
		event.setOriginatingViewer(viewer); // this allows only the originating view to select the new file
		sr.fireEvent(event);	
		*/	
		// step 2: if the selected input is a filter, vs a folder, refresh that filter...
		if (isInputAFilter && (viewer instanceof ISystemTree))
		{
		    // get originating tree view	
			ISystemTree treeViewer = (ISystemTree)viewer;
		    // select new file/folder in this view only, and expand filter if not already expanded...
		    sr.fireEvent((ISystemResourceChangeListener)treeViewer,
                new SystemResourceChangeEvent(newFileOrFolder,ISystemResourceChangeEvents.EVENT_SELECT_REMOTE, selectedFilterRef));
		}
		
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
		  return getParentFolders((ISystemFilterReference)input);
		else
		  return null;
	}
	
	/**
	 * Given a filter reference, return a list of all the unique root folders, one per filter string at most.
	 * If all of these fail to resolve, an exception is thrown. If some fail to resolve, they are just ignored.
	 */
	public static IRemoteFile[] getParentFolders(ISystemFilterReference filterRef) throws SystemMessageException
	{
		ISystemFilter filter = filterRef.getReferencedFilter();
		IRemoteFileSubSystem parentSubSystem = (IRemoteFileSubSystem)filterRef.getSubSystem();
		IRemoteFileSubSystemConfiguration parentFactory   = parentSubSystem.getParentRemoteFileSubSystemConfiguration();
		String[] filterStrings = filter.getFilterStrings();
        RemoteFileFilterString rffs = null;
        Vector v = new Vector();
        Vector uniqueNames = new Vector();
        IRemoteFile folder = null;
        String pathName = null;
        SystemMessageException lastExc = null;
		for (int idx=0; idx<filterStrings.length; idx++)
		{
			rffs = new RemoteFileFilterString(parentFactory, filterStrings[idx]);
			if (rffs.listRoots())
			{
				try {
				IRemoteFile[] roots = parentSubSystem.listRoots(null);
				for (int rootIdx = 0; (roots!=null) && (rootIdx<roots.length); rootIdx++)
				{
			         if (uniqueNames.indexOf(roots[idx].getName())==-1)
			           v.add(roots[rootIdx]);
				}
				} catch (Exception exc) {}
			}
			else
			{
			  pathName = rffs.getPath();
			  if (uniqueNames.indexOf(pathName)==-1)
			  {
				  uniqueNames.add(pathName);
				  try {
			        folder = parentSubSystem.getRemoteFileObject(pathName, new NullProgressMonitor());
			        // decided to do folder existence checking when Finish pressed
			        //if (folder.exists())
			          v.add(folder);
			        //else
			        //{
			        //	SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_FOLDER_NOTFOUND);
			        //	msg.makeSubstitution(pathName);
			        //	lastExc = new SystemMessageException(msg);
			        //}
				  } catch (SystemMessageException exc)
				  {
				  	lastExc = exc;
				  }
			  }
			}
		}
		if ((lastExc != null) && (v.size()==0))
		  throw lastExc;
		IRemoteFile[] folders = new IRemoteFile[v.size()];
		for (int idx=0; idx<folders.length; idx++)
		   folders[idx] = (IRemoteFile)v.elementAt(idx);
		return folders; 
	}
	
} // end class
