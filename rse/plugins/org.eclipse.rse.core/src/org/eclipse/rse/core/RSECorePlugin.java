/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.core;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.rse.core.internal.RSECoreRegistry;
import org.osgi.framework.BundleContext;

/**
 * The RSE core plugin class. Clients may extend this class.
 */
public class RSECorePlugin extends Plugin {

	// the shared instance
	private static RSECorePlugin plugin;

	/**
	 * The constructor.
	 */
	public RSECorePlugin() {
		plugin = this;
	}

	/**
	 * Starts the bundle.
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped.
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * @return the shared instance.
	 */
	public static RSECorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the RSE core registry. Clients should use this method to get the registry which
	 * is the starting point for working with the RSE framework.
	 * @return the RSE core registry.
	 */
	public IRSECoreRegistry getRegistry() {
		return RSECoreRegistry.getDefault();
	}
}