/*******************************************************************************
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *     Torkild U. Resheim - add preference to control target selector
 *     Vincent Guignot - Ingenico - add preference to control Build button
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.launchbar.ui.ILaunchBarUIManager;
import org.eclipse.launchbar.ui.internal.target.LaunchTargetUIManager;
import org.eclipse.launchbar.ui.target.ILaunchTargetUIManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.launchbar.ui"; //$NON-NLS-1$

	// Images
	public static final String IMG_LOCAL_TARGET = "localTarget"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		ImageRegistry imageRegistry = getImageRegistry();
		imageRegistry.put(IMG_LOCAL_TARGET, imageDescriptorFromPlugin(PLUGIN_ID, "icons/localTarget.png")); //$NON-NLS-1$

		context.registerService(ILaunchTargetUIManager.class, new LaunchTargetUIManager(), null);
		context.registerService(ILaunchBarUIManager.class, new LaunchBarUIManager(), null);
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
	public static Activator getDefault() {
		return plugin;
	}

	public Image getImage(String id) {
		Image im = getImageRegistry().get(id);
		if (im == null) {
			ImageDescriptor des = getImageDescriptor(id);
			if (des != null) {
				im = des.createImage();
				getImageRegistry().put(id, im);
			}
		}
		return im;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	public static void log(Exception e) {
		if (e instanceof CoreException)
			log(((CoreException) e).getStatus());
		plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e));
	}

	public static <T> T getService(Class<T> cls) {
		BundleContext context = getDefault().getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(cls);
		return ref != null ? context.getService(ref) : null;
	}

}
