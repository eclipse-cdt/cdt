/********************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [210534] Remove ISystemHostPool.getHostList() and setName()
 * David Dykstal (IBM) - [210537] removed exception signalling from this interface, no longer needed
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 ********************************************************************************/

package org.eclipse.rse.core.model;

import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.IRSEUserIdConstants;

/**
 * An ordered list of connections ({@link IHost} objects), owned by an
 * {@link ISystemProfile}.
 * <p>
 * Implementations of this interface are expected to be thread-safe in the sense
 * that integrity of the host list is maintained even if multiple threads call
 * multiple methods in this interface concurrently.
 * </p>
 * @noimplement This interface is not intended to be implemented by clients.
 * The standard implementations are included in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISystemHostPool extends IRSEPersistableContainer {

	/**
	 * Return the system profile that owns this connection pool.
	 * @return the system profile that owns this connection pool.
	 */
	public ISystemProfile getSystemProfile();

	/**
	 * Return the name of this host pool.
	 * @return The value of the Name attribute.
	 */
	String getName();

	/**
	 * Rename this connection pool.
	 * @param newName the new name for this connection pool.
	 */
	public void renameHostPool(String newName);

	/**
	 * Return array of connections in this pool.
	 * 
	 * The returned array is a copy of the internal connection list. Modifications by
	 * clients will not affect the internal list of connections, but modifications to
	 * the array elements will affect the actual IHost objects maintained in the list.
	 * 
	 * @return array of connections in this pool.
	 */
	public IHost[] getHosts();

	/**
	 * Create a connection object, given only the minimal information.
	 * <p>
	 * THE RESULTING CONNECTION OBJECT IS ADDED TO THE LIST OF EXISTING CONNECTIONS FOR YOU.
	 * @param systemType system type matching one of the system types
	 *     defined via the systemTypes extension point.
	 * @param aliasName unique connection name.
	 * @param hostName IP name or address of the host.
	 * @return IHost object, or null if it failed to create
	 *   because the aliasName is not unique. All other errors throw an exception.
	 */
	public IHost createHost(IRSESystemType systemType, String aliasName, String hostName);

	/**
	 * Create a connection object, given all the possible attributes except default userId.
	 * <p>
	 * THE RESULTING CONNECTION OBJECT IS ADDED TO THE LIST OF EXISTING CONNECTIONS FOR YOU.
	 * @param systemType system type matching one of the system types
	 *     defined via the systemTypes extension point.
	 * @param aliasName unique connection name.
	 * @param hostName IP name or address of the host.
	 * @param description optional description of the connection. Can be null.
	 * @return IHost object, or <code>null</code> if it failed to create
	 *   because the aliasName is not unique. All other errors throw an exception.
	 */
	public IHost createHost(IRSESystemType systemType, String aliasName, String hostName, String description);

	/**
	 * Create a connection object, given all the possible attributes.
	 * <p>
	 * The new connection is added to the list and saved to disk.
	 * @param systemType system type matching one of the system types
	 *     defined via the systemTypes extension point.
	 * @param aliasName unique connection name.
	 * @param hostName IP name or address of the host.
	 * @param description optional description of the connection. Can be null.
	 * @param defaultUserId userId to use as the default for the subsystems.
	 * @param defaultUserIdLocation where to set the given default user Id. See IRSEUserIdConstants for values.
	 * @return IHost object, or <code>null</code> if it failed to create
	 *   because the aliasName is not unique.
	 * @see IRSEUserIdConstants
	 */
	public IHost createHost(IRSESystemType systemType, String aliasName, String hostName, String description, String defaultUserId, int defaultUserIdLocation);

	/**
	 * Update an existing connection given the new information.
	 * This method:
	 * <ul>
	 *  <li>calls the setXXX methods on the given connection object, updating the information in it.
	 *  <li>saves the connection to disk (renaming its folder if needed)
	 * </ul>
	 * <p>
	 * @param conn IHost to be updated
	 * @param systemType system type matching one of the system types
	 *     defined via the systemType extension point.
	 * @param aliasName unique connection name.
	 * @param hostName IP name or address of the host.
	 * @param description optional description of the connection. Can be null.
	 * @param defaultUserId userId to use as the default for the subsystems.
	 * @param defaultUserIdLocation where to set the given default user Id from IRSEUserIdConstants.
	 * @see IRSEUserIdConstants
	 */
	public void updateHost(IHost conn, IRSESystemType systemType, String aliasName, String hostName, String description, String defaultUserId, int defaultUserIdLocation);

	/**
	 * Return a connection object, given its alias name.
	 * 
	 * Can be used to test if an alias name is already used (non-null return).
	 * 
	 * @param aliasName unique aliasName (case insensitive) to search on.
	 * @return IHost object with unique aliasName, or null if
	 *  no connection object with this name exists.
	 */
	public IHost getHost(String aliasName);

	/**
	 * Return the connection at the given zero-based offset.
	 * @param pos zero-based offset of requested connection in the connection list.
	 * @return IHost object requested.
	 */
	public IHost getHost(int pos);

	/**
	 * Add a new connection to the list.
	 * <p>
	 * This method will not ensure that the added connection's alias name is
	 * different (case-insensitive) than all other existing alias names.
	 * Clients are responsible for maintaining this invariant.
	 * </p>
	 * @param conn Connection to add. Must not be <code>null</code>.
	 * @return <code>true</code> if the new connection was added successfully, false otherwise.
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
	 * @param conn IHost object to remove
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
	 * @param conn IHost object to rename
	 * @param newName The new alias name to give that connection.
	 *     The alias name is not checked for uniqueness. Clients are responsible
	 *     themselves for ensuring that no two connections with the same alias
	 *     name (compared case insensitive) are created.
	 */
	public void renameHost(IHost conn, String newName);

	/**
	 * Return the zero-based position of a connection object within this host pool.
	 * @param conn connection to find in this host pool.
	 * @return the zero-based position of the requested connection in this host pool,
	 *     or -1 if the connection was not found.
	 */
	public int getHostPosition(IHost conn);

	/**
	 * Return the number of connections within this pool.
	 * @return the number of IHost objects within this pool.
	 */
	public int getHostCount();

	/**
	 * Duplicates a given connection in this list within this list or another list.
	 * @param targetPool The ISystemHostPool to hold the copied connection. Can equal this pool,
	 *     as long as alias name is unique.
	 * @param conn IHost object (within our pool) to clone.
	 * @param aliasName New, unique, alias name to give this connection. Clone will fail
	 *     (returning <code>null</code> as result) if this is not unique.
	 * @return the cloned host, or <code>null</code> if the new alias name was not unique.
	 */
	public IHost cloneHost(ISystemHostPool targetPool, IHost conn, String aliasName);

	/**
	 * Move existing connections a given number of positions in the same pool.
	 * If the delta is negative, they are all moved up (left) by the given amount. If
	 * positive, they are all moved down (right) by the given amount.<p>
	 * <ul>
	 * <li>After the move, the pool containing the moved connection is saved to disk.
	 * <li>The connection's alias name must be unique in pool.
	 * </ul>
	 * <b>TODO PROBLEM: CAN'T RE-ORDER FOLDERS SO CAN WE SUPPORT THIS ACTION?</b>
	 * @param conns an Array of hosts to move, can be empty but must not be null.
	 * @param delta the amount by which to move the hosts within this pool
	 */
	public void moveHosts(IHost conns[], int delta);

	/**
	 * Order connections by alias name, in the order given by the names parameter.
	 * 
	 * Called after restore to order by user preferences. Alias names are case-insensitive.
	 * Existing connections in the internal connection list that do not match any
	 * alias name in the given name list, will be deleted from this host pool!
	 * 
	 * @param names list of connection alias names in expected order.
	 */
	public void orderHosts(String[] names);

}