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
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.subsystems.files.core.model.ISystemFileAPIProvider;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.view.SystemAbstractAPIProvider;


/**
 * Where to start when looking to traverse a remote file system.
 */
public class SystemFileAPIProviderImpl 
       extends SystemAbstractAPIProvider
       implements ISystemFileAPIProvider
{


	protected ISystemRegistry sr = null;
	protected boolean directoryMode = false;
	//protected Object[] emptyList = new Object[0];	
	
	/**
	 * Constructor for SystemFileAPIProvider
	 * @param directoryMode true if you only want to traverse directories, false for both files and directories.
	 */
	public SystemFileAPIProviderImpl(boolean directoryMode)
	{
		super();
		this.directoryMode = directoryMode;
		sr = SystemPlugin.getTheSystemRegistry();
	}
	
	/**
	 * Get the directories-only mode. 
	 */
	public boolean isDirectoriesOnly()
	{
		return directoryMode;
	}

    // ----------------------------------
    // SYSTEMVIEWINPUTPROVIDER METHODS...
    // ----------------------------------
	/**
	 * Return the children objects to consistute the root elements in the system view tree.
	 * We return all connections containing a remote file subsystem, for all active profiles.
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
		return (getConnectionCount()>0);		
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
     * Return true to show the action bar (ie, toolbar) above the viewer.
     * The action bar contains connection actions, predominantly.
     */
    public boolean showActionBar()
    {
    	return true;
    }
    /**
     * Return true to show the button bar above the viewer.
     * The tool bar contains "Get List" and "Refresh" buttons and is typicall
     * shown in dialogs that list only remote system objects.
     */
    public boolean showButtonBar()
    {
    	return false;
    }	
    /**
     * Return true to show right-click popup actions on objects in the tree.
     */
    public boolean showActions()
    {
    	return true;
    }
    

    // ----------------------------------
    // OUR OWN METHODS...
    // ----------------------------------

    /**
     * Return all connections which have at least one subsystem that implements/extends RemoteFileSubSystem
     */
    public IHost[] getConnections()
    {
    	int connCount = getConnectionCount();
    	IHost[] ourConns = new IHost[connCount];
    	IHost[] allConns = sr.getHosts();
    	int ourConnsIdx = 0;
    	if (allConns != null)
    	{
    	  for (int idx=0; idx<allConns.length; idx++)
    	  {
    	  	ISubSystem[] subsystems = sr.getSubSystems(allConns[idx]);
    	    if (subsystems != null)
    	    {
    	      boolean match = false;
    	      for (int jdx=0; !match && (jdx<subsystems.length); jdx++)
    	      {
    	         if (subsystems[jdx] instanceof IRemoteFileSubSystem)
    	         {
    	           ourConns[ourConnsIdx++] = allConns[idx];
    	           match = true;
    	         }
    	      }
    	    }
    	  }    	  
    	}
    	return ourConns;
    }
    
    /**
     * Return a count of all connections which have at least one subsystem that implements/extends RemoteFileSubSystem
     */
    public int getConnectionCount()
    {
    	int count = 0;
    	IHost[] allConns = sr.getHosts();
    	if (allConns != null)
    	{
    	  for (int idx=0; idx<allConns.length; idx++)
    	  {
    	  	ISubSystem[] subsystems = sr.getSubSystems(allConns[idx]);
    	    boolean match = false;
    	    if (subsystems != null)
    	    {
    	      for (int jdx=0; !match && (jdx<subsystems.length); jdx++)
    	      {
    	         if (subsystems[jdx] instanceof IRemoteFileSubSystem)
    	           match = true;  	
    	      }
    	    }
    	    if (match)
    	      ++count;
    	  }    	  
    	}
    	return count;
    }
}