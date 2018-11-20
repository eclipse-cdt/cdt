/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.launch.serial.ui.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.cdt.launch.serial.ui"; //$NON-NLS-1$

	public static final String IMG_CDT_LOGO = "org.eclipse.cdt.launch.serial.ui.cdt_logo_16"; //$NON-NLS-1$

	private static AbstractUIPlugin plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);

		reg.put(IMG_CDT_LOGO, imageDescriptorFromPlugin(PLUGIN_ID, "icons/cdt_logo_16.png")); //$NON-NLS-1$
	}

	public static Image getImage(String key) {
		return plugin.getImageRegistry().get(key);
	}

	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

}
