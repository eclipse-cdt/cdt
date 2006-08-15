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

package org.eclipse.rse.core.subsystems;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IRSEModelObject;
import org.eclipse.rse.model.IHost;
import org.eclipse.swt.widgets.Shell;


/**
 * This is the interface implemented by ConnectorService (formerly System) objects.
 * <p>
 * A connector service manages a live connection to a remote system, with
 * operations for connecting and disconnecting, and storing information
 * typically cached from a subsystem: user ID, password, port, etc.
 * <p>
 * The SubSystem interface includes a method, getConnectorService(), which returns an
 * instance of an object that implements this interface for that subsystem.
 * <p>
 * A single connector service object can be unique to a subsystem instance, but
 * it can also be shared across multiple subsystems in a single host if those
 * subsystems share a physical connection to the remote system. This sharing is done via
 * subclasses of {@link org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager}
 * which are returned by another getter method in SubSystem.
 */ 
public interface IConnectorService extends IRSEModelObject
{
	
	/**
     * Return the subsystem object this system is associated with
     */
    public ISubSystem getPrimarySubSystem();	
    
    /**
     * Return all the subsystems that use this service
     * @return the subsystems that use this service
     */
    public ISubSystem[] getSubSystems();
    
	/**
	 * Set the subsystem, when its not known at constructor time
	 */
	public void registerSubSystem(ISubSystem ss);

	/**
	 * Deregister the subsystem
	 * @param ss
	 */
	 public void deregisterSubSystem(ISubSystem ss);
	 
	/**
	 * Return true if currently connected.
	 */
    public boolean isConnected();
    /**
     * Attempt to connect to the remote system.
     */    
    public void connect(IProgressMonitor monitor) throws Exception;    
    /**
     * Disconnect from the remote system
     */
    public void disconnect(IProgressMonitor monitor) throws Exception;
    /**
     * Notifies all listeners of a disconnection through a communications event
     */
    public void notifyDisconnection();
    /**
     * Notifies all listeners of a connection through a communications event
     */
    public void notifyConnection();
    /**
     * Notifies all listeners of an error through a communications event
     */
    public void notifyError();
    /**
     * Reset after some fundamental change, such as a hostname change.
     * Clear any memory of the current connection.
     */    
    public void reset();   
    /**
     * Return the version, release, modification of the remote system,
     *  if connected, if applicable and if available. Else return null.
     * <p>
     * Up to each implementer to decide if this will be cached.
     */
    public String getVersionReleaseModification();	
    /**
     * Return the home directory of the remote system for the current user, if available.
     * <p>
     * Up to each implementer to decide how to implement, and if this will be cached.
     */
    public String getHomeDirectory();	
    /**
     * Return the temp directory of the remote system for the current user, if available.
     * <p>
     * Up to each implementer to decide how to implement, and if this will be cached.
     */
    public String getTempDirectory();	        
    
    // --------------------------------------------------------------------
    // Utility methods that offer combined connection and subsystem info...
    // --------------------------------------------------------------------
    /**
     * Return the system type for this connection.
     */
    public String getHostType();
    
    /**
     * Return the name of this connector service
     * @return the name of this connector service
     */
    public String getName();
    
    public void setHost(IHost host);
    
    /**
     * Return the host
     * @return
     */
    public IHost getHost();
    
    /**
     * Return the host name for the connection this system's subsystem is associated with
     */
    public String getHostName();
    
    /**
     * Return the port for this connector
     */
    public int getPort();
    
    /**
     * Set the port for this connector
     * @param port
     */
    public void setPort(int port);
    
    /**
     * Return the userId for this system's subsystem we are associated with
     */
    public String getUserId();
       
    /**
     * Set the user id for this connector
     * @param userId
     */
    public void setUserId(String userId);
    
    
    public boolean isUsingSSL();
    
    public void setIsUsingSSL(boolean flag);
    
    /**
     * Return the password for this system's subsystem we are associated with.
     * <p>
     * If not currently set in transient memory, prompts the user for a password.
     * <p>
     * Throws InterruptedException if user is prompted and user cancels that prompt.
     * @param shell parent for the prompt dialog if needed. Can be null if know password exists.
     * @param forcePrompt forces the prompt dialog to be displayed even if the password is currently
	 * in memory.
     */
    public void promptForPassword(Shell shell, boolean forcePrompt)
           throws InterruptedException;
    /**
     * Set the password if you got it from somewhere
     */
    public void setPassword(String matchingUserId, String password);
    
    /**
     * Set the password if you got it from somewhere
     */
    public void setPassword(String matchingUserId, String password, boolean persist);
    
    /**
     * Clear internal userId cache. Called when user uses the property dialog to 
     * change his userId.
     */
    public void clearUserIdCache();
    /**
     * Clear internal password cache. Called when user uses the property dialog to 
     * change his userId.
     */
    public void clearPasswordCache();
        
    /**
     * Clear internal password cache. Called when user uses the property dialog to 
     * change his userId.  
     * @param clearDiskCache if true, clears the password from disk 
     */
    public void clearPasswordCache(boolean clearDiskCache);
	/**
	 * Return true if password is currently cached.
	 */
    public boolean isPasswordCached();  
    
    
    /**
	 * Return true if password is currently cached.
	 */
    public boolean isPasswordCached(boolean onDisk);  

    /**
     * Return true if this system can inherit the uid and password of
     * other ISystems in this connection
     * 
     * @return true if it can inherit the user/password
     */
    public boolean inheritConnectionUserPassword();
    
    /*
     * Return true if this system can share it's uid and password
     * with other ISystems in this connection
     * 
     * @return true if it can share the user/password
     */
    public boolean shareUserPasswordWithConnection();
    
    /**
     * Register a communications listener
     */
    public void addCommunicationsListener(ICommunicationsListener listener);
    /**
     * Remove a communications listener
     */
    public void removeCommunicationsListener(ICommunicationsListener listener);
	/**
	 * Returns the suppressSignonPrompt flag.  If this is set to true then the user
	 * will not be prompted to signon, instead an InterruptedException will be thrown 
	 * by the promptForPassword method.
	 * 
	 * @return boolean
	 */
	public boolean isSuppressSignonPrompt();
	/**
	 * Sets the suppressSignonPrompt flag.  Tool writers can use this to temporarily
	 * disable the user from being prompted to signon.  This would cause the promptForPassword
	 * method to throw an InterruptedException instead of prompting.  The intent of this
	 * method is to allow tool writeres to prevent multiple signon prompts during a 
	 * set period of time (such as a series of related communication calls) if the user
	 * cancels the first prompt.  <b>It is the callers responsability to set this value 
	 * back to false when the tool no longer needs to suppress the signon prompt or all
	 * other tools sharing this connection will be affected.</b>
	 * 
	 * @param suppressSignonPrompt
	 */
	public void setSuppressSignonPrompt(boolean suppressSignonPrompt);
	
	/**
	 * Returns the value of the '<em><b>Remote Server Launcher</b></em>' containment reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.rse.core.subsystems.IServerLauncherProperties#getParentSubSystem <em>Parent Sub System</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * Get the remote server launcher, which may be null. This an optional object containing
	 *  properties used to launch the remote server that communicates with this subsystem.
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Remote Server Launcher</em>' containment reference.
	 * @see #setRemoteServerLauncher(IServerLauncherProperties)
	 * @see org.eclipse.rse.core.subsystems.SubsystemsPackage#getSubSystem_RemoteServerLauncher()
	 * @see org.eclipse.rse.core.subsystems.IServerLauncherProperties#getParentSubSystem
	 * @model opposite="parentSubSystem" containment="true"
	 * @generated
	 */
	IServerLauncherProperties getRemoteServerLauncherProperties();

	/**
	 * Sets the value of the '{@link org.eclipse.rse.core.subsystems.ISubSystem#getRemoteServerLauncher <em>Remote Server Launcher</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * Set the remote server launcher, which is an optional object containing
	 *  properties used to launch the remote server that communicates with this subsystem.
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Remote Server Launcher</em>' containment reference.
	 * @see #getRemoteServerLauncher()
	 * @generated
	 */
	void setRemoteServerLauncherProperties(IServerLauncherProperties value);
	
	boolean hasRemoteServerLauncherProperties();
	
	boolean supportsRemoteServerLaunching();
	
	/**
	 * Tell us if this subsystem factory supports server launch properties, which allow the user
	 * to configure how the server-side code for these subsystems are started. There is a Server
	 * Launch Setting property page, with a pluggable composite, where users can configure these 
	 * properties. 
	 */
	public boolean supportsServerLaunchProperties();
	
    /**
     * Report if this connector service can use a user identifier.
	 * Returns true in default implementation.
	 * Typically used to indicate if a login dialog needs to be presented when connecting.
     * @return true if and only if the connector service can use a user id.
     */
    public boolean supportsUserId();
    
    /**
     * Report if this connector service requires a user id.
	 * Returns true in default implementation.
	 * Typically used to indicate if a login dialog can allow an empty user id.
	 * Must be ignored if supportsUserId() is false.
     * @return true or false to indicate if the connector service requires a user id.
     */
    public boolean requiresUserId();
    
    /**
     * Can be used to determine if a password field is present on a login dialog for this connector service.
     * The default implementation of this interface should return true.
     * @return true if the subsystem can use a password, false if a password is irrelevant.
     */
    public boolean supportsPassword();
    
    /**
     * If a password is supported this is used to determine if the password is required.
     * Must be ignored if supportsPassword() returns false.
     * The default implementation of this interface should return true.
     * @return true if the connector service requires a password, false if a password may be empty.
     */
    public boolean requiresPassword();
    

}