/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui.wizards;

import java.util.Vector;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.files.ui.FileResources;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemRemoteChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileFilterString;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.subsystems.files.core.subsystems.IVirtualRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileIOException;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileSecurityException;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.ISystemTree;
import org.eclipse.rse.ui.wizards.AbstractSystemWizard;


public class SystemNewFileWizard 
                  extends AbstractSystemWizard 
                  implements  ISystemMessages 
{	
	
	private SystemNewFileWizardMainPage mainPage;
	//protected IRemoteFile parentFolder;
	//protected IStructuredSelection selection;
  
    private static final String CLASSNAME = "SystemNewFileWizard";   

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
	      addPage((WizardPage)mainPage);
	      //super.addPages();
	   } catch (Exception exc)
	   {
	   	 SystemBasePlugin.logError("New File: Error in createPages: ",exc);
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
            IRemoteFileSubSystem rfss = parentFolder.getParentRemoteFileSubSystem(); 
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
            IRemoteFile newFile = null;            
            try {    	
                IRemoteFile newFilePath = rfss.getRemoteFileObject(absName); 
                newFile = rfss.createFile(newFilePath);
            } catch (RemoteFileIOException exc ) {
               SystemBasePlugin.logDebugMessage(CLASSNAME+ ":", " Creating remote file "+ absName + " failed with RemoteFileIOException " );  	
               msg = (RSEUIPlugin.getPluginMessage(FILEMSG_CREATE_FILE_FAILED_EXIST)).makeSubstitution(absName);
	           mainPage.setMessage(msg);
	           ok = false;
//DY        } catch (Exception RemoteFileSecurityException)  {
            } catch (RemoteFileSecurityException e)  {
               msg = (RSEUIPlugin.getPluginMessage(FILEMSG_CREATE_FILE_FAILED)).makeSubstitution(absName);
	           SystemBasePlugin.logDebugMessage(CLASSNAME+ ":", " Creating remote file "+ absName + " failed with RemoteFileSecurityException ");  	
               //SystemMessage.displayErrorMessage(SystemMessage.getDefaultShell(), msg); 
	           mainPage.setMessage(msg);	                                                
	           ok = false;
            } catch (SystemMessageException e) {
            	SystemBasePlugin.logError(CLASSNAME+ ":", e);
            	mainPage.setMessage(e.getSystemMessage());
            	ok = false;
            }
		          
		   // return ok;
		   if (ok) 
		     updateGUI(parentFolder, newFile, getViewer(), isInputAFilter(), getSelectedFilterReference());

		}
		else
		  ok = false;
		  
		
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
		boolean meets = false;
		IRemoteFileSubSystem parentSubSystem = (IRemoteFileSubSystem)selectedFilterRef.getSubSystem();
		meets = parentSubSystem.doesFilterMatch(selectedFilterRef.getReferencedFilter(),newAbsName);
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
	 * Called after all a successful create operation to update the GUI in an intuitive way:
	 * <ul>
	 *   <li>The parent folder is refreshed in all views
	 *   <li>The newly created file or folder is selected in current view
	 * </ul>
	 */
	protected static void updateGUI(IRemoteFile parentFolder, IRemoteFile newFileOrFolder, Viewer viewer,  
	                                  boolean isInputAFilter, ISystemFilterReference selectedFilterRef)
	{
		ISystemRegistry sr = RSEUIPlugin.getTheSystemRegistry();
		if (selectedFilterRef != null)
		{
			selectedFilterRef.markStale(true);
		}
		
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
				IRemoteFile[] roots = parentSubSystem.listRoots();
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
			        folder = parentSubSystem.getRemoteFileObject(pathName);
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