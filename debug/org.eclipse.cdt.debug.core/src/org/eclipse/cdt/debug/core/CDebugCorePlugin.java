/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core;

import org.eclipse.cdt.debug.internal.core.CDebuggerManager;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

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

	private CDebuggerManager debuggerManager;

	/**
	 * The constructor.
	 */
	public CDebugCorePlugin( IPluginDescriptor descriptor )
	{
		super(descriptor);
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
	
	public ICDebuggerManager getDebuggerManager() {
		if ( debuggerManager == null ) {
			debuggerManager = new CDebuggerManager();
		} 
		return debuggerManager;
	}
}
