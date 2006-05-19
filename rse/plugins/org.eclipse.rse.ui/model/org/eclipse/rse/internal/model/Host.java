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

package org.eclipse.rse.internal.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemHostPool;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.ui.RSEUIPlugin;



/**
 * A single connection object.
 */
public class Host extends RSEModelObject implements IHost, IAdaptable
{

	/**
	 * The default value of the '{@link #getSystemType() <em>System Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSystemType()
	 * @generated
	 * @ordered
	 */
	protected static final String SYSTEM_TYPE_EDEFAULT = null;

	private boolean              ucId = true;
	private boolean              userIdCaseSensitive = true;
	private ISystemHostPool pool;
    protected String             previousUserIdKey;
    	
	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String systemType = SYSTEM_TYPE_EDEFAULT;
	/**
	 * The default value of the '{@link #getAliasName() <em>Alias Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAliasName()
	 * @generated
	 * @ordered
	 */
	protected static final String ALIAS_NAME_EDEFAULT = null;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String aliasName = ALIAS_NAME_EDEFAULT;
	/**
	 * The default value of the '{@link #getHostName() <em>Host Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHostName()
	 * @generated
	 * @ordered
	 */
	protected static final String HOST_NAME_EDEFAULT = null;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String hostName = HOST_NAME_EDEFAULT;
	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String description = DESCRIPTION_EDEFAULT;
	/**
	 * The default value of the '{@link #getDefaultUserId() <em>Default User Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDefaultUserId()
	 * @generated
	 * @ordered
	 */
	protected static final String DEFAULT_USER_ID_EDEFAULT = null;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected String defaultUserId = DEFAULT_USER_ID_EDEFAULT;
	/**
	 * The default value of the '{@link #isPromptable() <em>Promptable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isPromptable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean PROMPTABLE_EDEFAULT = false;

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected boolean promptable = PROMPTABLE_EDEFAULT;
	/**
	 * The default value of the '{@link #isOffline() <em>Offline</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOffline()
	 * @generated
	 * @ordered
	 */
	protected static final boolean OFFLINE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isOffline() <em>Offline</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOffline()
	 * @generated
	 * @ordered
	 */
	protected boolean offline = OFFLINE_EDEFAULT;

	protected ISystemProfile _profile; 
	
	/**
	 * Constructor
	 */
	protected Host(ISystemProfile profile)
    {
		super();
		_profile = profile;
	}
	/**
     * Set the parent connection pool this is owned by.
     * Connection pools are internal management objects, one per profile.
     */
    public void setHostPool(ISystemHostPool pool)
    {
    	this.pool = pool;
    	previousUserIdKey = getPreferencesKey();    	
    }
    /**
     * Set the parent connection pool this is owned by.
     * Connection pools are internal management objects, one per profile.
     */
    public ISystemHostPool getHostPool()
    {
    	return pool;
    }
    
    /**
     * Return all the connector services provided for this host
     * @return
     */
    public IConnectorService[] getConnectorServices()
    {
    	return RSEUIPlugin.getTheSystemRegistry().getConnectorServices(this);
    }
    
    /**
     * Return the subsystem instances under this connection.<br>
     * Just a shortcut to {@link org.eclipse.rse.model.ISystemRegistry#getSubSystems(IHost)} 
     */
    public ISubSystem[] getSubSystems()
    {
    	return RSEUIPlugin.getTheSystemRegistry().getSubSystems(this);
    }

	
	
	/**
	 * Private method called when this connection is being deleted, so
	 * we can do any pre-death cleanup we need.
	 * <p>
	 * What we need to do is delete our entry in the preference store for our default userId.
	 */
	public void deletingHost()
	{
    	String oldUserId = null;
    	if (previousUserIdKey != null)
    	  oldUserId = getLocalDefaultUserId(previousUserIdKey);
    	// if the userId attribute held a preference store key of the form profileName.connectionName,
    	// we have to delete that key entry from the preference store and re-gen a new keyed entry with the same
    	// value (the actual user id) the old keyed entry held.
    	if (oldUserId != null)
    	{
   	      SystemPreferencesManager prefMgr = getPreferencesManager();    	    		
   	      prefMgr.clearUserId(previousUserIdKey);
    	}		  	
	}
	/**
	 * Private method called when this connection's profile is being rename, so
	 * we can do any pre-death cleanup we need.
	 * <p>
	 * What we need to do is rename our entry in the preference store for our default userId.
	 */
	public void renamingSystemProfile(String oldName, String newName)
    {
    	String userIdValue = null;
    	if (previousUserIdKey!=null)  
   	      userIdValue = getLocalDefaultUserId(previousUserIdKey); 
    	// if the userId attribute held a preference store key of the form profileName.connectionName,
    	// we have to delete that key entry from the preference store and re-gen a new keyed entry with the same
    	// value (the actual user id) the old keyed entry held.
    	String newKey = getPreferencesKey(newName, getAliasName());
    	if ((userIdValue != null) && (userIdValue.length()>0))
    	{
   	      SystemPreferencesManager prefMgr = getPreferencesManager();    	    		
   	      prefMgr.clearUserId(previousUserIdKey);
    	  prefMgr.setUserId(newKey, userIdValue); // store old value with new preference key
    	}
    	previousUserIdKey = newKey;    	
    }

    /**
     * Return the system profile that owns this connection
     */
    public ISystemProfile getSystemProfile()
    {
    	return _profile;
    }
    /**
     * Return the name of system profile that owns this connection
     */
    public String getSystemProfileName()
    {
    	if (pool == null)
    	  return null;
    	else
    	{
    	  ISystemProfile profile = pool.getSystemProfile();
    	  if (profile!=null)
    	    return profile.getName();
    	  else
    	    return null;
    	}
    }

	/**
     * Intercept of setAliasName so we can potentially rename the default-user-id key
     * for the preferences store. That key is profileName.connectionAliasName so is 
     * affected when the alias name changes.
     */
	public void setAliasName(String newName)
    {
    	String oldName = getAliasName(); // what it used to be.
    	String userIdValue = null;
    	if (previousUserIdKey != null)
   	       userIdValue = getLocalDefaultUserId(previousUserIdKey);     	  
    	this.setAliasNameGen(newName); // update mof-modelled attribute
    	// if the userId attribute held a preference store key of the form profileName.connectionAliasName,
    	// we have to delete that key entry from the preference store and re-gen a new keyed entry with the same
    	// value (the actual user id) the old keyed entry held.
    	String newKey = getPreferencesKey(getSystemProfileName(), newName);
    	if ((userIdValue != null) && (userIdValue.length()>0))
    	{
   	      SystemPreferencesManager prefMgr = getPreferencesManager();    	    		
   	      prefMgr.clearUserId(previousUserIdKey);
    	  prefMgr.setUserId(newKey, userIdValue); // store old value with new preference key
    	}
    	previousUserIdKey = newKey;
    }
	/**
     * Intercept of setSystemType so we can decide if the user ID is case sensitive
     */
	public void setSystemType(String systemType)
    {
    	// defect 43219
    	if (systemType != null)
    	{
    	  boolean forceUC = systemType.equals(IRSESystemType.SYSTEMTYPE_ISERIES);
    	  boolean caseSensitiveUID = systemType.equals(IRSESystemType.SYSTEMTYPE_UNIX)
    	                    || systemType.equals(IRSESystemType.SYSTEMTYPE_LINUX)
    	                    || (systemType.equals(IRSESystemType.SYSTEMTYPE_LOCAL) &&
    	                        !System.getProperty("os.name").toLowerCase().startsWith("windows"));
    	  setForceUserIdToUpperCase(forceUC);
    	  setUserIdCaseSensitive(caseSensitiveUID);
    	}
    	this.setSystemTypeGen(systemType);
    }
	/**
     * Intercept of setHostName so we can force it to uppercase
     */
	public void setHostName(String name)
    {
    	if (name != null)
    	  name = name.toUpperCase();
    	this.setHostNameGen(name);
    }
    /**
     * Intercept of setDefaultUserId so we can force it to uppercase.
     * Also, we do not store the user Id per se in the attribute, but rather
     * we store it in the preference with a key name unique to this connection.
     * We store that key name in this attribute. However, this is all transparent to
     * the caller.
     */
    public void setDefaultUserId(String newId)
    {
    	if ((newId != null) && ucId)
    	  newId = newId.toUpperCase();

    	if ((newId == null) || (newId.length()==0)) // a "clear" request?
        {          
          clearLocalDefaultUserId();
        }
    	else
    	{
    	  String key = getPreferencesKey();
    	  if (key != null)
    	  {
   	        SystemPreferencesManager prefMgr = getPreferencesManager();    	
    	    prefMgr.setUserId(key, newId);
    	  }
    	}
    }    
    /**
	 * Returns the default UserId for this Host.
	 * Note that we don't store it directly in
	 * the model, since we don't want the team to share it. Rather,
	 * we store the actual it in the preference store keyed by 
	 * (profileName.connectionName).
	 * <p>
	 * Further, it is possible that there is no default UserId. If so, this 
	 * method will go to the preference store and will try to get the default
	 * UserId for this connection's system type.
	 * <p>
	 * This is all transparent to the caller though.
	 * <p>
	 * @return The value of the DefaultUserId attribute
     */
    public String getDefaultUserId()
    {
    	String uid = getLocalDefaultUserId();
    	if ((uid == null) || (uid.length()==0))
    	{    		
   	      SystemPreferencesManager prefMgr = getPreferencesManager();    	
    	  uid = prefMgr.getDefaultUserId(getSystemType()); // resolve from preferences	
    	  if ((uid != null) && ucId)
    	    uid = uid.toUpperCase();          
    	}
    	return uid;
    }
    /**
     * Return the local default user Id without resolving up the food chain.
     * @see #getDefaultUserId()
     */
    protected static String getLocalDefaultUserId(String key)
    {
    	String uid = null;
    	if ((key!=null) && (key.length()>0))
    	{    	  
   	      SystemPreferencesManager prefMgr = getPreferencesManager();    	
    	  uid = prefMgr.getUserId(key); // resolve from preferences	
    	}   
    	return uid; 	
    }
    /**
     * Return the local default user Id without resolving up the food chain.
     * @see #getDefaultUserId()
     */
    public String getLocalDefaultUserId()
    {
    	return getLocalDefaultUserId(getPreferencesKey());
    }

    /**
     * Clear the local default user Id so next query will return the value from
     * the preference store.
     * <p>
     * Same as calling setDefaultUserId(null)
     * @see #setDefaultUserId(String)
     */
    public void clearLocalDefaultUserId()
    {
    	if (previousUserIdKey!=null)
     	  getPreferencesManager().clearUserId(previousUserIdKey);    	
    }   

    /**
     * Helper method to return preference manager
     */
    protected static SystemPreferencesManager getPreferencesManager()
    {
   	    return SystemPreferencesManager.getPreferencesManager();
    }
    
    /**
     * Helper method to compute a unique name for a given subsystem instance
     */
    protected String getPreferencesKey()
    {
    	if ((getSystemProfileName()==null) || (getAliasName()==null))
    	  return null;
    	return getPreferencesKey(getSystemProfileName());
    }    
    /**
     * Helper method to compute a unique name for a given subsystem instance, given a profile name
     */
    protected String getPreferencesKey(String profileName)
    {
    	String connectionName = getAliasName();
    	if (connectionName == null)
    	  return null;
    	return getPreferencesKey(profileName, connectionName);
    }
    /**
     * Helper method to compute a unique name for a given subsystem instance, given a profile name and connection name
     */
    protected String getPreferencesKey(String profileName, String connectionName)
    {
   	    return profileName + "." + connectionName;
    }
    
    
    /**
     * Call this with false to turn off the default behaviour of forcing the default userId to uppercase.
     */
    public void setForceUserIdToUpperCase(boolean force)
    {
    	this.ucId = force;
    }
    /**
     * Call this to turn off the default behaviour of considering case when comparing userIds
     */
    public void setUserIdCaseSensitive(boolean caseSensitive)
    {
    	this.userIdCaseSensitive = caseSensitive;
    }

    /**
     * Call this to query whether the default userId is to be uppercased.
     */
    public boolean getForceUserIdToUpperCase()
    {
    	return ucId;
    }
    /**
     * Call this to query whether the default userId is case sensitive
     */
    public boolean getUserIdCaseSensitive()
    {
    	return userIdCaseSensitive;
    }
    /**
     * Call this to compare two userIds taking case sensitivity
     */
    public boolean compareUserIds(String userId1, String userId2)
    {
    	if (userId1 == null)
    	  userId1 = "";
    	if (userId2 == null)
    	  userId2 = "";
    	if (userIdCaseSensitive)
    	  return userId1.equals(userId2);
    	else
    	  return userId1.equalsIgnoreCase(userId2);
    }
     
	public String toString()
    {
        if (getAliasName() == null)
          return this.toStringGen();
        else
          return getAliasName();
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

    
	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getSystemType()
	{
		return systemType;
	}

	/**
	 * Returns the alias name for this host
	 */
	public String getName()
	{
		return getAliasName();
	}
	
	/**
	 * @generated This field/method will be replaced during code generation 
	 * The unique key for this object. Unique per connection pool
	 */
	public String getAliasName()
	{
		return aliasName;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getHostName()
	{
		return hostName;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setDescription(String newDescription)
	{
		description = newDescription;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public boolean isPromptable()
	{
		return promptable;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setPromptable(boolean newPromptable)
	{
		promptable = newPromptable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * Query if this connection is offline or not. It is up to each subsystem to honor this
	 *  flag. 
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isOffline()
	{
		return offline;
	}

	/**
	 * <!-- begin-user-doc -->
	 * Specify if this connection is offline or not. It is up to each subsystem to honor this
	 *  flag. 
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOffline(boolean newOffline)
	{
		offline = newOffline;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setSystemTypeGen(String newSystemType)
	{
		systemType = newSystemType;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setAliasNameGen(String newAliasName)
	{
		aliasName = newAliasName;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setHostNameGen(String newHostName)
	{
		hostName = newHostName;
	}

	/**
	 * @generated This field/method will be replaced during code generation 
	 */
	public String getDefaultUserIdGen()
	{
		return defaultUserId;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setDefaultUserIdGen(String newDefaultUserId)
	{
		defaultUserId = newDefaultUserId;
	}

	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	public String toStringGen()
	{
		

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (systemType: ");
		result.append(systemType);
		result.append(", aliasName: ");
		result.append(aliasName);
		result.append(", hostName: ");
		result.append(hostName);
		result.append(", description: ");
		result.append(description);
		result.append(", defaultUserId: ");
		result.append(defaultUserId);
		result.append(", promptable: ");
		result.append(promptable);
		result.append(", offline: ");
		result.append(offline);
		result.append(')');
		return result.toString();
	}
	
	public boolean commit() 
	{
		return RSEUIPlugin.getThePersistenceManager().commit(this.getSystemProfile());
	}

}