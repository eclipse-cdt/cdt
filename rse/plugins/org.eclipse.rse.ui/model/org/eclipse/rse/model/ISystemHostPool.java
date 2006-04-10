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

package org.eclipse.rse.model;

import java.util.List;

import org.eclipse.rse.persistence.IRSEPersistableContainer;


//
/**
 * A list of connections.
 */
/**
 * @lastgen interface SystemConnectionPool  {}
 */

public interface ISystemHostPool extends IRSEPersistableContainer
{



    /**
     * Return the system profile that owns this connection pool
     */
    public ISystemProfile getSystemProfile();	
	/**
	 * Rename this connection pool.
	 */
	public void renameHostPool(String newName);
    /**
     * Return array of connections in this pool
     */
    public IHost[] getHosts();
    /**
     * Create a connection.
     */    
    public IHost createHost(String systemType, String aliasName, String hostName)
           throws Exception;
    /**
     * Create a connection.
     */    
    public IHost createHost(String systemType, String aliasName, String hostName, String description)
           throws Exception;
    /**
     * Create a connection.
     */    
    public IHost createHost(String systemType, String aliasName, String hostName, String description,
                                             String defaultUserId, int defaultUserIdLocation)
           throws Exception;                                             
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
     *                    systemtype extension point.
     * @param aliasName unique connection name.
     * @param hostName ip name of host.
     * @param description optional description of the connection. Can be null.
     * @param defaultUserId userId to use as the default for the subsystems.
     * @param defaultUserIdLocation where to set the given default user Id. See ISystemUserIdConstants
     */
    public void updateHost(IHost conn, String systemType,
                                 String aliasName, String hostName,
                                 String description,String defaultUserId, int defaultUserIdLocation)
        throws Exception;
       
    /**
     * Return a connection given its name.
     */    
    public IHost getHost(String aliasName);
    /**
     * Return the connection at the given zero-based offset
     */
    public IHost getHost(int pos);
    /**
     * Add a new connection to the list.
     */
    public boolean addHost(IHost conn);
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
    public void deleteHost(IHost conn);
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
    public void renameHost(IHost conn, String newName) throws Exception;
    /**
     * Return the zero-based position of a SystemConnection object within its profile.
     */
    public int getHostPosition(IHost conn);
    /**
     * Return the number of SystemConnection objects within this pool.
     */
    public int getHostCount();

    /**
     * Duplicates a given connection in this list within this list or another list.
     * @param targetPool The SystemConnectionPool to hold the copied connection. Can equal this connection, as long as alias name is unique
     * @param conn SystemConnection object (within our pool) to clone
     * @param alias New, unique, alias name to give this connection. Clone will fail if this is not unique.
     */
    public IHost cloneHost(ISystemHostPool targetPool, IHost conn, String aliasName)
       throws Exception;

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
    public void moveHosts(IHost conns[], int delta);
   
    /**
     * Order connections according to user preferences.
     * Called after restore.
     */
    public void orderHosts(String[] names);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the Name attribute
	 */
	String getName();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the Name attribute
	 */
	void setName(String value);

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @return The list of Connections references
	 */
	List getHostList();

}