/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.files.ui.view;

import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.files.ui.actions.SystemFileUpdateFilterAction;
import org.eclipse.rse.files.ui.actions.SystemNewFileAction;
import org.eclipse.rse.files.ui.actions.SystemNewFileFilterAction;
import org.eclipse.rse.files.ui.actions.SystemNewFolderAction;
import org.eclipse.rse.files.ui.resources.SystemIFileProperties;
import org.eclipse.rse.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.files.ui.wizards.SystemFileNewConnectionWizardPage;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.view.SubsystemFactoryAdapter;
import org.eclipse.rse.ui.wizards.ISystemNewConnectionWizardPage;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;


public class RemoteFileSubSystemFactoryAdapter extends SubsystemFactoryAdapter
{
	
	SystemNewFileFilterAction _newFileFilterAction;
	SystemFileUpdateFilterAction _changeFilerAction;
	
	Vector _additionalActions;
	
	// -----------------------------------
	// WIZARD PAGE CONTRIBUTION METHODS... (defects 43194 and 42780)
	// -----------------------------------
	/**
	 * Optionally return one or more wizard pages to append to the New Connection Wizard if
	 *  the user selects a system type that this subsystem factory supports.
	 * <p>
	 * Tip: consider extending AbstractSystemWizardPage for your wizard page class.
	 */
	public ISystemNewConnectionWizardPage[] getNewConnectionWizardPages(ISubSystemConfiguration factory, IWizard wizard)
	{
		ISystemNewConnectionWizardPage[] basepages = super.getNewConnectionWizardPages(factory, wizard);
		
		// DKM - for now reverting back to not showing port on wizard page
		//  in UCD sessions, users were too confused by that page
		if (false /*isPortEditable()*/)
		{
		  SystemFileNewConnectionWizardPage page = new SystemFileNewConnectionWizardPage(wizard, factory);		  
		  ISystemNewConnectionWizardPage[] newPages = new ISystemNewConnectionWizardPage[basepages.length + 1];
		  newPages[0] = page;
		  for (int i = 0; i < basepages.length; i++)
		  {
		  	newPages[i+1] = basepages[i];
		  }
		  basepages = newPages;
		}
		return basepages;
	}
	
    /**
     * Overridable parent method to return the action for creating a new filter inside a filter pool
     * Returns new SystemNewFileFilterAction.
     */
    protected IAction getNewFilterPoolFilterAction(ISubSystemConfiguration factory, ISystemFilterPool selectedPool, Shell shell)
    {
    	if (_newFileFilterAction == null)
    	{
    		_newFileFilterAction = new SystemNewFileFilterAction((IRemoteFileSubSystemConfiguration)factory, selectedPool, shell); 
    	}
    	else
    	{
    		_newFileFilterAction.setParentFilterPool(selectedPool);
    		

    	}
    	return _newFileFilterAction;
    }

    /**
     * Overridable method to return the action for changing an existing filter.
     * Returns new SystemFileUpdateFilterAction.
     */
    protected IAction getChangeFilterAction(ISubSystemConfiguration factory, ISystemFilter selectedFilter, Shell shell)
    {
    	if (_changeFilerAction == null)
    	{
    		_changeFilerAction = new SystemFileUpdateFilterAction(shell);
    	}
    	return _changeFilerAction;
    }  
    
    /**
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#getAdditionalFilterActions(ISystemFilter,Shell)
     */
    protected Vector getAdditionalFilterActions(ISubSystemConfiguration factory, ISystemFilter selectedFilter, Shell shell)
    {
    	if (_additionalActions == null)
    	{
	    	_additionalActions = super.getAdditionalFilterActions(factory, selectedFilter, shell);
	    	if (selectedFilter.isPromptable())
	    	  return _additionalActions;
			if (_additionalActions == null)
				_additionalActions = new Vector();	
	
			// following added by Phil for release 2, Nov 10 2002
			_additionalActions.add(new SystemNewFileAction(shell));
			_additionalActions.add(new SystemNewFolderAction(shell));
			
			// DKM
			// FIXME - can't do this here anymore
			//_additionalActions.add(new SystemCommandAction(shell, true));
			
			ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
			Clipboard clipboard = registry.getSystemClipboard();
			_additionalActions.add(new SystemPasteFromClipboardAction(shell, clipboard));
    	}
		return _additionalActions;	
    }  
    
	/**
	 * Called by SystemRegistry's renameSystemProfile method to ensure we update our
	 *  subsystem names within each subsystem.
	 * <p>
	 * Must be called AFTER changing the profile's name!!
	 */
	public void renameSubSystemProfile(ISubSystemConfiguration factory, String oldProfileName, String newProfileName)
	{		
		super.renameSubSystemProfile(factory, oldProfileName, newProfileName);
		
		// change all IFile properties in remote systems temp files tree
		IProject project = SystemBasePlugin.getWorkspaceRoot().getProject(SystemRemoteEditManager.REMOTE_EDIT_PROJECT_NAME);
		if (project != null)
		{
			IFolder folder = project.getFolder(oldProfileName);
			if (folder != null && folder.exists())
			{		
				// recursively change all subsystem ids	for the temp files
				recursivelyUpdateIFileProperties(newProfileName, folder);
			}				
		}
	}


	protected void recursivelyUpdateIFileProperties(String newName, IFolder container)
	{
		try
		{
		IResource[] resources = container.members();		
		for (int i = 0; i < resources.length; i++)
		{
			IResource resource = resources[i];
			if (resource instanceof IFile)
			{				
				IFile file = (IFile)resource;
				SystemIFileProperties properties = new SystemIFileProperties(file);
				
				String absoluteSubSystemName = properties.getRemoteFileSubSystem();
				if (absoluteSubSystemName != null)
				{
					int profileDelim = absoluteSubSystemName.indexOf(".");
					String theRest = absoluteSubSystemName.substring(profileDelim, absoluteSubSystemName.length());										
					properties.setRemoteFileSubSystem(newName + theRest);			
				}
			}
			else if (resource instanceof IFolder)
			{
				recursivelyUpdateIFileProperties(newName, (IFolder)resource);	
			}
		}		
		}
		catch (Exception e)
		{
		}
	}
}