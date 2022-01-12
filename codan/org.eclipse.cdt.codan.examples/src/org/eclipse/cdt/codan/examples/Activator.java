/*******************************************************************************
 * Copyright (c) 2010, 2016 Alena Laskavaia and others.
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
package org.eclipse.cdt.codan.examples;

import org.eclipse.cdt.codan.examples.uicontrib.GrepCheckerExamplePreferenceChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.codan.examples"; //$NON-NLS-1$
	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		GrepCheckerExamplePreferenceChangeListener.getInstance();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		GrepCheckerExamplePreferenceChangeListener.getInstance().dispose();
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
	 * Logs the specified status with this plug-in's log.
	 *
	 * @param status
	 *        status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Logs an internal error with the specified {@code Throwable}.
	 *
	 * @param t
	 *        the {@code Throwable} to be logged
	 */
	public static void log(Throwable t) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 1, "Internal Error", t)); //$NON-NLS-1$
	}

	/**
	 * Logs an internal error with the specified message.
	 *
	 * @param message
	 *        the error message to log
	 */
	public static void log(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 1, message, null));
	}

	/**
	 * Logs an internal error with the specified message and {@code Throwable}.
	 *
	 * @param message
	 *        the error message to log
	 * @param t
	 *        the {@code Throwable} to be logged
	 *
	 * @since 2.1
	 */
	public static void log(String message, Throwable t) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 1, message, t));
	}
}
