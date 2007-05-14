/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - moved SystemPreferencesManager to a new package
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 ********************************************************************************/

package org.eclipse.rse.internal.ui.actions;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemPreferenceChangeEvents;
import org.eclipse.rse.internal.core.model.SystemPreferenceChangeEvent;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemPreferencesManager;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.swt.widgets.Shell;


/**
 * The action allows users to decide whether or not to show filter pools in the Remote System Explorer.
 * It is a fastpath/convenience method for this option in the preferences page.
 */
public class SystemPreferenceShowFilterPoolsAction extends SystemBaseAction 
                                 
{
	
    //private SystemRegistry sr = null;
	/**
	 * Constructor
	 */
	public SystemPreferenceShowFilterPoolsAction(Shell parent) 
	{
		super(SystemResources.ACTION_PREFERENCE_SHOW_FILTERPOOLS_LABEL,SystemResources.ACTION_PREFERENCE_SHOW_FILTERPOOLS_TOOLTIP,
		      parent);
        allowOnMultipleSelection(true);        
        setChecked(SystemPreferencesManager.getShowFilterPools());
        setSelectionSensitive(false);
        
		setHelp(RSEUIPlugin.HELPPREFIX+"actn0011"); //$NON-NLS-1$
	}

	/**
	 * This is the method called when the user selects this action.
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() 
	{
		boolean newState = isChecked();
        SystemPreferencesManager.setShowFilterPools(newState);
    	firePreferenceChangeEvent(ISystemPreferenceChangeEvents.EVENT_SHOWFILTERPOOLS,
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