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
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.internal.model.SystemPreferenceChangeEvent;
import org.eclipse.rse.model.ISystemPreferenceChangeEvents;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;


/**
 * The action is a shortcut to the preferences setting for showing connection names
 *   qualified by profile name.
 */
public class SystemPreferenceQualifyConnectionNamesAction extends SystemBaseAction 
                                 
{
	
    private ISystemRegistry sr = null;
	/**
	 * Constructor
	 */
	public SystemPreferenceQualifyConnectionNamesAction(Shell parent) 
	{
		super(SystemResources.ACTION_QUALIFY_CONNECTION_NAMES_LABEL,SystemResources.ACTION_QUALIFY_CONNECTION_NAMES_TOOLTIP,
		      parent);
        setSelectionSensitive(false);
        allowOnMultipleSelection(true);
        sr = SystemPlugin.getTheSystemRegistry();	        
        setChecked(sr.getQualifiedHostNames());

		setHelp(SystemPlugin.HELPPREFIX+"actn0008");
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		boolean newState = isChecked();
        sr.setQualifiedHostNames(newState);        
    	firePreferenceChangeEvent(ISystemPreferenceChangeEvents.EVENT_QUALIFYCONNECTIONNAMES,
    	                          !newState,newState);  // defect 41794             
	}		
	
    /**
     * Fire a preference change event
     */
    private void firePreferenceChangeEvent(int type, boolean oldValue, boolean newValue)
    {
    	SystemPlugin.getDefault().getSystemRegistry().fireEvent(
    	  new SystemPreferenceChangeEvent(type,
    	                                  oldValue ? Boolean.TRUE : Boolean.FALSE,
    	                                  newValue ? Boolean.TRUE : Boolean.FALSE));
    }
	
}