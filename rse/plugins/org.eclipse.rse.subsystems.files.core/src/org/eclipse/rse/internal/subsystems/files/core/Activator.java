/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [180519][api] declaratively register adapter factories
 * Martin Oberhuber (wind River) - [203105] Decouple recursive plugin activation of UI adapters
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.subsystems.files.core.subsystems.RemoteFileEmpty;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends AbstractUIPlugin {

	//The shared instance.
	private static Activator plugin;
	
	public static final String PLUGIN_ID = "org.eclipse.rse.subsystems.files.core"; //$NON-NLS-1$
	
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

		// make sure that required adapters factories are loaded 
		//(will typically activate org.eclipse.rse.files.ui)
		//TODO Check that this does not fire up the UI if we want to be headless
		//Decouple from the current Thread
		new Thread("files.ui adapter loader") { //$NON-NLS-1$
			public void run() {
				Platform.getAdapterManager().loadAdapter(new RemoteFileEmpty(), "org.eclipse.rse.ui.view.ISystemViewElementAdapter"); //$NON-NLS-1$
			}
		}.start();
		// Others (RemoteSearchResultSet, RemoteSearchResult, 
		// RemoteFileSystemConfigurationAdapter will be available
		// automatically once the plugin is loaded

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

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.rse.subsystems.files.core", path); //$NON-NLS-1$
	}
}
