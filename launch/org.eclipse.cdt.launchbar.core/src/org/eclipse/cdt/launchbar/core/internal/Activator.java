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
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.cdt.launchbar.core";
	private static BundleContext context;
	private LaunchBarManager launchBarManager;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
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
		Activator.context = null;
		launchBarManager = null;
	}

	public static void throwCoreException(Exception e) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e));
	}

}
