/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * Plugin class.
 */
public class MIPlugin extends Plugin {
    
    public static final String PLUGIN_ID = "org.eclipse.dsdp.debug.gdb.core"; //$NON-NLS-1$

    // Debugging flag
    public static boolean DEBUG = false;

    //The shared instance.
	private static MIPlugin fgPlugin;

    private static BundleContext fgBundleContext; 
    
	/**
	 * The constructor.
	 */
	public MIPlugin() {
		fgPlugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
    public void start(BundleContext context) throws Exception {
        fgBundleContext = context;
		super.start(context);
        DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.dd.mi.core/debug"));  //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		super.stop(context);
		fgBundleContext = null;
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance.
	 */
	public static MIPlugin getDefault() {
		return fgPlugin;
	}

    public static BundleContext getBundleContext() {
        return fgBundleContext;
    }
    
    public static void debug(String message) {
        if (DEBUG) {
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
