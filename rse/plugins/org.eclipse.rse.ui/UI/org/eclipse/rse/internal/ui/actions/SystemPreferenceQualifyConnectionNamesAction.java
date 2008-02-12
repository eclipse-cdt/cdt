/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [215820] Move SystemRegistry implementation to Core
 ********************************************************************************/

package org.eclipse.rse.internal.ui.actions;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemPreferenceChangeEvents;
import org.eclipse.rse.internal.core.model.SystemPreferenceChangeEvent;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;


/**
 * The action is a shortcut to the preferences setting for showing connection names
 *   qualified by profile name.
 */
public class SystemPreferenceQualifyConnectionNamesAction extends SystemBaseAction 
                                 
{
	/**
	 * Constructor
	 */
	public SystemPreferenceQualifyConnectionNamesAction(Shell parent) 
	{
		super(SystemResources.ACTION_QUALIFY_CONNECTION_NAMES_LABEL,SystemResources.ACTION_QUALIFY_CONNECTION_NAMES_TOOLTIP,
		      parent);
        setSelectionSensitive(false);
        allowOnMultipleSelection(true);
        setChecked(RSEUIPlugin.getTheSystemRegistryUI().getQualifiedHostNames());

		setHelp(RSEUIPlugin.HELPPREFIX+"actn0008"); //$NON-NLS-1$
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		boolean newState = isChecked();
		RSEUIPlugin.getTheSystemRegistryUI().setQualifiedHostNames(newState);        
    	firePreferenceChangeEvent(ISystemPreferenceChangeEvents.EVENT_QUALIFYCONNECTIONNAMES,
    	                          !newState,newState);  // defect 41794             
	}		
	
    /**
     * Fire a preference change event
     */
    private void firePreferenceChangeEvent(int type, boolean oldValue, boolean newValue)
    {
    	RSECorePlugin.getTheSystemRegistry().fireEvent(
    	  new SystemPreferenceChangeEvent(type,
    	                                  oldValue ? Boolean.TRUE : Boolean.FALSE,
    	                                  newValue ? Boolean.TRUE : Boolean.FALSE));
    }
	
}