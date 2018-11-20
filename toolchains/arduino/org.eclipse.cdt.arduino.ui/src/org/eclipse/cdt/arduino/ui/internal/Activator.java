/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.arduino.ui"; //$NON-NLS-1$

	public static final String IMG_ARDUINO = PLUGIN_ID + ".arduino"; //$NON-NLS-1$
	public static final String IMG_CONNECTION_TYPE = PLUGIN_ID + ".connectionType"; //$NON-NLS-1$
	public static final String IMG_ADD = PLUGIN_ID + ".add"; //$NON-NLS-1$
	public static final String IMG_DELETE = PLUGIN_ID + ".delete"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	@Override
	protected ImageRegistry createImageRegistry() {
		ImageRegistry registry = super.createImageRegistry();
		registry.put(IMG_ARDUINO, imageDescriptorFromPlugin(PLUGIN_ID, "icons/cprojects.gif")); //$NON-NLS-1$
		registry.put(IMG_CONNECTION_TYPE, imageDescriptorFromPlugin(PLUGIN_ID, "icons/arduino.png")); //$NON-NLS-1$
		registry.put(IMG_ADD, imageDescriptorFromPlugin(PLUGIN_ID, "icons/list-add.gif")); //$NON-NLS-1$
		registry.put(IMG_DELETE, imageDescriptorFromPlugin(PLUGIN_ID, "icons/list-delete.gif")); //$NON-NLS-1$
		return registry;
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

	public static String getId() {
		return plugin.getBundle().getSymbolicName();
	}

	public static void log(Exception e) {
		if (e instanceof CoreException) {
			plugin.getLog().log(((CoreException) e).getStatus());
		} else {
			plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e));
		}
	}

	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

}
