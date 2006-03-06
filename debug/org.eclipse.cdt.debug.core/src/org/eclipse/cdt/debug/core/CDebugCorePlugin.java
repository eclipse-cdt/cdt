/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.internal.core.DebugConfiguration;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.core.ListenerList;
import org.eclipse.cdt.debug.internal.core.SessionManager;
import org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CommonSourceLookupDirector;
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

	private HashMap fDebugConfigurations;
	
	private HashSet fActiveDebugConfigurations;

	/**
	 * Breakpoint listener list.
	 */
	private ListenerList fBreakpointListeners;
	
	/**
	 * Dummy source lookup director needed to manage common source containers.
	 */
	private CommonSourceLookupDirector fCommonSourceLookupDirector;

	private SessionManager fSessionManager = null;

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
		getDefault().getLog().log( new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), INTERNAL_ERROR, message, null ) );
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

	private void initializeActiveDebugConfigurations() {
		fActiveDebugConfigurations = new HashSet( getDebugConfigurations().length );
		fActiveDebugConfigurations.addAll( fDebugConfigurations.keySet() );
		String[] filteredTypes = CDebugCorePlugin.getDefault().getPluginPreferences().getString( ICDebugConstants.PREF_FILTERED_DEBUGGERS ).split( "\\," ); //$NON-NLS-1$
		fActiveDebugConfigurations.removeAll( Arrays.asList( filteredTypes ) );
	}

	public ICDebugConfiguration[] getDebugConfigurations() {
		if ( fDebugConfigurations == null ) {
			initializeDebugConfiguration();
		}
		return (ICDebugConfiguration[])fDebugConfigurations.values().toArray( new ICDebugConfiguration[0] );
	}

	public ICDebugConfiguration[] getActiveDebugConfigurations() {
		if ( fDebugConfigurations == null ) {
			initializeDebugConfiguration();
		}
		if ( fActiveDebugConfigurations == null ) {
			initializeActiveDebugConfigurations();
		}
		ArrayList list = new ArrayList( fActiveDebugConfigurations.size() );
		Iterator it = fActiveDebugConfigurations.iterator();
		while( it.hasNext() ) {
			Object o = fDebugConfigurations.get( it.next() );
			if ( o != null )
				list.add( o );
		}
		return (ICDebugConfiguration[])list.toArray( new ICDebugConfiguration[list.size()] );
	}

	public ICDebugConfiguration[] getDefaultActiveDebugConfigurations() {
		List filtered = Arrays.asList( CDebugCorePlugin.getDefault().getPluginPreferences().getDefaultString( ICDebugConstants.PREF_FILTERED_DEBUGGERS ).split( "\\," ) ); //$NON-NLS-1$
		HashMap all = (HashMap)fDebugConfigurations.clone();
		all.keySet().removeAll( filtered );
		return (ICDebugConfiguration[])all.values().toArray( new ICDebugConfiguration[all.size()] ); 
	}

	public void saveFilteredDebugConfigurations( ICDebugConfiguration[] configurations ) {
		disposeActiveDebugConfigurations();
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < configurations.length; ++i ) {
			sb.append( configurations[i].getID() ).append( ',' );
		}
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_FILTERED_DEBUGGERS, sb.toString() );
		CDebugCorePlugin.getDefault().savePluginPreferences();
	}

	public void saveDefaultDebugConfiguration( String id ) {
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_DEFAULT_DEBUGGER_TYPE, ( id != null ) ? id : "" ); //$NON-NLS-1$
	}

	public ICDebugConfiguration getDefaultDebugConfiguration() {
		ICDebugConfiguration result = null;
		try {
			result = getDebugConfiguration( CDebugCorePlugin.getDefault().getPluginPreferences().getString( ICDebugConstants.PREF_DEFAULT_DEBUGGER_TYPE ) );
		}
		catch( CoreException e ) {
		}
		return result;
	}

	public ICDebugConfiguration getDefaultDefaultDebugConfiguration() {
		ICDebugConfiguration result = null;
		try {
			result = getDebugConfiguration( CDebugCorePlugin.getDefault().getPluginPreferences().getDefaultString( ICDebugConstants.PREF_DEFAULT_DEBUGGER_TYPE ) );
		}
		catch( CoreException e ) {
		}
		if ( result == null ) {
		}
		return result;
	}

	public boolean isDefaultDebugConfiguration( String id ) {
		return id.compareTo( CDebugCorePlugin.getDefault().getPluginPreferences().getString( ICDebugConstants.PREF_DEFAULT_DEBUGGER_TYPE ) ) == 0;
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
		initializeCommonSourceLookupDirector();
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
		disposeCommonSourceLookupDirector();
		disposeDebugConfigurations();
		super.stop( context );
	}

	private void initializeCommonSourceLookupDirector() {
		if ( fCommonSourceLookupDirector == null ) {
			fCommonSourceLookupDirector = new CommonSourceLookupDirector();
			String newMemento = CDebugCorePlugin.getDefault().getPluginPreferences().getString( ICDebugInternalConstants.PREF_COMMON_SOURCE_CONTAINERS );
			if ( newMemento.length() == 0 ) {
				// Convert source locations to source containers
				convertSourceLocations( fCommonSourceLookupDirector );
			}
			else {
				try {
					fCommonSourceLookupDirector.initializeFromMemento( newMemento );
				}
				catch( CoreException e ) {
					log( e.getStatus() );
				}
			}
		}
	}

	private void disposeCommonSourceLookupDirector() {
		if ( fCommonSourceLookupDirector != null )
			fCommonSourceLookupDirector.dispose();
	}

	public CSourceLookupDirector getCommonSourceLookupDirector() {
		return fCommonSourceLookupDirector;
	}

	private void convertSourceLocations( CommonSourceLookupDirector director ) {
		director.setSourceContainers( SourceUtils.convertSourceLocations( getCommonSourceLocations() ) );
	}

	private void disposeActiveDebugConfigurations() {
		if ( fActiveDebugConfigurations != null ) {
			fActiveDebugConfigurations.clear();
			fActiveDebugConfigurations = null;
		}
	}

	private void disposeDebugConfigurations() {
		disposeActiveDebugConfigurations();
		if ( fDebugConfigurations != null ) {
			fDebugConfigurations.clear();
			fDebugConfigurations = null;
		}
	}
}
