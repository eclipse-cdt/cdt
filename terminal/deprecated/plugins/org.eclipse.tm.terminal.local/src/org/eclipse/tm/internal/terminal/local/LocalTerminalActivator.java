/***************************************************************************************************
 * Copyright (c) 2008, 2010 Mirko Raner and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - initial implementation for Eclipse Bug 196337
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The class {@link LocalTerminalActivator} is the bundle activator for the Local Terminal
 * Connector plug-in.
 *
 * @author Mirko Raner
 * @version $Revision: 1.1 $
 */
public class LocalTerminalActivator extends AbstractUIPlugin {

	/**
	 * The plug-in ID of the Local Terminal Connector plug-in.
	 */
	public static final String PLUGIN_ID = "org.eclipse.tm.terminal.local"; //$NON-NLS-1$

	/**
	 * The preference key for confirming process termination during workbench shutdown.
	 * Value: "CONFIRM_TERMINATE".
	 * If the corresponding Preference slot is set to "true", a confirmation dialog
	 * will be shown when quitting Workbench while a local Terminal is still running.
	 * For details, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=313643
	 * @since 0.1
	 */
	public final static String PREF_CONFIRM_TERMINATE_ON_SHUTDOWN = "CONFIRM_TERMINATE";//$NON-NLS-1$

	private static LocalTerminalActivator plugin;

	/**
	 * Creates a new {@link LocalTerminalActivator}.
	 */
	public LocalTerminalActivator() {

		super();
	}

	/**
	 * Returns the shared plug-in instance.
	 *
	 * @return the shared instance
	 */
	public static LocalTerminalActivator getDefault() {

		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 *
	 * @param path the path to the image
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {

		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Starts the bundle and initializes the shared plug-in reference.
	 *
	 * @param context the {@link BundleContext}
	 *
	 * @see AbstractUIPlugin#start(BundleContext)
	 */
	public void start(BundleContext context) throws Exception {

		super.start(context);
		plugin = this;
	}

	/**
	 * Stops the bundle and resets the the shared plug-in reference.
	 *
	 * @param context the {@link BundleContext}
	 *
	 * @see AbstractUIPlugin#stop(BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {

		plugin = null;
		super.stop(context);
	}
}
