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

package org.eclipse.rse.internal.subsystems.files.core;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.subsystems.files.core.model.ISystemFileAPIProvider;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystemConfiguration;



/**
 * This class is a provider of root nodes to the remote systems tree viewer part.
 * It is used when the intial connections are to be subset to those that support a particular remote file subsystem factory,
 *   and subsystems shown within those connections are to be subset to only those from this subsystem factory.
 */
public class SystemFileSubSystemFactoryAPIProviderImpl 
       extends SystemFileAPIProviderImpl
       implements ISystemFileAPIProvider
{


	private IRemoteFileSubSystemConfiguration subsystemFactory = null;
	//protected Object[] emptyList = new Object[0];	
	
	/**
	 * Constructor
	 * @param subsystemFactory The remote file subsystem factory. Users will drill down from connections that support this factory.
	 * @param directoryMode true if you only want to traverse directories, false for both files and directories.
	 */
	public SystemFileSubSystemFactoryAPIProviderImpl(IRemoteFileSubSystemConfiguration subsystemFactory, boolean directoryMode)
	{
		super(directoryMode);
		this.subsystemFactory = subsystemFactory;
	}
	
	/**
	 * Get the input subsystem object. 
	 */
	public IRemoteFileSubSystemConfiguration getSubSystemFactory()
	{
		return subsystemFactory;
	}

    // ----------------------------------
    // SYSTEMVIEWINPUTPROVIDER METHODS...
    // ----------------------------------
	/**
	 * Return the children objects to consistute the root elements in the system view tree.
	 * We return all filter pools for this subsystem
	 */
	public Object[] getSystemViewRoots()
	{
		return getConnections();
	}
	/**
	 * Return true if {@link #getSystemViewRoots()} will return a non-empty list
	 * We return true if there are any connections containing a remote file subsystem, for any active profile.
	 */
	public boolean hasSystemViewRoots()
	{
		return hasConnections();		
	}
	/**
	 * This method is called by the connection adapter when the user expands
	 *  a connection. This method must return the child objects to show for that
	 *  connection.
	 */
	public Object[] getConnectionChildren(IHost selectedConnection)
	{
		return sr.getConnectionChildren(selectedConnection);
	}
	/**
	 * This method is called by the connection adapter when deciding to show a plus-sign
	 * or not beside a connection. Return true if this connection has children to be shown.
	 */
	public boolean hasConnectionChildren(IHost selectedConnection)
	{
		return sr.hasConnectionChildren(selectedConnection);		
	}

    /**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 */
    public Object getAdapter(Class adapterType)
    {
   	    return Platform.getAdapterManager().getAdapter(this, adapterType);	
    }           

    // ----------------------------------
    // OUR OWN METHODS...    
    // ----------------------------------
    /**
     * Return connections defined which this factory supports
     */
    public IHost[] getConnections()
    {
    	return sr.getHostsBySubSystemConfiguration(subsystemFactory);
    }
    /**
     * Return a count of all connections which have at least one subsystem that implements/extends RemoteFileSubSystem
     */
    public int getConnectionCount()
    {
    	return getConnections().length;
    }    
    /**
     * Return true if there are any connections defined which this factory supports
     */
    public boolean hasConnections()
    {
    	ISubSystem[] allOurSubSystems = subsystemFactory.getSubSystems(true); // true => full get; do restore from disk if not already    
    	return ((allOurSubSystems!=null) && (allOurSubSystems.length>0));
    }

}