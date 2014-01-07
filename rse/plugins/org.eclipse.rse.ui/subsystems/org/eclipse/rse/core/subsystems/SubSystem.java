/********************************************************************************
 * Copyright (c) 2002, 2014 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - 141803: Fix cpu usage 100% while connecting
 * David Dykstal (IBM) - 168870: moved SystemPreferencesManager to a new package
 * David Dykstal (IBM) - 168870: created and used RSEPreferencesManager
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [183165] Do not implement constant interfaces
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [187218] Fix error reporting for connect()
 * Xuan Chen        (IBM)        - [187342] Open in New Window expand failed error when not connected
 * David McKnight   (IBM)        - [186363] remove deprecated calls in checkIsConnected
 * David McKnight   (IBM)        - [186363] get rid of obsolete calls to SubSystem.connect()
 * David McKnight   (IBM)        - [211472] [api][breaking] IRemoteObjectResolver.getObjectWithAbsoluteName() needs a progress monitor
 * David McKnight   (IBM)        - [212403] [apidoc][breaking] Fixing docs of SubSystem#getConnectorService() and making internalConnect() private
 * David Dykstal (IBM) - [197036] pulled up subsystem configuration switching logic from the service subsystem layer
 *                                implemented IServiceSubSystem here so that subsystem configuration switching can be
 *                                made common among all service subsystems.
 * David Dykstal (IBM) - [217556] remove service subsystem types
 * David McKnight   (IBM)        - [220309] [nls] Some GenericMessages and SubSystemResources should move from UI to Core
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * David Dykstal (IBM) - [225089][ssh][shells][api] Canceling connection leads to exception
 * Martin Oberhuber (Wind River) - [218304] Improve deferred adapter loading
 * Martin Oberhuber (Wind River) - [190231] Prepare API for UI/Non-UI Splitting
 * David McKnight (IBM) 		 - [225747] [dstore] Trying to connect to an "Offline" system throws an NPE
 * David McKnight (IBM)          - [233435] SubSystem.resolveFilterStrings(*) does not prompt for a connection when the subsystem is not connected
 * David Dykstal (IBM) - [233876] filters lost after restart
 * David McKnight (IBM)          - [238609] Substitution value missing for disconnect failed message
 * David McKnight   (IBM)        - [237970]  Subsystem.connect( ) fails for substituting host name when isOffline( ) is true
 * David McKnight   (IBM)        - [244270] Explicit check for isOffline and just returning block implementing a cache for Work Offline
 * Don Yantzi       (IBM)        - [244807] Delay connecting if resolving filters while restoring from cache
 * David McKnight   (IBM)        - [226787] [services] Dstore processes subsystem is empty after switching from shell processes
 * David McKnight   (IBM)        - [262930] Remote System Details view not restoring filter memento input
 * David McKnight   (IBM)        - [272882] [api] Handle exceptions in IService.initService()
 * David McKnight   (IBM)        - [284018] concurrent SubSystem.connect() calls can result in double login-prompt
 * David McKnight   (IBM)        - [318836] Period in filter name causes wrong message on drag and drop
 * David McKnight   (IBM)        - [326555] Dead lock when debug session starts
 * David McKnight   (IBM)         -[425014] profile commit job don't always complete during shutdown
 *  ********************************************************************************/

package org.eclipse.rse.core.subsystems;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.IRSEInteractionProvider;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.RSEPreferencesManager;
import org.eclipse.rse.core.events.ISystemModelChangeEvent;
import org.eclipse.rse.core.events.ISystemModelChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IRSECallback;
import org.eclipse.rse.core.model.IRSEPersistableContainer;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.RSEModelObject;
import org.eclipse.rse.internal.core.RSECoreMessages;
import org.eclipse.rse.internal.core.SystemResourceConstants;
import org.eclipse.rse.internal.core.filters.HostOwnedFilterPoolPattern;
import org.eclipse.rse.internal.core.model.ISystemProfileOperation;
import org.eclipse.rse.internal.core.model.SystemModelChangeEvent;
import org.eclipse.rse.internal.core.model.SystemProfileManager;
import org.eclipse.rse.services.clientserver.messages.CommonMessages;
import org.eclipse.rse.services.clientserver.messages.ICommonMessageIds;
import org.eclipse.rse.services.clientserver.messages.SimpleSystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * This class is designed to be subclassed. Its role is three-fold:
 * <ol>
 *   <li>Storing of tool-specific persistent properties per connection.
 *   <li>Accessing of an IConnectorService object to enable the subsystem's connect and disconnect actions.
 *   <li>Doing actual remote accessing. This usually just involves overriding the <code>internalResolveFilterString</code> methods to
 *         populate the remote resources shown when the subsystem's filters are expanded.
 * </ol>
 * <p>
 * This is the base class that subsystem suppliers subclass.
 * Each instance of this class represents a subsystem instance for a particular connection.
 * <p>
 * When a {@link IHost} is created, this subsystem's factory will be asked to create an
 * instance of its subsystem. If desired, your GUI may also allow users to create additional
 * instances.
 * <p>
 * There are only a handful of methods to implement in child classes (and indeed most of these
 *  are supplied as empty, so you only override those you want to support).
 * These are required:
 * <ul>
 * <li>{@link #getConnectorService()}.
 * </ul>
 * These are optional:
 * <ul>
 * <li>{@link #getObjectWithAbsoluteName(String)}
 * <li>{@link #internalResolveFilterString(String filterString, IProgressMonitor monitor)}
 * <li>{@link #internalResolveFilterString(Object parent, String filterString, IProgressMonitor monitor)}
 * <li>{@link #internalGetProperty(Object subject, String key, IProgressMonitor monitor)}
 * <li>{@link #internalSetProperty(Object subject, String key, String value, IProgressMonitor monitor)}
 * <li>{@link #internalGetProperties(Object subject, String[] keys, IProgressMonitor monitor)}
 * <li>{@link #internalSetProperties(Object subject, String[] keys, String[] values, IProgressMonitor monitor)}
 * </ul>
 *
 */

public abstract class SubSystem extends RSEModelObject
implements IAdaptable, ISubSystem, ISystemFilterPoolReferenceManagerProvider
{
	protected static final String SUBSYSTEM_FILE_NAME = "subsystem"; //$NON-NLS-1$

	//protected transient SubSystemConfiguration parentFactory = null;
	protected static final int OPERATION_RESOLVE_ABSOLUTE = 0;
	protected static final int OPERATION_RESOLVE_ABSOLUTES= 1;
	protected static final int OPERATION_RESOLVE_RELATIVE = 2;
	protected static final int OPERATION_RUN_COMMAND      = 3;
	protected static final int OPERATION_GET_PROPERTY     = 4;
	protected static final int OPERATION_SET_PROPERTY     = 5;
	protected static final int OPERATION_GET_PROPERTIES   = 6;
	protected static final int OPERATION_SET_PROPERTIES   = 7;
	protected static final int OPERATION_CONNECT          = 8;
	protected static final int OPERATION_DISCONNECT       = 9;
	protected static final int OPERATION_RUN_SHELL = 10;
	protected static final int OPERATION_SEND_COMMAND_TO_SHELL = 11;
	protected static final int OPERATION_CANCEL_SHELL = 12;
	protected static final int OPERATION_REMOVE_SHELL = 13;

	protected ISubSystemConfiguration parentSubSystemConfiguration;
	protected String           previousUserIdKey;
	private IRSEInteractionProvider _interactionProvider = null;
	protected Shell shell = null;

	protected boolean supportsConnecting = true;
	protected boolean sortResults = true;
	protected boolean runInThread = true;

	protected boolean cancelable = true;
	protected boolean doConnection = false;
	protected ProgressMonitorDialog pmDialog;
	protected String saveFileName;
	protected IConnectorService _connectorService = null;

	protected boolean _connectionError = false;
	protected boolean _disconnecting = false;

	protected IHost   _host;

	protected String _name = null;
	protected String _subsystemConfigurationId = null;
	protected boolean _hidden = false;
	private boolean _isInitialized = false;


	/**
	 * @generated This field/method will be replaced during code generation.
	 */
	protected ISystemFilterPoolReferenceManager filterPoolReferenceManager = null;

	private Map poolReferencesMap = new HashMap();

	private class NullRunnableContext implements IRunnableContext {
		public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
			IProgressMonitor monitor = new NullProgressMonitor();
			runnable.run(monitor);
		}
	}


	public class SystemMessageDialogRunnable implements Runnable
	{
		private SystemMessageDialog _dlg;
		public SystemMessageDialogRunnable(SystemMessageDialog dlg)
		{
			_dlg = dlg;
		}

		public void run()
		{
			_dlg.open();
		}
	}

	protected SubSystem(IHost host, IConnectorService connectorService)
	{
		super();
		_host = host;
		_connectorService = connectorService;
		_connectorService.registerSubSystem(this);
	}

	/**
	 * Set an Interaction Provider specific for this subsystem.
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is no guarantee that this API will work
	 * or that it will remain the same. Please do not use this API without
	 * consulting with the <a href="http://www.eclipse.org/tm/">Target
	 * Management</a> team.
	 * </p>
	 *
	 * @param p the new interaction provider to use, or <code>null</code> to
	 *            fall back to the default interaction provider (from
	 *            RSECorePlugin).
	 * @since 3.0
	 */
	public void setInteractionProvider(IRSEInteractionProvider p) {
		synchronized (this) {
			_interactionProvider = p;
		}
	}

	/**
	 * Get the current Interaction Provider. Returns a specific one for this
	 * subsystem if it has been set, or falls back to the default one from
	 * RSECorePlugin otherwise.
	 * <p>
	 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
	 * part of a work in progress. There is no guarantee that this API will work
	 * or that it will remain the same. Please do not use this API without
	 * consulting with the <a href="http://www.eclipse.org/tm/">Target
	 * Management</a> team.
	 * </p>
	 *
	 * @return the interaction provider to use.
	 * @since 3.0
	 */
	public IRSEInteractionProvider getInteractionProvider() {
		synchronized (this) {
			if (_interactionProvider != null) {
				return _interactionProvider;
			}
		}
		return RSECorePlugin.getDefault().getDefaultInteractionProvider();
	}

	/**
	 * Internal method to select the appropriate command subsystem when there are multiple defined for this connection.
	 * The default implementation is to return the first, but child classes can refine this. Input is always an array of
	 * length greater than one.
	 */
	protected ISubSystem selectCommandSubSystem(ISubSystem[] allCmdSubSystems)
	{
		if (allCmdSubSystems != null && allCmdSubSystems.length > 0)
			return allCmdSubSystems[0];
		return null;
	}

	/**
	 * Return the parent subsystem factory that owns this subsystem.
	 */
	public ISubSystemConfiguration getSubSystemConfiguration()
	{
		return parentSubSystemConfiguration;
	}
	/**
	 * Return the parent subsystem factory that owns this subsystem.
	 */
	public void setSubSystemConfiguration(ISubSystemConfiguration ssf)
	{
		parentSubSystemConfiguration = ssf;
		supportsConnecting = ssf.supportsSubSystemConnect();
		//System.out.println("subsystem supports connecting? " + supportsConnecting);
	}
	/**
	 * Set the parent connection that owns this subsystem.
	 */
	public void setHost(IHost conn)
	{
		_host = conn;
		previousUserIdKey = getPreferencesKey();
	}

	/**
	 * Return true if userId and password should be forced to uppercase.
	 * INTERACTIVEcut to calling same method in parent connection.
	 */
	public boolean forceUserIdToUpperCase()
	{
		/* defect 43219
    	if (parentSubSystemConfiguration != null)
    	  return parentSubSystemConfiguration.forceUserIdToUpperCase();
    	else
    	  return true;
		 */
		IHost currConn = getHost();
		if (currConn != null)
			return currConn.getForceUserIdToUpperCase();
		else
			return false;
	}

	/**
	 * The profile is being renamed, so we are called to do our thing with
	 * any information stored that is keyed by the profile name.
	 * <p>
	 * This is called AFTER the profile is renamed!
	 */
	public void renamingProfile(String oldName, String newName)
	{
		String userIdValue = null;
		if (previousUserIdKey!=null)
			userIdValue = getLocalUserId(previousUserIdKey);
		// if the userId attribute held a preference store key of the form profileName.connectionName.subsystemName,
		// we have to delete that key entry from the preference store and re-gen a new keyed entry with the same
		// value (the actual user id) the old keyed entry held.
		String newKey = getPreferencesKey(newName, getHostAliasName());
		if ((userIdValue != null) && (userIdValue.length()>0))
		{
			RSEPreferencesManager.clearUserId(previousUserIdKey);
			RSEPreferencesManager.setUserId(newKey, userIdValue); // store old value with new preference key
		}
		previousUserIdKey = newKey;

		// now we need to potentially rename any filter pool associated with this connection...
		ISystemProfile profile = getSystemProfile();
		ISystemFilterPoolManager mgr = getSubSystemConfiguration().getFilterPoolManager(profile);
		ISystemFilterPool[] pools = mgr.getSystemFilterPools();
		boolean done = false;
		if (pools != null)
		{
			for (int idx = 0; !done && (idx < pools.length); idx++)
			{
				if ( (pools[idx].getOwningParentName()!=null) && // a connection-related pool
						(pools[idx].getOwningParentName().equals(getHostAliasName())) ) // this connection!
				{
					done = true;
					try
					{
						// re-gen name: %1 Filter Pool for connection %2, where %1 is profile name...
						mgr.renameSystemFilterPool(pools[idx], getConnectionOwnedFilterPoolName(newName, getHostAliasName()));
					}
					catch (Exception exc)
					{
						String msg = "Unexpected error renaming connection-specific filter pool " + getConnectionOwnedFilterPoolName(newName, getHostAliasName()); //$NON-NLS-1$
						SystemBasePlugin.logError(msg, exc);
						System.err.println(msg + ": " + exc); //$NON-NLS-1$
					}
				}
			}
		}
	}
	/**
	 * The connection is being renamed, so we are called to do our thing with
	 * any information stored that is keyed by the connection name.
	 */
	public void renamingConnection(String newName)
	{
		String userIdValue = null;
		if (previousUserIdKey != null)
			userIdValue = getLocalUserId(previousUserIdKey);  // see if we previous stored a value for this connection
		// if the userId attribute held a preference store key of the form profileName.connectionName.subsystemName,
		// we have to delete that key entry from the preference store and re-gen a new keyed entry with the same
		// value (the actual user id) the old keyed entry held.
		String newKey = getPreferencesKey(getSystemProfileName(), newName);
		if ((userIdValue != null) && (userIdValue.length()>0))
		{
			RSEPreferencesManager.clearUserId(previousUserIdKey);
			RSEPreferencesManager.setUserId(newKey, userIdValue); // store old value with new preference key
		}
		previousUserIdKey = newKey;

		// rename the connection-private filter pool, if it exists:
		ISystemFilterPool privatePool = getConnectionPrivateFilterPool(false); // false => don't create if not found
		if (privatePool != null)
		{
			ISystemFilterPoolManager mgr = getSubSystemConfiguration().getFilterPoolManager(getSystemProfile());
			privatePool.setOwningParentName(newName);
			try {
				mgr.renameSystemFilterPool(privatePool, getConnectionOwnedFilterPoolName(getSystemProfileName(), newName));
			}
			catch (Exception exc)
			{
				SystemBasePlugin.logError("Error renaming conection-private pool to: "+newName, exc); //$NON-NLS-1$
			}
		}
	}
	/**
	 * Private method called when the parent connection is being deleted, so
	 * the subsystem can do any pre-death cleanup it needs to.
	 * <p>
	 * What we need to do is delete our entry in the preference store for our userId.
	 */
	public void deletingConnection()
	{
		String oldUserId = null;
		if (previousUserIdKey != null)
			oldUserId = getLocalUserId(previousUserIdKey);
		// if the userId attribute held a preference store key of the form profileName.connectionName.subsystemName,
		// we have to delete that key entry from the preference store and re-gen a new keyed entry with the same
		// value (the actual user id) the old keyed entry held.
		if (oldUserId != null)
		{
			RSEPreferencesManager.clearUserId(previousUserIdKey);
		}
		// delete the connection-private filter pool, if it exists:
		ISystemFilterPool privatePool = getConnectionPrivateFilterPool(false); // false => don't create if not found
		if (privatePool != null)
		{
			ISystemFilterPoolManager mgr = getSubSystemConfiguration().getFilterPoolManager(getSystemProfile());
			try {
				mgr.deleteSystemFilterPool(privatePool);
			}
			catch (Exception exc)
			{
				SystemBasePlugin.logError("Error deleting conection-private pool for: "+getHostAliasName(), exc); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Returns the value of this subsystem's local user id if it is not null. If it
	 * is null, it returns the parent connection object's default user Id. It in turn
	 * queries the preferences if its local value is null.
	 * <p>
	 * In fact, we now don't store the user Id in the subsystem object itself, but rather store it in the
	 * user preferences, so that such things are not shared among the team on a synchronize operation.
	 * This is transparent to callers of this method however, as this method resolves from the preferences.
	 *
	 * @see org.eclipse.rse.core.model.IHost#getDefaultUserId()
	 * @see #getLocalUserId()
	 * @see #clearLocalUserId()
	 * @return The value of the UserId attribute
	 */
	public String getUserId()
	{
		String uid = getLocalUserId();
		if ((uid == null) || (uid.length()==0))
		{
			IHost conn = getHost();
			uid = conn.getDefaultUserId();
		}
		return uid;
	}

	/**
	 * Helper method to compute a unique name for a given subsystem instance
	 */
	protected String getPreferencesKey()
	{
		if ((_host==null) || (getName()==null))
			return null;
		return getPreferencesKey(getSystemProfileName(), getHostAliasName());
	}
	/**
	 * Helper method to compute a unique name for a given subsystem instance, given a profile and connection name
	 */
	protected String getPreferencesKey(String profileName, String connectionName)
	{
		String key = profileName + "." + connectionName + "." + getName(); //$NON-NLS-1$ //$NON-NLS-2$
		//System.out.println("in SubSystemImpl.getPreferencesKey(): Subsystem key name: " + key);
		return key;
	}

	/**
	 * Internal-use method for getting the local user ID, without resolution.
	 */
	protected String getLocalUserId(String key)
	{
		String uid = null;
		if ((key!=null) && (key.length()>0))
		{
			uid = RSEPreferencesManager.getUserId(key); // resolve from preferences
		}
		return uid;
	}

	/**
	 * Alternative to getUserId when we don't want to resolve it from parent connection.
	 * This is used when showing the properties.
	 * <p>
	 * Unlike getUserId() this one does not defer to the connection's default user Id if
	 * the subsystem's userId attribute is null.
	 * <p>
	 * To set the local user Id, simply call setUserId(String id). To clear it, call
	 * {@link #clearLocalUserId()}.
	 * <p>
	 * @see org.eclipse.rse.core.model.IHost#getDefaultUserId()
	 * @see #clearLocalUserId()
	 * @see #getUserId()
	 */
	public String getLocalUserId()
	{
		return getLocalUserId(getPreferencesKey());
	}

	/**
	 * Called to clear the local user Id such that subsequent requests to getUserId() will
	 * return the parent connection's default user Id. Sets the user Id attribute for this
	 * subsystem to null.
	 * <p>
	 * @see org.eclipse.rse.core.model.IHost#getDefaultUserId()
	 * @see #getUserId()
	 * @see #getLocalUserId()
	 */
	public void clearLocalUserId()
	{
		if (previousUserIdKey != null)
			RSEPreferencesManager.clearUserId(previousUserIdKey);
		IConnectorService system = getConnectorService();
		if (system != null)
			system.clearCredentials();
	}

	/**
	 * @{inheritDoc
	 * @since 3.0
	 */
	public void checkIsConnected(IProgressMonitor monitor) throws SystemMessageException
	{
		// for 244270, don't connect when offline
		// or when restoring from memento
		if (!isConnected() && !isOffline()  && !((getCacheManager() != null) && getCacheManager().isRestoreFromMemento())) 
		{
			try
			{
				if (monitor != null)
				{
					connect(monitor, false);
				}
				else
				{
					Display display = Display.getCurrent();
					if (display != null)
					{
						connect(false, null);
					}
					else
					{
						// Not on UI-thread
						connect(new NullProgressMonitor(), false);
					}
				}
			}
			catch (Exception e)
			{
				if (e instanceof SystemMessageException)
				{
					throw (SystemMessageException) e;
				}
				else
					if (e instanceof OperationCanceledException)
					{
						String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_CANCELLED, getHost().getAliasName());
						SystemMessage msg = new SimpleSystemMessage(RSECorePlugin.PLUGIN_ID,
								ICommonMessageIds.MSG_CONNECT_CANCELLED,
								IStatus.CANCEL, msgTxt);
						throw new SystemMessageException(msg);
					}
					else
					{
						String msgTxt = NLS.bind(CommonMessages.MSG_DISCONNECT_FAILED, getHostName());
						SystemMessage msg = new SimpleSystemMessage(RSECorePlugin.PLUGIN_ID,
								ICommonMessageIds.MSG_DISCONNECT_FAILED,
								IStatus.ERROR, msgTxt);
						throw new SystemMessageException(msg);
					}
			}
		}
	}

	/**
	 * This is the method required by the IAdaptable interface.
	 * Given an adapter class type, return an object castable to the type, or
	 *  null if this is not possible.
	 * <p>
	 * By default this returns Platform.getAdapterManager().getAdapter(this, adapterType);
	 * This in turn results in the default subsystem adapter SystemViewSubSystemAdapter,
	 * in package org.eclipse.rse.ui.view.
	 */
	public Object getAdapter(Class adapterType)
	{
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	/**
	 * Return the system profile object this subsystem is associated with.
	 */
	public ISystemProfile getSystemProfile()
	{
		if (_host != null)
			return _host.getSystemProfile();
		else
			return null;
	}

	/**
	 * Return the name of system profile object this subsystem is associated with.
	 */
	public String getSystemProfileName()
	{
		ISystemProfile profile = getSystemProfile();
		if (profile != null)
			return profile.getName();
		else
			return null;
	}

	/**
	 * Return the connection object this subsystem is associated with.
	 */
	public IHost getHost()
	{
		return _host;
	}

	/**
	 * Return the name of system connection object this subsystem is associated with.
	 */
	public String getHostAliasName()
	{
		IHost connection = getHost();
		if (connection != null)
			return connection.getAliasName();
		else
			return null;
	}

	/**
	 * Return the owning filter pool that is unique to this provider.
	 * From SystemFilterPoolReferenceManagerProvider interface. We map to
	 * a call to {@link #getConnectionPrivateFilterPool(boolean)}.
	 */
	public ISystemFilterPool getUniqueOwningSystemFilterPool(boolean createIfNotFound)
	{
		return getConnectionPrivateFilterPool(createIfNotFound);
	}

	/**
	 * Find or create a new filter pool, unique to this subsystem's connection. This will be
	 * in the same profile as the connection and it will follow a naming convention that ties
	 * it to the connection.
	 * @param createIfNotFound - true to create the pool if it doesn't exist
	 * @return the filter pool that was found or created
	 */
	public ISystemFilterPool getConnectionPrivateFilterPool(boolean createIfNotFound) {
		ISystemFilterPool pool = null;
		ISubSystemConfiguration config = getSubSystemConfiguration();
		ISystemProfile profile = getSystemProfile();
		ISystemFilterPoolManager fpm = config.getFilterPoolManager(profile);
		ISystemFilterPool[] allPoolsInProfile = fpm.getSystemFilterPools();
		IHost host = getHost();
		String hostName = host.getAliasName();
		if (allPoolsInProfile != null) {
			for (int idx = 0; idx < allPoolsInProfile.length; idx++) {
				ISystemFilterPool currentPool = allPoolsInProfile[idx];
				String poolOwnerName = currentPool.getOwningParentName();
				if (poolOwnerName == null) {
					HostOwnedFilterPoolPattern pattern = new HostOwnedFilterPoolPattern(config.getId());
					if (pattern.matches(currentPool.getName())) {
						currentPool.setOwningParentName(hostName); // TODO these pools should have been created with the owner set properly
						poolOwnerName = hostName;
					}
				}
				if (hostName.equals(poolOwnerName)) {
					pool = currentPool;
				}
			}
		}
		if ((pool == null) && createIfNotFound) {
			try {
				String profileName = profile.getName();
				pool = fpm.createSystemFilterPool(getConnectionOwnedFilterPoolName(profileName, hostName), true); // true=>is deletable by user
				if (pool != null) {
					pool.setNonRenamable(true);
					pool.setOwningParentName(hostName);
					pool.commit();
					ISystemFilterPoolReferenceManager fprm = getSystemFilterPoolReferenceManager();
					if (fprm.getReferenceToSystemFilterPool(pool) == null) {
						fprm.addReferenceToSystemFilterPool(pool);
					}
				}
			} catch (Exception exc) {
				SystemBasePlugin.logError("Error creating connection-private filter pool for connection: " + hostName, exc); //$NON-NLS-1$
			}
		}
		return pool;
	}

	/**
	 * Constructs the name of a connection specific filter pool from its parts.
	 * @param profileName the name of the profile that contains this filter pool.
	 * @param connectionName the name of the connection the "owns" this filter pool.
	 * @return the name for the connection-owned filter pool.
	 */
	public String getConnectionOwnedFilterPoolName(String profileName, String connectionName) {
		/*
		 * Need to keep this name short and not translatable
		 * since it names a team sharable resource. Not qualified by the profile
		 * name since that is implicit by being in a profile.
		 */
		HostOwnedFilterPoolPattern pattern = new HostOwnedFilterPoolPattern(getConfigurationId());
		String name = pattern.make(connectionName);
		return name;
	}
	
	// -------------------------
	// Filter Testing Methods...
	// -------------------------
	/**
	 * Override this method if you support typed filters. Given an absolute remote object name,
	 *  you can test if this filter's type matches that of the remote object. This is called as
	 *  a pre-test in the following methods.
	 */
	protected boolean doesFilterTypeMatch(ISystemFilter filter, String remoteObjectAbsoluteName)
	{
		return true;
	}

	/**
	 * Return true if the given filter lists the contents of the given remote object.
	 *  For example, if given a folder, return true if any of the filter strings in this filter
	 *  lists the contents of that folder. Used in impact analysis when a remote object is
	 *  created, deleted, renamed, copied or moved, so as to establish which filters need to be
	 *  refreshed or collapsed (if the folder is deleted, say).
	 * <p>
	 * Subclasses do not need to override this method. Rather, this method walks each
	 *  filter string and calls doesFilterStringListContentsOf(...), and that is the method
	 *  that child classes must override.
	 */
	public boolean doesFilterListContentsOf(ISystemFilter filter, String remoteObjectAbsoluteName)
	{
		if (filter.isPromptable()) // || !doesFilterTypeMatch(filter, remoteObjectAbsoluteName))
			return false;
		boolean would = false;
		ISystemFilterString[] strings = filter.getSystemFilterStrings();
		if (strings != null)
			for (int idx=0; !would && (idx<strings.length); idx++)
				would = doesFilterStringListContentsOf(strings[idx], remoteObjectAbsoluteName);
		return would;
	}
	/**
	 * Return true if the given filter string lists the contents of the given remote object.
	 *  For example, if given a folder, return true if the given filter string
	 *  lists the contents of that folder. Used in impact analysis when a remote object is
	 *  created, deleted, renamed, copied or moved, so as to establish which filters need to be
	 *  refreshed or collapsed (if the folder is deleted, say).
	 * <p>
	 * This should only return true if the filter string directly lists the contents of the given
	 *  object, versus indirectly.
	 * <p>
	 * Subclasses should override this.
	 */
	public boolean doesFilterStringListContentsOf(ISystemFilterString filterString, String remoteObjectAbsoluteName)
	{
		return false;
	}

	/**
	 * Return true if the given remote object name will pass the filtering criteria for any of
	 *  the filter strings in this filter.
	 * <p>
	 * Subclasses do not need to override this method. Rather, this method walks each
	 *  filter string and calls doesFilterStringMatch(...), and that is the method
	 *  that child classes must override.
	 */
	public boolean doesFilterMatch(ISystemFilter filter, String remoteObjectAbsoluteName)
	{
		if (filter.isPromptable() || !doesFilterTypeMatch(filter, remoteObjectAbsoluteName))
			return false;
		boolean would = false;

		String[] strings = filter.getFilterStrings();
		if (strings != null)
		{
			for (int idx=0; !would && (idx<strings.length); idx++)
			{
				if (strings[idx].equals("*")) //$NON-NLS-1$
					would = true;
				else if (strings[idx].equals("./*")) //$NON-NLS-1$
					would = true;
				else
					would = doesFilterStringMatch(strings[idx], remoteObjectAbsoluteName, filter.areStringsCaseSensitive());
			}
		}
		return would;
	}
	/**
	 * Return true if the given remote object name will pass the filtering criteria for
	 *  the given filter string.
	 * <p>
	 * Subclasses need to override this.
	 * If in doubt, return true.
	 */
	public boolean doesFilterStringMatch(String filterString, String remoteObjectAbsoluteName, boolean caseSensitive)
	{
		return true;
	}



	// -------------------------------------
	// GUI methods
	// -------------------------------------
	/**
	 * Return the single property page to show in the tabbed notebook for the
	 *  for SubSystem property of the parent Connection. Return null if no
	 *  page is to be contributed for this. You are limited to a single page,
	 *  so you may have to compress. It is recommended you prompt for the port
	 *  if applicable since the common base subsystem property page is not shown
	 *  To help with this you can use the SystemPortPrompt widget.
	 */
	public PropertyPage getPropertyPage(Composite parent)
	{
		return null;
	}

	// --------------------------------------------------------------------------------------
	// Methods for encoding and decoding remote objects for drag and drop, and clipboard copy
	// --------------------------------------------------------------------------------------


	// --------------
	// Methods for encoding and decoding remote objects for drag and drop, and
	// clipboard copy
	// ------------------------------------------------------------------------
	// --------------

	/**
	 * Return the remote object that corresponds to the specified unique ID.
	 * <p>
	 * Since the abstract subsystem implementation does not know anything about
	 * the specific kinds of resources managed by concrete implementations, this
	 * method can only resolve filter references.
	 * </p>
	 * <p>
	 * <strong>subsystem implementations must override this method in order to
	 * resolve IDs for the remote objects they manage, to support drag and drop,
	 * clipboard copy and other remote object resolving schemes.</strong>
	 * Extenders that want to support filters should call
	 * <code>super.getObjectWithAbsoluteName(key)</code> when they do not find a
	 * reference for the key themselves.
	 * </p>
	 *
	 * @see org.eclipse.rse.core.subsystems.IRemoteObjectResolver#
	 * 	getObjectWithAbsoluteName(String, IProgressMonitor)
	 *
	 * @param key the unique id of the remote object. Must not be
	 * 		<code>null</code>.
	 * @param monitor the progress monitor
	 * @return the remote object instance, or <code>null</code> if no object is
	 * 	found with the given id.
	 * @throws Exception in case an error occurs contacting the remote system
	 * 		while retrieving the requested remote object. Extenders are
	 * 		encouraged to throw {@link SystemMessageException} in order to
	 * 		support good user feedback in case of errors. Since exceptions
	 * 		should only occur while retrieving new remote objects during
	 * 		startup, clients are typically allowed to ignore these exceptions
	 * 		and treat them as if the remote object were simply not there.
	 * @since 3.0
	 */
	public Object getObjectWithAbsoluteName(String key, IProgressMonitor monitor) throws Exception
	{
		// by default, the subsystem will attempt to find a filter reference for the key.
		// Return null when no such filter is found.
		return getFilterReferenceWithAbsoluteName(key);
	}

	/**
	 * @deprecated use getObjectWithAbsoluteName(String key, IProgressMonitor monitor)
	 */
	public Object getObjectWithAbsoluteName(String key) throws Exception
	{
		return getObjectWithAbsoluteName(key, new NullProgressMonitor());
	}

	/**
	 * Return the filter reference that corresponds to the specified key.  If there
	 * @param key the absolute name for an object.
	 * @return a filter reference if there is one matching the key,
	 *     or <code>null</code> if no such filter is found.
	 */
	protected Object getFilterReferenceWithAbsoluteName(String key)
	{
		//		figure out if there is a filter
 		String filterID = key;
		try
		{
			ISystemFilterPoolReferenceManager filterMgr = getFilterPoolReferenceManager();
			String modString = filterID.replace('.', ',');
			
			String[] segments = modString.split(",");

			if (segments.length > 0)
			{
				// this is the profile
				String mgrName = segments[0];

				// this is the filter pool manager for the profile
				ISystemFilterPoolManager mgr = parentSubSystemConfiguration.getSystemFilterPoolManager(mgrName);

				if (mgr != null && segments.length > 1){
					
					int segNo = 0; 	
					
					ISystemFilterPool filterPool = null;
					ISystemFilterPool[] filterPools = mgr.getSystemFilterPools();
					for (int p = 0; p < filterPools.length && filterPool == null; p++){
						segNo = 2; // initial segment number for filter pool is 2nd to last	
						
						ISystemFilterPool pool = filterPools[p];
						String realPoolName = pool.getName();
						
						// check for match 

						while (filterPool == null && segNo < segments.length){
							String filterPoolName = segments[segments.length - segNo];
							
							for (int s = segNo + 1; s < segments.length && filterPool == null; s++){
								if (filterPoolName.equals(realPoolName)){
									filterPool = pool;
								}
								else if (realPoolName.endsWith(filterPoolName)){
									filterPoolName = segments[segments.length - s] + '.' + filterPoolName;
								}
								else {
									// no match
									break;								
								}
							}					
							if (filterPool == null){
								segNo++; // move further up the string
							}
						}
					}


					if (filterPool != null)
					{
						// name of the filter is the last segment
						//String filterName = segments[segments.length - 1];
						StringBuffer filterBuf = new StringBuffer();
						for (int i = segNo - 1; i > 0; i--){ // dealing with filtername that potentially had a dot in it
							String filterPartName = segments[segments.length - i];
							filterBuf.append(filterPartName);		
							if (i > 1){
								filterBuf.append('.');
							}
						}
						String filterName = filterBuf.toString();
						
						ISystemFilter filter = filterPool.getSystemFilter(filterName);
						ISystemFilterReference ref = filterMgr.getSystemFilterReference(this, filter);
						if (ref != null)
						{
							// if we get here, it's a filter
							return ref;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
		}
		return null;
	}


	// ---------------------------------------------------
	// Methods for business partners to add their own
	//  persisted attributes to the subsystem object...
	// ---------------------------------------------------

	/**
	 * @deprecated use property sets directly now
	 */
	public void setVendorAttribute(String vendor, String attributeName, String attributeValue)
	{
		IPropertySet set = getPropertySet(vendor);
		if (set == null)
		{
			set = createPropertySet(vendor, ""); //$NON-NLS-1$
		}
		set.addProperty(attributeName, attributeValue);
	}
	/**
	 * @deprecated use property sets directly now
	 */
	public String getVendorAttribute(String vendor, String attributeName)
	{
		IPropertySet set = getPropertySet(vendor);
		if (set != null)
		{
			return set.getPropertyValue(attributeName);
		}
		else
		{
			return null;
		}
	}


	// ---------------------------------------------------
	// Methods for ISVs to add their own
	// persisted attributes to the subsystem object...
	// ---------------------------------------------------

	/**
	 * @deprecated
	 */
	public void setRemoteAttribute(String attributeName, String attributeValue)
	{
		IPropertySet set = getPropertySet("Remote"); //$NON-NLS-1$
		if (set == null)
		{
			set = createPropertySet("Remote", getDescription()); //$NON-NLS-1$
		}
		set.addProperty(attributeName, attributeValue);
	}
	/**
	 * @deprecated
	 */
	public String getRemoteAttribute(String attributeName)
	{
		IPropertySet set = getPropertySet("Remote"); //$NON-NLS-1$
		if (set != null)
		{
			return set.getPropertyValue(attributeName);
		}
		else
		{
			return null;
		}
	}





	// ------------------
	// Utility methods...
	// ------------------

	/**
	 * Return the host name for the connection this system's subsystem is associated with
	 */
	public String getHostName()
	{
		IHost conn = getHost();
		if (conn == null)
			return null;
		else
			return conn.getHostName();
	}

	/**
	 * Display message on message thread
	 */
	protected void displayAsyncMsg(SystemMessageException msg)
	{
		DisplayErrorMessageJob job = new DisplayErrorMessageJob(getShell(), msg);
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(true);
		job.schedule();
	}
	/**
	 * Nested class which extends WorkbenchJob to allow us to show an error message, which is a GUI operation,
	 *  from a non-GUI thread. This is done by creating an instance of this class and then scheduling it.
	 */
	public static class DisplayErrorMessageJob extends WorkbenchJob
	{
		private Shell shell;
		private SystemMessageException msgExc;

		/**
		 * Constructor
		 */
		public DisplayErrorMessageJob(Shell shell, SystemMessageException msgExc)
		{
			super(""); //$NON-NLS-1$
			this.shell = shell; //FIXME remove this
			this.msgExc = msgExc;
		}

		/**
		 * @see UIJob#run(IProgressMonitor monitor)
		 */
		public IStatus runInUIThread(IProgressMonitor monitor)
		{
			if ((shell != null) && (shell.isDisposed() || !shell.isEnabled() || !shell.isVisible()))
				shell = null;
			if (shell == null)
			{
				Shell[] shells = Display.getCurrent().getShells();
				for (int i = 0; i < shells.length && shell == null; i++)
					if (!shells[i].isDisposed() && shells[i].isVisible() && shells[i].isEnabled())
						shell = shells[i];
			}
			if (shell != null)
				SystemMessageDialog.displayMessage(shell, msgExc);
			return Status.OK_STATUS;
		}
	}

	// ---------------------------------------------------
	// SystemFilterPoolReferenceManagerProvider methods...
	// ---------------------------------------------------
	/**
	 * Return the system filter pool reference manager, as per the
	 * interface SystemFilterPoolReferenceManagerProvider
	 */
	public ISystemFilterPoolReferenceManager getSystemFilterPoolReferenceManager()
	{
		return getFilterPoolReferenceManager();
	}
	/*
	 * Helper method to fire an event...
	 */
	protected void fireEvent(SystemResourceChangeEvent event)
	{
		RSECorePlugin.getTheSystemRegistry().fireEvent(event);
	}
	/*
	 * Helper method to fire a reference event...
	 */
	protected void fireEvent(SystemResourceChangeEvent event, Object grandParent)
	{
		event.setGrandParent(grandParent);
		RSECorePlugin.getTheSystemRegistry().fireEvent(event);
	}
	/*
	 * Helper method to create and then fire an event...
	 */
	protected void fireEvent(Object src, int eventId, Object parent)
	{
		fireEvent(new SystemResourceChangeEvent(src, eventId, parent));
	}
	/*
	 * Helper method to create and then fire an event...
	 */
	protected void fireEvent(Object[] multiSrc, int eventId, Object parent)
	{
		fireEvent(new SystemResourceChangeEvent(multiSrc, eventId, parent));
	}
	/*
	 * Helper method to create and then fire an event...
	 */
	protected void fireEvent(Object[] src, int eventId, Object parent, int position)
	{
		SystemResourceChangeEvent event = new SystemResourceChangeEvent(src, eventId, parent);
		event.setPosition(position);
		fireEvent(event);
	}

	/*
	 * Helper method to create and then fire a reference event...
	 */
	protected void fireEvent(Object src, int eventId, Object parent, Object grandParent)
	{
		fireEvent(new SystemResourceChangeEvent(src, eventId, parent), grandParent);
	}
	// -------------------------------
	// FILTER POOL REFERENCE EVENTS...
	// -------------------------------

	/**
	 * A new filter pool reference has been created. Fire the appropriate events for this.
	 */
	public void filterEventFilterPoolReferenceCreated(ISystemFilterPoolReference newPoolRef) {
		if (getSubSystemConfiguration().showFilterPools()) {
			fireEvent(newPoolRef, ISystemResourceChangeEvents.EVENT_ADD, this);
			fireEvent(newPoolRef, ISystemResourceChangeEvents.EVENT_REVEAL_AND_SELECT, this);
		} else {
			ISystemFilterPool pool = newPoolRef.getReferencedFilterPool();
			if (pool != null && pool.getSystemFilterCount() > 0) {
				ISystemFilterReference[] filterRefs = newPoolRef.getSystemFilterReferences(this);
				fireEvent(filterRefs, ISystemResourceChangeEvents.EVENT_ADD_MANY, this, -1); // -1 means append to end
			}
		}
		try {
			getSubSystemConfiguration().saveSubSystem(this);
			RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_ADDED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOLREF, newPoolRef, null);
		} catch (Exception exc) {
			SystemBasePlugin.logError("Error saving subsystem " + getName(), exc); //$NON-NLS-1$
		}
	}

	/**
	 * A filter pool reference has been deleted
	 */
	public void filterEventFilterPoolReferenceDeleted(ISystemFilterPoolReference filterPoolRef)
	{
		if (getSubSystemConfiguration().showFilterPools())
		{
			fireEvent(filterPoolRef, ISystemResourceChangeEvents.EVENT_DELETE, this);
		}
		else if (filterPoolRef.getReferencedFilterPool().getSystemFilterCount()>0)
		{
			ISystemFilterReference[] filterRefs = filterPoolRef.getSystemFilterReferences(this);
			fireEvent(filterRefs, ISystemResourceChangeEvents.EVENT_DELETE_MANY, this);
		}

		try {
			getSubSystemConfiguration().saveSubSystem(this);
			// fire model change event in case any BP code is listening...
			RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REMOVED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOLREF, filterPoolRef, null);
		}
		catch (Exception exc)
		{
			SystemBasePlugin.logError("Error saving subsystem "+getName(),exc); //$NON-NLS-1$
		}
	}
	/**
	 * A single filter pool reference has been reset to reference a new pool
	 */
	public void filterEventFilterPoolReferenceReset(ISystemFilterPoolReference filterPoolRef)
	{
		fireEvent(filterPoolRef, ISystemResourceChangeEvents.EVENT_PROPERTYSHEET_UPDATE, this); // we assume its a move operation so little impact
		try {
			getSubSystemConfiguration().saveSubSystem(this);
			// fire model change event in case any BP code is listening...
			RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOLREF, filterPoolRef, null);
		}
		catch (Exception exc)
		{
			SystemBasePlugin.logError("Error saving subsystem "+getName(),exc); //$NON-NLS-1$
		}
	}
	/**
	 * All filter pool references has been reset. Happens after SelectFilterPools dialog
	 */
	public void filterEventFilterPoolReferencesReset()
	{
		fireEvent(this, ISystemResourceChangeEvents.EVENT_CHANGE_CHILDREN, this);
		try
		{
			getSubSystemConfiguration().saveSubSystem(this);
			ISystemFilterPoolReference[] poolRefs = getFilterPoolReferenceManager().getSystemFilterPoolReferences();
			for (int idx=0; idx<poolRefs.length; idx++)
				RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOLREF, poolRefs[idx], null);
		}
		catch (Exception exc)
		{
			SystemBasePlugin.logError("Error saving subsystem "+getName(),exc); //$NON-NLS-1$
		}
	}
	/**
	 * A filter pool reference has been renamed (ie, its reference filter pool renamed)
	 */
	public void filterEventFilterPoolReferenceRenamed(ISystemFilterPoolReference poolRef, String oldName)
	{
		if (getSubSystemConfiguration().showFilterPools())
		{
			fireEvent(poolRef, ISystemResourceChangeEvents.EVENT_RENAME, this);
		}
		else
		{
			//fireEvent(filterPoolRef, EVENT_PROPERTYSHEET_UPDATE, this);
		}
		try {
			getSubSystemConfiguration().saveSubSystem(this);
		}
		catch (Exception exc)
		{
			SystemBasePlugin.logError("Error saving subsystem "+getName(),exc); //$NON-NLS-1$
		}
	}
	/**
	 * One or more filter pool references have been re-ordered within their manager
	 */
	public void filterEventFilterPoolReferencesRePositioned(ISystemFilterPoolReference[] poolRefs, int delta)
	{
		fireEvent(poolRefs, ISystemResourceChangeEvents.EVENT_MOVE_MANY, this, delta);
		try {
			getSubSystemConfiguration().saveSubSystem(this);
			// fire model change event in case any BP code is listening...
			for (int idx=0; idx<poolRefs.length; idx++)
				RSECorePlugin.getTheSystemRegistry().fireModelChangeEvent(ISystemModelChangeEvents.SYSTEM_RESOURCE_REORDERED, ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOLREF, poolRefs[idx], null);
		}
		catch (Exception exc)
		{
			SystemBasePlugin.logError("Error saving subsystem "+getName(),exc); //$NON-NLS-1$
		}
	}
	// -------------------------------
	// FILTER REFERENCE EVENTS...
	// -------------------------------
	/**
	 * A new filter has been created. This is called when a filter pool reference is selected and a new filter
	 *  is created, so that the provider can expand the selected filter pool reference and reveal the new filter
	 *  within the selected pool reference.
	 * <p>
	 * Only the selected node should be expanded if not already. All other references to this pool will already
	 * have been informed of the new addition, and will have refreshed their children but not expanded them.
	 */
	public void filterEventFilterCreated(Object selectedObject, ISystemFilter newFilter)
	{
		fireEvent(newFilter, ISystemResourceChangeEvents.EVENT_REVEAL_AND_SELECT, selectedObject);
	}
	// ---------------------------------
	// FILTER STRING REFERENCE EVENTS...
	// ---------------------------------
	/**
	 * A new filter string has been created. This is called when a filter reference is selected and a new filter
	 *  string is created, so that the provider can expand the selected filter reference and reveal the new filter
	 *  string within the selected filter reference.
	 * <p>
	 * Only the selected node should be expanded if not already. All other references to this filter will already
	 * have been informed of the new addition, and will have refreshed their children but not expanded them.
	 */
	public void filterEventFilterStringCreated(Object selectedObject, ISystemFilterString newFilterString)
	{
		fireEvent(newFilterString, ISystemResourceChangeEvents.EVENT_REVEAL_AND_SELECT, selectedObject);
	}

	/**
	 * Represents an operation that can be performed by the subsystem. Since this class
	 * extends Job, it is run on a separate thread, but reports progress to the Main thread.
	 * Takes care of some common error handling and Status creation for SubSystem Operations.
	 * @author mjberger
	 */
	protected abstract class SubSystemOperationJob extends Job
	{
		protected Object[] runOutputs;
		protected String[] runOutputStrings;
		protected boolean  _hasStarted = false;

		public class ContextRunnable implements IRunnableWithProgress
		{
			private SubSystemOperationJob _job;
			private IStatus _status;

			public ContextRunnable(SubSystemOperationJob job)
			{
				_job = job;
			}

			public void run(IProgressMonitor monitor)
			{
				_status = _job.run(monitor);
			}

			public IStatus getStatus()
			{
				return _status;
			}
		}

		public SubSystemOperationJob(String operationName)
		{
			super(operationName + " (" + RSECoreMessages.RSESubSystemOperation_message + ")");    	 //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * Override this method with the actual operation performed by your subsystem operation. Make sure to
		 * report progress to the Progress monitor.
		 * @throws InterruptedException if the user presses cancel
		 * @throws InvocationTargetException if there is some error performing the operation
		 * @throws Exception if there is some other error
		 */
		public abstract void performOperation(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException, Exception;

		/**
		 * SubSystemOperationJobs are designed to be run synchronously - if you require output from them. Clients can query any output
		 * using getOutputs() or getOutputStrings().
		 */
		public Object[] getOutputs()
		{
			return runOutputs;
		}

		/**
		 * SubSystemOperationJobs are designed to be run synchronously - if you require output from them. Clients can query any output
		 * using getOutputs() or getOutputStrings().
		 */
		public String[] getOutputStrings()
		{
			return runOutputStrings;
		}

		public IStatus runInContext(IRunnableContext context)
		{
			_hasStarted = true;
			ContextRunnable conRun = new ContextRunnable(this);
			try
			{
				context.run(false, true, conRun);
				return conRun.getStatus();
			}
			catch (Exception e)
			{
				String excMsg = e.getMessage();
				if ((excMsg == null) || (excMsg.length()==0))
					excMsg = "Exception " + e.getClass().getName(); //$NON-NLS-1$
				String msgTxt = NLS.bind(CommonMessages.MSG_OPERATION_FAILED, excMsg);

				return new Status(IStatus.ERROR, RSEUIPlugin.PLUGIN_ID, IStatus.OK, msgTxt, e);
			}
		}

		public boolean hasStarted()
		{
			return _hasStarted;
		}

		public IStatus run(IProgressMonitor monitor)
		{
			_hasStarted = true;
			try
			{
				performOperation(monitor);

				if (monitor.isCanceled()) // sometimes our kids don't do this like they should!
				{
					return Status.CANCEL_STATUS;
				}
				monitor.done();
				return Status.OK_STATUS;
			}
			catch(OperationCanceledException exc)
			{
				monitor.done();
				return Status.CANCEL_STATUS;
			}
			catch(java.lang.InterruptedException exc)
			{
				monitor.done();
				return Status.CANCEL_STATUS;
			}
			catch(InvocationTargetException exc)
			{
				//exc.printStackTrace();
				monitor.done();
				String excMsg = exc.getTargetException().getMessage();
				if ((excMsg == null) || (excMsg.length()==0))
					excMsg = "Exception " + exc.getTargetException().getClass().getName(); //$NON-NLS-1$
				return new Status(IStatus.ERROR, RSEUIPlugin.PLUGIN_ID, IStatus.OK, excMsg, exc.getTargetException());
			}
			catch(Exception exc)
			{
				monitor.done();
				String excMsg = exc.getMessage();
				if ((excMsg == null) || (excMsg.length()==0))
					excMsg = "Exception " + exc.getClass().getName(); //$NON-NLS-1$
				String msgTxt = NLS.bind(CommonMessages.MSG_OPERATION_FAILED, excMsg);
				return new Status(IStatus.ERROR, RSEUIPlugin.PLUGIN_ID, IStatus.OK, msgTxt, exc);
			}
		}
	}

	/**
	 * Represents the subsystem operation of resolving absolute filter strings.
	 */
	protected class ResolveAbsoluteJob extends SubSystemOperationJob
	{
		protected String _filterString;

		/**
		 * Create a new ResolveAbsoluteJob
		 * @param filterString the absolute filter string to resolve
		 */
		public ResolveAbsoluteJob(String filterString)
		{
			super(RSECoreMessages.RSESubSystemOperation_Resolve_filter_strings_message);
			_filterString = filterString;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, InvocationTargetException, Exception
		{
			String msg = null;
			int totalWorkUnits = IProgressMonitor.UNKNOWN;

			msg = getResolvingMessage(_filterString);

			if (!implicitConnect(false, mon, msg, totalWorkUnits)){
				String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_FAILED, getHostName());
				throw new Exception(msgTxt);
			}
			runOutputs = internalResolveFilterString(_filterString, mon);
		}
	}

	/**
	 * Represents the subsystem operation of resolving a set of absolute filter strings.
	 */
	protected class ResolveAbsolutesJob extends SubSystemOperationJob
	{
		protected String _filterString;
		protected String[] _filterStrings;

		/**
		 * Create a new ResolveAbsolutesJob
		 * @param filterStrings the set of absolute filter strings to resolve
		 */
		public ResolveAbsolutesJob(String filterString, String[] filterStrings)
		{
			super(RSECoreMessages.RSESubSystemOperation_Resolve_filter_strings_message);
			_filterString = filterString;
			_filterStrings = filterStrings;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, InvocationTargetException, Exception
		{
			String msg = null;
			int totalWorkUnits = IProgressMonitor.UNKNOWN;

			msg = getResolvingMessage(_filterString);

			if (!implicitConnect(false, mon, msg, totalWorkUnits)){
				String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_FAILED, getHostName());
				throw new Exception(msgTxt);
			}
			runOutputs = internalResolveFilterStrings(_filterStrings, mon);
		}
	}

	/**
	 * Represents the subsystem operation of resolving relative filter strings.
	 */
	protected class ResolveRelativeJob extends SubSystemOperationJob
	{
		protected String _filterString;
		protected Object _parent;

		/**
		 * Create a new ResolveRelativeJob
		 * @param filterString the relative filter string to resolve
		 * @param parent the object within which the relative filter string will be resolved
		 */
		public ResolveRelativeJob(String filterString, Object parent)
		{
			super(RSECoreMessages.RSESubSystemOperation_Resolve_filter_strings_message);
			_filterString = filterString;
			_parent = parent;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, InvocationTargetException, Exception
		{
			String msg = null;
			int totalWorkUnits = IProgressMonitor.UNKNOWN;

			if (_filterString == null)
			{
				// DKM - we shouldn't be using parent context for filter strings because
				// now we have multiple contexts for the same resources
				_filterString = "*"; //$NON-NLS-1$
			}
			msg = getResolvingMessage(_filterString);

			if (!implicitConnect(false, mon, msg, totalWorkUnits)){
				String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_FAILED, getHostName());
				throw new Exception(msgTxt);
			}
			runOutputs = internalResolveFilterString(_parent, _filterString, mon);
		}
	}

	/**
	 * Represents the subsystem operation of getting a property value from a remote object.
	 */
	protected class GetPropertyJob extends SubSystemOperationJob
	{
		protected Object _subject;
		protected String _key;

		/**
		 * Constructs a new GetPropertyJob
		 * @param subject The object whose property will be queried
		 * @param key The property to query
		 */
		public GetPropertyJob(Object subject, String key)
		{
			super(RSECoreMessages.RSESubSystemOperation_Get_property_message);
			_subject = subject;
			_key = key;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, InvocationTargetException, Exception
		{
			String msg = null;
			int totalWorkUnits = IProgressMonitor.UNKNOWN;

			msg = getQueryingMessage(_key);

			if (!implicitConnect(false, mon, msg, totalWorkUnits)){
				String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_FAILED, getHostName());
				throw new Exception(msgTxt);
			}
			runOutputStrings = new String[] {internalGetProperty(_subject, _key, mon)};
		}
	}

	/**
	 * Represents the subsystem operation of setting a property of a remote object.
	 */
	protected class SetPropertyJob extends SubSystemOperationJob
	{
		protected Object _subject;
		protected String _key;
		protected String _value;

		/**
		 * Constructs a new SetPropertyJob
		 * @param subject the object whose property is to be set
		 * @param key the property to set
		 * @param value the new value for the property
		 */
		public SetPropertyJob(Object subject, String key, String value)
		{
			super(RSECoreMessages.RSESubSystemOperation_Set_property_message);
			_subject = subject;
			_key = key;
			_value = value;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, InvocationTargetException, Exception
		{
			String msg = null;
			int totalWorkUnits = IProgressMonitor.UNKNOWN;
			msg = getSettingMessage(_key);

			if (!implicitConnect(false, mon, msg, totalWorkUnits)){
				String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_FAILED, getHostName());
				throw new Exception(msgTxt);
			}
			runOutputs = new Object[] {internalSetProperty(_subject, _key, _value, mon)};
		}
	}

	/**
	 * Represents the subsystem operation of getting a set of property values from a remote object.
	 */
	protected class GetPropertiesJob extends SubSystemOperationJob
	{
		protected Object _subject;
		protected String[] _keys;

		/**
		 * Constructs a new GetPropertiesJob
		 * @param subject the object on which to perform the property query
		 * @param keys the properties to query
		 */
		public GetPropertiesJob(Object subject, String[] keys)
		{
			super(RSECoreMessages.RSESubSystemOperation_Get_properties_message);
			_subject = subject;
			_keys = keys;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, InvocationTargetException, Exception
		{
			String msg = null;
			int totalWorkUnits = IProgressMonitor.UNKNOWN;
			msg = getQueryingMessage();

			if (!implicitConnect(false, mon, msg, totalWorkUnits)){
				String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_FAILED, getHostName());
				throw new Exception(msgTxt);
			}
			runOutputStrings = internalGetProperties(_subject, _keys, mon);
		}
	}

	/**
	 * Represents the subsystem operation of setting a set of properties of a remote object.
	 */
	protected class SetPropertiesJob extends SubSystemOperationJob
	{
		protected Object _subject;
		protected String[] _keys;
		protected String[] _values;

		/**
		 * Constructs a new SetPropertiesJob
		 * @param subject the object whose properties are to be set
		 * @param keys the set of properties to set
		 * @param values the set of new values for the properties, corresponding with <code>keys</code>
		 */
		public SetPropertiesJob(Object subject, String[] keys, String[] values)
		{
			super(RSECoreMessages.RSESubSystemOperation_Set_properties_message);
			_subject = subject;
			_keys = keys;
			_values = values;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, InvocationTargetException, Exception
		{
			String msg = null;
			int totalWorkUnits = IProgressMonitor.UNKNOWN;

			msg = getSettingMessage();

			if (!implicitConnect(false, mon, msg, totalWorkUnits)){
				String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_FAILED, getHostName());
				throw new Exception(msgTxt);
			}
			runOutputs = new Object[] {internalSetProperties(_subject, _keys, _values, mon)};
		}
	}

	/**
	 * Represents the subsystem operation of connecting the subsystem to the remote machine.
	 */
	public class ConnectJob extends SubSystemOperationJob
	{
		private SubSystem _ss;
		private IRSECallback _callback;

		public ConnectJob(SubSystem ss, IRSECallback callback)
		{
			super(RSECoreMessages.RSESubSystemOperation_Connect_message);
			_ss = ss;
			_callback = callback;
		}

		public IStatus run(IProgressMonitor monitor) {
			IStatus status = super.run(monitor);
			if (_callback != null)
			{
				_callback.done(status, null);
			}
			return status;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, Exception
		{
			String msg = null;
			int totalWorkUnits = IProgressMonitor.UNKNOWN;

			msg = SubSystemConfiguration.getConnectingMessage(getHostName(), getConnectorService().getPort());
			SystemBasePlugin.logInfo(msg);

			if (!isOffline()){
				if (!implicitConnect(true, mon, msg, totalWorkUnits)){
					String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_FAILED, getHostName());
					throw new Exception(msgTxt);
				}
				internalConnect(mon);

				ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
				registry.connectedStatusChange(_ss, true, false);
			}
		}
	}

	/**
	 * Represents the subsystem operation of disconnecting the subsystem to the remote machine.
	 */
	protected class DisconnectJob extends SubSystemOperationJob
	{
		public class PostDisconnect implements Runnable
		{

			public void run()
			{
				getConnectorService().reset();
				ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
				sr.connectedStatusChange(_subsystem, false, true, _collapseTree);
			}

		}

		private boolean _collapseTree;
		private SubSystem _subsystem;
		public DisconnectJob(boolean collapseTree, SubSystem subsystem)
		{
			super(RSECoreMessages.RSESubSystemOperation_Disconnect_message);
			_collapseTree = collapseTree;
			_subsystem = subsystem;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, Exception
		{
			internalDisconnect(mon);
			_disconnecting = false;
			_connectionError = false;
			Display.getDefault().asyncExec(new PostDisconnect());
		}

	}

	/**
	 * Represents the operation of changing the connection status of this subsystem.
	 */
	protected class ChangeStatusJob extends UIJob
	{
		private final ISubSystem _subsystem;

		public ChangeStatusJob(ISubSystem ss)
		{
			super(RSECoreMessages.RSESubSystemOperation_Notifying_registry_message);
			_subsystem = ss;
		}

		public IStatus runInUIThread(IProgressMonitor monitor)
		{
			final ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			sr.connectedStatusChange(_subsystem, true, false);
			return Status.OK_STATUS;
		}
	}

	protected boolean implicitConnect(boolean isConnectOperation, IProgressMonitor mon, String msg, int totalWorkUnits) throws SystemMessageException, InvocationTargetException, OperationCanceledException
	{
		boolean didConnection = false;
		if ( doConnection && !isConnected())// caller wants to do connection first as part operation
		{
			if (isOffline() || (supportsCaching() && getCacheManager().isRestoreFromMemento()))
			{
				if (!supportsCaching())
				{
					// offline and no caching support so throw exception
					String msgTxt = NLS.bind(RSECoreMessages.MSG_OFFLINE_CANT_CONNECT,getHost().getAliasName());
					String msgDetails = NLS.bind(RSECoreMessages.MSG_OFFLINE_CANT_CONNECT_DETAILS, getHost().getAliasName());
					SystemMessage sMsg = new SimpleSystemMessage(RSECorePlugin.PLUGIN_ID,
							SystemResourceConstants.MSG_OFFLINE_CANT_CONNECT,
							IStatus.INFO, msgTxt, msgDetails);
					throw new SystemMessageException(sMsg);
				}

				// we are not either offline or we support caching and are restoring from memento so
				// postpone connecting until we determine the information cannot be retrieved from the cache,
				// it is left to individual api calls to determine this
				doConnection = false;
			}
			else
			{
				doConnection = false; // reset
				didConnection = true;
				mon.beginTask(SubSystemConfiguration.getConnectingMessage(getHostName(), getConnectorService().getPort()), totalWorkUnits);
				internalConnect(mon);
				mon.worked(1);

				ChangeStatusJob job = new ChangeStatusJob(this);
				job.setSystem(true);
				job.setPriority(Job.INTERACTIVE);
				job.schedule();
			}
		}
		else
		{
			doConnection = false;
		}

		if (isConnected() || isConnectOperation || isOffline() || (supportsCaching() && getCacheManager().isRestoreFromMemento()))
		{
			if (!didConnection)
				mon.beginTask(msg, (totalWorkUnits==1) ? IProgressMonitor.UNKNOWN : totalWorkUnits);
			else
				mon.setTaskName(msg);
			return true;
		}
		else // the implicit connect must have failed
		{
			mon.done();
			return false;
		}
	}

	protected void showOperationMessage(Exception exc, Shell shell)
	{
		if (exc instanceof java.lang.InterruptedException)
			showOperationCancelledMessage(shell);
		else if (exc instanceof java.lang.reflect.InvocationTargetException)
			showOperationErrorMessage(shell, ((java.lang.reflect.InvocationTargetException)exc).getTargetException());
		else
			showOperationErrorMessage(shell, exc);
	}

	/**
	 * Show an error message when the connection fails.
	 * Shows a common message by default.
	 * Overridable.
	 */
	protected void showConnectErrorMessage(Shell shell, String hostName, int port, Throwable exc)
	{
		SystemMessage msg = null;
		if (exc instanceof SystemMessageException)
		{
			SystemBasePlugin.logError("Connection error", exc); //$NON-NLS-1$
			msg = ((SystemMessageException) exc).getSystemMessage();
		}
		else if (exc instanceof java.net.UnknownHostException)
		{
			SystemBasePlugin.logError("Connection error", exc);    	 //$NON-NLS-1$
			String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_UNKNOWNHOST, hostName);
			msg = new SimpleSystemMessage(RSECorePlugin.PLUGIN_ID,
					ICommonMessageIds.MSG_CONNECT_UNKNOWNHOST,
					IStatus.ERROR, msgTxt, exc);
		}
		else
		{
			SystemBasePlugin.logError("Connection error", exc); //$NON-NLS-1$
			String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_FAILED, hostName);
			msg = new SimpleSystemMessage(RSECorePlugin.PLUGIN_ID,
					ICommonMessageIds.MSG_CONNECT_FAILED,
					IStatus.ERROR, msgTxt, exc);
		}

		SystemMessageDialog msgDlg = new SystemMessageDialog(shell, msg);
		msgDlg.setException(exc);
		msgDlg.open();
	}
	/**
	 * Show an error message when the user cancels the connection.
	 * Shows a common message by default.
	 * Overridable.
	 */
	protected void showConnectCancelledMessage(Shell shell, String hostName, int port)
	{
		String msgTxt = NLS.bind(CommonMessages.MSG_CONNECT_CANCELLED, hostName);
		SystemMessage msg = new SimpleSystemMessage(RSECorePlugin.PLUGIN_ID,
				ICommonMessageIds.MSG_CONNECT_CANCELLED,
				IStatus.CANCEL, msgTxt);
		SystemMessageDialog msgDlg = new SystemMessageDialog(shell, msg);
		msgDlg.open();
	}
	/**
	 * Show an error message when the disconnection fails.
	 * Shows a common message by default.
	 * Overridable.
	 */
	protected void showDisconnectErrorMessage(Shell shell, String hostName, int port, Exception exc)
	{
		String msgTxt = NLS.bind(CommonMessages.MSG_DISCONNECT_FAILED, hostName);
		SystemMessage msg = new SimpleSystemMessage(RSECorePlugin.PLUGIN_ID,
				ICommonMessageIds.MSG_DISCONNECT_FAILED,
				IStatus.CANCEL, msgTxt, exc);

		SystemMessageDialog msgDlg = new SystemMessageDialog(shell,msg);
		msgDlg.setException(exc);
		msgDlg.open();
	}
	/**
	 * Show an error message when the user cancels the disconnection.
	 * Shows a common message by default.
	 * Overridable.
	 */
	protected void showDisconnectCancelledMessage(Shell shell, String hostName, int port)
	{
		String msgTxt = NLS.bind(CommonMessages.MSG_DISCONNECT_CANCELLED, hostName);
		SystemMessage msg = new SimpleSystemMessage(RSECorePlugin.PLUGIN_ID,
				ICommonMessageIds.MSG_DISCONNECT_CANCELLED,
				IStatus.CANCEL, msgTxt);

		SystemMessageDialog msgDlg = new SystemMessageDialog(shell,msg);
		msgDlg.open();
	}


	/**
	 * Helper method to return the message "Resolving to &1..."
	 */
	protected static String getResolvingMessage(String filterString)
	{
		String msgTxt = NLS.bind(CommonMessages.MSG_RESOLVE_PROGRESS, filterString);
		return msgTxt;
	}
	/**
	 * Helper method to return the message "Running command &1..."
	 */
	protected static String getRunningMessage(String cmd)
	{
		String msgTxt = NLS.bind(CommonMessages.MSG_RUN_PROGRESS, cmd);
		return msgTxt;
	}
	/**
	 * Helper method to return the message "Querying &1..."
	 */
	protected static String getQueryingMessage(String key)
	{
		String msgTxt = NLS.bind(CommonMessages.MSG_QUERY_PROGRESS, key);
		return msgTxt;
	}
	/**
	 * Helper method to return the message "Setting &1..."
	 */
	protected static String getSettingMessage(String key)
	{
		String msgTxt = NLS.bind(CommonMessages.MSG_SET_PROGRESS, key);
		return msgTxt;
	}
	/**
	 * Helper method to return the message "Querying properties..."
	 */
	protected static String getQueryingMessage()
	{
		return CommonMessages.MSG_QUERY_PROPERTIES_PROGRESS;
	}
	/**
	 * Helper method to return the message "Setting properties..."
	 */
	protected static String getSettingMessage()
	{
		return CommonMessages.MSG_SET_PROPERTIES_PROGRESS;
	}

	/**
	 * Show an error message when the operation fails.
	 * Shows a common message by default, unless the exception is of type SystemMessageException,
	 *   in which case the message is retrieved via getSystemMessage() and it is shown.
	 * Overridable.
	 */
	protected void showOperationErrorMessage(Shell shell, Throwable exc)
	{
		SystemMessage sysMsg = null;
		if (exc instanceof SystemMessageException)
		{
			displayAsyncMsg((SystemMessageException)exc);
			//sysMsg = ((SystemMessageException)exc).getSystemMessage();
		}
		else
		{
			String excMsg = exc.getMessage();
			if ((excMsg == null) || (excMsg.length()==0))
				excMsg = "Exception " + exc.getClass().getName(); //$NON-NLS-1$

			String msgTxt = NLS.bind(CommonMessages.MSG_OPERATION_FAILED, excMsg);

			sysMsg = new SimpleSystemMessage(RSECorePlugin.PLUGIN_ID,
					ICommonMessageIds.MSG_OPERATION_FAILED,
					IStatus.ERROR, msgTxt, exc);


			SystemMessageDialog msgDlg = new SystemMessageDialog(shell, sysMsg);
			msgDlg.setException(exc);
			msgDlg.open();
			//RSEUIPlugin.logError("Operation failed",exc); now done successfully in msgDlg.open()
		}

	}
	/**
	 * Show an error message when the user cancels the operation.
	 * Shows a common message by default.
	 * Overridable.
	 */
	protected void showOperationCancelledMessage(Shell shell)
	{
		String msgTxt = CommonMessages.MSG_OPERATION_CANCELLED;
		SystemMessage msg = new SimpleSystemMessage(RSECorePlugin.PLUGIN_ID,
				ICommonMessageIds.MSG_OPERATION_CANCELLED,
				IStatus.CANCEL, msgTxt);
		SystemMessageDialog msgDlg = new SystemMessageDialog(shell, msg);
		msgDlg.open();
	}


	// ----------------------------------
	// PUBLIC METHODS HANDLED FOR YOU....
	// ----------------------------------
	/**
	 * Return true if this subsystem is currently connected to its remote system.
	 * If the subsystem configuration indicates its subsystems support connecting, then
	 *  this method will call getSystem().isConnect().
	 */
	public boolean isConnected()
	{
		IConnectorService system = getConnectorService();
		if (system != null)
			return system.isConnected();
		else if (!supportsConnecting)
			return true;
		else
			return false;
	}

	/**
	 * Returns the offline property for this subsystem's System Connection.
	 */
	public boolean isOffline()
	{
		return getHost().isOffline();
	}
	/**
	 * CHILD CLASSES MAY OVERRIDE THIS.
	 * By default it returns true iff we support filters and there are filter pool references.
	 */
	public boolean hasChildren()
	{
		if (getSubSystemConfiguration().supportsFilters())
		{
			ISystemFilterPoolReferenceManager mgr = getSystemFilterPoolReferenceManager();
			if (mgr == null)
				return false;
			else
				return mgr.getSystemFilterPoolReferenceCount() > 0;
		}
		else
			return false;
	}
	/**
	 * Return the children of this subsystem, to populate the GUI subtree of
	 * this subsystem. By default, this method:
	 * <ul>
	 * <li>Returns the filter pool references of this subsystem, if
	 * supportsFilters() is true for our factory.
	 * <li>If supportsFilters() is false from our factory, returns null
	 * </ul>
	 * So, be sure to override this method IF you do not support filters.
	 *
	 * Lazy Loading: Note that if your subsystem does not support connecting,
	 * and you do not support filters, here is a good point to ensure that the
	 * bundles which declare your UI adapters get loaded, since the default code
	 * which overriders usually place in
	 * {@link #initializeSubSystem(IProgressMonitor)} is not called in that
	 * case. Similarly, if your subsystem declares custom images for filters or
	 * filter pools, overriding the getChildren() call here to first load your
	 * filter adapters and THEN super.getChildren() is a good idea.
	 */
	public Object[] getChildren()
	{
		if (getSubSystemConfiguration().supportsFilters())
		{
			ISystemFilterPoolReferenceManager mgr = getSystemFilterPoolReferenceManager();
			if (mgr == null)
				return null;
			else
			{
				boolean showFilterPools = getSubSystemConfiguration().showFilterPools();
				if (showFilterPools)
					return mgr.getSystemFilterPoolReferences();
				else
				{
					ISystemFilterReference[] allRefs = mgr.getSystemFilterReferences(this);
					return allRefs;
				}
			}
		}
		else
			return null;
	}

	protected void scheduleJob(SubSystemOperationJob job, ISchedulingRule rule) throws InterruptedException
	{
		IRunnableContext context = getRunnableContext(/*shell*/); // dwd needed for side effect or for prompt?
		if (context instanceof SystemPromptDialog)
		{
			IStatus status = job.runInContext(context);
			if (!status.isOK())
			{
				showOperationErrorMessage(shell, status.getException());
			}
			return;
		}
		job.setPriority(Job.INTERACTIVE);
		//job.setUser(true);
		if (rule != null)
		{
			job.setRule(rule);
		}
		job.schedule();
	}

	/**
	 * Sort the concatenated list of all objects returned by resolving one or more
	 *  filter strings.
	 * The default implementation does nothing. Child classes can override if they wish
	 *  to show their resulting objects sorted.
	 */
	protected Object[] sortResolvedFilterStringObjects(Object[] input)
	{
		return input;
	}


	/**
	 * Resolve an <i>absolute</i> filter string.
	 *
	 * This is only applicable if the subsystem
	 *  factory reports true for {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsFilters()},
	 *  which is the default. Otherwise, {@link org.eclipse.rse.core.subsystems.SubSystem#getChildren()}
	 *  is called when the subsystem itself is expanded.
	 * <p>
	 * When a user <u>expands a filter</u> this method is invoked for each filter string and the
	 *  results are concatenated and displayed to the user. You can affect the post-concatenated
	 *  result by overriding {@link #sortResolvedFilterStringObjects(Object[])} if you desire to
	 *  sort the result, say, or pick our redundancies.
	 * <p>
	 * The resulting objects are displayed in the tree in the Remote System {@link org.eclipse.rse.internal.ui.view.SystemView view}.
	 * There are <u>two requirements</u> on the returned objects:</p>
	 * <ol>
	 *   <li>They must implement {@link org.eclipse.core.runtime.IAdaptable}.
	 *   <li>Their must be an RSE {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter remote-adapter} registered
	 *        for the object's class or interface type. Further, if this subsystem is {@link org.eclipse.rse.core.subsystems.SubSystem#isHidden() visible}
	 *        in the RSE, which is the default, then there must also be an RSE {@link org.eclipse.rse.ui.view.ISystemViewElementAdapter GUI-adapter} registered
	 *        with the platform. The base class implementation of this interface is {@link org.eclipse.rse.ui.view.AbstractSystemViewAdapter}.
	 * </ol>
	 * <p>A good place to start with your remote-resource classes to subclasss {@link org.eclipse.rse.core.subsystems.AbstractResource}, as it
	 * already implements IAdaptable, and maintains a reference to this owning subsystem, which helps when
	 * implementing the {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter remote-adapter}.
	 * <p>
	 * Be sure to register your adapter factory in your plugin's startup method.
	 * <p>
	 * <b>You do not need to override this, as it does the progress monitor and error message
	 *  displaying for you. Just override internalResolveFilterString.</b>
	 * <p>
	 * @param filterString filter pattern for objects to return.
	 * @param monitor the process monitor associated with this operation
	 *
	 * @return Array of objects that are the result of this filter string
	 */
	public Object[] resolveFilterString(String filterString, IProgressMonitor monitor) throws Exception
	{
		// for bug 233435, implicit connect if the connection is not connected
		checkIsConnected(monitor);

		if (isConnected() || isOffline() || (getCacheManager() != null && getCacheManager().isRestoreFromMemento()))
		{
			if (!supportsConnecting && !_isInitialized) {
				// Lazy Loading: Load adapters (e.g. Local Subsystem)
				initializeSubSystem(monitor);
			}
			Object[] results = internalResolveFilterString(filterString, monitor);
			if (sortResults && (results!=null))
				results = sortResolvedFilterStringObjects(results);
			return results;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Resolve multiple absolute filter strings. This is only applicable if the subsystem
	 *  factory reports true for supportsFilters().
	 * <p>
	 * This is the same as {@link #resolveFilterString(String, IProgressMonitor)} but takes an array of
	 *  filter strings versus a single filter string.
	 * <p>
	 * The default implementation of this simply calls {@link #internalResolveFilterStrings(String[], IProgressMonitor)}.
	 * <p>
	 * After successful resolve, the sort method is called to sort the concatenated results before
	 *  returning them.
	 *
	 * @param filterStrings array of filter patterns for objects to return.
	 * @param monitor the process monitor associated with this operation
	 *
	 * @return Array of objects that are the result of this filter string
	 */
	public Object[] resolveFilterStrings(String[] filterStrings, IProgressMonitor monitor)
	throws Exception
	{
		// for bug 233435, implicit connect if the connection is not connected
		checkIsConnected(monitor);
		
		if ((filterStrings == null) || (filterStrings.length == 0)) {
			SystemBasePlugin.logInfo("Filter strings are null"); //$NON-NLS-1$
			return null;
		}
		if (isConnected() || isOffline() || (getCacheManager() != null && getCacheManager().isRestoreFromMemento()))
		{
			if (!supportsConnecting && !_isInitialized) {
				// Lazy Loading: Load adapters (e.g. Local Subsystem)
				initializeSubSystem(monitor);
			}
			Object[] results = internalResolveFilterStrings(filterStrings, monitor);
			if (sortResults && (results!=null))
				results = sortResolvedFilterStringObjects(results);
			return results;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Resolve a <i>relative</i> filter string.
	 * <p>
	 * When a user <u>expands a remote resource</u> this method is invoked and the
	 *  results are potentially sorted and displayed to the user. You can affect the sorting
	 *  behaviour by overriding {@link #sortResolvedFilterStringObjects(Object[])} if you desire to
	 *  sort the result, say, or pick our redundancies.
	 * <p>
	 * The resulting objects are displayed in the tree in the Remote System {@link org.eclipse.rse.internal.ui.view.SystemView view}.
	 * There are <u>two requirements</u> on the returned objects:</p>
	 * <ol>
	 *   <li>They must implement {@link org.eclipse.core.runtime.IAdaptable}.
	 *   <li>Their must be an RSE {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter remote-adapter} registered
	 *        for the object's class or interface type. Further, if this subsystem is {@link org.eclipse.rse.core.subsystems.SubSystem#isHidden() visible}
	 *        in the RSE, which is the default, then there must also be an RSE {@link org.eclipse.rse.ui.view.ISystemViewElementAdapter GUI-adapter} registered
	 *        with the platform. The base class implementation of this interface is {@link org.eclipse.rse.ui.view.AbstractSystemViewAdapter}.
	 * </ol>
	 * <p>A good place to start with your remote-resource classes to subclasss {@link org.eclipse.rse.core.subsystems.AbstractResource}, as it
	 * already implements IAdaptable, and maintains a reference to this owning subsystem, which helps when
	 * implementing the {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter remote-adapter}.
	 * <p>
	 * Be sure to register your adapter factory in your plugin's startup method.
	 * <p>
	 * <b>You do not need to override this, as it does the progress monitor and error message
	 *  displaying for you. Override internalResolveFilterString instead. </b>
	 * <p>
	 * @param parent Object that is being expanded.
	 * @param filterString filter pattern for children of parent. Typically just "*".
	 * @param monitor the process monitor associated with this operation
	 *
	 * @return Array of objects that are the result of this filter string
	 */
	public Object[] resolveFilterString(Object parent, String filterString, IProgressMonitor monitor)
	throws Exception
	{
		
		// for bug 237970, can't connect when we're in offline mode
		if (!isOffline()){			
			// for bug 233435, implicit connect if the connection is not connected
			checkIsConnected(monitor);
		}
		
		if (isConnected() || isOffline() || (getCacheManager() != null && getCacheManager().isRestoreFromMemento()))
		{
			if (!supportsConnecting && !_isInitialized) {
				// Lazy Loading: Load adapters (e.g. Local Subsystem)
				initializeSubSystem(monitor);
			}
			Object[] results= internalResolveFilterString(parent, filterString, monitor);
			if (sortResults && (results!=null))
				results =  sortResolvedFilterStringObjects(results);
			return results;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Provide list of executed commands on subsystem.This is only applicable if the subsystem factory reports
	 *  true for supportsCommands().
	 */
	public String[] getExecutedCommands()
	{
		return null;
	}


	/**
	 * Set a remote property. Subsystems interpret as they wish. Eg, this might be to set
	 *  a remote environment variable. This is only applicable if the subsystem factory reports
	 *  true for supportsProperties().
	 * @param subject Identifies which object to get the properties of
	 * @param key Identifies property to set
	 * @param value Value to set property to
	 * @return Object interpretable by subsystem. Might be a Boolean, or the might be new value for confirmation.
	 *
	 * @deprecated this shouldn't be used
	 */
	public Object setProperty(Object subject, String key, String value)
	throws Exception
	{
		return null;
	}

	/**
	 * Get a remote property. Subsystems interpret as they wish. Eg, this might be to get
	 *  a remote environment variable. This is only applicable if the subsystem factory reports
	 *  true for supportsProperties().
	 * @param subject Identifies which object to get the properties of
	 * @param key Identifies property to get value of
	 * @return String The value of the requested key.
	 *
	 * @deprecated this shouldn't be used
	 */
	public String getProperty(Object subject, String key)
	throws Exception
	{
		return null;
	}

	/**
	 * Set multiple remote properties. Subsystems interpret as they wish. Eg, this might be to set
	 *  a number of remote environment variables. This is only applicable if the subsystem factory reports
	 *  true for supportsProperties().
	 * @param subject identifies which object to get the properties of.
	 * @param keys the array of propertie keys to set.
	 * @param values the array of values to set. The value at a certain index corresponds to the property key at the same index.
	 * @return Object interpretable by subsystem. Might be a Boolean, or the might be new values for confirmation.
	 *
	 * @deprecated this shouldn't be used
	 */
	public Object setProperties(Object subject, String[] keys, String[] values)
	throws Exception
	{
		return null;
	}

	/**
	 * Initialize this subsystem instance after the corresponding
	 * {@link IConnectorService} connect method finishes. This method should be
	 * overridden if any initialization for the subsystem needs to occur at this
	 * time.
	 * <p>
	 * The default implementation currently does nothing, but overriding methods
	 * should call super before doing any other work.
	 *
	 * @param monitor a progress monitor that can be used to show progress
	 *            during long-running operation. Cancellation is typically not
	 *            supported since it might leave the system in an inconsistent
	 *            state.
	 * @throws SystemMessageException if an error occurs during initialization.          
	 */
	public void initializeSubSystem(IProgressMonitor monitor) throws SystemMessageException {
		_isInitialized = true;
	}

	/**
	 * Uninitialize this subsystem just after disconnect. The default
	 * implementation currently does nothing. Overriding methods should call
	 * super after doing their own work.
	 *
	 * @param monitor a progress monitor that can be used to show progress
	 *            during long-running operation. Cancellation is typically not
	 *            supported since it might leave the system in an inconsistent
	 *            state.
	 */
	public void uninitializeSubSystem(IProgressMonitor monitor) {
		_isInitialized = false;
	}

	
	private static class ConnectorServicePool {
		
		private static List _connectingConnectorServices = new ArrayList();
		
		public synchronized void add(IConnectorService cs) {
			_connectingConnectorServices.add(cs);
		}
		
		public synchronized void remove(IConnectorService cs) {
			_connectingConnectorServices.remove(cs);
			notifyAll();
		}
		
		public synchronized boolean contains(IConnectorService cs) {
			return _connectingConnectorServices.contains(cs);
		}
		
		public synchronized void waitUntilNotContained(IConnectorService cs) {
			while (contains(cs) &&                    // wait until the connector service is no longer in the list
					Display.getCurrent() == null){    // for bug 326555, don't wait when on the main thread - otherwise there will be a hang
				try {				
						wait();			
				}
				catch (InterruptedException e){			
					e.printStackTrace();
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	 private static ConnectorServicePool _connectorServicePool = new ConnectorServicePool();
	 
	/*
	 * Connect to a remote system with a monitor.
	 * Required for Bug 176603
	 * @see org.eclipse.rse.core.subsystems.ISubSystem#connect(org.eclipse.core.runtime.IProgressMonitor, boolean)
	 */
	public void connect(IProgressMonitor monitor, boolean forcePrompt) throws Exception
	{
		if (!isConnected())
		{
			String msg = null;

			msg = SubSystemConfiguration.getConnectingMessage(getHostName(), getConnectorService().getPort());
			SystemBasePlugin.logInfo(msg);
			monitor.beginTask(msg, 10);
			final boolean promptForPassword = forcePrompt;

			final Exception[] exception=new Exception[1];
			exception[0]=null;
					
			IConnectorService connectorService = getConnectorService();			
			// is this connector service already connecting?	
			boolean alreadyConnecting = _connectorServicePool.contains(connectorService);
			
			if (alreadyConnecting){
				// connector service already attempting connect
				// need to wait for it to complete
				// before we can return out of this method
				_connectorServicePool.waitUntilNotContained(connectorService);
			}
			else {
				_connectorServicePool.add(connectorService);
				
				try {
					Display.getDefault().syncExec(new Runnable() {				
						public void run() {
							try
							{
								promptForPassword(promptForPassword);
							} catch(Exception e) {
								exception[0]=e;
							}
						}
					});
										
					Exception e = exception[0];
					if (e == null) {
						getConnectorService().connect(monitor);
						if (isConnected()) {
							final SubSystem ss = this;
							//Notify connect status change
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									RSECorePlugin.getTheSystemRegistry().connectedStatusChange(ss, true, false);
								}
							});
						}
					} else {						
						throw e;
					}
				} finally {
					_connectorServicePool.remove(connectorService);
					monitor.done();
				}
			} 
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.ISubSystem#connect(boolean, org.eclipse.rse.core.model.IRSECallback)
	 */
	public void connect(boolean forcePrompt, IRSECallback callback) throws Exception {
		// yantzi: artemis60, (defect 53082) check that the connection has not been deleted before continuing,
		// this is a defenisve measure to protect against code that stores a handle to subsystems but does
		// not do this check
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		IHost host = getHost();
		String hostName = host.getAliasName();
		ISystemProfile profile = getSystemProfile();
		if (registry.getHost(profile, hostName) == null) { // connection no longer exists
			String msgTxt = NLS.bind(RSECoreMessages.MSG_CONNECTION_DELETED, hostName);

			SystemMessage msg = new SimpleSystemMessage(RSECorePlugin.PLUGIN_ID,
					SystemResourceConstants.MSG_CONNECTION_DELETED,
					IStatus.ERROR, msgTxt);
			throw new SystemMessageException(msg);
		}
		// yantzi: artemis 6.0, offline support
		if (isOffline()) {
			String msgTxt = NLS.bind(RSECoreMessages.MSG_OFFLINE_CANT_CONNECT, hostName);
			String msgDetails = NLS.bind(RSECoreMessages.MSG_OFFLINE_CANT_CONNECT_DETAILS, hostName);

			SystemMessage msg = new SimpleSystemMessage(RSECorePlugin.PLUGIN_ID,
					SystemResourceConstants.MSG_OFFLINE_CANT_CONNECT,
					IStatus.ERROR, msgTxt, msgDetails);

			throw new SystemMessageException(msg);
		}
		//DY operation = OPERATION_CONNECT;
		if (!isConnected() && supportsConnecting) {
			getRunnableContext(/*shell*/); // needed only for side effect of setting shell to the shell for the active workbench window
			//dwd			IRunnableContext runnableContext = getRunnableContext(shell);
			//dwd			if (runnableContext instanceof ProgressMonitorDialog) {
			//dwd				((ProgressMonitorDialog) runnableContext).setCancelable(true);
			//dwd			}
			getConnectorService().acquireCredentials(forcePrompt); // prompt for userid and password
			//FIXME Error reporting from the ConnectJob? How is the exception thrown?
			ConnectJob job = new ConnectJob(this, callback);
			scheduleJob(job, null);
		}
	}

	/**
	 * A convenience method, fully equivalent to promptForPassword(false).
	 */
	public boolean promptForPassword() throws Exception
	{
		return promptForPassword(false);
	}

	/**
	 * Prompt the user for a password to the remote system. The primary request was something else,
	 * but we have detected the user is not connected so we prompt for password outside
	 * of the progress monitor, then set a flag to do the connection within the progress
	 * monitor.
	 * @param force true if the prompting should be forced, false if prompting can be skipped if credentials have been stored.
	 * @return true if the credentials are obtained
	 */
	public boolean promptForPassword(boolean force) throws Exception
	{
		boolean ok = false;
		if (!supportsConnecting)
			return true;

		if (isOffline())
		{
			// offline so don't bother prompting
			doConnection = true;	// this gets handled later when it comes time to connect
			return true;
		}

		try
		{
			getConnectorService().acquireCredentials(force); // prompt for password
			doConnection = true;
			ok = true;
		}
		catch (OperationCanceledException exc) // user cancelled
		{
			throw exc;
		}
		catch (Exception exc)
		{
			showConnectErrorMessage(shell, getHostName(), getConnectorService().getPort(), exc);
		}
		return ok;
	}

	/**
	 * Disconnect from the remote system
	 * You do not need to override this, as it does the progress monitor and error message
	 *  displaying for you.
	 * <p>
	 * Override internalDisconnect if you want, but by default it calls getSystem().disconnect(IProgressMonitor).
	 *
	 */
	public void disconnect() throws Exception
	{
		disconnect(true);
	}

	/**
	 * Disconnect from the remote system
	 * You do not need to override this, as it does the progress monitor and error message
	 *  displaying for you.
	 * <p>
	 * Override internalDisconnect if you want, but by default it calls getSystem().disconnect(IProgressMonitor).
	 *
	 * @param collapseTree collapse the tree in the system view
	 */
	public void disconnect(boolean collapseTree) throws Exception
	{
		_disconnecting = true;
		if (!isConnected() || !supportsConnecting)
		{
			// disconnected but may not have notified viewers (i.e. network problem)
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			sr.connectedStatusChange(this, false, true, collapseTree);
			return;
		}
		/*
    	try
    	{
		 */

		DisconnectJob job = new DisconnectJob(collapseTree, this);
		job.schedule();
		/*
    	}
    	catch (InterruptedException exc)
    	{
    	  if (shell != null)
            showDisconnectCancelledMessage(shell, getHostName(), getConnectorService().getPort());
    	  throw exc;
    	}
    	finally
    	{
    		_disconnecting = false;
    		_connectionError = false;
    	}
		 */
	}

	/**
	 * Get a remote property. Subsystems interpret as they wish. Eg, this might be to get
	 *  a remote environment variable. This is only applicable if the subsystem factory reports
	 *  true for supportsProperties().
	 * @param subject Identifies which object to get the properties of
	 * @param keys the array of property keys.
	 * @return the values for the given property keys.
	 *
	 * @deprecated this shouldn't be used
	 */
	public String[] getProperties(Object subject, String[] keys)
	throws Exception
	{
		return null;
	}


	/**
	 * Return the {@link org.eclipse.rse.core.subsystems.IConnectorService IConnectorService} object that represents the live connection for this system.
	 * This must return an object that implements {@link IConnectorService}. A good starting point for that
	 *  is the base class {@link AbstractConnectorService}.
	 *
	 *  The connector service gets passed in to the constructor for the subsystem so there's normally no reason
	 *  to override this method.
	 *
	 */
	public IConnectorService getConnectorService()
	{
		return _connectorService;
	}

	/**
	 * Sets the {@link org.eclipse.rse.core.subsystems.IConnectorService IConnectorService} object that represents the live connection for this system.
	 *
	 * @param connectorService the connector service
	 */
	public void setConnectorService(IConnectorService connectorService)
	{
		if (_connectorService != connectorService)
		{
			_connectorService = connectorService;
			_connectorService.registerSubSystem(this);
			setDirty(true);
		}
		else
		{
			// register the subsystem
			_connectorService.registerSubSystem(this);
		}
	}

	// ----------------------------------
	// METHODS THAT MUST BE OVERRIDDEN...
	// ----------------------------------


	/**
	 * Check if the SubSystem supports caching.  This is the default implementation
	 * which returns false.  Subclasses must override to support caching.
	 */
	public boolean supportsCaching()
	{
		return false;
	}

	/**
	 * Return the CacheManager for this subsystem.  This is the default implementation
	 * which just returns null.
	 *
	 * @see #supportsCaching()
	 */
	public ICacheManager getCacheManager()
	{
		return null;
	}

	/**
	 * Connect to the remote host. This is called by the implicitConnect(boolean, IProgressMonitor, String, int) method.
	 * Rather, this calls connect(IProgressMonitor) in the IConnectorService class that is returned from getConnectorService().
	 * <p>
	 * Your connect method in your IConnectorService class must follow these IRunnableWithProgress rules:
	 * <ul>
	 *   <li>if the user cancels (monitor.isCanceled()), throw new OperationCanceledException()
	 *   <li>if something else bad happens, throw new java.lang.reflect.InvocationTargetException(exc)
	 *       - well, actually you can throw anything and we'll wrap it here in an InvocationTargetException
	 *   <li>do not worry about calling monitor.done() ... caller will do that.
	 * </ul>
	 *
	 */
	private void internalConnect(IProgressMonitor monitor)
	throws InvocationTargetException, OperationCanceledException
	{
		try
		{
			getConnectorService().connect(monitor);
		}
		catch(InvocationTargetException exc) {
			throw exc;
		}
		catch (OperationCanceledException exc) {
			throw exc;
		}
		catch (Exception exc) {
			throw new InvocationTargetException(exc);
		}
	}

	/**
	 * Actually disconnect from the remote host. This is called by the run(IProgressMonitor monitor) method.
	 * <p>
	 * You DO NOT OVERRIDE THIS. Rather, this calls connect(IProgressMonitor) in your
	 * IConnectorService class that is returned from getConnectorService(). That is where your code to disconnect should go!
	 * <p>
	 * Your disconnect method in your IConnectorService class must follow these IRunnableWithProgress rules:
	 * <ul>
	 *   <li>if the user cancels (monitor.isCanceled()), throw new InterruptedException()
	 *   <li>if the host is unknown, throw new java.lang.reflect.InvocationTargetException(new java.net.UnknownHostException));
	 *   <li>if something else bad happens, throw new java.lang.reflect.InvocationTargetException(exc);
	 *       - well, actually you can throw anything and we'll wrap it here in an InvocationTargetException
	 *   <li>do not worry about calling monitor.done() ... caller will do that!
	 * </ul>
	 */
	protected void internalDisconnect(IProgressMonitor monitor)
	throws InvocationTargetException, InterruptedException
	{
		try
		{
			getConnectorService().disconnect(monitor);
		}
		catch (InterruptedException exc) {
			throw exc;
		}
		catch (InvocationTargetException exc) {
			throw exc;
		}
		catch(Exception exc) {
			throw new InvocationTargetException(exc);
		}
	}

	/**
	 * Resolve an <i>absolute</i> filter string. This is only applicable if the subsystem
	 *  factory reports true for {@link org.eclipse.rse.core.subsystems.SubSystemConfiguration#supportsFilters()},
	 *  which is the default. Otherwise, {@link org.eclipse.rse.core.subsystems.SubSystem#getChildren()}
	 *  is called when the subsystem itself is expanded.
	 * <p>
	 * When a user <u>expands a filter</u> this method is invoked for each filter string and the
	 *  results are concatenated and displayed to the user. You can affect the post-concatenated
	 *  result by overriding {@link #sortResolvedFilterStringObjects(Object[])} if you desire to
	 *  sort the result, say, or pick our redundancies.
	 * <p>
	 * The resulting objects are displayed in the tree in the Remote System {@link org.eclipse.rse.internal.ui.view.SystemView view}.
	 * There are <u>two requirements</u> on the returned objects:</p>
	 * <ol>
	 *   <li>They must implement {@link org.eclipse.core.runtime.IAdaptable}.
	 *   <li>Their must be an RSE {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter remote-adapter} registered
	 *        for the object's class or interface type. Further, if this subsystem is {@link org.eclipse.rse.core.subsystems.SubSystem#isHidden() visible}
	 *        in the RSE, which is the default, then there must also be an RSE {@link org.eclipse.rse.ui.view.ISystemViewElementAdapter GUI-adapter} registered
	 *        with the platform. The base class implementation of this interface is {@link org.eclipse.rse.ui.view.AbstractSystemViewAdapter}.
	 * </ol>
	 * <p>A good place to start with your remote-resource classes to subclasss {@link org.eclipse.rse.core.subsystems.AbstractResource}, as it
	 * already implements IAdaptable, and maintains a reference to this owning subsystem, which helps when
	 * implementing the {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter remote-adapter}.
	 * <p>
	 * Be sure to register your adapter factory in your plugin's startup method.
	 * <p>
	 * Actually resolve an absolute filter string. This is called by the
	 *  run(IProgressMonitor monitor) method, which in turn is called by resolveFilterString.
	 * <p>
	 * As per IRunnableWithProgress rules:
	 * <ul>
	 *   <li>if the user cancels (monitor.isCanceled()), throw new InterruptedException()
	 *   <li>if something else bad happens, throw new java.lang.reflect.InvocationTargetException(exc);
	 *   <li>do not worry about calling monitor.done() ... caller will do that!
	 * </ul>
	 * YOU MUST OVERRIDE THIS IF YOU SUPPORT FILTERS!
	 */
	protected Object[] internalResolveFilterString(String filterString, IProgressMonitor monitor)
	throws java.lang.reflect.InvocationTargetException,
	java.lang.InterruptedException
	{
		return null;
	}
	/**
	 * Resolve multiple absolute filter strings. This is only applicable if the subsystem
	 *  factory reports true for supportsFilters().
	 * <p>
	 * This is the same as {@link #internalResolveFilterString(Object, String, IProgressMonitor)} but takes an array of
	 *  filter strings versus a single filter string.
	 * <p>
	 * The default implementation of this simply calls {@link #internalResolveFilterString(String, IProgressMonitor)}
	 *  once for each filter string, and concatenates the result. The method sortResolvedFilterStringObject
	 *  is called on the concatenated result, given subclasses an opportunity to sort the result.
	 * <p>
	 * After successful resolve, the sort method is called to sort the concatenated results before
	 *  returning them.
	 * @param filterStrings array of filter patterns for objects to return.
	 * @param monitor the progress monitor we are running under
	 * @return Array of objects that are the result of resolving all the filter strings
	 */
	public Object[] internalResolveFilterStrings(String[] filterStrings, IProgressMonitor monitor)
	throws java.lang.reflect.InvocationTargetException,
	java.lang.InterruptedException
	{
		Object[] children = null;
		Vector vChildren = new Vector();
		for (int idx=0; idx<filterStrings.length; idx++)
		{
			if (monitor != null)
			{
				monitor.setTaskName(getResolvingMessage(filterStrings[idx]));
			}
			children = internalResolveFilterString(filterStrings[idx], monitor);
			//monitor.worked(1);
			if (children != null)
				addResolvedFilterStringObjects(vChildren, children, filterStrings, idx);
		}
		int nbrChildren = vChildren.size();
		children = new Object[nbrChildren];
		for (int idx=0; idx<nbrChildren; idx++)
			children[idx] = vChildren.elementAt(idx);
		return children;
	}

	/**
	 * Overridable extension point for adding the results of a filter string
	 *  to the overall list of results.
	 * <p>
	 * Can be used to filter out redundant entries in the concatenated list, if this
	 *  is desired.
	 */
	protected void addResolvedFilterStringObjects(Vector allChildrenSoFar, Object[] childrenForThisFilterString,
			String[] allFilterStrings, int currFilterStringIndex)
	{
		for (int jdx = 0; jdx<childrenForThisFilterString.length; jdx++)
			allChildrenSoFar.addElement(childrenForThisFilterString[jdx]);
	}

	/**
	 * Resolve a <i>relative</i> filter string.
	 * <p>
	 * When a user <u>expands a remote resource</u> this method is invoked and the
	 *  results are potentially sorted and displayed to the user. You can affect the sorting
	 *  behaviour by overriding {@link #sortResolvedFilterStringObjects(Object[])} if you desire to
	 *  sort the result, say, or pick our redundancies.
	 * <p>
	 * The resulting objects are displayed in the tree in the Remote System {@link org.eclipse.rse.internal.ui.view.SystemView view}.
	 * There are <u>two requirements</u> on the returned objects:</p>
	 * <ol>
	 *   <li>They must implement {@link org.eclipse.core.runtime.IAdaptable}.
	 *   <li>Their must be an RSE {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter remote-adapter} registered
	 *        for the object's class or interface type. Further, if this subsystem is {@link org.eclipse.rse.core.subsystems.SubSystem#isHidden visible}
	 *        in the RSE, which is the default, then there must also be an RSE {@link org.eclipse.rse.ui.view.ISystemViewElementAdapter GUI-adapter} registered
	 *        with the platform. The base class implementation of this interface is {@link org.eclipse.rse.ui.view.AbstractSystemViewAdapter}.
	 * </ol>
	 * <p>A good place to start with your remote-resource classes to subclasss {@link org.eclipse.rse.core.subsystems.AbstractResource}, as it
	 * already implements IAdaptable, and maintains a reference to this owning subsystem, which helps when
	 * implementing the {@link org.eclipse.rse.ui.view.ISystemRemoteElementAdapter remote-adapter}.
	 * <p>
	 * Be sure to register your adapter factory in your plugin's startup method.
	 * <p>
	 * This is called by the run(IProgressMonitor monitor) method, which in turn is called by resolveFilterString.
	 * <p>
	 * As per IRunnableWithProgress rules:
	 * <ul>
	 *   <li>if the user cancels (monitor.isCanceled()), throw new InterruptedException()
	 *   <li>if something else bad happens, throw new java.lang.reflect.InvocationTargetException(exc);
	 *   <li>do not worry about calling monitor.done() ... caller will do that!
	 * </ul>
	 * YOU MUST OVERRIDE THIS IF YOU SUPPORT FILTERS!
	 */
	protected Object[] internalResolveFilterString(Object parent, String filterString, IProgressMonitor monitor)
	throws java.lang.reflect.InvocationTargetException,
	java.lang.InterruptedException
	{
		return null;
	}
	/**
	 * Called by resolveFilterString when given null for the filter string, meaning we defer
	 *  getting a filter string until later, where we query it from the parent. In this case
	 *  we need the first filter string for the progress monitor msg.<br>
	 * Override if needed. By default we return "*";
	 */
	protected String getFirstParentFilterString(Object parent)
	{
		return "*"; //$NON-NLS-1$
	}


	/**
	 * Actually get a remote property. This is called by the
	 *  run(IProgressMonitor monitor) method, which in turn is called by getProperty(...).
	 * <p>
	 * As per IRunnableWithProgress rules:
	 * <ul>
	 *   <li>if the user cancels (monitor.isCanceled()), throw new InterruptedException()
	 *   <li>if something else bad happens, throw new java.lang.reflect.InvocationTargetException(exc);
	 *   <li>do not worry about calling monitor.done() ... caller will do that!
	 * </ul>
	 * YOU MUST OVERRIDE THIS IF YOU SUPPORT PROPERTIES!
	 */
	protected String internalGetProperty(Object subject, String key, IProgressMonitor monitor)
	throws java.lang.reflect.InvocationTargetException,
	java.lang.InterruptedException
	{
		return null;
	}

	/**
	 * Actually set a remote property. This is called by the
	 *  run(IProgressMonitor monitor) method, which in turn is called by setProperty(...).
	 * <p>
	 * As per IRunnableWithProgress rules:
	 * <ul>
	 *   <li>if the user cancels (monitor.isCanceled()), throw new InterruptedException()
	 *   <li>if something else bad happens, throw new java.lang.reflect.InvocationTargetException(exc);
	 *   <li>do not worry about calling monitor.done() ... caller will do that!
	 * </ul>
	 * YOU MUST OVERRIDE THIS IF YOU SUPPORT PROPERTIES!
	 */
	protected Object internalSetProperty(Object subject, String key, String value, IProgressMonitor monitor)
	throws java.lang.reflect.InvocationTargetException,
	java.lang.InterruptedException
	{
		return null;
	}

	/**
	 * Actually get multiple remote properties. This is called by the
	 *  run(IProgressMonitor monitor) method, which in turn is called by getProperties(...).
	 * <p>
	 * As per IRunnableWithProgress rules:
	 * <ul>
	 *   <li>if the user cancels (monitor.isCanceled()), throw new InterruptedException()
	 *   <li>if something else bad happens, throw new java.lang.reflect.InvocationTargetException(exc);
	 *   <li>do not worry about calling monitor.done() ... caller will do that!
	 * </ul>
	 * YOU MUST OVERRIDE THIS IF YOU SUPPORT PROPERTIES!
	 */
	protected String[] internalGetProperties(Object subject, String[] keys, IProgressMonitor monitor)
	throws java.lang.reflect.InvocationTargetException,
	java.lang.InterruptedException
	{
		return null;
	}

	/**
	 * Actually set multiple remote properties. This is called by the
	 *  run(IProgressMonitor monitor) method, which in turn is called by setProperties(...).
	 * <p>
	 * As per IRunnableWithProgress rules:
	 * <ul>
	 *   <li>if the user cancels (monitor.isCanceled()), throw new InterruptedException()
	 *   <li>if something else bad happens, throw new java.lang.reflect.InvocationTargetException(exc);
	 *   <li>do not worry about calling monitor.done() ... caller will do that!
	 * </ul>
	 * YOU MUST OVERRIDE THIS IF YOU SUPPORT PROPERTIES!
	 */
	protected Object internalSetProperties(Object subject, String[] keys, String[] values, IProgressMonitor monitor)
	throws java.lang.reflect.InvocationTargetException,
	java.lang.InterruptedException
	{
		return null;
	}

	/**
	 * Get the progress monitor dialog for this operation. We try to
	 *  use one for all phases of a single operation, such as connecting
	 *  and resolving.
	 * @deprecated this is scheduled to be removed since we want to
	 *     avoid UI components in SubSystem.
	 */
	protected IRunnableContext getRunnableContext(/*Shell rshell*/) {
		if (Display.getCurrent() == null) {
			return new NullRunnableContext();
		}
		// for wizards and dialogs use the specified context that was placed in the registry
		IRunnableContext irc = RSEUIPlugin.getTheSystemRegistryUI().getRunnableContext();
		if (irc != null) {
			SystemBasePlugin.logInfo("Got runnable context from system registry"); //$NON-NLS-1$
			return irc;
		} else {
			// for other cases, use statusbar
			IWorkbenchWindow win = SystemBasePlugin.getActiveWorkbenchWindow();
			if (win != null) {
				Shell winShell = SystemBasePlugin.getActiveWorkbenchShell();
				if (winShell != null && !winShell.isDisposed() && winShell.isVisible()) {
					SystemBasePlugin.logInfo("Using active workbench window as runnable context"); //$NON-NLS-1$
					shell = winShell;
					return win;
					//dwd				} else {
					//dwd					win = null;
				}
			}
			//dwd			if (shell == null || shell.isDisposed() || !shell.isVisible()) {
			//dwd				SystemBasePlugin.logInfo("Using progress monitor dialog with given shell as parent");
			//dwd				shell = rshell;
			//dwd			}
			//dwd			IRunnableContext dlg = new ProgressMonitorDialog(rshell);
			IRunnableContext dlg = new ProgressMonitorDialog(shell);
			return dlg;
		}
	}

	/**
	 * Return the shell for the current operation
	 */
	protected Shell getShell()
	{
		return shell;
	}

	/**
	 * <i><b>Private</b>. Do not override.</i>
	 * @generated This field/method will be replaced during code generation
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * <i><b>Private</b>. Do not override.</i>
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setName(String newName)
	{
		String oldName = _name;
		if (oldName == null || !oldName.equals(newName))
		{
			_name = newName;
			setDirty(true);
		}
	}




	/**
	 * <i><b>Private</b>. Do not override.</i>
	 * @generated This field/method will be replaced during code generation
	 * Ties this subsystem to its owning subsystemconfiguration, via the
	 * id key string of the configuration.
	 */
	public String getConfigurationId()
	{
		return _subsystemConfigurationId;
	}

	/**
	 * <i><b>Private</b>. Do not override.</i>
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setConfigurationId(String newConfigurationId)
	{
		String oldFactoryId = _subsystemConfigurationId;
		if (oldFactoryId == null || !oldFactoryId.equals(newConfigurationId))
		{
			_subsystemConfigurationId = newConfigurationId;
			setDirty(true);
		}
	}

	/**
	 * <i><b>Private</b>. Do not override.</i>
	 * @generated This field/method will be replaced during code generation
	 */
	public boolean isHidden()
	{
		return _hidden;
	}

	/**
	 * <i><b>Private</b>. Do not override.</i>
	 * @generated This field/method will be replaced during code generation.
	 */
	public void setHidden(boolean newHidden)
	{
		boolean oldHidden = _hidden;
		if (oldHidden != newHidden)
		{
			_hidden = newHidden;
			setDirty(true);
		}
	}

	/**
	 * <i><b>Private</b>. Do not override.</i>
	 * @generated This field/method will be replaced during code generation
	 */
	public ISystemFilterPoolReferenceManager getFilterPoolReferenceManager()
	{
		return filterPoolReferenceManager;
	}

	public void setFilterPoolReferenceManager(ISystemFilterPoolReferenceManager newFilterPoolReferenceManager)
	{
		filterPoolReferenceManager = newFilterPoolReferenceManager;
		return;
	}




	public boolean isPrimarySubSystem()
	{
		return false;
	}

	/*
	 * Returns the first subsystem associated with the connection
	 */
	public ISubSystem getPrimarySubSystem()
	{
		ISubSystem firstSS = null;
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		ISubSystem[] sses = registry.getSubSystems(getHost());
		for (int i = 0; i < sses.length; i++)
		{
			ISubSystem ss = sses[i];
			if (ss.getConnectorService() == getConnectorService())
			{
				if (firstSS == null)
				{
					firstSS = ss;
				}
				if (ss.isPrimarySubSystem())
				{
					return ss;
				}
			}
		}
		if (firstSS == null)
		{
			firstSS = this;
		}
		return firstSS;
	}



	public Object getTargetForFilter(ISystemFilterReference filterRef)
	{
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#isConnectionError()
	 */
	public boolean isConnectionError()
	{
		return _connectionError;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.SubSystem#setConnectionError(boolean)
	 */
	public void setConnectionError(boolean error)
	{
		_connectionError = error;
	}

	public Object[] getTargetsForFilter(ISystemFilterReference filterRef)
	{
		return null;
	}

	/**
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	public boolean contains(ISchedulingRule rule)
	{
		if (this.equals(rule)) return true;
		if (rule instanceof IResource) return true;
		else return false;
	}

	/**
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	public boolean isConflicting(ISchedulingRule rule)
	{
		if (this == rule) return true;
		else return false;
	}

	public boolean commit()
	{
		ISystemProfile profile = getSystemProfile();
		if (PlatformUI.getWorkbench().isClosing()){
			// commit job may fail due to shutdown
			// force this by directly calling with true
			IStatus status = SystemProfileManager.getDefault().commitSystemProfile(profile, true);
			return status.isOK();
		}
		else {
			boolean result = profile.commit();
			return result;
		}
	}

	public IRSEPersistableContainer getPersistableParent() {
		return _host;
	}

	public IRSEPersistableContainer[] getPersistableChildren() {
		List children = new ArrayList(10);
		ISystemFilterPoolReferenceManager manager = getSystemFilterPoolReferenceManager();
		if (manager != null) {
			children.addAll(Arrays.asList(manager.getSystemFilterPoolReferences()));
		}
		children.addAll(Arrays.asList(getPropertySets()));
		IRSEPersistableContainer[] result = new IRSEPersistableContainer[children.size()];
		children.toArray(result);
		return result;
	}

	/* Service Subsystem support */

	/**
	 * Perform the subsystem specific processing required to complete a
	 * subsystem configuration switch for a service subsystem. The subsystem
	 * will typically query this configuration for interesting properties or
	 * policies. It should also reset any state to a fresh start. This supplied
	 * implementation does nothing. Subclasses may override if they implement a
	 * service subsystem.
	 *
	 * @param newConfiguration the configuration this subsystem should use from
	 * 		this point.
	 * @since 3.0
	 */
	protected void internalSwitchSubSystemConfiguration(ISubSystemConfiguration newConfiguration) {
	}

	/**
	 * Determine if a service subsystem is capable of switching to this new
	 * configuration. This is usually a test of this configuration's type
	 * against the type expected by this subsystem. This supplied implementation
	 * returns false. Subclasses should override if they implement a service
	 * subsystem.
	 *
	 * @param configuration the configuration to which this subsystem may switch
	 * @return true if this subsystem is capable of switching to this
	 * 	configuration, false otherwise. This implementation returns false.
	 * @see ISubSystem#canSwitchTo(ISubSystemConfiguration)
	 * @since 3.0
	 */
	public boolean canSwitchTo(ISubSystemConfiguration configuration) {
		return false;
	}

	/**
	 * Switch to use another subsystem configuration. This default
	 * implementation will test if the subsystem is a service subsystem and if
	 * the subsystem is compatible with the suggested configuration. If it is
	 * the switch will be performed and internalSwitchSubSystemConfiguration
	 * will be called.
	 *
	 * @see ISubSystem#switchServiceFactory(ISubSystemConfiguration)
	 * @see #internalSwitchSubSystemConfiguration(ISubSystemConfiguration)
	 * @since 3.0
	 */
	public void switchServiceFactory(final ISubSystemConfiguration config) {
		if (config != getSubSystemConfiguration() && canSwitchTo(config)) {
			// define the operation to be executed
			ISystemProfileOperation op = new ISystemProfileOperation() {
				public IStatus run() {
					doSwitchServiceConfiguration(config);
					return Status.OK_STATUS;
				}
			};
			// execute the operation in a commit guard
			SystemProfileManager.run(op);
		}
	}

	/**
	 * Return the service type for this subsystem.
	 *
	 * @return the default implementation returns null. Subclasses that
	 * 	implement service subsystems should return a type as specified in the
	 * 	interface.
	 * @see ISubSystem#getServiceType()
	 * @since 3.0
	 */
	public Class getServiceType() {
		return null;
	}

	/**
	 * Actually perform the switch inside the commit guard
	 * @param newConfig
	 */
	private void doSwitchServiceConfiguration(ISubSystemConfiguration newConfig) {
		
		// only disconnect the connector service if there are no only services that use it
		IConnectorService oldConnectorService = getConnectorService();
		if (oldConnectorService.isConnected()){
			ISubSystem[] associatedSubSystems = oldConnectorService.getSubSystems();
			if (associatedSubSystems != null && associatedSubSystems.length > 1){
				// at least one other subsystem still using this connector service so don't disconnect
				// instead uninitialize this subsystem
				uninitializeSubSystem(new NullProgressMonitor());
			}
			else {
				// no more associated subsystems
				try {
					disconnect();
				} catch (Exception e) {
				}
			}
		}
		
		
		
		IHost host = getHost();
		// remove the old references and store them for later use
		ISubSystemConfiguration oldConfig = getSubSystemConfiguration();
		if (oldConfig.supportsFilters()) {
			ISystemFilterPoolReferenceManager fprm = getSystemFilterPoolReferenceManager();
			List poolReferences = Arrays.asList(fprm.getSystemFilterPoolReferences());
			poolReferencesMap.put(oldConfig, poolReferences);
			for (Iterator z = poolReferences.iterator(); z.hasNext();) {
				ISystemFilterPoolReference poolReference = (ISystemFilterPoolReference) z.next();
				fprm.removeSystemFilterPoolReference(poolReference, true);
			}
			fprm.setSystemFilterPoolManagerProvider(null);
		}
		setSubSystemConfiguration(newConfig);
		setConfigurationId(newConfig.getId());
		setName(newConfig.getName());
		// add the new pools to the manager
		if (newConfig.supportsFilters()) {
			ISystemFilterPoolReferenceManager fprm = getSystemFilterPoolReferenceManager();
			fprm.setSystemFilterPoolManagerProvider(newConfig);
			List poolReferences = (List) poolReferencesMap.get(newConfig);
			if (poolReferences == null) {
				// create default pools
				ISystemProfile profile = host.getSystemProfile();
				ISystemFilterPoolManager poolManager = newConfig.getFilterPoolManager(profile, true); // get and initialize the new filter pool manager
				int eventType = ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED;
				int resourceType = ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_CONNECTION;
				ISystemModelChangeEvent event = new SystemModelChangeEvent(eventType, resourceType, host);
				RSECorePlugin.getTheSystemRegistry().fireEvent(event); // signal a model change event as well
				// create references to those pools
				fprm.setDefaultSystemFilterPoolManager(poolManager);
				ISystemFilterPool[] pools = poolManager.getSystemFilterPools();
				for (int i = 0; i < pools.length; i++) {
					ISystemFilterPool pool = pools[i];
					fprm.addReferenceToSystemFilterPool(pool);
				}
			} else { // use the found pools
				for (Iterator z = poolReferences.iterator(); z.hasNext();) {
					ISystemFilterPoolReference poolReference = (ISystemFilterPoolReference) z.next();
					fprm.addSystemFilterPoolReference(poolReference);
				}
			}
			filterEventFilterPoolReferencesReset(); // signal a resource change event
		}

		// switch the connector service

		
		oldConnectorService.deregisterSubSystem(this);
		IConnectorService newConnectorService = newConfig.getConnectorService(host);
		setConnectorService(newConnectorService);
		oldConnectorService.commit();
		newConnectorService.commit();

		// call the subsystem specfic switching support
		internalSwitchSubSystemConfiguration(newConfig);
	
		if (newConnectorService.isConnected()){
			// make sure that the new service is initialized properly
			// since we're already connected and normally it's done as part of connect
			try {
				initializeSubSystem(new NullProgressMonitor());
			}
			catch (SystemMessageException e){
				SystemBasePlugin.logError(e.getMessage(), e);
			}
		}
		

		// commit the subsystem
		setDirty(true);
		commit();
	}

}