/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.core;

import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.internal.core.DebugConfiguration;
import org.eclipse.cdt.debug.internal.core.ListenerList;
import org.eclipse.cdt.debug.internal.core.SessionManager;
import org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint;
import org.eclipse.cdt.debug.internal.core.sourcelookup.SourceUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.osgi.framework.BundleContext;

/**
 * The plugin class for C/C++ debug core.
 */
public class CDebugCorePlugin extends Plugin {

	/**
	 * The plug-in identifier (value <code>"org.eclipse.cdt.debug.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.core" ; //$NON-NLS-1$

	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 1000;

	/**
	 * The shared instance.
	 */
	private static CDebugCorePlugin plugin;

	private static ResourceBundle fgResourceBundle;

	private HashMap fDebugConfigurations;

	/**
	 * Breakpoint listener list.
	 */
	private ListenerList fBreakpointListeners;

	private SessionManager fSessionManager = null;
	static {
		try {
			fgResourceBundle = ResourceBundle.getBundle( "org.eclipse.cdt.debug.internal.core.CDebugCorePluginResources" ); //$NON-NLS-1$
		}
		catch( MissingResourceException x ) {
			fgResourceBundle = null;
		}
	}

	/**
	 * The constructor.
	 */
	public CDebugCorePlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static CDebugCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 * 
	 * @return the workspace instance
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 * 
	 * @return the unique identifier of this plugin
	 */
	public static String getUniqueIdentifier() {
		if ( getDefault() == null ) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	public static String getResourceString( String key ) {
		try {
			return fgResourceBundle.getString( key );
		}
		catch( MissingResourceException e ) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch( NullPointerException e ) {
			return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}



	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}

	/**
	 * Logs the specified throwable with this plug-in's log.
	 * 
	 * @param t throwable to log 
	 */
	public static void log( Throwable t ) {
		Throwable top = t;
		if ( t instanceof DebugException ) {
			DebugException de = (DebugException)t;
			IStatus status = de.getStatus();
			if ( status.getException() != null ) {
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
	public static void log( IStatus status ) {
		getDefault().getLog().log( status );
	}

	/**
	 * Logs the specified message with this plug-in's log.
	 * 
	 * @param status status to log
	 */
	public static void log( String message ) {
		getDefault().getLog().log( new Status( IStatus.ERROR, CDebugModel.getPluginIdentifier(), INTERNAL_ERROR, message, null ) );
	}

	private void initializeDebugConfiguration() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint( getUniqueIdentifier(), "CDebugger" ); //$NON-NLS-1$
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		fDebugConfigurations = new HashMap( infos.length );
		for( int i = 0; i < infos.length; i++ ) {
			IConfigurationElement configurationElement = infos[i];
			DebugConfiguration configType = new DebugConfiguration( configurationElement );
			fDebugConfigurations.put( configType.getID(), configType );
		}
	}

	public ICDebugConfiguration[] getDebugConfigurations() {
		if ( fDebugConfigurations == null ) {
			initializeDebugConfiguration();
		}
		return (ICDebugConfiguration[])fDebugConfigurations.values().toArray( new ICDebugConfiguration[0] );
	}

	public ICDebugConfiguration getDebugConfiguration( String id ) throws CoreException {
		if ( fDebugConfigurations == null ) {
			initializeDebugConfiguration();
		}
		ICDebugConfiguration dbgCfg = (ICDebugConfiguration)fDebugConfigurations.get( id );
		if ( dbgCfg == null ) {
			IStatus status = new Status( IStatus.ERROR, getUniqueIdentifier(), 100, DebugCoreMessages.getString( "CDebugCorePlugin.0" ), null ); //$NON-NLS-1$
			throw new CoreException( status );
		}
		return dbgCfg;
	}

	protected void resetBreakpointsInstallCount() {
		IBreakpointManager bm = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = bm.getBreakpoints( getUniqueIdentifier() );
		for( int i = 0; i < breakpoints.length; ++i ) {
			if ( breakpoints[i] instanceof CBreakpoint ) {
				try {
					((CBreakpoint)breakpoints[i]).resetInstallCount();
				}
				catch( CoreException e ) {
					log( e.getStatus() );
				}
			}
		}
	}

	protected SessionManager getSessionManager() {
		return fSessionManager;
	}

	protected void setSessionManager( SessionManager sm ) {
		if ( fSessionManager != null )
			fSessionManager.dispose();
		fSessionManager = sm;
	}

	public void saveCommonSourceLocations( ICSourceLocation[] locations ) {
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_SOURCE_LOCATIONS, SourceUtils.getCommonSourceLocationsMemento( locations ) );
	}

	public ICSourceLocation[] getCommonSourceLocations() {
		return SourceUtils.getCommonSourceLocationsFromMemento( CDebugCorePlugin.getDefault().getPluginPreferences().getString( ICDebugConstants.PREF_SOURCE_LOCATIONS ) );
	}

	/**
	 * Adds the given breakpoint listener to the debug model.
	 * 
	 * @param listener breakpoint listener
	 */
	public void addCBreakpointListener( ICBreakpointListener listener ) {
		fBreakpointListeners.add( listener );
	}

	/**
	 * Removes the given breakpoint listener from the debug model.
	 * 
	 * @param listener breakpoint listener
	 */
	public void removeCBreakpointListener( ICBreakpointListener listener ) {
		fBreakpointListeners.remove( listener );
	}

	/**
	 * Returns the list of breakpoint listeners registered with this plugin.
	 *   
	 * @return the list of breakpoint listeners registered with this plugin
	 */
	public Object[] getCBreakpointListeners() {
		return fBreakpointListeners.getListeners();
	}

	private void createBreakpointListenersList() {
		fBreakpointListeners = new ListenerList( 1 );
	}

	private void disposeBreakpointListenersList() {
		fBreakpointListeners.removeAll();
		fBreakpointListeners = null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start( BundleContext context ) throws Exception {
		super.start( context );
		createBreakpointListenersList();
		resetBreakpointsInstallCount();
		setSessionManager( new SessionManager() );
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop( BundleContext context ) throws Exception {
		setSessionManager( null );
		disposeBreakpointListenersList();
		resetBreakpointsInstallCount();
		super.stop( context );
	}
}