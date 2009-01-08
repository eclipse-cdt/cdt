/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
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

import org.eclipse.core.runtime.Plugin;
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
}
