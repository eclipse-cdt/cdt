/*******************************************************************************
 * Copyright (c) 2010, 2011 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.tests;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CodanCoreTestActivator extends Plugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.codan.core.test"; //$NON-NLS-1$
	// The shared instance
	private static CodanCoreTestActivator plugin;

	/**
	 * The constructor
	 */
	public CodanCoreTestActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

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
	public static CodanCoreTestActivator getDefault() {
		return plugin;
	}
}
