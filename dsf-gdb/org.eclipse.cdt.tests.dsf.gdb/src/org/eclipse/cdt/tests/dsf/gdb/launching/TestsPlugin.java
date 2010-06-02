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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class TestsPlugin extends Plugin {
    //The shared instance.
    private static TestsPlugin plugin;
    //Resource bundle.
    private ResourceBundle resourceBundle;
    private static BundleContext bundleContext;
	
    public static final String PLUGIN_ID = "org.eclipse.cdt.tests.dsf.gdb"; //$NON-NLS-1$
    
    /** Base tracing option for this plugin */
    public static final boolean DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.cdt.tests.dsf.gdb/debug"));  //$NON-NLS-1$//$NON-NLS-2$
    
    /**
     * The constructor.
     */
    public TestsPlugin() {
        super();
        plugin = this;
        try {
            resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.tests.dsf.gdb.TestsPluginResources"); //$NON-NLS-1$
        }
        catch (MissingResourceException x) {
            resourceBundle = null;
        }
    }
    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        bundleContext = context;
    }
    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }
    /**
     * Returns the shared instance.
     */
    public static TestsPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = TestsPlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        }
        catch (MissingResourceException e) {
            return key;
        }
    }
    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }        
    /**
     * Returns the plugin's bundle context,
     */
    public static BundleContext getBundleContext() {
        return bundleContext;
    }        
    /**     
     * Convenience method which returns the unique identifier of this plugin.
     */    
    public static String getUniqueIdentifier() {
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
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 *            the exception to be logged
	 */
	public static void log( Throwable e ) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), "Internal Error", e)); //$NON-NLS-1$
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
			log(new Status(IStatus.ERROR, getUniqueIdentifier(), "\"dsf.gdb.tests.timeout.multiplier\" property incorrectly specified. Should be a float value (e.g., \"1.5\") or not specified at all.")); //$NON-NLS-1$
			return timeoutMs;
		}
	}
}
