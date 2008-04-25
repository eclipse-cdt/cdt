/********************************************************************************
 * Copyright (c) 2008 MontaVista Software, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo (MontaVista) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.internal.subsystems.terminals.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends SystemBasePlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.rse.subsystems.terminals.core";

    // The shared instance
    private static Activator plugin;

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
        // make sure that org.eclipse.rse.terminals.ui plugin is loaded
        // Decouple from the current Thread
        new Thread("terminals.ui adapter loader") { //$NON-NLS-1$
            public void run() {

                try {
                    Bundle bundle = Platform
                            .getBundle("org.eclipse.rse.terminals.ui");
                    if (bundle != null) {
                        bundle
                                .loadClass("org.eclipse.rse.internal.terminals.ui.TerminalServiceHelper");

                    }
                } catch (ClassNotFoundException e) {
                    logError(e.getLocalizedMessage(), e);
                }

            }
        }.start();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
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

    protected void initializeImageRegistry() {

    }

}
