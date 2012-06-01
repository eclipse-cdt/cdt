/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Abeer Bagul (Tensilica) - Updated error message (Bug 339048)
 *     Jason Litton (Sage Electronic Engineering, LLC) - Added support for dynamic tracing option (Bug 379169)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GdbPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.dsf.gdb"; //$NON-NLS-1$

	// The shared instance
	private static GdbPlugin plugin;
	
    private static BundleContext fgBundleContext;
    
	/**
	 * The constructor
	 */
	public GdbPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
        fgBundleContext = context;
		super.start(context);
		plugin = this;
		
        new GdbDebugOptions(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
	    shutdownActiveLaunches();
		plugin = null;
		super.stop(context);
        fgBundleContext = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GdbPlugin getDefault() {
		return plugin;
	}

    public static BundleContext getBundleContext() {
        return fgBundleContext;
    }
    
    /** 
     * Shuts down any active launches.  We must shutdown any active sessions 
     * and services associated with this plugin before this plugin is stopped.
     * Any attempts to use the plugins {@link BundleContext} after the plugin
     * is shut down will result in exceptions. 
     */
    private void shutdownActiveLaunches() {
    	
    	DebugPlugin debugPlugin = DebugPlugin.getDefault();
    	if (debugPlugin == null) {
			// Simple junit tests don't cause the platform debug plugins to load
    		return;
    	}

        for (ILaunch launch : debugPlugin.getLaunchManager().getLaunches()) {
            if (launch instanceof GdbLaunch && ((GdbLaunch)launch).getSession().isActive()) {
                final GdbLaunch gdbLaunch = (GdbLaunch)launch;
                
                Query<Object> launchShutdownQuery = new Query<Object>() {
                    @Override
                    protected void execute(DataRequestMonitor<Object> rm) {
                        gdbLaunch.shutdownSession(rm);
                    }
                };
                
                try {
                    gdbLaunch.getSession().getExecutor().execute(launchShutdownQuery);
                } catch (RejectedExecutionException e) {
                    // We can get this exception if the session is shutdown concurrently
                    // to this method running.
                    break;
                }

                // The Query.get() method is a synchronous call which blocks until the 
                // query completes.  
                try {
                    launchShutdownQuery.get();
                } catch (InterruptedException e) { 
                    getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "InterruptedException while shutting down launch " + gdbLaunch, e.getCause())); //$NON-NLS-1$
                } catch (ExecutionException e) {
                    getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Exception while shutting down launch " + gdbLaunch, e.getCause())); //$NON-NLS-1$
                }
            }
        }
    }

    public static String getDebugTime() {
        StringBuilder traceBuilder = new StringBuilder();
        
        // Record the time
        long time = System.currentTimeMillis();
        long seconds = (time / 1000) % 1000;
        if (seconds < 100) traceBuilder.append('0');
        if (seconds < 10) traceBuilder.append('0');
        traceBuilder.append(seconds);
        traceBuilder.append(',');
        long millis = time % 1000;
        if (millis < 100) traceBuilder.append('0');
        if (millis < 10) traceBuilder.append('0');
        traceBuilder.append(millis);
        return traceBuilder.toString();
    }

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message
	 *            the error message to log
	 */
	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 *            the exception to be logged
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}
}
