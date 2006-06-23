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

package org.eclipse.rse.internal.model;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rse.core.ISystemUserIdConstants;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemHostPool;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;


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
    private static String CONNECTION_FILE_NAME = "connection";


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
    // CONNECTION POOL METHODS...
    // -------------------------------------------------------------------------------------
	/**
	 * Return (and create if necessary) the connection pool for a given system profile.
	 */
	public static ISystemHostPool getSystemHostPool(ISystemProfile profile)
	    throws Exception
	{
		//System.out.println("in getSystemConnectionPool for " + profile);
		if (pools == null)
		  pools = new Hashtable();
		SystemHostPool pool = (SystemHostPool)pools.get(profile);
		//System.out.println("... pool: " + pool);
		if (pool == null)
		{
		  pool = new SystemHostPool();
			  // FIXME (SystemConnectionPoolImpl)initMOF().createSystemConnectionPool();
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
    	return SystemProfileManager.getSystemProfileManager().getSystemProfile(getName());
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
     */
    public void printConnections()
    {
        java.util.List conns = getHostList();
        Iterator connsList = conns.iterator();
        if (!connsList.hasNext())
        {
          System.out.println();
          System.out.println("No connections");
        }
        while (connsList.hasNext())
        {
           System.out.println();
           IHost conn = (IHost)connsList.next();
           System.out.println("  AliasName.....: " + conn.getAliasName());
           System.out.println("  -----------------------------------------------------");
           System.out.println("  HostName......: " + conn.getHostName());
           System.out.println("  SystemType....: " + conn.getSystemType());
           System.out.println("  Description...: " + conn.getDescription());
           System.out.println("  UserId........: " + conn.getDefaultUserId());
        }
    }


    // -------------------------------------------------------------------------------------
    // CONNECTION METHODS...
    // -------------------------------------------------------------------------------------
    /**
     * Create a connection object, given only the minimal information.
     * <p>
     * THE RESULTING CONNECTION OBJECT IS ADDED TO THE LIST OF EXISTING CONNECTIONS FOR YOU.
     * @param systemType system type matching one of the system type names defined via the
     *                    systemTypes extension point.
     * @param aliasName unique connection name.
     * @param hostName ip name of host.
     * @return SystemConnection object, or null if it failed to create
     *   because the aliasName is not unique. All other errors throw an exception.
     */
    public IHost createHost(String systemType, String aliasName, String hostName)
        throws Exception                                                 
    {
        return createHost(systemType,aliasName,hostName,null,null,ISystemUserIdConstants.USERID_LOCATION_CONNECTION);
    }
    /**
     * Create a connection object, given all the possible attributes except default userId.
     * <p>
     * THE RESULTING CONNECTION OBJECT IS ADDED TO THE LIST OF EXISTING CONNECTIONS FOR YOU.
     * @param systemType system type matching one of the system type names defined via the
     *                    systemTypes extension point.
     * @param aliasName unique connection name.
     * @param hostName ip name of host.
     * @param description optional description of the connection. Can be null.
     * @return SystemConnection object, or null if it failed to create
     *   because the aliasName is not unique. All other errors throw an exception.
     */
    public IHost createHost(String systemType, String aliasName, String hostName, String description)
        throws Exception                                                 
    {
        return createHost(systemType,aliasName,hostName,description,null,ISystemUserIdConstants.USERID_LOCATION_CONNECTION);
    }
    /**
     * Create a connection object, given all the possible attributes.
     * <p>
     * The new connection is added to the list and saved to disk.
     * @param systemType system type matching one of the system type names defined via the
     *                    systemTypes extension point.
     * @param aliasName unique connection name.
     * @param hostName ip name of host.
     * @param description optional description of the connection. Can be null.
     * @param defaultUserId userId to use as the default for the subsystems.
     * @param defaultUserIdLocation where to set the given default user Id. See ISystemUserIdConstants
     * @return SystemConnection object, or null if it failed to create
     *   because the aliasName is not unique. All other errors throw an exception.
     */
    public IHost createHost(String systemType, String aliasName, String hostName,
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
          conn = new Host(profile);
          addHost(conn); // only record internally if saved successfully
          conn.setHostPool(this);          
          conn.setAliasName(aliasName);
          // DWD if default userID is null, and location is in the connection we should retrieve it and use it as the initial value.
          if (defaultUserId == null && defaultUserIdLocation == ISystemUserIdConstants.USERID_LOCATION_CONNECTION) {
              defaultUserId = conn.getDefaultUserId();
          }
          updateHost(conn, systemType, aliasName, hostName, description, defaultUserId, defaultUserIdLocation);          

        } catch (Exception e)
        {
          throw e;
        }
        return conn;
    }
    /**
     * Update an existing connection given the new information.
     * This method:
     * <ul>
     *  <li>calls the setXXX methods on the given connection object, updating the information in it.
     *  <li>saves the connection to disk (renaming its folder if needed)
     * </ul>
     * <p>
     * @param conn SystemConnection to be updated
     * @param systemType system type matching one of the system type names defined via the
     *                    systemTypes extension point.
     * @param aliasName unique connection name.
     * @param hostName ip name of host.
     * @param description optional description of the connection. Can be null.
     * @param defaultUserId userId to use as the default for the subsystems.
     * @param defaultUserIdLocation where to set the given default user Id. See ISystemUserIdConstants
     */
    public void updateHost(IHost conn, String systemType,
                                 String aliasName, String hostName,
                                 String description,String defaultUserId, int defaultUserIdLocation)
        throws Exception
    {
    	boolean aliasNameChanged = !aliasName.equalsIgnoreCase(conn.getAliasName());    	
    	if (aliasNameChanged)
    	  renameHost(conn,aliasName);
    	conn.setSystemType(systemType);
    	conn.setHostName(hostName);
    	if (defaultUserIdLocation != ISystemUserIdConstants.USERID_LOCATION_NOTSET)
    	{
    	  if (defaultUserIdLocation != ISystemUserIdConstants.USERID_LOCATION_CONNECTION)
    	  {
    	    conn.setDefaultUserId(null); // clear what was there, to ensure inheritance
   	        SystemPreferencesManager prefMgr = SystemPreferencesManager.getPreferencesManager();    	
   	        boolean forceToUpperCase = conn.getForceUserIdToUpperCase();
   	        if (forceToUpperCase && (defaultUserId != null))
   	          defaultUserId = defaultUserId.toUpperCase();
    	    if (defaultUserIdLocation == ISystemUserIdConstants.USERID_LOCATION_DEFAULT_SYSTEMTYPE)
    	    {
    	      prefMgr.setDefaultUserId(systemType, defaultUserId);    	      
    	    }
    	    //else if (defaultUserIdLocation == ISystemUserIdConstants.USERID_LOCATION_DEFAULT_OVERALL)
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
        RSEUIPlugin.getThePersistenceManager().commit(conn.getSystemProfile());
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
        conn.setDirty(true);
   	    commit(conn);
    }


    /**
     * Duplicates a given connection in this list within this list or another list.
     * @param targetPool The SystemConnectionPool to hold the copied connection. Can equal this connection, as long as alias name is unique
     * @param conn SystemConnection object (within our pool) to clone
     * @param alias New, unique, alias name to give this connection. Clone will fail if this is not unique.
     */
    public IHost cloneHost(ISystemHostPool targetPool, IHost conn, String aliasName)
       throws Exception
    {
        IHost copy =
            targetPool.createHost(conn.getSystemType(), aliasName,
                 conn.getHostName(), conn.getDescription(), conn.getLocalDefaultUserId(), ISystemUserIdConstants.USERID_LOCATION_CONNECTION);
        return copy;
    }

    /**
     * Move existing connections a given number of positions in the same pool.
     * If the delta is negative, they are all moved up by the given amount. If 
     * positive, they are all moved down by the given amount.<p>
     * <ul>
     * <li>After the move, the pool containing the moved connection is saved to disk.
     * <li>The connection's alias name must be unique in pool.
     * </ul>
     * <b>TODO PROBLEM: CAN'T RE-ORDER FOLDERS SO CAN WE SUPPORT THIS ACTION?</b>
     * @param conns Array of SystemConnections to move.
     * @param newPosition new zero-based position for the connection
     */
    public void moveHosts(IHost conns[], int delta)
    {
    	int[] oldPositions = new int[conns.length];
    	for (int idx=0; idx<conns.length; idx++)
    	   oldPositions[idx] = getHostPosition(conns[idx]);    	
    	if (delta > 0) // moving down, process backwards
          for (int idx=conns.length-1; idx>=0; idx--)
             moveConnection(conns[idx], oldPositions[idx]+delta);	
        else    	   
          for (int idx=0; idx<conns.length; idx++)
             moveConnection(conns[idx], oldPositions[idx]+delta);	

          commit();    	
    }
    
    /**
     * Move one connection to a new location
     * <b>TODO PROBLEM: CAN'T RE-ORDER FOLDERS SO CAN WE SUPPORT THIS ACTION?</b>
     */
    private void moveConnection(IHost conn, int newPos)
    {
    	/*
    	 * DWD revisit, make sure that connections can be "moved", whatever that means.
    	 * It appears that connections can be moved up and down in the list which
    	 * probably provides for some rational for keeping this around.
    	 */
//        java.util.List connList = getHostList();
       //FIXME connList.move(newPos, conn);
        invalidateCache();
    }

    /**
     * Order connections according to user preferences.
     * Called after restore.
     */
    public void orderHosts(String[] names)
    {
    	java.util.List connList = getHostList();
    	IHost[] conns = new IHost[names.length];
    	for (int idx=0; idx<conns.length; idx++)
    	   conns[idx] = getHost(names[idx]);
    	connList.clear();
    	//System.out.println("Ordering connections within pool " + getName() + "...");
    	for (int idx=0; idx<conns.length; idx++)
    	{
    	   connList.add(conns[idx]); 
    	   //System.out.println("  '"+conns[idx].getAliasName()+"'");
    	}
    	//System.out.println();
        invalidateCache();
    }

	// -------------------------
	// SAVE / RESTORE METHODS...
	// -------------------------
	/**
	 * Save all connections to disk.
	 * Attempts to save all of them, swallowing exceptions, then at the end throws the last exception caught.
	 */
	public boolean commit()
	{
		return RSEUIPlugin.getThePersistenceManager().commit(this);
	}
	
    /**
     * Attempt to save single connection to disk.
     */
    public void commit(IHost connection)
    {
    	commit();
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
		return SystemResources.RESID_MODELOBJECTS_HOSTPOOL_DESCRIPTION;
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
	 * @generated This field/method will be replaced during code generation.
	 */
	public String toStringGen()
	{
		

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(')');
		return result.toString();
	}
	

}