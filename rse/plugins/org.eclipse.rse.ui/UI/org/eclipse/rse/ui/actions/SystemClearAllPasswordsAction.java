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

package org.eclipse.rse.ui.actions;


import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Shell;


/**
 * Action to clear cached passwords for all subsystems in a connection.
 */
public class SystemClearAllPasswordsAction extends SystemBaseAction {

	/**
	 * Constructor.
	 * @param shell the parent shell.
	 */
	public SystemClearAllPasswordsAction(Shell shell) {
	    super(SystemResources.ACTION_CLEARPASSWORD_ALL_LABEL, SystemResources.ACTION_CLEARPASSWORD_ALL_TOOLTIP, shell);
	    allowOnMultipleSelection(false);
	    setContextMenuGroup(ISystemContextMenuConstants.GROUP_CONNECTION);
	}

    /**
     * @see org.eclipse.rse.ui.actions.SystemBaseAction#checkObjectType(java.lang.Object)
     */
    public boolean checkObjectType(Object selectedObject) {
        
		if (!(selectedObject instanceof IHost)) {
		    return false;
		}
		else {
		    
	        IHost conn = (IHost)selectedObject;

	        ISubSystem[] subsystems = conn.getSubSystems();      
            
            boolean anyOk = false;
	        
	        for (int i = 0; i < subsystems.length; i++) {
	            
	            ISubSystem subsystem = subsystems[i];
	            IConnectorService system = subsystem.getConnectorService();
	            
	            anyOk = !system.isConnected() && system.isPasswordCached();
	            
	            if (anyOk) 
	            {
	                return true;
	            }
	        }
	        
	        return anyOk;
		}
    }
    
    /**
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        
        IHost conn = (IHost)getFirstSelection();
        
        ISubSystem[] subsystems = conn.getSubSystems();
        
        for (int i = 0; i < subsystems.length; i++) 
        {
            ISubSystem ss = subsystems[i];
        	try 
        	{
    			IConnectorService system = ss.getConnectorService();
    		
    			if (system.isPasswordCached())
    			{
	    			// get the user id
	    			String userId = system.getUserId();
	    			
	    			// clear userid/password from memory and fire event
	    			//DKM and disk now
	    			system.clearPasswordCache(true);
	    			SystemPlugin.getTheSystemRegistry().fireEvent(new SystemResourceChangeEvent(ss, 
	    					ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE, 
	    					ss.getHost()));
    			}

    		}
    		catch (Exception exc) 
    		{
    			// msg already shown
    		}
        }
    }
}