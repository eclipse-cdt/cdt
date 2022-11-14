/*******************************************************************************
 * Copyright (c) 2016, 2022 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.launchbar.ui.controls"; //$NON-NLS-1$

	// images
	public static final String IMG_BUTTON_BUILD = "build"; //$NON-NLS-1$
	public static final String IMG_CONFIG_CONFIG = "config_config"; //$NON-NLS-1$
	public static final String IMG_EDIT_COLD = "edit_cold"; //$NON-NLS-1$

	// Preference ids
	public static final String PREF_ENABLE_LAUNCHBAR = "enableLaunchBar"; //$NON-NLS-1$
	public static final String PREF_ALWAYS_TARGETSELECTOR = "alwaysTargetSelector"; //$NON-NLS-1$
	public static final String PREF_ENABLE_BUILDBUTTON = "enableBuildButton"; //$NON-NLS-1$
	public static final String PREF_LAUNCH_HISTORY_SIZE = "launchHistorySize"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put(IMG_BUTTON_BUILD, ImageDescriptor.createFromURL(plugin.getBundle().getEntry("/icons/build_exec.png"))); //$NON-NLS-1$
		reg.put(IMG_CONFIG_CONFIG,
				ImageDescriptor.createFromURL(plugin.getBundle().getEntry("/icons/config_config.png"))); //$NON-NLS-1$
		reg.put(IMG_EDIT_COLD, ImageDescriptor.createFromURL(plugin.getBundle().getEntry("/icons/edit_cold.png"))); //$NON-NLS-1$
	}

	@Override
	public void stop(BundleContext context) throws Exception {
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

	public static void log(Throwable e) {
		IStatus status;
		if (e instanceof CoreException) {
			status = ((CoreException) e).getStatus();
		} else {
			status = new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e);
		}
		plugin.getLog().log(status);
	}

	public static void log(String msg) {
		plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, msg));
	}
}
