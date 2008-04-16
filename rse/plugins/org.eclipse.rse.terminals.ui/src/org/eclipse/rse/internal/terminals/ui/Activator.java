/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Yu-Fen Kuo (MontaVista) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.internal.terminals.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.rse.ui.SystemBasePlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends SystemBasePlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.rse.internal.terminals.ui"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private ResourceBundle resourceBundle;

    public static String ICON_ID_LAUNCH_TERMINAL = "icon_id_launch_terminal"; //$NON-NLS-1$
    public static String ICON_ID_TERMINAL_SUBSYSTEM = "icon_id_terminal_subsystem"; //$NON-NLS-1$
    public static String ICON_ID_TERMINAL_SUBSYSTEM_LIVE = "icon_id_terminal_subsystem_live"; //$NON-NLS-1$
    public static String ICON_ID_REMOVE_TERMINAL = "icon_id_remove_terminal"; //$NON-NLS-1$

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        resourceBundle = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     * 
     * @see java.util.ResourceBundle#getString(String)
     * 
     * @param key
     *                the key for the desired string
     * @return the string for the given key
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = Activator.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Return the plugin's Resource bundle.
     * 
     * @return the Resource bundle
     */
    public ResourceBundle getResourceBundle() {
        try {
            if (resourceBundle == null)
                resourceBundle = ResourceBundle
                        .getBundle("org.eclipse.rse.internal.terminals.ui.TerminalUIResources"); //$NON-NLS-1$
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
        return resourceBundle;
    }

    /**
     * Initialize the image registry by declaring all of the required graphics.
     */
    protected void initializeImageRegistry() {
        String path = getIconPath();
        putImageInRegistry(ICON_ID_LAUNCH_TERMINAL, path + "terminal_view.gif"); //$NON-NLS-1$
        putImageInRegistry(ICON_ID_TERMINAL_SUBSYSTEM, path
                + "terminalcommands_obj.gif"); //$NON-NLS-1$
        putImageInRegistry(ICON_ID_TERMINAL_SUBSYSTEM_LIVE, path
                + "terminalcommandslive_obj.gif"); //$NON-NLS-1$
        putImageInRegistry(ICON_ID_REMOVE_TERMINAL, path + "removeterminal.gif"); //$NON-NLS-1$

    }

}
