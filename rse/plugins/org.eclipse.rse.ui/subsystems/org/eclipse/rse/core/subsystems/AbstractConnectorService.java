/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation. All rights reserved.
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
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 ********************************************************************************/

package org.eclipse.rse.core.subsystems;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.IRSEUserIdConstants;
import org.eclipse.rse.core.PasswordPersistenceManager;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.core.subsystems.util.ISubSystemConfigurationAdapter;
import org.eclipse.rse.logging.Logger;
import org.eclipse.rse.logging.LoggerFactory;
import org.eclipse.rse.model.ISystemRegistryUI;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.dialogs.ISignonValidator;
import org.eclipse.rse.ui.dialogs.ISystemPasswordPromptDialog;
import org.eclipse.rse.ui.dialogs.SystemChangePasswordDialog;
import org.eclipse.rse.ui.dialogs.SystemPasswordPromptDialog;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


/**
 * This is a base class to make it easier to create connector service classes.
 * <p>
 * An {@link org.eclipse.rse.core.subsystems.IConnectorService} object
 * is returned from a subsystem object via getConnectorService(), and
 * it is used to represent the live connection to a particular subsystem.
 * <p>
 * You must override/implement
 * <ul>
 * <li>isConnected
 * <li>internalConnect
 * <li>internalDisconnect
 * </ul>
 * You should override:
 * <ul>
 * <li>reset 
 * <li>getVersionReleaseModification
 * <li>getHomeDirectory
 * <li>getTempDirectory
 * </ul>
 * You can override:
 * <ul>
 * <li>supportsUserId
 * <li>requiresUserId
 * <li>supportsPassword
 * <li>requiresPassword
 * </ul>
 * 
 * @see org.eclipse.rse.core.subsystems.AbstractConnectorServiceManager
 */ 
public abstract class AbstractConnectorService extends SuperAbstractConnectorService implements IRSEUserIdConstants
{

    private String _userId;
    private transient SystemSignonInformation _passwordInfo;
    protected Shell shell;
	// dy:  March 24, 2003 Added _suppressSignonPrompt flag to suppress prompting the
	// user to signon if they already cancelled signon.  Then intent is to allows tools
	// writers to prevent multiple signon prompts during a "transaction" if the user cancel 
	// the first signon prompt
	private boolean _suppressSignonPrompt = false;


	public AbstractConnectorService(String name, String description, IHost host, int port)
	{
		super(name, description, host, port);
	}	

	
    /**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Returns the active userId if we are connected.
	 * If not it returns the userId for the primary subsystem ignoring the 
	 * cached userId.
	 */
	final public String getUserId() {
		String result = null;
		if (supportsUserId()) {
			result = getLocalUserId();
			if (result == null) {
				result = getSubSystemUserId();
			}
		}
		return result;
	}

	/**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Return the userId for this connector service. If there is none
	 * set for this service then it is retrieved from the primary subsystem.
	 */
	final protected String getLocalUserId() {
		return _userId;
	}
	
	/**
	 * @return the userId from the primary subsystem.
	 */
	private String getSubSystemUserId() {
		ISubSystem ss = getPrimarySubSystem();
		String result = ss.getUserId();
		return result;
	}
    
    /* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setUserId(java.lang.String)
	 */
	final public void setUserId(String newId) 
	{
		
		if (_userId == null || !_userId.equals(newId)) {
			_userId = newId;
			setDirty(true);
		}
	}
    
    /**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Clear internal userId cache. Called when user uses the property dialog to 
	 * change his userId. By default, sets internal userId value to null so that on
	 * the next call to getUserId() it is requeried from subsystem. 
	 * Also calls {@link #clearPasswordCache()}.
	 */
	final public void clearUserIdCache() {
		_userId = null;
		clearPasswordCache();
	}

	/**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Clear internal password cache. Called when user uses the property dialog to 
	 * change his userId.  This method does not remove the password from the disk
	 * cache - only the memory cache.
	 * 
	 * @see #clearUserIdCache()
	 */
	final public void clearPasswordCache() {
		clearPasswordCache(false);
	}

	/**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Clear internal password cache. Called when user uses the property dialog to 
	 * change his userId.  
	 * 
	 * @param onDisk if this is true, clear the password from the disk cache as well
	 * @see #clearUserIdCache()
	 */
	final public void clearPasswordCache(boolean onDisk) {
		setPasswordInformation(null);
		String userId = getUserId();
		if (onDisk) {
			//  now get rid of userid/password from disk
			String systemType = getHostType();
			String hostName = getHostName();
			if (userId != null)
				PasswordPersistenceManager.getInstance().remove(systemType, hostName, userId);
		}
		if (shareUserPasswordWithConnection()) {
			// clear this uid/password with other ISystems in connection
			clearPasswordForOtherSystemsInConnection(userId, onDisk);
		}
	}

	/**
	 * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Return true if password is currently saved either here or in its persisted
	 * form.
	 * @param onDisk true if the check should be made for a persisted form as well, 
	 * false if the check should be made for a password in memory only.
	 * @return true if the password is known, false otherwise.
	 */
	final public boolean isPasswordCached(boolean onDisk) {
		boolean cached = (getPasswordInformation() != null);
		if (!cached && onDisk) {
			//  now check if cached on disk
			String systemType = getHostType();
			String hostName = getHostName();
			String userId = getUserId();
			if (userId != null) {
				return PasswordPersistenceManager.getInstance().passwordExists(systemType, hostName, getUserId());
			}
		}
		return cached;
	}
    
	/**
     * <i>Useful utility method. Fully implemented, do not override.</i><br>
	 * Return true if password is currently cached.
	 */
    final public boolean isPasswordCached()
    {
        return isPasswordCached(false);
    }
    
    /**
     * Return true if this system can inherit the uid and password of
     * other ISystems in this connection
     * 
     * @return true if it can inherit the user/password
     */
    final public boolean inheritConnectionUserPassword()
    {
        return true;
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#requiresPassword()
	 */
	public boolean requiresPassword() {
		return true;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#supportsPassword()
	 */
	public boolean supportsPassword() {
		return true;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#requiresUserId()
	 */
	public boolean requiresUserId() {
		return true;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#supportsUserId()
	 */
	public boolean supportsUserId()
	{
		return true;
	}
	
	/**
     * Return true if this connector service can share it's uid and password
     * with other connector services in this host (connection).
     * @return true if it can share the user/password
     */
    public boolean shareUserPasswordWithConnection()
    {
        return true;
    }
    
    /**
     * <i>Do not override.</i>
     * Sets the signon information for this connector service.
     * The search order for the password is as follows:</p>
     * <ol>
     * <li>First check if the password is already known by this connector service and that it is still valid.
     * <li>If password not known then look in the password store and verify that it is still valid.
     * <li>If a valid password is not found then prompt the user.
     * </ol>
     * Must be run in the UI thread. 
     * Can be null if the password is known to exist in either this class or in the password store.
     * @param forcePrompt if true then present the prompt even if the password was found and is valid.
     * @throws InterruptedException if user is prompted and user cancels that prompt or if isSuppressSignonPrompt is true.
     */
    public void promptForPassword(boolean forcePrompt) throws InterruptedException {
		// dy:  March 24, 2003:  check if prompting is temporarily suppressed by a tool
		// vendor, this should only be suppressed if the user cancelled a previous signon
		// dialog (or some other good reason)
		if (isSuppressSignonPrompt()) {
			throw new InterruptedException();
		}

		ISubSystem subsystem = getPrimarySubSystem();
		IHost host = subsystem.getHost();
		String hostName = host.getHostName();
		String hostType = host.getSystemType();
		boolean savePassword = false;
		if (_passwordInfo == null) {
			_passwordInfo = new SystemSignonInformation(hostName, null, null, hostType);
		}
		if (supportsUserId()) {
			String userId = getUserId();
			if (_passwordInfo.getUserid() == null) {
				_passwordInfo.setUserid(userId);
			}
			boolean sameUserId = host.compareUserIds(userId, _passwordInfo.getUserid());
			boolean sameHost = hostName.equalsIgnoreCase(_passwordInfo.getHostname());
			if (!(sameHost && sameUserId)) {
				_passwordInfo.setPassword(null);
				_passwordInfo.setUserid(userId);
			}
		}
		if (supportsPassword()) {
			if (_passwordInfo.getPassword() == null) {
				PasswordPersistenceManager ppm = PasswordPersistenceManager.getInstance();
				String userId = _passwordInfo.getUserid();
				SystemSignonInformation savedPasswordInformation = ppm.find(hostType, hostName, userId);
				if (savedPasswordInformation != null) {
					_passwordInfo = savedPasswordInformation;
					savePassword = true;
				}
			}
		}
		ISignonValidator validator = getSignonValidator();
		boolean signonValid = (validator == null) || validator.isValid(shell, _passwordInfo);

		// If we ran into an invalid password we need to tell the user.
		// DWD refactor - want to move this to a pluggable class so that this can be moved to core.
		// DWD not sure this is necessary, shouldn't this message show up on the password prompt itself?
		if (!signonValid) {
       		SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_COMM_PWD_INVALID);
       		msg.makeSubstitution(getLocalUserId(), getHostName());
       		SystemMessageDialog dialog = new SystemMessageDialog(shell, msg);
       		dialog.open();
		}
		if (shell == null)
		{
			try
			{
				shell = SystemBasePlugin.getActiveWorkbenchShell();
			}
			catch (Exception e)
			{
				shell = new Shell();
			}
		}
    		
		if (supportsPassword() || supportsUserId()) 
		{
			if (shell == null)
			{
				try
				{
					shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				}
				catch (Exception e)
				{
					shell = new Shell();
				}
			}
			if (shell != null) {
				boolean passwordNeeded = supportsPassword() && _passwordInfo.getPassword() == null;
				boolean userIdNeeded = supportsUserId() && _passwordInfo.getUserid() == null;
				if (passwordNeeded || userIdNeeded || forcePrompt) {
					ISystemPasswordPromptDialog dialog = getPasswordPromptDialog(shell);
					if (dialog != null) {
						dialog.setSystemInput(this);
						dialog.setSignonValidator(getSignonValidator());
						if (supportsUserId()) {
							dialog.setUserIdValidator(getUserIdValidator());
						}
						if (supportsPassword()) {
							String password = _passwordInfo.getPassword();
							dialog.setSavePassword(savePassword);
							dialog.setPassword(password);
							dialog.setPasswordValidator(getPasswordValidator());
						}
			    	  	try {
			  	        	dialog.open();
			    	  	} catch (Exception e) {
			    	  		logException(e);
			    	  	}
			    	  	if (dialog.wasCancelled()) {
			    	  		_passwordInfo = null;
			    	  		throw new InterruptedException();
			    	  	}
			    	  	String userId = dialog.getUserId();
			    	  	boolean userIdChanged = dialog.getIsUserIdChanged();
			    	  	boolean saveUserId = dialog.getIsUserIdChangePermanent();
			    	  	String password = dialog.getPassword();
			    	  	savePassword = dialog.getIsSavePassword();
				    	if (supportsUserId() && userIdChanged) {
				      		if (saveUserId) {
			            		updateDefaultUserId(subsystem, userId);
				      		} else {
				      			setUserId(userId);
				        		_passwordInfo.setUserid(userId);
				      		}
				    	}
				    	if (supportsPassword()) {
					    	setPassword(userId, password, savePassword);
					    	if (shareUserPasswordWithConnection()) {
					    	    updatePasswordForOtherSystemsInConnection(userId, password, savePassword);
					    	}
				    	}
					}
				}
			}
		}
    }        
    
    protected void clearPasswordForOtherSystemsInConnection(String uid, boolean fromDisk)
    {
    	if (uid != null)
    	{
	        IHost connection = getHost();
	        ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
	        ISubSystem[] subsystems = registry.getSubSystems(connection);
	        
	        List uniqueSystems = new ArrayList();
	        for (int i = 0; i < subsystems.length; i++)
	        {
	            IConnectorService system = subsystems[i].getConnectorService();
	            if (system != this && system.inheritConnectionUserPassword())
	            {
	                if (!uniqueSystems.contains(system))
	                {
	                    uniqueSystems.add(system);
	                }
	            }
	        }
	        
	        for (int s = 0; s < uniqueSystems.size(); s++)
	        {
	            IConnectorService system = (IConnectorService)uniqueSystems.get(s);
	            if (system.isPasswordCached(fromDisk))
	            {
	                system.clearPasswordCache(fromDisk);
	            }            
	        }
    	}
    }
    
    
    protected void updatePasswordForOtherSystemsInConnection(String uid, String password, boolean persistPassword)
    {
        IHost connection = getPrimarySubSystem().getHost();
        ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
        ISubSystem[] subsystems = registry.getSubSystems(connection);
        
        List uniqueSystems = new ArrayList();
        for (int i = 0; i < subsystems.length; i++)
        {
            IConnectorService system = subsystems[i].getConnectorService();
            if (system != this && system.inheritConnectionUserPassword())
            {
                if (!uniqueSystems.contains(system))
                {
                    uniqueSystems.add(system);
                }
            }
        }
        
        for (int s = 0; s < uniqueSystems.size(); s++)
        {
            IConnectorService system = (IConnectorService)uniqueSystems.get(s);
            if (!system.isConnected() && !system.isPasswordCached())
            {
                if (system.getPrimarySubSystem().forceUserIdToUpperCase())
                {
                    system.setPassword(uid.toUpperCase(), password.toUpperCase(), persistPassword);
                }
                else
                {
                    system.setPassword(uid, password, persistPassword);
                }
            }            
        }
    }
    
    /**
	 * Change the default user Id value in the SubSystem if it is non-null, else
	 * update it in the Connection object
	 */
	private void updateDefaultUserId(ISubSystem subsystem, String userId) {
		String ssLocalUserId = subsystem.getLocalUserId(); 
		if (ssLocalUserId != null) { // defect 42709
			ISubSystemConfiguration ssc = subsystem.getSubSystemConfiguration(); 
			ssc.updateSubSystem(subsystem, true, userId, false, 0);
		} else { // it seems intuitive to update the connection object. defect 42709. Phil
			int whereToUpdate = IRSEUserIdConstants.USERID_LOCATION_HOST;
			IHost conn = subsystem.getHost();
			ISystemRegistryUI sr = RSEUIPlugin.getDefault().getSystemRegistry();
			sr.updateHost(null, conn, conn.getSystemType(), conn.getAliasName(), conn.getHostName(), conn.getDescription(), userId, whereToUpdate);
		}
	}

    /**
	 * <i>A default implementation is supplied, but can be overridden if desired.</i><br>
	 * Instantiates and returns the dialog to prompt for the userId and password.
	 * <p>
	 * By default returns an instance of SystemPasswordPromptDialog. Calls forcePasswordToUpperCase() to decide whether the user Id and password should be folded to uppercase.
	 * <p>
	 * Calls {@link #getUserIdValidator()} and {@link #getPasswordValidator()} to set the validators. These return null by default by you can override them.
	 * <p>
	 * Before calling open() on the dialog, the getPassword(Shell) method that calls this will call setSystemInput(this).
	 * <p>
	 * After return, it will call wasCancelled() and getUserId(), getIsUserIdChanged(), getIsUserIdChangePermanent() and getPassword().
	 * <p>
	 * 
	 * @return An instance of a dialog class that implements the ISystemPasswordPromptDialog interface
	 */
    protected final ISystemPasswordPromptDialog getPasswordPromptDialog(Shell shell)
    {
    	  ISystemPasswordPromptDialog dlg = new SystemPasswordPromptDialog(shell);
    	  dlg.setForceToUpperCase(forcePasswordToUpperCase());
  	      dlg.setUserIdValidator(getUserIdValidator());
  	      dlg.setPasswordValidator(getPasswordValidator());
  	      dlg.setSignonValidator(getSignonValidator());
    	  return dlg;
    }

    /**
	 * <i>Useful utility method. Fully implemented, no need to override.</i><br>
	 * @return the password information for the primary subsystem of this
	 * connector service. Assumes it has been set by the subsystem at the 
	 * time the subsystem acquires the connector service.
	 */
	protected SystemSignonInformation getPasswordInformation() {
		return _passwordInfo;
	}

	/**
	 * <i>Useful utility method. Fully implemented, no need to override.</i><br>
	 * Sets the password information for this system's subsystem.
	 * @param passwordInfo the password information object
	 */
	protected void setPasswordInformation(SystemSignonInformation passwordInfo) {
		_passwordInfo = passwordInfo;
		if (passwordInfo != null) {
			_userId = passwordInfo.getUserid();
		}
	}            
    
    /**
	 * <i>Useful utility method. Fully implemented, no need to override.</i><br>
	 * Set the password if you got it from somewhere
	 * @param matchingUserId the user for which to set the password
	 * @param password the password to set for this userid
	 * @param persist true if the password is to be persisted as well
	 */
	public void setPassword(String matchingUserId, String password, boolean persist) {
		if (getPrimarySubSystem().forceUserIdToUpperCase()) {
			matchingUserId = matchingUserId.toUpperCase();
		}
		SystemSignonInformation tempPasswordInfo = new SystemSignonInformation(getHostName(), matchingUserId, password, getHostType());
		setPasswordInformation(tempPasswordInfo);
		if (persist) { // if password should be persisted, then add to disk
			PasswordPersistenceManager.getInstance().add(tempPasswordInfo, true, true);
		} else { // otherwise, remove from both memory and disk
			String systemType = getHostType();
			String hostName = getHostName();
			PasswordPersistenceManager.getInstance().remove(systemType, hostName, _userId);
		}
	}
    
    /**
	 * <i>Useful utility method. Fully implemented, no need to override.</i><br>
	 * A convenience method, fully equivalent to <code>setPassword(matchingUserId, password, false)</code>
	 * @param matchingUserId the user for which to set the password
	 * @param password the password to set for this userid
	 */
	public void setPassword(String matchingUserId, String password) {
		setPassword(matchingUserId, password, false);
	}
    
    /**
     * <i>Useful utility method. Fully implemented, no need to override.</i><br>
     * Should passwords be folded to uppercase?
     * By default, returns: 
     *   <pre><code>getSubSystem().forceUserIdToUpperCase()</code></pre>
     */
    protected boolean forcePasswordToUpperCase()
    {
    	return getPrimarySubSystem().forceUserIdToUpperCase();
    }
    
	/**
     * <i>Useful utility method. Fully implemented, no need to override.</i><br>
     * Get the userId input validator to use in the password dialog prompt.
     * <p>
     * By default, returns </p>
     *   <pre><code>getSubSystem().getParentSubSystemConfiguration().getUserIdValidator()</code></pre>
     */
    public ISystemValidator getUserIdValidator()
    {
  		ISubSystemConfiguration ssFactory = getPrimarySubSystem().getSubSystemConfiguration();
		ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssFactory.getAdapter(ISubSystemConfigurationAdapter.class);

    	return adapter.getUserIdValidator(ssFactory);
    }

    /**
     * <i>Useful utility method. Fully implemented, no need to override.</i><br>
     * Get the password input validator to use in the password dialog prompt.
     * <p>
     * By default, returns:</p>
     *   <pre><code>getSubSystem().getParentSubSystemConfiguration().getPasswordValidator()</code></pre>
     */
    public ISystemValidator getPasswordValidator()
    {
    	ISubSystemConfiguration ssFactory = getPrimarySubSystem().getSubSystemConfiguration();
		ISubSystemConfigurationAdapter adapter = (ISubSystemConfigurationAdapter)ssFactory.getAdapter(ISubSystemConfigurationAdapter.class);

    	return adapter.getPasswordValidator(ssFactory);
    }
    /**
     * <i>Optionally overridable, not implemented by default.</i><br>
     * Get the signon validator to use in the password dialog prompt.
     * By default, returns null.
     */
    public ISignonValidator getSignonValidator()
    {
    	return null;
    }

    
    // ---------------------------------------------------
    // methods that must be overridden by child classes...
    // ---------------------------------------------------
 
    
    /**
     * This connection method wrappers the others (internal connect) so that registered subsystems 
     * can be notified and initialized after a connect
     * Previous implementations that overrode this method should now change
     * their connect() method to internalConnect()
     */
    public final void connect(IProgressMonitor monitor) throws Exception
    {
    	internalConnect(monitor);
    	intializeSubSystems(monitor);
    }
    
    //    protected void internalConnect(IProgressMonitor monitor) throws Exception
//    { 
//    	if (supportsServerLaunchProperties())
//    	{
//    		IServerLauncher starter = getRemoteServerLauncher();
//    		starter.setSignonInformation(getPasswordInformation());
//    		starter.setServerLauncherProperties(getRemoteServerLauncherProperties());
//			launchResult = null;
//    		if (!starter.isLaunched())
//    		{ 
//				try {
//					launchResult = starter.launch(monitor);		
//				} catch (Exception exc) {
//					throw new java.lang.reflect.InvocationTargetException(exc);				
//				}				
//    		}
//			connectResult = null;
//			try {
//				connectResult = starter.connect(monitor, getConnectPort());		
//			} catch (Exception exc) {
//				throw new java.lang.reflect.InvocationTargetException(exc);				
//			}				    		
//    	}
//    }
    /**
     * Disconnects from the remote system.
     * <p>
     * You must override
     * if <code>subsystem.getParentSubSystemConfiguration().supportsServerLaunchProperties</code> 
     * returns false.
     * <p>
     * If the subsystem supports server launch
     * the default behavior is to use the same remote server
     * launcher created in <code>connect()</code> and call <code>disconnect()</code>.
	 * <p>
	 * This is called, by default, from the <code>disconnect()</code>
	 * method of the subsystem.
	 * @see IServerLauncher#disconnect()
     */
    public final void disconnect(IProgressMonitor monitor) throws Exception
    {
    	internalDisconnect(monitor);
		unintializeSubSystems(monitor);
		clearPasswordCache();
    }
    
    /**
	 * Returns the suppressSignonPrompt flag.  If this is set to true then the user
	 * will not be prompted to signon, instead an InterruptedException will be thrown 
	 * by the promptForPassword method.
	 * 
	 * @return boolean
	 */
	public boolean isSuppressSignonPrompt()
	{
		return _suppressSignonPrompt;
	}

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
	public void setSuppressSignonPrompt(boolean suppressSignonPrompt)
	{
		_suppressSignonPrompt = suppressSignonPrompt;
	}

	private void logException(Throwable t) {
		Logger log = LoggerFactory.getLogger(RSEUIPlugin.getDefault());
		log.logError("Unexpected exception", t); //$NON-NLS-1$
	}
	
	public NewPasswordInfo promptForNewPassword(SystemMessage prompt) throws InterruptedException
	{
		ShowPromptForNewPassword msgAction = new ShowPromptForNewPassword(prompt);
		Display.getDefault().syncExec(msgAction);
		if (msgAction.isCancelled()) throw new InterruptedException();
		return new NewPasswordInfo(msgAction.getNewPassword(), msgAction.isSavePassword());
	} 
	
	private class ShowPromptForNewPassword implements Runnable
	{
		private SystemMessage _msg;
		private String newPassword;
		private boolean savePassword;
		private boolean cancelled = false;
		
		public ShowPromptForNewPassword(SystemMessage msg)
		{
			_msg = msg;
		}
		
		public void run()
		{
			SystemChangePasswordDialog dlg = new SystemChangePasswordDialog(SystemBasePlugin.getActiveWorkbenchShell(), getHostName(), getUserId(), _msg);
    	  	// Check if password was saved, if so preselect the save checkbox
			if (getLocalUserId() != null)
			{
				dlg.setSavePassword(PasswordPersistenceManager.getInstance().passwordExists(getHostType(), getHostName(), getLocalUserId()));
			}
			dlg.open();
			if (dlg.wasCancelled())
			{
				cancelled = true;
				return;
			}
			newPassword = dlg.getNewPassword();
			savePassword = dlg.getIsSavePassword();
			return;
		}
		
		public boolean isCancelled()
		{
			return cancelled;
		}
		
		public String getNewPassword()
		{
			return newPassword;
		}
		
		public boolean isSavePassword()
		{
			return savePassword;
		}
	}
	
	public class NewPasswordInfo
	{
		public String newPassword;
		public boolean savePassword;
		
		public NewPasswordInfo(String newPW, boolean savePW)
		{
			newPassword = newPW;
			savePassword = savePW;	
		}
	}
}