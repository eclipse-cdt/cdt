/*******************************************************************************
 * Copyright (c) 2015 Patrick Hofer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Hofer - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class UICoreTestActivator extends Plugin {
	// The plug-in ID
//	public static final String PLUGIN_ID = "org.eclipse.cdt.ui.core.test"; //$NON-NLS-1$
	public static final String PLUGIN_ID = "org.eclipse.cdt.ui.tests"; //$NON-NLS-1$
	// The shared instance
	private static UICoreTestActivator plugin;

	/**
	 * The constructor
	 */
	public UICoreTestActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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
	public static UICoreTestActivator getDefault() {
		return plugin;
	}
}
