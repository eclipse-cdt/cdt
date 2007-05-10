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
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 ********************************************************************************/

package org.eclipse.rse.connectorservice.dstore;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.connectorservice.dstore.ConnectorServiceResources;



/**
 * IConnectorService manager class.
 * There should be only one of these instantiated.
 * Use {@link #getInstance()} to get that singleton.
 * <p>
 * The job of this manager is to manage and return IConnectorService objects.
 * It ensures there is only ever one per unique SystemConnection,
 *  so that both the file and command subsystems can share the same system object.
 */
public class DStoreConnectorServiceManager extends AbstractConnectorServiceManager
{
	private static DStoreConnectorServiceManager inst = null;
  
    /**
     * Private constructor to ensure not instantiated this way.
     * Use {@link #getInstance()} instead.
     */
    private DStoreConnectorServiceManager()
    {
    }
    
    /**
     * Return singleton instance of this class
     */
    public static DStoreConnectorServiceManager getInstance()
    {
    	if (inst == null)
    	  inst = new DStoreConnectorServiceManager();
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
     * Return the actual IConnectorService object. We return an instance of UniversalSystem.
     */
    public IConnectorService createConnectorService(IHost host)
    {
    	IConnectorService service =  new DStoreConnectorService(ConnectorServiceResources.DStore_ConnectorService_Label, ConnectorServiceResources.DStore_ConnectorService_Description, host);
    	return service;
    }    

    /**
     * For all subsystems in a particular SystemConnection, we need to know which
     *  ones are to share a single IConnectorService object. To do this, we need a key which
     *  is canonical for all subsystems in a given connection. This can be anything,
     *  but is typically a unique interface that all subsystems supported a shared
     *  IConnectorService object implement. 
     * <p>
     * Whatever is returned from here is used as the key into a hashtable to find the
     *  singleton IConnectorService object in getSystemObject.
     * <p>
     * @return IUniversalSubSystem.class
     */
    public Class getSubSystemCommonInterface(ISubSystem subsystem)
    {
    	return IUniversalDStoreSubSystem.class;
    }
	/**
	 * Given another subsystem, return true if that subsystem shares a single IConnectorService object
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
	 * @return true if otherSubSystem instanceof IUniversalSubSystem
	 */
	public boolean sharesSystem(ISubSystem otherSubSystem)
	{
		return (otherSubSystem instanceof IUniversalDStoreSubSystem);
	}
}