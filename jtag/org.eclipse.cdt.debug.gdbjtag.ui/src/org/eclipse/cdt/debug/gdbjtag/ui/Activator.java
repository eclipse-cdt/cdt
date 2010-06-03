/*******************************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.gdbjtag.ui";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
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

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status
	 *            status to log
	 */
	static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message
	 *            the error message to log
	 */
	static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, null));
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 *            the exception to be logged
	 */
	static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, e.getMessage(), e));
	}
}
