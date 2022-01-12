/*******************************************************************************
 * Copyright (c) 2010, 2012 Alena Laskavaia and others.
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
package org.eclipse.cdt.codan.checkers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CodanCheckersActivator extends Plugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.codan.checkers"; //$NON-NLS-1$
	// The shared instance
	private static CodanCheckersActivator plugin;

	/**
	 * The constructor
	 */
	public CodanCheckersActivator() {
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
	public static CodanCheckersActivator getDefault() {
		return plugin;
	}

	/**
	 * @param e
	 */
	public static void log(Throwable e) {
		getDefault().getLog().log(getStatus(e));
	}

	public static void log(String message) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
	}

	/**
	 * @param e
	 * @return
	 */
	public static IStatus getStatus(Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e);
	}
}
