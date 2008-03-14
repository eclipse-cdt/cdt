/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * David Dykstal (IBM) - [186589] move user actions API out of org.eclipse.rse.ui   
 *******************************************************************************/
package org.eclipse.rse.internal.useractions;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends AbstractUIPlugin {
	//The shared instance.
	private static Activator plugin;
	public static final String PLUGIN_ID = "org.eclipse.rse.useractions"; //$NON-NLS-1$
	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
	 */
	protected void initializeImageRegistry(ImageRegistry registry) {
		super.initializeImageRegistry(registry);
		registry.put(IUserActionsImageIds.COMPILE_0, getImageDescriptor("icons/full/dlcl16/compile.gif")); //$NON-NLS-1$
		registry.put(IUserActionsImageIds.COMPILE_1, getImageDescriptor("icons/full/elcl16/compile.gif")); //$NON-NLS-1$
		registry.put(IUserActionsImageIds.WORK_WITH_COMPILE_COMMANDS_0, getImageDescriptor("icons/full/dlcl16/workwithcompilecmds.gif")); //$NON-NLS-1$
		registry.put(IUserActionsImageIds.WORK_WITH_COMPILE_COMMANDS_1, getImageDescriptor("icons/full/elcl16/workwithcompilecmds.gif")); //$NON-NLS-1$
		registry.put(IUserActionsImageIds.WORK_WITH_NAMED_TYPES_0, getImageDescriptor("icons/full/dlcl16/workwithnamedtypes.gif")); //$NON-NLS-1$
		registry.put(IUserActionsImageIds.WORK_WITH_NAMED_TYPES_1, getImageDescriptor("icons/full/elcl16/workwithnamedtypes.gif")); //$NON-NLS-1$
		registry.put(IUserActionsImageIds.WORK_WITH_USER_ACTIONS_0, getImageDescriptor("icons/full/dlcl16/workwithuseractions.gif")); //$NON-NLS-1$
		registry.put(IUserActionsImageIds.WORK_WITH_USER_ACTIONS_1, getImageDescriptor("icons/full/elcl16/workwithuseractions.gif")); //$NON-NLS-1$
	}
	
	/**
	 * Gets the image descriptor for images in the plugin bundle.
	 * @param path the plugin relative path of the image
	 * @return the image descriptor
	 */
	public ImageDescriptor getImageDescriptor(String path) {
		ImageDescriptor descriptor = null;
		URL url = getBundle().getResource(path);
		if (url != null) {
			descriptor = ImageDescriptor.createFromURL(url);
		}
		return descriptor;
	}

}
