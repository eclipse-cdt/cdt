/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal;

import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnectionListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends Plugin {

	private static Plugin plugin;

	public static BundleContext getContext() {
		return plugin.getBundle().getBundleContext();
	}

	public static Plugin getPlugin() {
		return plugin;
	}

	public static String getId() {
		return plugin.getBundle().getSymbolicName();
	}

	public static void log(Exception e) {
		if (e instanceof CoreException) {
			plugin.getLog().log(((CoreException) e).getStatus());
		} else {
			plugin.getLog().log(new Status(IStatus.ERROR, Activator.getId(), e.getLocalizedMessage(), e));
		}
	}

	public void start(BundleContext bundleContext) throws Exception {
		plugin = this;
		IRemoteServicesManager remoteManager = getService(IRemoteServicesManager.class);
		remoteManager.addRemoteConnectionChangeListener(ArduinoRemoteConnectionListener.INSTANCE);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		IRemoteServicesManager remoteManager = getService(IRemoteServicesManager.class);
		remoteManager.removeRemoteConnectionChangeListener(ArduinoRemoteConnectionListener.INSTANCE);
		plugin = null;
	}

	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

}
