/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launch.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class LaunchImages {
	private static final String NAME_PREFIX= LaunchUIPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH= NAME_PREFIX.length();
	
	// The plugin registry
	private static ImageRegistry imageRegistry = new ImageRegistry();

	// Subdirectory (under the package containing this class) where 16 color images are
	private static URL fgIconBaseURL;
	static {
		fgIconBaseURL= Platform.getBundle(LaunchUIPlugin.getUniqueIdentifier()).getEntry("/icons/"); //$NON-NLS-1$
	}	

	private static final String T_TABS = "view16/"; //$NON-NLS-1$
	private static final String T_OBJS = "obj16/"; //$NON-NLS-1$

	public static String IMG_VIEW_MAIN_TAB = NAME_PREFIX + "main_tab.gif"; //$NON-NLS-1$
	public static String IMG_VIEW_ARGUMENTS_TAB = NAME_PREFIX + "arguments_tab.gif"; //$NON-NLS-1$
	public static String IMG_VIEW_ENVIRONMENT_TAB = NAME_PREFIX + "environment_tab.gif"; //$NON-NLS-1$
	public static String IMG_VIEW_DEBUGGER_TAB = NAME_PREFIX + "debugger_tab.gif"; //$NON-NLS-1$
	public static String IMG_VIEW_SOURCE_TAB = NAME_PREFIX + "source_tab.gif"; //$NON-NLS-1$

	public static final ImageDescriptor DESC_TAB_MAIN= createManaged(T_TABS, IMG_VIEW_MAIN_TAB);
	public static final ImageDescriptor DESC_TAB_ARGUMENTS = createManaged(T_TABS, IMG_VIEW_ARGUMENTS_TAB);
	public static final ImageDescriptor DESC_TAB_ENVIRONMENT = createManaged(T_TABS, IMG_VIEW_ENVIRONMENT_TAB);
	public static final ImageDescriptor DESC_TAB_DEBUGGER = createManaged(T_TABS, IMG_VIEW_DEBUGGER_TAB);
	public static final ImageDescriptor DESC_TAB_SOURCE = createManaged(T_TABS, IMG_VIEW_SOURCE_TAB);

	public static String IMG_OBJS_EXEC= NAME_PREFIX + "exec_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_EXEC = createManaged(T_OBJS, IMG_OBJS_EXEC);

	public static void initialize() {
	}
	
	private static ImageDescriptor createManaged(String prefix, String name) {
		return createManaged(imageRegistry, prefix, name);
	}
	
	private static ImageDescriptor createManaged(ImageRegistry registry, String prefix, String name) {
		ImageDescriptor result= ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
		registry.put(name, result);
		return result;
	}
	
	public static Image get(String key) {
		return imageRegistry.get(key);
	}

	
	private static URL makeIconFileURL(String prefix, String name) {
		StringBuffer buffer= new StringBuffer(prefix);
		buffer.append(name);
		try {
			return new URL(fgIconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			LaunchUIPlugin.log(e);
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
