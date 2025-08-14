/*******************************************************************************
 * Copyright (c) 2003, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Anna Dushistova (MontaVista) - [227537] moved actions from terminal.view to terminal plugin
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control.impl;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class TerminalPlugin extends AbstractUIPlugin {
	private static TerminalPlugin plugin;
	public static final String PLUGIN_ID = "org.eclipse.terminal.control"; //$NON-NLS-1$
	public static final String HELP_VIEW = PLUGIN_ID + ".terminal_view"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public TerminalPlugin() {
	}

	/**
	 * Returns the shared instance.
	 */
	public static TerminalPlugin getDefault() {
		return plugin;
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

	public static boolean isOptionEnabled(String strOption) {
		String strEnabled = Platform.getDebugOption(strOption);
		if (strEnabled == null)
			return false;

		return Boolean.parseBoolean(strEnabled);
	}

}
