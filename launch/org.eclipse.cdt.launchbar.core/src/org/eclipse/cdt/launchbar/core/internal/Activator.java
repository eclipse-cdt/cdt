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
package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class Activator extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.cdt.launchbar.core";
	private static Plugin plugin;
	private LaunchBarManager launchBarManager;

	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
		
		bundleContext.registerService(ILaunchBarManager.class, new ServiceFactory<ILaunchBarManager>() {
			@Override
			public synchronized ILaunchBarManager getService(Bundle bundle, ServiceRegistration<ILaunchBarManager> registration) {
				if (launchBarManager == null) {
					try {
						launchBarManager = new LaunchBarManager();
					} catch (CoreException e) {
						// TODO log
						e.printStackTrace();
					}
				}
				return launchBarManager;
			}

			@Override
			public synchronized void ungetService(Bundle bundle,
					ServiceRegistration<ILaunchBarManager> registration,
					ILaunchBarManager service) {
			}
		}, null);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		plugin = null;
		launchBarManager = null;
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

	public static void log(Exception exception) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, exception.getLocalizedMessage(), exception));
	}

	private static final String DEBUG_ONE =
	        PLUGIN_ID + "/debug/launchbar";

	public static void trace(String str) {
		if (plugin == null || (plugin.isDebugging() && "true".equalsIgnoreCase(Platform.getDebugOption(DEBUG_ONE))))
			System.out.println("launchbar: " + str);
	}
}
