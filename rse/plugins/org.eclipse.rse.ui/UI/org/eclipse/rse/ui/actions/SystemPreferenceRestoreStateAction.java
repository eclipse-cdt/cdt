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

package org.eclipse.rse.ui.actions;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.internal.model.SystemPreferenceChangeEvent;
import org.eclipse.rse.model.ISystemPreferenceChangeEvents;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;


/**
 * The action is a shortcut to the preferences setting for restoring the RSE to its 
 *   previous state.
 */
public class SystemPreferenceRestoreStateAction extends SystemBaseAction 
                                 
{
	
    private ISystemRegistry sr = null;
	/**
	 * Constructor
	 */
	public SystemPreferenceRestoreStateAction(Shell parent) 
	{
		super(SystemResources.ACTION_RESTORE_STATE_PREFERENCE_LABEL,SystemResources.ACTION_RESTORE_STATE_PREFERENCE_TOOLTIP, parent);
        setSelectionSensitive(false);
        allowOnMultipleSelection(true);
        sr = RSEUIPlugin.getTheSystemRegistry();	        
        setChecked(SystemPreferencesManager.getPreferencesManager().getRememberState());

		setHelp(RSEUIPlugin.HELPPREFIX+"aprefres");
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		boolean newState = isChecked();
		SystemPreferencesManager.getPreferencesManager().setRememberState(newState);        
    	firePreferenceChangeEvent(ISystemPreferenceChangeEvents.EVENT_RESTORESTATE,
    	                          !newState,newState);               
	}		
	
    /**
     * Fire a preference change event
     */
    private void firePreferenceChangeEvent(int type, boolean oldValue, boolean newValue)
    {
    	RSEUIPlugin.getDefault().getSystemRegistry().fireEvent(
    	  new SystemPreferenceChangeEvent(type,
    	                                  oldValue ? Boolean.TRUE : Boolean.FALSE,
    	                                  newValue ? Boolean.TRUE : Boolean.FALSE));
    }	
}