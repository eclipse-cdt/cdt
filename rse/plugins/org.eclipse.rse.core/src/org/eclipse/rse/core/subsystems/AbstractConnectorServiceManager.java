/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * David McKnight   (IBM)        - [198802] Incorrect logic for getting dummy host	
 * David McKnight   (IBM)        - [243382] [dstore] Server launcher settings are shared by multiple connections
 *******************************************************************************/

package org.eclipse.rse.core.subsystems;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.rse.core.model.DummyHost;
import org.eclipse.rse.core.model.IHost;


/**
 * This class is only needed if you need/want to support multiple
 *  subsystems and want them to share a single {@link org.eclipse.rse.core.subsystems.IConnectorService IConnectorService} object per 
 *  connection. This is the base connector service manager implementation
 *  that returns the same IConnectorService object for all subsystems in the
 *  same system connection, which implement a given interface. 
 * <p>
 * For this to work all your subsystem classes that wish to share the
 *  same IConnectorService object must implement a common interface of your choosing.
 * <p>
 * Another benefit of using this class or a subclass of it, is that whenever
 *  the user changes the core properties for a single subsystem (port, userId)
 *  then this manager is used to cascade that to all other subsystems in this
 *  connection which share that same common interface. This means the user can change 
 *  these properties in any one subsystem, and they are changed in all related
 *  subsystems magically. This is necessary because while these properties are
 *  persisted in the subsystem object, they really belong to the IConnectorService object,
 *  so when multiple subsystems share an IConnectorService object, changes to these properties
 *  in one subsystem need to be cascaded to the other subsystems.
 * <p>
 * Because you only need a singleton of these class, the constructor is protected.
 * <p>
 * Your subclass <b>must supply</b> a singleton factory method like the following:</p>
 * <pre><code>
 * public static MyConnectorServiceManager <b>getInstance</b>()
 *  {
 *  	if (inst == null)
 *  	  inst = new MyConnectorServiceManager();
 *  	return inst;
 *  }
 * </code></pre>
 */
public abstract class AbstractConnectorServiceManager implements IConnectorServiceManager
{


    // we maintain a hashtable of hashtables. The first is keyed by SystemConnection.
    // The hashtable for each connection, maintains a list of IConnectorService objects keyed by
    // a unique interface class object supplied by the subclasses.
    private Hashtable systemConnectionRegistry = new Hashtable();
          
    /**
     * Protected constructor to ensure not instantiated this way.
     * Use subclass-supplied static singleton factory method getInstance() instead.
     */
    protected AbstractConnectorServiceManager()
    {
    }

    public void setConnectorService(IHost host, Class commonSSinterface, IConnectorService connectorService)
    {
      	Hashtable connHT = (Hashtable)systemConnectionRegistry.get(host);
    	if (connHT == null)
    	{
    		connHT = new Hashtable();
    		systemConnectionRegistry.put(host, connHT);
    	}
    	// replaces any previous service here
    	connHT.put(commonSSinterface, connectorService);
    }

    public IConnectorService getConnectorService(IHost host, Class commonSSinterface)
    {
    	Hashtable connHT = (Hashtable)systemConnectionRegistry.get(host);
    	if (connHT == null)
    	{
    		if (host instanceof DummyHost)
    		{
    			connHT = findConnHTForDummyHost(host);
    		}
    		if (connHT == null)
    		{
    			connHT = new Hashtable();
    			systemConnectionRegistry.put(host, connHT);
    		}
    	}
    	IConnectorService systemObject = (IConnectorService)connHT.get(commonSSinterface);
    	if (systemObject == null)
    	{
			systemObject = createConnectorService(host);
    	  	connHT.put(commonSSinterface, systemObject);
    	}
    	else
    	{
    		IHost currentHost = systemObject.getHost();
    		if (currentHost instanceof DummyHost && host != currentHost)
    		{
    			systemObject.setHost(host);
    		}
    	}

    	return systemObject;
    }   
    
    protected Hashtable findConnHTForDummyHost(IHost newHost)
    {
    	Set keyset = systemConnectionRegistry.keySet();
    	Object[] keys = keyset.toArray();
    	for (int i = 0; i < keys.length; i++)
    	{
    		Object key = keys[i];
    		if (key instanceof DummyHost)
    		{
    			IHost host = (IHost)key;
    			// A previous host of the same hostName should not be used since it causes bug 243382. 
    			// A remaining problem here is that we shouldn't keep a host around in 
    			// systemConnectionRegistry after it's corresponding connection has been created
    			// but at the moment there's no API to do that outside of this class
    			if (host.equals(newHost))	
    			{
    				Hashtable table = (Hashtable)systemConnectionRegistry.remove(host);
    				systemConnectionRegistry.put(newHost, table);    	
    				return table;
    			}
    		}
    	}
    	return null;
    }
    
    /**
     * Return the actual IConnectorService object. Must be overridden by subclass.
     */
    public abstract IConnectorService createConnectorService(IHost host);

	/**
	 * Given another subsystem, return true if that subsystem shares a single IConnectorService object
	 * with this one. You must override this to return true if you recognize that subsystem 
	 * as one of your own. You are guaranteed the other subsystem will be from the same 
	 * SystemConnection as this one.
	 * <p>
	 * You can't assume a SystemConnection will only have subsystems that you created,
	 * so you should only return true if it implements your interface or you know it is an
	 * instance of your subsystem class.
	 * <p>
	 * This should simply return (otherSubSystem instanceof interface) where interface is 
	 * the same one returned from getSubSystemCommonInterface.
	 */
	public abstract boolean sharesSystem(ISubSystem otherSubSystem);
	
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
     * @param subsystem - rarely used, but if you support multiple common interfaces then this will help you
     *    decide which one to return.
     * @return a common, yet unique to you, interface that all your subsystems implement.
     */
    public abstract Class getSubSystemCommonInterface(ISubSystem subsystem);
        
  
}
