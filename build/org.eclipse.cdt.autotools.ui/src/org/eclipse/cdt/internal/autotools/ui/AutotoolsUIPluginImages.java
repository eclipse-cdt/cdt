/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Red Hat Inc. - completely modified for Autotools usage
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * Bundle of all images used by the C plugin.
 */
public class AutotoolsUIPluginImages {

	// The plugin registry
	private static ImageRegistry imageRegistry = new ImageRegistry(CUIPlugin.getStandardDisplay());

	// Subdirectory (under the package containing this class) where 16 color images are
	private static URL fgIconBaseURL;

	static {
		try {
			fgIconBaseURL = new URL(AutotoolsUIPlugin.getDefault().getBundle().getEntry("/"), "icons/"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (MalformedURLException e) {
			CUIPlugin.log(e);
		}
	}
	private static final String NAME_PREFIX = AutotoolsUIPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	public static final String T_OBJ = "ac16/"; //$NON-NLS-1$
	public static final String T_BUILD = "elcl16/"; //$NON-NLS-1$

	public static final String IMG_OBJS_IF = NAME_PREFIX + "if_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ELSE = NAME_PREFIX + "else_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ELIF = NAME_PREFIX + "elif_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CASE = NAME_PREFIX + "case_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CONDITION = NAME_PREFIX + "condition_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_FOR = NAME_PREFIX + "for_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_WHILE = NAME_PREFIX + "while_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ACMACRO = NAME_PREFIX + "acmacro_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_AMMACRO = NAME_PREFIX + "ammacro_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ACMACRO_ARG = NAME_PREFIX + "acmacro_arg_obj.gif"; //$NON-NLS-1$
	public static final String IMG_BUILD_CONFIG = NAME_PREFIX + "build_configs.gif"; //$NON-NLS-1$
	public static final String IMG_CFG_CATEGORY = NAME_PREFIX + "config_category.gif"; //$NON-NLS-1$
	public static final String IMG_CFG_TOOL = NAME_PREFIX + "config_tool.gif"; //$NON-NLS-1$

	public static final ImageDescriptor DESC_OBJS_IF = createManaged(T_OBJ, IMG_OBJS_IF);
	public static final ImageDescriptor DESC_OBJS_ELSE = createManaged(T_OBJ, IMG_OBJS_ELSE);
	public static final ImageDescriptor DESC_OBJS_ELIF = createManaged(T_OBJ, IMG_OBJS_ELIF);
	public static final ImageDescriptor DESC_OBJS_CASE = createManaged(T_OBJ, IMG_OBJS_CASE);
	public static final ImageDescriptor DESC_OBJS_CONDITION = createManaged(T_OBJ, IMG_OBJS_CONDITION);
	public static final ImageDescriptor DESC_OBJS_FOR = createManaged(T_OBJ, IMG_OBJS_FOR);
	public static final ImageDescriptor DESC_OBJS_WHILE = createManaged(T_OBJ, IMG_OBJS_WHILE);
	public static final ImageDescriptor DESC_OBJS_ACMACRO = createManaged(T_OBJ, IMG_OBJS_ACMACRO);
	public static final ImageDescriptor DESC_OBJS_AMMACRO = createManaged(T_OBJ, IMG_OBJS_AMMACRO);
	public static final ImageDescriptor DESC_OBJS_ACMACRO_ARG = createManaged(T_OBJ, IMG_OBJS_ACMACRO_ARG);
	public static final ImageDescriptor DESC_BUILD_CONFIG = createManaged(T_BUILD, IMG_BUILD_CONFIG);
	public static final ImageDescriptor DESC_CFG_CATEGORY = createManaged(T_BUILD, IMG_CFG_CATEGORY);
	public static final ImageDescriptor DESC_CFG_TOOL = createManaged(T_BUILD, IMG_CFG_TOOL);

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

	private static ImageDescriptor create(String prefix, String name) {
		return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
	}

	private static URL makeIconFileURL(String prefix, String name) {
		StringBuilder buffer = new StringBuilder(prefix);
		buffer.append(name);
		try {
			return new URL(fgIconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			CUIPlugin.log(e);
			return null;
		}
	}

	/**
	 * Sets all available image descriptors for the given action.
	 */
	public static void setImageDescriptors(IAction action, String type, String relPath) {
		if (relPath.startsWith(NAME_PREFIX))
			relPath = relPath.substring(NAME_PREFIX_LENGTH);
		action.setDisabledImageDescriptor(create("d" + type, relPath)); //$NON-NLS-1$
		//		action.setHoverImageDescriptor(create("c" + type, relPath)); //$NON-NLS-1$
		action.setImageDescriptor(create("e" + type, relPath)); //$NON-NLS-1$

		// We are still not sure about this, let see TF results first.
		//		Use the managed version so that we ensure that there is no resource handle leaks
		//		Let the widget itself manage the disabled/hover attribution.  This was a huge leak
		//ImageDescriptor desc = getImageRegistry().getDescriptor(relPath);
		//if(desc == null) {
		//	desc = createManaged(T + "c" + type, relPath);
		//}
		//action.setImageDescriptor(desc);
	}

	/**
	 * Helper method to access the image registry from the CUIPlugin class.
	 */
	static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}
}
