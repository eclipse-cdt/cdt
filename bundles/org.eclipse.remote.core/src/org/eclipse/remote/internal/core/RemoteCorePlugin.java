/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.core;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.launch.IRemoteLaunchConfigService;
import org.eclipse.remote.internal.core.launch.RemoteLaunchConfigService;
import org.eclipse.remote.internal.core.preferences.Preferences;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class RemoteCorePlugin extends Plugin {
	private static final String PLUGIN_ID = "org.eclipse.remote.core"; //$NON-NLS-1$

	// The shared instance
	private static RemoteCorePlugin plugin;

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RemoteCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Get unique identifier for this plugin
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message
	 *            the error message to log
	 */
	public static void log(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 *            the exception to be logged
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
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

	/**
	 * The constructor
	 */
	public RemoteCorePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		context.registerService(IRemoteServicesManager.class, new RemoteServicesManager(), null);
		context.registerService(IRemoteLaunchConfigService.class, new RemoteLaunchConfigService(), null);
		RemoteDebugOptions.configure(context);
		ResourcesPlugin.getWorkspace().addSaveParticipant(getUniqueIdentifier(), new ISaveParticipant() {
			@Override
			public void saving(ISaveContext saveContext) throws CoreException {
				Preferences.savePreferences();
			}

			@Override
			public void rollback(ISaveContext saveContext) {
				// Do nothing
			}

			@Override
			public void prepareToSave(ISaveContext saveContext) throws CoreException {
				// Do nothing
			}

			@Override
			public void doneSaving(ISaveContext saveContext) {
				// Do nothing
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		Preferences.savePreferences();
		plugin = null;
		super.stop(context);
	}
}
