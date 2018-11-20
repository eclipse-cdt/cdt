/*******************************************************************************
 * Copyright (c) 2007, 2012 QNX Software Systems and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.ui;

import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.gdbjtag.ui";

	private static final String HARDWARE_LAUNCH_TYPE = "org.eclipse.cdt.debug.gdbjtag.launchConfigurationType"; //$NON-NLS-1$

	private static final String PREFERRED_DEBUG_HARDWARE_LAUNCH_DELEGATE = "org.eclipse.cdt.debug.gdbjtag.core.dsfLaunchDelegate"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		setDefaultLaunchDelegates();
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

	private void setDefaultLaunchDelegates() {
		// Set the default launch delegates as early as possible, and do it only once (Bug 312997)
		ILaunchManager launchMgr = DebugPlugin.getDefault().getLaunchManager();

		HashSet<String> debugSet = new HashSet<>();
		debugSet.add(ILaunchManager.DEBUG_MODE);

		ILaunchConfigurationType remoteCfg = launchMgr.getLaunchConfigurationType(HARDWARE_LAUNCH_TYPE);
		try {
			if (remoteCfg.getPreferredDelegate(debugSet) == null) {
				ILaunchDelegate[] delegates = remoteCfg.getDelegates(debugSet);
				for (ILaunchDelegate delegate : delegates) {
					if (PREFERRED_DEBUG_HARDWARE_LAUNCH_DELEGATE.equals(delegate.getId())) {
						remoteCfg.setPreferredDelegate(debugSet, delegate);
						break;
					}
				}
			}
		} catch (CoreException e) {
		}
	}
}
