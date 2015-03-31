/*******************************************************************************
 * Copyright (c) 2015 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson) - Find / Replace for 16 bits addressable sizes (Bug 462073)
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class UITestsPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.tests.dsf.gdb.ui"; //$NON-NLS-1$
	private BundleContext fBundleContext;

	// The shared instance
	private static UITestsPlugin plugin;
	
	/**
	 * The constructor
	 */
	public UITestsPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		fBundleContext = context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static UITestsPlugin getDefault() {
		return plugin;
	}
	
    /**
     * Returns the plugin's bundle context,
     */
    public BundleContext getBundleContext() {
        return fBundleContext;
    } 

}
