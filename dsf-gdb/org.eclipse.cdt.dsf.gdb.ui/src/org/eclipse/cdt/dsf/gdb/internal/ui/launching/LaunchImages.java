/*******************************************************************************
 * Copyright (c) 2008, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ericsson             - Modified for DSF
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class LaunchImages {
	private static final String NAME_PREFIX = GdbUIPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	// The plugin registry
	private static ImageRegistry imageRegistry = new ImageRegistry();

	// Subdirectory (under the package containing this class) where 16 color images are
	private static URL fgIconBaseURL;
	static {
		fgIconBaseURL = Platform.getBundle(GdbUIPlugin.PLUGIN_ID).getEntry("/icons/"); //$NON-NLS-1$
	}

	private static final String T_TABS = "full/view16/"; //$NON-NLS-1$
	private static final String T_OBJS = "full/obj16/"; //$NON-NLS-1$

	public static String IMG_VIEW_MAIN_TAB = NAME_PREFIX + "main_tab.gif"; //$NON-NLS-1$
	public static String IMG_VIEW_ARGUMENTS_TAB = NAME_PREFIX + "arguments_tab.gif"; //$NON-NLS-1$
	public static String IMG_VIEW_DEBUGGER_TAB = NAME_PREFIX + "debugger_tab.gif"; //$NON-NLS-1$

	public static final ImageDescriptor DESC_TAB_MAIN = createManaged(T_TABS, IMG_VIEW_MAIN_TAB);
	public static final ImageDescriptor DESC_TAB_ARGUMENTS = createManaged(T_TABS, IMG_VIEW_ARGUMENTS_TAB);
	public static final ImageDescriptor DESC_TAB_DEBUGGER = createManaged(T_TABS, IMG_VIEW_DEBUGGER_TAB);

	public static String IMG_OBJS_EXEC = NAME_PREFIX + "exec_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_EXEC = createManaged(T_OBJS, IMG_OBJS_EXEC);

	public static final String IMG_OBJS_REMOTE = NAME_PREFIX + "connect.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_REMOTE = createManaged(T_OBJS, IMG_OBJS_REMOTE);

	public static void initialize() {
	}

	private static ImageDescriptor createManaged(String prefix, String name) {
		return createManaged(imageRegistry, prefix, name);
	}

	private static ImageDescriptor createManaged(ImageRegistry registry, String prefix, String name) {
		ImageDescriptor result = ImageDescriptor
				.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
		registry.put(name, result);
		return result;
	}

	public static Image get(String key) {
		return imageRegistry.get(key);
	}

	private static URL makeIconFileURL(String prefix, String name) {
		StringBuilder buffer = new StringBuilder(prefix);
		buffer.append(name);
		try {
			return new URL(fgIconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			GdbUIPlugin.log(e);
			return null;
		}
	}

	/**
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}
}
