/*******************************************************************************
 * Copyright (c) 2003, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Anna Dushistova (MontaVista) - [227537] moved actions from terminal.view to terminal plugin
 * Martin Oberhuber (Wind River) - [168186] Add Terminal User Docs
 * Michael Scharf (Wind River) - [172483] switch between connections
 * Michael Scharf (Wind River) - [240023] Get rid of the terminal's "Pin" button
 *******************************************************************************/
package org.eclipse.remote.internal.terminal.view;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class TerminalViewPlugin extends AbstractUIPlugin {
	protected static TerminalViewPlugin fDefault;

	public static final String PLUGIN_ID = "org.eclipse.remote.terminal"; //$NON-NLS-1$

	/**
	 * Generate a unique identifier
	 * 
	 * @return unique identifier string
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * The constructor.
	 */
	public TerminalViewPlugin() {
		fDefault = this;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry imageRegistry) {
		HashMap<String, String> map = new HashMap<>();

		try {
			// Local toolbars
			map.put(ImageConsts.IMAGE_NEW_TERMINAL, "newterminal.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_CLCL_CONNECT, "connect_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_CLCL_DISCONNECT, "disconnect_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_CLCL_SETTINGS, "properties_tsk.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_CLCL_COMMAND_INPUT_FIELD, "command_input_field.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_CLCL_SCROLL_LOCK, "lock_co.gif"); //$NON-NLS-1$

			loadImageRegistry(imageRegistry, ImageConsts.IMAGE_DIR_LOCALTOOL, map);

			map.clear();

			// Enabled local toolbars
			map.put(ImageConsts.IMAGE_NEW_TERMINAL, "newterminal.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_ELCL_CONNECT, "connect_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_ELCL_DISCONNECT, "disconnect_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_ELCL_SETTINGS, "properties_tsk.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_ELCL_COMMAND_INPUT_FIELD, "command_input_field.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_ELCL_SCROLL_LOCK, "lock_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_ELCL_REMOVE, "rem_co.gif"); //$NON-NLS-1$

			loadImageRegistry(imageRegistry, ImageConsts.IMAGE_DIR_ELCL, map);

			map.clear();

			// Disabled local toolbars
			map.put(ImageConsts.IMAGE_NEW_TERMINAL, "newterminal.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_DLCL_CONNECT, "connect_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_DLCL_DISCONNECT, "disconnect_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_DLCL_SETTINGS, "properties_tsk.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_DLCL_COMMAND_INPUT_FIELD, "command_input_field.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_DLCL_SCROLL_LOCK, "lock_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_DLCL_REMOVE, "rem_co.gif"); //$NON-NLS-1$

			loadImageRegistry(imageRegistry, ImageConsts.IMAGE_DIR_DLCL, map);

			map.clear();

			map.put(ImageConsts.IMAGE_TERMINAL_VIEW, "terminal_view.gif"); //$NON-NLS-1$

			loadImageRegistry(imageRegistry, ImageConsts.IMAGE_DIR_EVIEW, map);

			map.clear();

		} catch (MalformedURLException malformedURLException) {
			malformedURLException.printStackTrace();
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static TerminalViewPlugin getDefault() {
		return fDefault;
	}

	protected void loadImageRegistry(ImageRegistry imageRegistry, String strDir, HashMap<String, String> map)
			throws MalformedURLException {
		ImageDescriptor imageDescriptor;

		for (Entry<String, String> entry : map.entrySet()) {
			URL url = TerminalViewPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + strDir + entry.getValue());
			imageDescriptor = ImageDescriptor.createFromURL(url);
			imageRegistry.put(entry.getKey(), imageDescriptor);
		}
	}

	/**
	 * Create log entry from an IStatus
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Create log entry from a string
	 * 
	 * @param msg
	 *            message to log
	 */
	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}

	/**
	 * Create log entry from a Throwable
	 * 
	 * @param e
	 *            throwable to log
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}

	/**
	 * Return the OSGi service with the given service interface.
	 *
	 * @param service
	 *            service interface
	 * @return the specified service or null if it's not registered
	 */
	public static <T> T getService(Class<T> service) {
		final BundleContext context = fDefault.getBundle().getBundleContext();
		final ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

}
