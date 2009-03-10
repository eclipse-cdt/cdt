/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [206742] Make SystemHostPool thread-safe
 * David Dykstal (IBM) - [197036] cleaned javddoc for getConnectorServices()
 * David Dykstal (IBM) - [226561] Add API markup to RSE javadocs for extend / implement
 * David Dykstal (IBM) - [261486][api] add noextend to interfaces that require it
 *******************************************************************************/

package org.eclipse.rse.core.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * Interface for Host (SystemConnection) objects.
 *  
 * An IHost holds information identifying a remote system. It also logically contains
 * ISubSystem objects, although this containment is achievable programmatically versus via 
 * object oriented containment.
 * @noimplement This interface is not intended to be implemented by clients.
 * The standard implementations are included in the framework.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IHost extends IAdaptable, IRSEModelObject {
	
	/**
	 * Return the system profile that owns this connection
	 * @return the profile which contains this host
	 */
	public ISystemProfile getSystemProfile();

	/**
	 * Return the name of the system profile that owns this connection
	 * FIXME Check how this is different from getSystemProfile().getName()
	 */
	public String getSystemProfileName();

	/**
	 * Set the parent connection pool this is owned by.
	 * Connection pools are internal management objects, one per profile.
	 */
	public void setHostPool(ISystemHostPool pool);

	/**
	 * Set the parent connection pool this is owned by.
	 * Connection pools are internal management objects, one per profile.
	 */
	public ISystemHostPool getHostPool();

	/**
	 * Return the subsystem instances under this connection.
	 * Just a shortcut to {@link org.eclipse.rse.core.model.ISystemRegistry#getSubSystems(IHost)} 
	 */
	public ISubSystem[] getSubSystems();

	/**
	 * Return the local default user Id without resolving up the food chain.
	 * @see #getDefaultUserId()
	 */
	public String getLocalDefaultUserId();

	/**
	 * Clear the local default user Id so next query will return the value from
	 * the preference store.
	 * <p>
	 * Same as calling setDefaultUserId(null)
	 * @see #setDefaultUserId(String)
	 */
	public void clearLocalDefaultUserId();

	/**
	 * Notification method called when this connection is being deleted.
	 * Allows doing pre-death cleanup in overriders.
	 * <p>
	 * What we need to do is delete our entry in the preference store for our default userId.
	 */
	public void deletingHost();

	/**
	 * Notification method called when this connection's profile is being renamed.
	 * Allows doing pre-death cleanup in overriders.
	 * <p>
	 * Implementations must not fork off other threads in the implementation of this method,
	 * since the old and new profiles will be locked during the rename operation so deadlock
	 * could occur when another thread tries to access theprofile during the time of rename
	 * ongoing.
	 * </p><p>
	 * What we need to do is rename our entry in the preference store for our default userId.
	 * </p>
	 */
	public void renamingSystemProfile(String oldName, String newName);

	/**
	 * Query whether the default userId is to be uppercased.
	 * @return <code>true</code> if the user id is to be uppercased.
	 */
	public boolean getForceUserIdToUpperCase();

	/**
	 * Compare two userIds taking case sensitivity into account.
	 * @param userId1 first id to compare
	 * @param userId2 second id to compare
	 */
	public boolean compareUserIds(String userId1, String userId2);

	/**
	 * Get the system type.
	 * @return The value of the SystemType attribute
	 */
	public IRSESystemType getSystemType();

	/**
	 * Set the system type.
	 * @param value The new value of the SystemType attribute
	 */
	public void setSystemType(IRSESystemType value);

	/**
	 * Get the unique user-visible connection name.
	 * This is a key that is unique per connection pool.
	 * @return The value of the AliasName attribute
	 */
	public String getAliasName();

	/**
	 * Set the unique user-visible connection name.
	 * This needs to be a key that is unique per connection pool.
	 * @param value The new value of the AliasName attribute
	 */
	public void setAliasName(String value);

	/**
	 * Get the host name or IP address.
	 * @return The value of the HostName attribute
	 */
	public String getHostName();

	/**
	 * Set the host name or IP address.
	 * @param value The new value of the HostName attribute
	 */
	public void setHostName(String value);

	/**
	 * Return the description of this host.
	 * @return The value of the Description attribute
	 */
	public String getDescription();

	/**
	 * Set the description of this host.
	 * @param value The new value of the Description attribute
	 */
	public void setDescription(String value);

	/**
	 * Return the default user Id for this host.
	 * 
	 * Note that we don't store it directly in an attribute, as we don't want 
	 * the team to share it. The actual user Id is stored in the preference 
	 * store keyed by this connection's unique name (profile.connName) instead,
	 * and that key is stored in this attribute.
	 * <p>
	 * Further, it is possible that there is no default user id. If so, this 
	 * method will go to the preference store and will try to get the default user
	 * Id per this connection's system type.
	 * <p>
	 * This is all transparent to the caller though.
	 * <p>
	 * @return The value of the DefaultUserId attribute
	 */
	public String getDefaultUserId();

	/**
	 * Intercept of setDefaultUserId so we can force it to uppercase.
	 * Also, we do not store the user Id per se in the attribute, but rather
	 * we store it in the preference with a key name unique to this connection.
	 * We store that key name in this attribute. However, this is all transparent to
	 * the caller.
	 * @param value The new value of the DefaultUserId attribute
	 */
	public void setDefaultUserId(String value);

	/**
	 * Check if this host is promptable.
	 * @return The value of the Promptable attribute
	 */
	boolean isPromptable();
	
	/**
	 * Returns the default encoding of the host.
	 * <p>
	 * If fromRemote is <code>false</code>, returns an encoding preference
	 * specified locally without querying the remote system (for example, 
	 * an encoding set by a user).
	 * If fromRemote is <code>true</code>, it first checks to see if there is
	 * a local "overriding" encoding set by the client without querying the
	 * remote system, and if such a "local" encoding preference does not exist,
	 * returns the encoding that was set by the client by querying a remote system.
	 * </p>
	 * @param fromRemote <code>false</code> to get the encoding that was
	 *    obtained by the client by not querying the remote system,
	 *     <code>true</code> to also check the encoding, if needed, that was
	 *     set by the client after querying a remote system.
	 * @return the default encoding of the host, or <code>null</code> if
	 *     no encoding was set.
	 * @see #setDefaultEncoding(String, boolean)
	 */
	public String getDefaultEncoding(boolean fromRemote);
	
	/**
	 * Set the default encoding of the host.
	 * <p>
	 * This method can only be called when no subsystem is connected.
	 * The client has to obtain the encoding either by querying the
	 * remote system, or by some other means (for example, set by a user).
	 * </p>
	 * @param encoding the encoding of the host, or <code>null</code>
	 *     to erase the current encoding.
	 * @param fromRemote <code>true</code> if the encoding is set by the
	 *     client after querying the remote system, or <code>false</code> 
	 *     otherwise (e.g. setting a local user preference).
	 * @see #getDefaultEncoding(boolean)
	 */
	public void setDefaultEncoding(String encoding, boolean fromRemote);

	/**
	 * Set the promptable attribute.
	 * @param value The new value of the Promptable attribute
	 */
	void setPromptable(boolean value);

	/**
	 * Returns the value of the '<em><b>Offline</b></em>' attribute.
	 * <p>
	 * Query if this connection is offline or not. 
	 * If so, there is no live connection. Subsystems
	 * decide how much to enable while offline.
	 * It is up to each subsystem to honor this flag.
	 * </p>
	 * @return the value of the '<em>Offline</em>' attribute.
	 * @see #setOffline(boolean)
	 */
	boolean isOffline();

	/**
	 * Specify if this connection is offline or not.
	 * It is up to each subsystem to honor this flag.
	 *  
	 * @param value the new value of the '<em>Offline</em>' attribute.
	 * @see #isOffline()
	 */
	void setOffline(boolean value);

	/**
	 * Returns all the connector services currently configured for this host
	 * @return the connector services
	 */
	IConnectorService[] getConnectorServices();
}
