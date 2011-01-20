/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.launching;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * This is no longer an activator class since this is no longer a plug-in. It's
 * a fragment.
 */
public class TestsPlugin {
    private ResourceBundle resourceBundle;

	/**
	 * We're no longer a plug-in, but a fragment. Make this field an alias
	 * to our host plugin's ID.
	 */
    public static final String PLUGIN_ID = GdbPlugin.PLUGIN_ID;
    
    /** Base tracing option for this plugin */
    public static final boolean DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.cdt.tests.dsf.gdb/debug"));  //$NON-NLS-1$//$NON-NLS-2$
    
    /**
     * The constructor.
     */
    public TestsPlugin() {
        super();
        try {
            resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.tests.dsf.gdb.TestsPluginResources"); //$NON-NLS-1$
        }
        catch (MissingResourceException x) {
            resourceBundle = null;
        }
    }
    
    /**
     * Returns this fragment's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }        
    /**
     * Returns the host plug-in's bundle context,
     */
    public static BundleContext getBundleContext() {
        return GdbPlugin.getBundleContext();
    }        

	/**
	 * Logs the specified status with this host plug-in's log.
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log(IStatus status) {
		GdbPlugin.getDefault().getLog().log(status);
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 *            the exception to be logged
	 */
	public static void log( Throwable e ) {
		log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Internal Error", e)); //$NON-NLS-1$
	}

	/**
	 * Tests should use this utility when specifying a timeout value for a wait
	 * operation. This method checks for the existence of the property
	 * "dsf.gdb.tests.timeout.multiplier" and applies it to the specified value.
	 * The property should be specified as a float, e.g., "1.5". Such a value
	 * would up the timeout value by 50%. This gives the executor of the tests
	 * the ability to widen the timeouts across the board for all operations to
	 * accommodate a slow machine.
	 * 
	 * @param timeoutMs
	 *            the timeout, in milliseconds
	 * @return the adjusted value
	 */
	public static int massageTimeout(int timeoutMs) {
		String prop = System.getProperty("dsf.gdb.tests.timeout.multiplier");
		if (prop == null || prop.length() == 0) {
			return timeoutMs;
		}
		
		try {
			float multiplier = Float.valueOf(prop);
			return (int)(timeoutMs * multiplier);
		}
		catch (NumberFormatException exc) {
			log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "\"dsf.gdb.tests.timeout.multiplier\" property incorrectly specified. Should be a float value (e.g., \"1.5\") or not specified at all.")); //$NON-NLS-1$
			return timeoutMs;
		}
	}
}
