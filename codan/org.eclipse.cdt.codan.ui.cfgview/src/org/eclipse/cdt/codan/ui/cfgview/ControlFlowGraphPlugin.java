/*******************************************************************************
 * Copyright (c) 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.ui.cfgview;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ControlFlowGraphPlugin extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.codan.ui.cfgview"; //$NON-NLS-1$
	// The shared instance
	private static ControlFlowGraphPlugin plugin;

	/**
	 * The constructor
	 */
	public ControlFlowGraphPlugin() {
	}

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

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ControlFlowGraphPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public ImageDescriptor getImageDescriptor(String key) {
		ImageRegistry registry = getImageRegistry();
		ImageDescriptor descriptor = registry.getDescriptor(key);
		if (descriptor == null) {
			descriptor = imageDescriptorFromPlugin(PLUGIN_ID, key);
			registry.put(key, descriptor);
		}
		return descriptor;
	}

	public Image getImage(String key) {
		ImageRegistry registry = getImageRegistry();
		Image image = registry.get(key);
		if (image == null) {
			ImageDescriptor descriptor = imageDescriptorFromPlugin(PLUGIN_ID, key);
			registry.put(key, descriptor);
			image = registry.get(key);
		}
		return image;
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 *
	 * @param status
	 *        status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Logs an internal error with the specified throwable
	 *
	 * @param e the exception to be logged
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(Throwable e) {
		log("Internal Error", e); //$NON-NLS-1$
	}

	/**
	 * Logs an internal error with the specified message and throwable
	 *
	 * @param message the error message to log
	 * @param e the exception to be logged
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String message, Throwable e) {
		log(new Status(IStatus.ERROR, CodanCorePlugin.PLUGIN_ID, 1, message, e));
	}

	/**
	 * Logs an internal error with the specified message.
	 *
	 * @param message the error message to log
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 1, message, null));
	}
}
