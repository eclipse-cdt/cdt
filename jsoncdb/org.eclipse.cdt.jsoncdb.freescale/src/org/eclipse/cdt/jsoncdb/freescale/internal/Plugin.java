/*******************************************************************************
 * Copyright (c) 2015-2020 Martin Weber.
 *                    2023 Thomas Kucharczyk
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.jsoncdb.freescale.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Martin Weber
 */
public class Plugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.cdt.jsoncdb.freescale"; //$NON-NLS-1$

	// The shared instance.
	private static Plugin plugin;

	/**
	 * The constructor.
	 */
	public Plugin() {
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static Plugin getDefault() {
		return plugin;
	}
}
