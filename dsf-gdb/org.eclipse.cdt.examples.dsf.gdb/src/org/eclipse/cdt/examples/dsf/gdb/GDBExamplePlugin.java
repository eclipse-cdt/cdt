/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GDBExamplePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.examples.dsf.gdb"; //$NON-NLS-1$

	// The shared instance
	private static GDBExamplePlugin plugin;
	
    private static BundleContext fgBundleContext;

	/**
	 * The constructor
	 */
	public GDBExamplePlugin() {
	}

	@Override
    public void start(BundleContext context) throws Exception {
        fgBundleContext = context;
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
        fgBundleContext = null;
	}


	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GDBExamplePlugin getDefault() {
		return plugin;
	}

    public static BundleContext getBundleContext() {
        return fgBundleContext;
    }

}
