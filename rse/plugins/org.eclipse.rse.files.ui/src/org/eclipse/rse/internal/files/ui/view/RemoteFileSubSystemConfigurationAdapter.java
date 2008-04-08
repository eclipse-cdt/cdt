/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [189123] Move renameSubSystemProfile() from UI to Core
 * David Dykstal (IBM) - [168976][api] move ISystemNewConnectionWizardPage from core to UI
 ********************************************************************************/

package org.eclipse.rse.internal.files.ui.view;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.files.ui.actions.SystemFileUpdateFilterAction;
import org.eclipse.rse.internal.files.ui.actions.SystemNewFileAction;
import org.eclipse.rse.internal.files.ui.actions.SystemNewFileFilterAction;
import org.eclipse.rse.internal.files.ui.actions.SystemNewFolderAction;
import org.eclipse.rse.internal.files.ui.wizards.SystemFileNewConnectionWizardPage;
import org.eclipse.rse.internal.subsystems.files.core.ISystemFilePreferencesConstants;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.view.IContextObject;
import org.eclipse.rse.ui.view.SubSystemConfigurationAdapter;
import org.eclipse.rse.ui.wizards.newconnection.ISystemNewConnectionWizardPage;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;


public class RemoteFileSubSystemConfigurationAdapter extends SubSystemConfigurationAdapter
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
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.ui.view.SubSystemConfigurationAdapter#getAdditionalFilterActions(org.eclipse.rse.core.subsystems.ISubSystemConfiguration, org.eclipse.rse.core.filters.ISystemFilter, org.eclipse.swt.widgets.Shell)
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
			
			Clipboard clipboard = RSEUIPlugin.getTheSystemRegistryUI().getSystemClipboard();
			_additionalActions.add(new SystemPasteFromClipboardAction(shell, clipboard));
    	}
		return _additionalActions;	
    }  
    
	/**
	 * Checks the preference setting for hidden files and filters out hidden files if the preference setting is to not show hidden files.
	 * @see org.eclipse.rse.ui.view.SubSystemConfigurationAdapter#applyViewFilters(org.eclipse.rse.ui.view.IContextObject, java.lang.Object[])
	 */
	public Object[] applyViewFilters(IContextObject parent, Object[] children) {
		
		boolean showHidden = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemFilePreferencesConstants.SHOWHIDDEN);
		
		if (showHidden) {
			return children;
		}
		else {
			
			ArrayList results = new ArrayList(children.length);
		
			for (int i = 0; i < children.length; i++) {
			
				if (children[i] instanceof IRemoteFile) {
					IRemoteFile remoteFile = (IRemoteFile)(children[i]);
				
					if (!remoteFile.isHidden()) {
						results.add(remoteFile);
					}
				}
				else if (children[i] instanceof ISystemMessageObject){
					results.add(children[i]);
				}
			}
			
			return results.toArray();
		}
	}
}