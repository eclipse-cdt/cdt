/*******************************************************************************
 * Copyright (c) 2009, 2015 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.p2;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.p2"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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

	public static BundleContext getContext() {
		return plugin.getBundle().getBundleContext();
	}

	/**
	 * Return a service from our context.
	 * 
	 * @param name name of the service
	 * @return the service
	 */
	public static <T> T getService(Class<T> clazz) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(clazz);
		return (ref != null) ? context.getService(ref) : null;
	}

	public static IStatus getStatus(int severity, String message) {
		return new Status(severity, PLUGIN_ID, message);
	}

	public static IStatus getStatus(int severity, Throwable e) {
		return new Status(severity, PLUGIN_ID, e.getLocalizedMessage(), e);
	}

	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	public static void log(CoreException e) {
		plugin.getLog().log(e.getStatus());
	}
	
	public static void log(Throwable e) {
		plugin.getLog().log(getStatus(IStatus.ERROR, e));
	}

}
