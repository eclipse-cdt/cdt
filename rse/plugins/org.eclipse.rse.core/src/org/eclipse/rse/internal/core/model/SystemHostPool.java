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
 * David Dykstal (IBM) - created and used RSEPReferencesManager
 *                     - moved SystemsPreferencesManager to a new plugin
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [186779] Fix IRSESystemType.getAdapter()
 ********************************************************************************/

package org.eclipse.rse.internal.core.model;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.IRSEUserIdConstants;
import org.eclipse.rse.core.RSEPreferencesManager;
import org.eclipse.rse.core.model.Host;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISystemHostPool;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.RSEModelObject;
import org.eclipse.rse.internal.core.RSECoreMessages;


/**
 * A pool of host objects.
 * There is one pool per profile.
 * It is named the same as its owning profile.
 * It is not persisted but provides a means of manipulating lists of host objects.
 * Hosts are created and destroyed by the host pool so that the the relationships between the two can be maintained. 
 */
public class SystemHostPool extends RSEModelObject implements ISystemHostPool
{

	protected static final String NAME_EDEFAULT = null;

    private static Hashtable pools = null;
    private static String CONNECTION_FILE_NAME = "connection"; //$NON-NLS-1$


	protected String name = NAME_EDEFAULT;
    private java.util.List connections = null;

    /**
     * Default constructor.
     */
	protected SystemHostPool()
    {
	 	  super();
	}

	/**
	 * Reset for a full refresh from disk, such as after a team synch
	 */
	public static void reset()
	{
		pools = null;
	}

    // -------------------------------------------------------------------------------------
    // Host Pool Methods
    // -------------------------------------------------------------------------------------
	/**
	 * Return (and create if necessary) the connection pool for a given system profile.
	 */
	public static ISystemHostPool getSystemHostPool(ISystemProfile profile)
	    throws Exception
	{
		if (pools == null)
		  pools = new Hashtable();
		SystemHostPool pool = (SystemHostPool)pools.get(profile);
		if (pool == null)
		{
		  pool = new SystemHostPool();
		  pool.setName(profile.getName());
		  try {
		    pool.restore(); // restore connections
		  } catch (Exception exc) {
		  }
		  pools.put(profile, pool); // store this pool reference, keyed by profile object.
		}
		return pool;
	}
	
    /**
     * Return the system profile that owns this connection pool
     */
    public ISystemProfile getSystemProfile()
    {
    	return SystemProfileManager.getDefault().getSystemProfile(getName());
    }	
	
	/**
	 * Rename this connection pool.
	 */
	public void renameHostPool(String newName)
	{
		IHost[] connections = getHosts();
		String oldName = getName();
		for (int idx=0; idx<connections.length; idx++)
		{
			connections[idx].renamingSystemProfile(oldName, newName);
		}
		setName(newName);
	}
	

    /**
     * Private debug method to print connections, to test restored ok.
     * @deprecated
     */
    public void printConnections()
    {
        java.util.List conns = getHostList();
        Iterator connsList = conns.iterator();
        if (!connsList.hasNext())
        {
          System.out.println();
          System.out.println("No connections"); //$NON-NLS-1$
        }
        while (connsList.hasNext())
        {
           System.out.println();
           IHost conn = (IHost)connsList.next();
           System.out.println("  AliasName.....: " + conn.getAliasName()); //$NON-NLS-1$
           System.out.println("  -----------------------------------------------------"); //$NON-NLS-1$
           System.out.println("  HostName......: " + conn.getHostName()); //$NON-NLS-1$
           System.out.println("  SystemType....: " + conn.getSystemType().getId()); //$NON-NLS-1$
           System.out.println("  Description...: " + conn.getDescription()); //$NON-NLS-1$
           System.out.println("  UserId........: " + conn.getDefaultUserId()); //$NON-NLS-1$
        }
    }

    // -------------------------------------------------------------------------------------
    // CONNECTION METHODS...
    // -------------------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#createHost(org.eclipse.rse.core.IRSESystemType, java.lang.String, java.lang.String)
     */
    public IHost createHost(IRSESystemType systemType, String aliasName, String hostName)
        throws Exception                                                 
    {
        return createHost(systemType,aliasName,hostName,null,null,IRSEUserIdConstants.USERID_LOCATION_HOST);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#createHost(org.eclipse.rse.core.IRSESystemType, java.lang.String, java.lang.String, java.lang.String)
     */
    public IHost createHost(IRSESystemType systemType, String aliasName, String hostName, String description)
        throws Exception                                                 
    {
        return createHost(systemType,aliasName,hostName,description,null,IRSEUserIdConstants.USERID_LOCATION_HOST);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#createHost(org.eclipse.rse.core.IRSESystemType, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
     */
    public IHost createHost(IRSESystemType systemType, String aliasName, String hostName,
                                             String description,String defaultUserId,int defaultUserIdLocation)        
        throws Exception                                             
    {
        IHost conn = null;
        boolean exists = getHost(aliasName) != null;
        if (exists)
        {
          return null;
        }
        try
        {
          ISystemProfile profile = getSystemProfile();
          
          // delegate the creation of the host object instance to the system type provider!!!
          if (systemType != null) {
        	  conn = systemType.createNewHostInstance(profile);
          }
          // Fallback to create host object instance here if failed by system type provider.
          if (conn == null) conn = new Host(profile);
          assert conn != null;
          
          addHost(conn); // only record internally if saved successfully
          conn.setHostPool(this);          
          conn.setAliasName(aliasName);
          conn.setSystemType(systemType);
          // if default userID is null, and location is in the connection we should retrieve it and use it as the initial value.
          if (defaultUserId == null && defaultUserIdLocation == IRSEUserIdConstants.USERID_LOCATION_HOST) {
              defaultUserId = conn.getDefaultUserId();
          }
          updateHost(conn, systemType, aliasName, hostName, description, defaultUserId, defaultUserIdLocation);          

        } catch (Exception e)
        {
          throw e;
        }
        return conn;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#updateHost(org.eclipse.rse.core.model.IHost, org.eclipse.rse.core.IRSESystemType, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
     */
    public void updateHost(IHost conn, IRSESystemType systemType,
                                 String aliasName, String hostName,
                                 String description,String defaultUserId, int defaultUserIdLocation)
        throws Exception
    {
    	boolean aliasNameChanged = !aliasName.equalsIgnoreCase(conn.getAliasName());    	
    	if (aliasNameChanged)
    	  renameHost(conn,aliasName);
    	conn.setSystemType(systemType);
    	conn.setHostName(hostName);
    	if (defaultUserIdLocation != IRSEUserIdConstants.USERID_LOCATION_NOTSET)
    	{
    	  if (defaultUserIdLocation != IRSEUserIdConstants.USERID_LOCATION_HOST)
    	  {
    	    conn.setDefaultUserId(null); // clear what was there, to ensure inheritance
   	        boolean forceToUpperCase = conn.getForceUserIdToUpperCase();
   	        if (forceToUpperCase && (defaultUserId != null))
   	          defaultUserId = defaultUserId.toUpperCase();
    	    if (defaultUserIdLocation == IRSEUserIdConstants.USERID_LOCATION_DEFAULT_SYSTEMTYPE)
    	    {
    	    	RSEPreferencesManager.setDefaultUserId(systemType, defaultUserId);    	      
    	    }
    	    //else if (defaultUserIdLocation == IRSEUserIdConstants.USERID_LOCATION_DEFAULT_OVERALL)
    	    //{
    	      //prefMgr.setDefaultUserId(defaultUserId);    	          	    	
    	    //}
    	  }
    	  else
    	  {
    	    conn.setDefaultUserId(defaultUserId);
    	  }
    	}
    	conn.setDescription(description);
   	    commit(conn);
    }
    
    
    /**
     * Return array of connections in this pool
     */
    public IHost[] getHosts()
    {

    	return (IHost[])getHostList().toArray(new IHost[connections.size()]);
    }
    
 
    
    /*
     * Invalidate cache so it will be regenerated
     */
    protected void invalidateCache()
    {
    	setDirty(true);
    }

    /**
     * Return a connection object, given its alias name.
     * Can be used to test if an alias name is already used (non-null return).
     * @param aliasName unique aliasName (case insensitive) to search on.
     * @return SystemConnection object with unique aliasName, or null if
     *  no connection object with this name exists.
     */
    public IHost getHost(String aliasName)
    {
        IHost conn = null;
        IHost currconn = null;
        java.util.List conns = getHostList();
        Iterator i = conns.iterator();
        while (i.hasNext() && (conn==null))
        {
           currconn = (IHost)i.next();
           if (currconn.getAliasName().equalsIgnoreCase(aliasName))
             conn = currconn;
        }
        return conn;
    }
    /**
     * Return the connection at the given zero-based offset
     */
    public IHost getHost(int pos)
    {
        java.util.List conns = getHostList();
        if (pos < conns.size())
          return (IHost)conns.get(pos);
        else
          return null;
    }
    /**
     * Return the zero-based position of a SystemConnection object within its profile.
     */
    public int getHostPosition(IHost conn)
    {
    	int position = -1;
    	boolean match = false;    	
    	java.util.List conns = getHostList();
    	Iterator i = conns.iterator();
    	int idx = 0;
    	while (!match && i.hasNext())
    	{
           IHost currConn = (IHost)i.next();
           if (conn.equals(currConn))
           {
           	 match = true;
           	 position = idx;
           }
           idx++;
    	}
    	return position;
    }
    
    /**
     * Return the number of SystemConnection objects within this pool.
     */
    public int getHostCount()
    {
        java.util.List conns = getHostList();    	
        return conns.size();
    }


    public boolean addHost(IHost conn)
    {
    	List hostList = getHostList();
    	if (!hostList.contains(conn))
    	{
    		hostList.add(conn);
    	}
        conn.setHostPool(this);
        invalidateCache();
        return true;
    }

    /**
     * Removes a given connection from the list and deletes it from disk.
     * <p>
	 * This will:
	 * <ul>
	 *    <li>Delete the connection in memory
	 *    <li>Delete the underlying folder
	 * </ul> 
     * <p>
     * @param conn SystemConnection object to remove
     */
    public void deleteHost(IHost conn)
    {
    	conn.deletingHost(); // let connection do any necessary cleanup
        getHostList().remove(conn);
        setDirty(true);
        conn.getSystemProfile().commit();
    }

    /**
     * Renames a given connection in the list.
	 * This will:
	 * <ul>
	 *    <li>Rename the profile in memory
	 *    <li>Rename the underlying folder
	 *    <li>Update the user preferences if this profile is currently active.
	 * </ul>
     * @param conn SystemConnection object to rename
     * @param newName The new name to give that connection.
     */
    public void renameHost(IHost conn, String newName)
           throws Exception
    {
        conn.setAliasName(newName);
        invalidateCache();
   	    commit(conn);
    }


    /**
     * Duplicates a given connection in this list within this list or another list.
     * @param targetPool The SystemConnectionPool to hold the copied connection. Can equal this connection, as long as alias name is unique
     * @param conn SystemConnection object (within our pool) to clone
     * @param aliasName New, unique, alias name to give this connection. Clone will fail if this is not unique.
     */
    public IHost cloneHost(ISystemHostPool targetPool, IHost conn, String aliasName)
       throws Exception
    {
        IHost copy =
            targetPool.createHost(conn.getSystemType(), aliasName,
                 conn.getHostName(), conn.getDescription(), conn.getLocalDefaultUserId(), IRSEUserIdConstants.USERID_LOCATION_HOST);
        return copy;
    }

    /**
	 * Move existing hosts a given number of positions in the same pool.
	 * If the delta is negative, they are all moved up (left) by the given amount. If 
	 * positive, they are all moved down (right) by the given amount.<p>
	 * After the move, the pool containing the moved host is committed.
	 * @param hosts an Array of hosts to move, can be empty but must not be null.
	 * @param delta the amount by which to move the hosts
	 */
	public void moveHosts(IHost hosts[], int delta) {
		/* 
		 * Determine the indices of the supplied hosts in this pool.
		 * If the delta is positive this list should be in descending order,
		 * if negative, the list should be in ascending oder.
		 */
		final int m = (delta > 0) ? -1 : 1; // modifier that determines the ordering
		SortedSet indices = new TreeSet(new Comparator() {
			public int compare(Object o1, Object o2) {
				return  m * ((Integer)o1).compareTo((Integer)o2);
			}
		});
		List hostList = getHostList();
		for (int i = 0; i < hosts.length; i++) {
			IHost host = hosts[i];
			int index = hostList.indexOf(host);
			if (index >= 0) {
				indices.add(new Integer(index));
			}
		}
		// Go through the sorted list of indices.
		boolean moved = indices.size() > 0;
		for (Iterator z = indices.iterator(); z.hasNext() && moved;) {
			int index = ((Integer) z.next()).intValue();
			moved &= moveHost(hostList, index, delta);
		}
		if (moved) {
			invalidateCache();
			commit();
		}
	}
    
    /**
	 * Move a host to a new location in the host pool.
	 * @param hostList the list of hosts to modify
	 * @param oldPos the index of the host to move. If outside the bounds of the list, the list is not altered.
	 * @param delta the amount by which to move the host. If the resulting
	 * position would be outside the bounds of the list, the list is not altered.
	 * If 0 then the list is not altered. 
	 * @return true if the host was moved, false if not
	 */
	private boolean moveHost(List hostList, int oldPos, int delta) {
		boolean moved = false; // assume the element will not be moved
		if (0 <= oldPos && oldPos < hostList.size() && delta != 0) {
			int newPos = oldPos + delta;
			if (0 <= newPos && newPos < hostList.size()) {
				IHost host = (IHost) hostList.remove(oldPos);
				hostList.add(newPos, host);
				moved = true;
			}
		}
		return moved;
	}

	/**
	 * Order connections according to user preferences.
	 * Called after restore.
	 */
	public void orderHosts(String[] names) {
		List connList = getHostList();
		IHost[] conns = new IHost[names.length];
		for (int idx = 0; idx < conns.length; idx++) {
			conns[idx] = getHost(names[idx]);
		}
		connList.clear();
		for (int idx = 0; idx < conns.length; idx++) {
			connList.add(conns[idx]);
		}
		invalidateCache();
	}

	/**
     * Return the unqualified save file name with the extension .xmi
     */
    protected static String getSaveFileName(IHost connection)
    {
        return null;//FIXME return SystemMOFHelpers.getSaveFileName(getRootSaveFileName(connection));
    }

    /**
     * Return the root save file name without the extension .xmi
     */
    protected static String getRootSaveFileName(IHost connection)
    {
        return getRootSaveFileName(connection.getAliasName());
    }
    /**
     * Return the root save file name without the extension .xmi
     */
    protected static String getRootSaveFileName(String connectionName)
    {
    	//String fileName = connectionName; // maybe a bad idea to include connection name in it!
    	String fileName = CONNECTION_FILE_NAME;
        return fileName;    	
    }
    
	public String toString()
    {
        if (getName() == null)
          return this.toStringGen();
        else
          return getName();
    }

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return RSECoreMessages.RESID_MODELOBJECTS_HOSTPOOL_DESCRIPTION;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setName(String newName)
	{
		name = newName;
	}


	public List getHostList()
	{
		if (connections == null)
		{
			connections = new ArrayList();
		}
		return connections;
	}

	/**
	 * @deprecated This field/method will be replaced during code generation.
	 */
	public String toStringGen()
	{
		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: "); //$NON-NLS-1$
		result.append(name);
		result.append(')');
		return result.toString();
	}
	
	/**
	 * Restore connections from disk
	 */
	protected void restore()
	   throws Exception
	{
		//System.out.println("... . in pool.restore ");
		
		//FIXME 
	}

	/**
	 * Restore a connection of a given name from disk...
	 */
	protected IHost restore(String connectionName)  
	    throws Exception  
	{
		/*FIXME
		//System.out.println("in SystemConnectionPoolImpl#restore for connection " + connectionName);
	    String fileName = getRootSaveFileName(connectionName);                
	    //System.out.println(".......fileName = " + fileName);
	    //System.out.println(".......folderName = " + getConnectionFolder(connectionName).getName());
		java.util.List ext = getMOFHelpers().restore(getConnectionFolder(connectionName),fileName);
		
	    // should be exactly one profile...
	    Iterator iList = ext.iterator();
	    SystemConnection connection = (SystemConnection)iList.next();        
	    if (connection != null)
	    {
	      if (!connection.getAliasName().equalsIgnoreCase(connectionName))
	      {
	        RSEUIPlugin.logDebugMessage(this.getClass().getName(),"Incorrect alias name found in connections.xmi file for " + connectionName+". Name was reset");
	        connection.setAliasName(connectionName); // just in case!
	      }
	      internalAddConnection(connection);
	    }        
	    return connection;
	    */
		return null;
	}

	/** 
	 * System host pools are not persisted and do not exist in the persistence hierarchy.
	 * @return null
	 */
	public IRSEPersistableContainer getPersistableParent() {
		return null;
	}
	
	public IRSEPersistableContainer[] getPersistableChildren() {
		return IRSEPersistableContainer.NO_CHILDREN;
	}

	// -------------------------
	// SAVE / RESTORE METHODS...
	// -------------------------
	/**
	 * Save all connections to disk.
	 * Attempts to save all of them, swallowing exceptions,
	 * then at the end throws the last exception caught.
	 */
	public boolean commit()
	{
		ISystemProfile profile = getSystemProfile();
		boolean result = profile.commit();
		return result;
	}

	/**
	 * Attempt to save single connection to disk.
	 */
	public void commit(IHost connection)
	{
		commit();
	 }

}
