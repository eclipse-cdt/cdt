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
package org.eclipse.cdt.dsf.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @since 1.0
 */
public class DsfPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.dsf"; //$NON-NLS-1$

	// The shared instance
	private static DsfPlugin fgPlugin;

    // BundleContext of this plugin
    private static BundleContext fgBundleContext; 

    // Debugging flag
    public static boolean DEBUG = false;

	/**
	 * The constructor
	 */
	public DsfPlugin() {
		fgPlugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
        fgBundleContext = context;
		super.start(context);
        DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.cdt.dsf/debug"));  //$NON-NLS-1$//$NON-NLS-2$
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
        fgBundleContext = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DsfPlugin getDefault() {
		return fgPlugin;
	}

    public static BundleContext getBundleContext() {
        return fgBundleContext;
    }

	/**
	 * Writes [message] to stdout, but only if the top level 'debug' tracing
	 * option for this plugin has been turned on
	 * 
	 * @param message
	 */
    public static void debug(String message) {
        if (DEBUG) {
            System.out.println(message);
        }
    }

	/**
	 * Returns a relative timestamp in the form "seconds,milliseconds". Each
	 * segment is zero-padded to three digits, ensuring a consistent length of
	 * seven characters. The timestamp has no absolute meaning. It is merely the
	 * elapsed time since January 1, 1970 UT truncated at 999 seconds. Do not
	 * use this for production code, especially for mathematically determining
	 * the relative time between two events, since the counter will flip to zero
	 * roughly every 16 minutes.
	 */
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
