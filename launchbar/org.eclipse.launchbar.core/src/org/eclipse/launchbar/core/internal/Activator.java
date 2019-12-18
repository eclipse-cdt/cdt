/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.core.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.internal.target.LaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.launchbar.core"; //$NON-NLS-1$
	private static Activator plugin;

	private static LaunchTargetManager launchTargetManager;
	private static LaunchBarManager launchBarManager;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;

		launchTargetManager = new LaunchTargetManager();
		bundleContext.registerService(ILaunchTargetManager.class, launchTargetManager, null);

		launchBarManager = new LaunchBarManager();
		bundleContext.registerService(ILaunchBarManager.class, launchBarManager, null);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		plugin = null;
		launchTargetManager = null;
		launchBarManager = null;
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static LaunchBarManager getLaunchBarManager() {
		return launchBarManager;
	}

	public static LaunchTargetManager getLaunchTargetManager() {
		return launchTargetManager;
	}

	/**
	 * Return the OSGi service with the given service interface.
	 *
	 * @param service
	 *            service interface
	 * @return the specified service or null if it's not registered
	 */
	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	public static void throwCoreException(Exception e) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e));
	}

	public static void log(IStatus status) {
		if (plugin != null)
			plugin.getLog().log(status);
		else
			System.err.println(status.getMessage());
	}

	public static void log(Throwable exception) {
		if (exception instanceof CoreException) {
			log(((CoreException) exception).getStatus());
		} else {
			log(new Status(IStatus.ERROR, PLUGIN_ID, exception.getLocalizedMessage(), exception));
		}
	}

	private static final String DEBUG_ONE = PLUGIN_ID + "/debug/launchbar"; //$NON-NLS-1$

	public static void trace(String str) {
		if (plugin == null || (plugin.isDebugging() && "true".equalsIgnoreCase(Platform.getDebugOption(DEBUG_ONE)))) //$NON-NLS-1$
			System.out.println("launchbar: " + str); //$NON-NLS-1$
	}
}
