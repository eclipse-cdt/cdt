/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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




import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
//

/**
 * Interface for SystemConnection objects. 
 * A SystemConnect holds information identifying a remote system. It also logically contains
 *   SubSystem objects, although this containment is achievable programmatically versus via 
 *   object oriented containment.
 * <p>
 */
/**
 * @lastgen interface SystemConnection  {}
 */
public interface IHost extends IRSEModelObject
{
	/**
     * Return the system profile that owns this connection
     */
    public ISystemProfile getSystemProfile();

    /**
     * Return the name of the system profile that owns this connection
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
	 * Just a shortcut to {@link org.eclipse.rse.model.ISystemRegistry#getSubSystems(IHost)} 
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
	 * Private method called when this connection is being deleted, so
	 * we can do any pre-death cleanup we need.
	 * <p>
	 * What we need to do is delete our entry in the preference store for our default userId.
	 */
	public void deletingHost();   

	/**
	 * Private method called when this connection's profile is being rename, so
	 * we can do any pre-death cleanup we need.
	 * <p>
	 * What we need to do is rename our entry in the preference store for our default userId.
	 */
	public void renamingSystemProfile(String oldName, String newName);   
    /**
     * Call this to query whether the default userId is to be uppercased.
     */
    public boolean getForceUserIdToUpperCase();     
    /**
     * Call this to compare two userIds taking case sensitivity
     */
    public boolean compareUserIds(String userId1, String userId2);        
/**
	 * @return The value of the SystemType attribute
	 */
	public String getSystemType();

/**
	 * @param value The new value of the SystemType attribute
	 */
	public void setSystemType(String value);

/**
	 * @return The value of the AliasName attribute
	 * The unique key for this object. Unique per connection pool
	 */
	public String getAliasName();

/**
	 * @param value The new value of the AliasName attribute
	 */
	public void setAliasName(String value);

/**
	 * @return The value of the HostName attribute
	 */
	public String getHostName();

/**
	 * @param value The new value of the HostName attribute
	 */
	public void setHostName(String value);

/**
	 * @return The value of the Description attribute
	 */
	public String getDescription();

/**
	 * @param value The new value of the Description attribute
	 */
	public void setDescription(String value);

/**
	 * We return the default user Id. Note that we don't store it directly in
	 * the mof-modelled attribute, as we don't want the team to share it. Rather,
	 * we store the actual user Id in the preference store keyed by this connection's
	 * unique name (profile.connName) and store that key in this attribute.
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
	 * @generated This field/method will be replaced during code generation 
	 * @return The value of the Promptable attribute
	 */
	boolean isPromptable();

	/**
	 * @generated This field/method will be replaced during code generation 
	 * @param value The new value of the Promptable attribute
	 */
	void setPromptable(boolean value);

	/**
	 * Returns the value of the '<em><b>Offline</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * Is this connection offline? If so, there is no live connection. Subsystems
	 *  decide how much to enable while offline.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Offline</em>' attribute.
	 * @see #setOffline(boolean)
	 * @see org.eclipse.rse.model.ModelPackage#getSystemConnection_Offline()
	 * @model 
	 * @generated
	 */
	boolean isOffline();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.model.IHost#isOffline <em>Offline</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Offline</em>' attribute.
	 * @see #isOffline()
	 * @generated
	 */
	void setOffline(boolean value);

	/**
	 * Returns all the connector services provided
	 * for this host
	 * @return the connector services
	 */
    IConnectorService[] getConnectorServices();
}