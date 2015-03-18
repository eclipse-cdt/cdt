/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.launchbar.core";
	private static Activator plugin;

	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
		bundleContext.registerService(ILaunchBarManager.class, new LaunchBarManager(), null);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		plugin = null;
	}

	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Return the OSGi service with the given service interface.
	 * 
	 * @param service service interface
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

	private static final String DEBUG_ONE =
			PLUGIN_ID + "/debug/launchbar";

	public static void trace(String str) {
		if (plugin == null || (plugin.isDebugging() && "true".equalsIgnoreCase(Platform.getDebugOption(DEBUG_ONE))))
			System.out.println("launchbar: " + str);
	}
}
