/********************************************************************************
 * Copyright (c) 2006 IBM Corporation and Wind River Systems, Inc.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - adapted template for daytime example.
 ********************************************************************************/

package org.eclipse.rse.examples.daytime;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleContext;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.examples.daytime.model.DaytimeAdapterFactory;
import org.eclipse.rse.examples.daytime.model.DaytimeResource;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends SystemBasePlugin {

	//The shared instance.
	private static Activator plugin;
	
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
		IAdapterManager manager = Platform.getAdapterManager();
		DaytimeAdapterFactory factory = new DaytimeAdapterFactory();
		manager.registerAdapters(factory, DaytimeResource.class);
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
	 *
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static final String ICON_ID_DAYTIME = "ICON_ID_DAYTIME"; //$NON-NLS-1$
	
	protected void initializeImageRegistry() {
		String path = getIconPath();
		putImageInRegistry(ICON_ID_DAYTIME, path+"full/obj16/daytime.gif"); //$NON-NLS-1$
	}

}
