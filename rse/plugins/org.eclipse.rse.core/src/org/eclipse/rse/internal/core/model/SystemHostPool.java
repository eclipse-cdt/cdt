/********************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - [176577] wrong enablement of "Move up/down" in connection context menu
 * Martin Oberhuber (Wind River) - [206742] Make SystemHostPool thread-safe
 * David Dykstal (IBM) - [210537] removed exception signaling from this class to match the interface
 * Tom Hochstein (freescale)     - [325923] Host copy doesn't copy contained property sets
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
import org.eclipse.rse.core.model.IProperty;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISystemHostPool;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.RSEModelObject;
import org.eclipse.rse.internal.core.RSECoreMessages;


/**
 * A pool of host objects.
 * <p>
 * The host pool is tightly coupled to its owning profile: there is exactly one pool
 * per profile, it always has the same name as the owning profile, and renaming it 
 * also renames the profile. Persistence of the host pool is also handled through 
 * persisting the owning profile.
 * </p><p>
 * It is not persisted but provides a means of manipulating lists of host objects.
 * Hosts are created and destroyed by the host pool so that the the relationships
 * between the two can be maintained.
 * </p><p>
 * This class is thread-safe in the sense that integrity of the host list is maintained
 * even if multiple threads call multiple methods in this interface concurrently.
 * </p>
 * @see ISystemHostPool
 */
public class SystemHostPool extends RSEModelObject implements ISystemHostPool
{

	protected static final String NAME_EDEFAULT = null;

    private static Hashtable pools = new Hashtable();
    private static String CONNECTION_FILE_NAME = "connection"; //$NON-NLS-1$

	protected String name = NAME_EDEFAULT;
    private List connections = new ArrayList();

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
		pools.clear();
	}

    // -------------------------------------------------------------------------------------
    // Host Pool Methods
    // -------------------------------------------------------------------------------------
	/**
	 * Return (and create if necessary) the connection pool for a given system profile.
	 * @param profile the profile to create a host pool for.
	 */
	public static ISystemHostPool getSystemHostPool(ISystemProfile profile)
	{
		SystemHostPool pool = (SystemHostPool)pools.get(profile);
		if (pool == null)
		{
		  pool = new SystemHostPool();
		  pool.setName(profile.getName());
		  pools.put(profile, pool); // store this pool reference, keyed by profile object.
		}
		return pool;
	}
	
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#getSystemProfile()
     */
    public ISystemProfile getSystemProfile()
    {
    	return SystemProfileManager.getDefault().getSystemProfile(getName());
    }	
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemHostPool#renameHostPool(java.lang.String)
	 */
	public void renameHostPool(String newName)
	{
		//Threading: We need to ensure that new hosts are not added while the rename is 
		//ongoing. Therefore, we lock even though renamingSystemProfile() is an alien
		//method -- Javadoc in that method warns about the possible deadlock.
		List hostList = getHostList();
		synchronized(hostList) {
			String oldName = getName();
			Iterator it = hostList.iterator();
			while (it.hasNext()) {
				IHost curHost = (IHost)it.next();
				curHost.renamingSystemProfile(oldName, newName);
				
			}
			setName(newName);
		}
	}
	

    /**
     * Private debug method to print connections, to test restored ok.
     * @deprecated this private debug method may be removed at any time.
     */
    public void printConnections()
    {
        List conns = getHostList();
    	synchronized(conns) {
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
    }

    // -------------------------------------------------------------------------------------
    // CONNECTION METHODS...
    // -------------------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#createHost(org.eclipse.rse.core.IRSESystemType, java.lang.String, java.lang.String)
     */
    public IHost createHost(IRSESystemType systemType, String aliasName, String hostName)
    {
        return createHost(systemType,aliasName,hostName,null,null,IRSEUserIdConstants.USERID_LOCATION_HOST);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#createHost(org.eclipse.rse.core.IRSESystemType, java.lang.String, java.lang.String, java.lang.String)
     */
    public IHost createHost(IRSESystemType systemType, String aliasName, String hostName, String description)
    {
        return createHost(systemType,aliasName,hostName,description,null,IRSEUserIdConstants.USERID_LOCATION_HOST);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#createHost(org.eclipse.rse.core.IRSESystemType, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
     */
    public IHost createHost(IRSESystemType systemType, String aliasName, String hostName,
                                             String description,String defaultUserId,int defaultUserIdLocation)
    {
        IHost conn = null;
        boolean exists = getHost(aliasName) != null;
        if (exists)
        {
          return null;
        }
        ISystemProfile profile = getSystemProfile();
        
        // delegate the creation of the host object instance to the system type provider!!!
        if (systemType != null) {
      	  conn = systemType.createNewHostInstance(profile);
        }
        // Fallback to create host object instance here if failed by system type provider.
        assert conn != null;
        if (conn == null) conn = new Host(profile);
        
        addHost(conn); // only record internally if saved successfully
        conn.setHostPool(this);          
        conn.setAliasName(aliasName);
        conn.setSystemType(systemType);
        // if default userID is null, and location is in the connection we should retrieve it and use it as the initial value.
        if (defaultUserId == null && defaultUserIdLocation == IRSEUserIdConstants.USERID_LOCATION_HOST) {
            defaultUserId = conn.getDefaultUserId();
        }
        updateHost(conn, systemType, aliasName, hostName, description, defaultUserId, defaultUserIdLocation);          
        return conn;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#updateHost(org.eclipse.rse.core.model.IHost, org.eclipse.rse.core.IRSESystemType, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
     */
    public void updateHost(IHost conn, IRSESystemType systemType,
                                 String aliasName, String hostName,
                                 String description,String defaultUserId, int defaultUserIdLocation)
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
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#getHosts()
     */
    public IHost[] getHosts()
    {
    	//Must be synchronized in order to avoid change of host list size while populating the array
    	List conns = getHostList();
    	synchronized(conns) {
        	return (IHost[])conns.toArray(new IHost[conns.size()]);
    	}
    }
    
    /**
     * Invalidate cache so it will be regenerated
     */
    protected void invalidateCache()
    {
    	setDirty(true);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#getHost(java.lang.String)
     */
    public final IHost getHost(String aliasName)
    {
    	//This method is final because it is called from inside the synchronized block
    	//in the orderHosts() method. Also, if subclasses want to override the way how
    	//hosts are returned and compared, they better need to override the List 
    	//implementation that's returned by getHostList().
        List conns = getHostList();
        synchronized(conns) {
            Iterator i = conns.iterator();
            while (i.hasNext())
            {
               IHost currconn = (IHost)i.next();
               if (currconn.getAliasName().equalsIgnoreCase(aliasName))
            	   return currconn;
            }
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#getHost(int)
     */
    public final IHost getHost(int pos)
    {
    	List conns = getHostList();
        synchronized(conns) {
            if (pos < conns.size())
              return (IHost)conns.get(pos);
            else
              return null;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#getHostPosition(org.eclipse.rse.core.model.IHost)
     */
    public int getHostPosition(IHost conn)
    {
    	List hostList = getHostList();
    	synchronized(hostList) {
    		return hostList.indexOf(conn);
    	}
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#getHostCount()
     */
    public int getHostCount()
    {
        List conns = getHostList();    	
    	synchronized(conns) {
            return conns.size();
    	}
    }


    public boolean addHost(IHost conn)
    {
    	assert conn!=null;
    	if (conn!=null) {
           	List hostList = getHostList();
       		synchronized(hostList) {
            	if (!hostList.contains(conn))
            	{
            		hostList.add(conn);
            	}
    		}
            conn.setHostPool(this);
            invalidateCache();
            return true;
    	}
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#deleteHost(org.eclipse.rse.core.model.IHost)
     */
    public void deleteHost(IHost conn)
    {
    	conn.deletingHost(); // let connection do any necessary cleanup
    	List hostList = getHostList();
		synchronized(hostList) {
			hostList.remove(conn);
		}
        setDirty(true);
        conn.getSystemProfile().commit();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#renameHost(org.eclipse.rse.core.model.IHost, java.lang.String)
     */
    public void renameHost(IHost conn, String newName)
    {
    	//must not change the alias name while a getHost() or orderHosts() is ongoing
    	synchronized(connections) {
            conn.setAliasName(newName);
    	}
        invalidateCache();
   	    commit(conn);
    }



    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#cloneHost(org.eclipse.rse.core.model.ISystemHostPool, org.eclipse.rse.core.model.IHost, java.lang.String)
     */
    public IHost cloneHost(ISystemHostPool targetPool, IHost conn, String aliasName)
    {
        IHost copy =
            targetPool.createHost(conn.getSystemType(), aliasName,
                 conn.getHostName(), conn.getDescription(), conn.getLocalDefaultUserId(), IRSEUserIdConstants.USERID_LOCATION_HOST);
        
        // Copy all properties as well.
		clonePropertySets(copy, conn.getPropertySets());
        return copy;
    }

    /**
     * Make copies of a list of property sets and add them to the specified host.
     * Each property set may contain its own list of property sets, so the
     * method is recursive.
     * @param copy
     * @param fromSets
     */
	private static void clonePropertySets(IHost copy, IPropertySet[] fromSets) {
		if (fromSets == null) {
			return;
		}
		for (int i = 0, n = fromSets.length; i < n; ++i) {
			IPropertySet fromSet = fromSets[i];
			IPropertySet copySet = copy.createPropertySet(fromSet.getName(), fromSet.getDescription());
			String[] fromKeys = fromSet.getPropertyKeys();
			for (int i2 = 0, n2 = fromKeys.length; i2 < n2; ++i2) {
				IProperty fromProperty = fromSet.getProperty(fromKeys[i2]);
				copySet.addProperty(fromProperty.getKey(), fromProperty.getValue(), fromProperty.getType());
			}
			clonePropertySets(copy, fromSet.getPropertySets());
		}
	}


    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.core.model.ISystemHostPool#moveHosts(org.eclipse.rse.core.model.IHost[], int)
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
		boolean moved;
		List hostList = getHostList();
		synchronized(hostList) {
			for (int i = 0; i < hosts.length; i++) {
				IHost host = hosts[i];
				int index = hostList.indexOf(host);
				if (index >= 0) {
					indices.add(new Integer(index));
				}
			}
			// Go through the sorted list of indices.
			moved = indices.size() > 0;
			for (Iterator z = indices.iterator(); z.hasNext() && moved;) {
				int index = ((Integer) z.next()).intValue();
				moved &= moveHost(hostList, index, delta);
			}
		}
		if (moved) {
			invalidateCache();
			commit();
		}
	}
    
    /**
	 * Move a host to a new location in the passed-in host list.
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.ISystemHostPool#orderHosts(java.lang.String[])
	 */
	public void orderHosts(String[] names) {
		List connList = getHostList();
		synchronized(connList) {
			//Threading: need to call getHost() from inside the synchronized block in order
			//to avoid problems with adding/removing hosts while the re-ordering is taking
			//place... hosts added during the re-ordering could otherwise be deleted.
			//In order to not have an alien method call here, getHost() is declared final.
			IHost[] conns = new IHost[names.length];
			for (int idx = 0; idx < conns.length; idx++) {
				conns[idx] = getHost(names[idx]); //may return null host
			}
			//TODO what should we do with hosts that are not in the name list?
			//Currently, these will be removed... should they be added at the end instead?
			connList.clear();
			for (int idx = 0; idx < conns.length; idx++) {
				if (conns[idx]!=null) {
					connList.add(conns[idx]);
				}
			}
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEModelObject#getName()
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
	 * Set the name of this host pool.
	 * <p>
	 * This method should not be called by clients directly in order 
	 * to maintain correct relationship to the owning profile. Clients
	 * should call {@link #renameHostPool(String)} instead.
	 * </p>
	 * @param newName The new value of the Name attribute.
	 */
	protected void setName(String newName)
	{
		name = newName;
	}

	/**
	 * Return the internal list of connection references.
	 * 
	 * The list returned is considered internal and not synchronized.
	 * Modifications will directly apply to any other client or thread.
	 * Therefore, users of the returned host list should typically be
	 * synchronized to avoid modification of the list through another thread
	 * when working on it.
	 * 
	 * Clients are not expected to get a handle on this list and
	 * are expected to use @link{#getHosts()} instead.
	 * 
	 * Subclasses may override this method to perform additional work
	 * or return a different kind of List implementation, but they must
	 * ensure that they always return the same List object (because 
	 * clients will synchronize on that object). In other words, the
	 * List may be modified but never totally exchanged.
	 * 
	 * @return The internal list of connection references.
	 */
	protected List getHostList()
	{
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
