/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.internal.core.DebugConfiguration;
import org.eclipse.cdt.debug.internal.core.SessionManager;
import org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint;
import org.eclipse.cdt.debug.internal.core.sourcelookup.SourceUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class CDebugCorePlugin extends Plugin
{
	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 1000;

	//The shared instance.
	private static CDebugCorePlugin plugin;
	private static ResourceBundle fgResourceBundle;

	private HashMap fDebugConfigurations;

	private IAsyncExecutor fAsyncExecutor = null;

	private SessionManager fSessionManager = null;
	
	static {
		try {
			fgResourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.debug.core.CDebugCorePluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			fgResourceBundle = null;
		}
	}
	/**
	 * The constructor.
	 */
	public CDebugCorePlugin( IPluginDescriptor descriptor )
	{
		super( descriptor );
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static CDebugCorePlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 * 
	 * @return the workspace instance
	 */
	public static IWorkspace getWorkspace()
	{
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 * 
	 * @return the unique identifier of this plugin
	 */
	public static String getUniqueIdentifier()
	{
		if ( getDefault() == null )
		{
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return "org.eclipse.cdt.debug.core"; //$NON-NLS-1$
		}
		return getDefault().getDescriptor().getUniqueIdentifier();
	}

	public static String getResourceString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NullPointerException e) {
			return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new String[] { arg });
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), args);
	}
	
	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}

	/**
	 * Logs the specified throwable with this plug-in's log.
	 * 
	 * @param t throwable to log 
	 */
	public static void log( Throwable t )
	{
		Throwable top = t;
		if ( t instanceof DebugException )
		{
			DebugException de = (DebugException)t;
			IStatus status = de.getStatus();
			if ( status.getException() != null )
			{
				top = status.getException();
			}
		}
		// this message is intentionally not internationalized, as an exception may
		// be due to the resource bundle itself
		log( new Status( IStatus.ERROR, getUniqueIdentifier(), INTERNAL_ERROR, "Internal error logged from CDI Debug: ", top ) ); //$NON-NLS-1$		
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status status to log
	 */
	public static void log( IStatus status )
	{
		getDefault().getLog().log( status );
	}
	
	/**
	 * Logs the specified message with this plug-in's log.
	 * 
	 * @param status status to log
	 */
	public static void log( String message )
	{
		getDefault().getLog().log( new Status( IStatus.ERROR, 
											   CDebugModel.getPluginIdentifier(),
											   INTERNAL_ERROR, 
											   message, 
											   null ) );
	}
	
	private void initializeDebugConfiguration() {
		IPluginDescriptor descriptor= getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint("CDebugger"); //$NON-NLS-1$
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
		fDebugConfigurations = new HashMap(infos.length);
		for (int i= 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			DebugConfiguration configType = new DebugConfiguration(configurationElement); 			
			fDebugConfigurations.put(configType.getID(), configType);
		}		
	}

	public ICDebugConfiguration[] getDebugConfigurations() {
		if (fDebugConfigurations == null) {
			initializeDebugConfiguration();
		}
		return (ICDebugConfiguration[]) fDebugConfigurations.values().toArray(new ICDebugConfiguration[0]);
	}
	
	public ICDebugConfiguration getDebugConfiguration(String id) throws CoreException {
		if (fDebugConfigurations == null) {
			initializeDebugConfiguration();
		}
		ICDebugConfiguration dbgCfg = (ICDebugConfiguration) fDebugConfigurations.get(id);
		if ( dbgCfg == null ) {
			IStatus status = new Status(IStatus.ERROR, getUniqueIdentifier(), 100, CDebugCorePlugin.getResourceString("core.CDebugCorePlugin.No_such_debugger"), null); //$NON-NLS-1$
			throw new CoreException(status);
		}
		return dbgCfg;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	public void shutdown() throws CoreException
	{
		setSessionManager( null );
		resetBreakpointsInstallCount();
		super.shutdown();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */
	public void startup() throws CoreException
	{
		super.startup();
		resetBreakpointsInstallCount();
		setSessionManager( new SessionManager() );
	}

	protected void resetBreakpointsInstallCount()
	{
		IBreakpointManager bm = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = bm.getBreakpoints( getUniqueIdentifier() );
		for ( int i = 0; i < breakpoints.length; ++i )
		{
			if ( breakpoints[i] instanceof CBreakpoint )
			{
				try
				{
					((CBreakpoint)breakpoints[i]).resetInstallCount();
				}
				catch( CoreException e )
				{
					log( e.getStatus() );
				}
			}
		}
	}
	
	public void setAsyncExecutor( IAsyncExecutor executor )
	{
		fAsyncExecutor = executor;
	}
	
	public void asyncExec( Runnable runnable )
	{
		if ( fAsyncExecutor != null )
			fAsyncExecutor.asyncExec( runnable );
	}

	protected SessionManager getSessionManager()
	{
		return fSessionManager;
	}

	protected void setSessionManager( SessionManager sm )
	{
		if ( fSessionManager != null )
			fSessionManager.dispose();
		fSessionManager = sm;
	}

	public void saveCommonSourceLocations( ICSourceLocation[] locations )
	{
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_SOURCE_LOCATIONS, SourceUtils.getCommonSourceLocationsMemento( locations ) );
	}

	public ICSourceLocation[] getCommonSourceLocations()
	{
		return SourceUtils.getCommonSourceLocationsFromMemento( CDebugCorePlugin.getDefault().getPluginPreferences().getString( ICDebugConstants.PREF_SOURCE_LOCATIONS ) );
	}
}
