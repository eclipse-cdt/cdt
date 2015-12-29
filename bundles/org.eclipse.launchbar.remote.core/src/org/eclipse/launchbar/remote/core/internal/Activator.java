/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.remote.core.internal;

import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.launchbar.remote.core"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private static RemoteConnectionListener remoteConnectionListener;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		remoteConnectionListener = new RemoteConnectionListener();
		getService(IRemoteServicesManager.class).addRemoteConnectionChangeListener(remoteConnectionListener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		getService(IRemoteServicesManager.class).removeRemoteConnectionChangeListener(remoteConnectionListener);
		remoteConnectionListener = null;

		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

}
