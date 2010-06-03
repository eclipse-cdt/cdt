/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GdbPlugin extends Plugin {

    // Debugging flag
    public static boolean DEBUG = false;

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
		
        DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.cdt.dsf.gdb/debug"));  //$NON-NLS-1$//$NON-NLS-2$
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
        for (ILaunch launch : DebugPlugin.getDefault().getLaunchManager().getLaunches()) {
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
                    getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "InterruptedException while shutting down PDA debugger launch " + gdbLaunch, e.getCause())); //$NON-NLS-1$
                } catch (ExecutionException e) {
                    getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Exception while shutting down PDA debugger launch " + gdbLaunch, e.getCause())); //$NON-NLS-1$
                }
            }
        }
    }

    public static void debug(String message) {
        if (DEBUG) {
			while (message.length() > 100) {
				String partial = message.substring(0, 100); 
				message = message.substring(100);
				System.out.println(partial + "\\"); //$NON-NLS-1$
			}
			System.out.print(message);
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

}
