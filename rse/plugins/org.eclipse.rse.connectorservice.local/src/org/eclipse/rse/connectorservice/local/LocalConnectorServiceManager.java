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

package org.eclipse.rse.connectorservice.local;

import org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;


/**
 * ISystem manager class.
 * There should be only one of these instantiated.
 * Use getTheLocalSystemManager to get that singleton.
 * <p>
 * The job of this manager is to manage and return ISystem objects.
 * It ensures there is only ever one per unique SystemConnection,
 *  so that both the file and cmd subsystems can share the same system object.
 */
public class LocalConnectorServiceManager extends AbstractConnectorServiceManager
{
 	private static LocalConnectorServiceManager inst = null;
  
    /**
     * Private constructor to ensure not instantiated this way.
     * Use getTheLocalSystemManager instead.
     */
    private LocalConnectorServiceManager()
    {
    }
    
    /**
     * Return singleton instance of this class
     */
    public static LocalConnectorServiceManager getTheLocalSystemManager()
    {
    	if (inst == null)
    	  inst = new LocalConnectorServiceManager();
    	return inst;
    }
    
    /**
     * Return true if the singleton has been created. 
     * This saves creating it at shutdown just to test for isConnected.
     */
    public static boolean isInstantiated()
    {
    	return (inst != null);
    }
    
    // -------------------------------------    
    // ABSTRACT METHODS FROM PARENT CLASS...
    // -------------------------------------
    

    
    /**
     * Return the actual ISystem object.
     */
    public IConnectorService createConnectorService(IHost host)
    {
 	   	IConnectorService connectorService =  new LocalConnectorService(host);
 	   	return connectorService;
    }    

    /**
     * For all subsystems in a particular SystemConnection, we need to know which
     *  ones are to share a single ISystem object. To do this, we need a key which
     *  is canonical for all subsystems in a given connection. This can be anything,
     *  but is typically a unique interface that all subsystems supported a shared
     *  ISystem object implement. 
     * <p>
     * Whatever is returned from here is used as the key into a hashtable to find the
     *  singleton ISystem object in getSystemObject.
     * <p>
     * @return ILocalSubSystem.class
     */
    public Class getSubSystemCommonInterface(ISubSystem subsystem)
    {
    	return ILocalSubSystem.class;
    }
	/**
	 * Given another subsystem, return true if that subsystem shares a single ISystem object
	 * with this one. You must override this to return true if you recognize that subsystem 
	 * as one of your own. You are guaranteed the other subsystem will be from the same 
	 * SystemConnection as this one.
	 * <p>
	 * You can't assume a SystemConnection will you only have subsystems of that you created,
	 * so you should only return true if it implements your interface or you know it is an
	 * instance of your subsystem class.
	 * <p>
	 * This should simply return (otherSubSystem instanceof interface) where interface is 
	 * the same one returned from getSubSystemCommonInterface
	 * 
	 * @return true if otherSubSystem instanceof ILocalSubSystem
	 */
	public boolean sharesSystem(ISubSystem otherSubSystem)
	{
		return (otherSubSystem instanceof ILocalSubSystem);
	}
}