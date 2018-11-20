/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class MakeUIImages {

	/**
	 * Bundle of all images used by the Make plugin.
	 */
	// The plugin registry
	private static ImageRegistry imageRegistry = new ImageRegistry();

	// Subdirectory (under the package containing this class) where 16 color images are
	private static URL fgIconBaseURL;
	static {
		try {
			fgIconBaseURL = new URL(AutotoolsUIPlugin.getDefault().getBundle().getEntry("/"), "icons/"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (MalformedURLException e) {
			AutotoolsUIPlugin.log(e);
		}
	}
	private static final String NAME_PREFIX = AutotoolsUIPlugin.getUniqueIdentifier() + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	public static final String OBJ = "obj16/"; //$NON-NLS-1$
	public static final String T_TOOL = "tool16/"; //$NON-NLS-1$
	public static final String T_LCL = "lcl16/"; //$NON-NLS-1$

	// For the build image
	public static final String IMG_OBJS_BUILD_TARGET = NAME_PREFIX + "target_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_BUILD_TARGET = createManaged(OBJ, IMG_OBJS_BUILD_TARGET);

	public static final String IMG_OBJS_ERROR = NAME_PREFIX + "error_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJ_ERROR = createManaged(OBJ, IMG_OBJS_ERROR);
	public static final String IMG_OBJS_WARNING = NAME_PREFIX + "warning_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJ_WARNING = createManaged(OBJ, IMG_OBJS_WARNING);

	public static final String IMG_TOOLS_MAKE_TARGET_BUILD = NAME_PREFIX + "target_build.gif"; //$NON-NLS-1$
	public static final String IMG_TOOLS_MAKE_TARGET_ADD = NAME_PREFIX + "target_add.gif"; //$NON-NLS-1$
	public static final String IMG_TOOLS_MAKE_TARGET_DELETE = NAME_PREFIX + "target_delete.gif"; //$NON-NLS-1$
	public static final String IMG_TOOLS_MAKE_TARGET_EDIT = NAME_PREFIX + "target_edit.gif"; //$NON-NLS-1$
	public static final String IMG_TOOLS_MAKE_TARGET_FILTER = NAME_PREFIX + "target_filter.gif"; //$NON-NLS-1$

	public static final String IMG_OBJS_MAKEFILE_MACRO = NAME_PREFIX + "macro_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_MAKEFILE_MACRO = createManaged(OBJ, IMG_OBJS_MAKEFILE_MACRO);

	public static final String IMG_OBJS_MAKEFILE_ACMACRO = NAME_PREFIX + "acmacro_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_MAKEFILE_ACMACRO = createManaged(OBJ, IMG_OBJS_MAKEFILE_ACMACRO);

	public static final String IMG_OBJS_MAKEFILE_TARGET_RULE = NAME_PREFIX + "trule_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_MAKEFILE_TARGET_RULE = createManaged(OBJ, IMG_OBJS_MAKEFILE_TARGET_RULE);

	public static final String IMG_OBJS_MAKEFILE_INFERENCE_RULE = NAME_PREFIX + "irule_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_MAKEFILE_INFERENCE_RULE = createManaged(OBJ,
			IMG_OBJS_MAKEFILE_INFERENCE_RULE);

	public static final String IMG_OBJS_MAKEFILE_RELATION = NAME_PREFIX + "relation_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_MAKEFILE_RELATION = createManaged(OBJ, IMG_OBJS_MAKEFILE_RELATION);

	public static final String IMG_OBJS_MAKEFILE_COMMAND = NAME_PREFIX + "command_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_MAKEFILE_COMMAND = createManaged(OBJ, IMG_OBJS_MAKEFILE_COMMAND);

	public static final String IMG_OBJS_MAKEFILE_INCLUDE = NAME_PREFIX + "include_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_MAKEFILE_INCLUDE = createManaged(OBJ, IMG_OBJS_MAKEFILE_INCLUDE);

	public static final String IMG_OBJS_ENVIRONMNET = NAME_PREFIX + "environment_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_ENVIRONMENT = createManaged(OBJ, IMG_OBJS_ENVIRONMNET);

	public static final String IMG_OBJS_ENV_VAR = NAME_PREFIX + "envvar_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_ENV_VAR = createManaged(OBJ, IMG_OBJS_ENV_VAR);

	public static final String IMG_TOOLS_ALPHA_SORTING = NAME_PREFIX + "alphab_sort_co.gif"; //$NON-NLS-1$
	public static final String IMG_TOOLS_MAKEFILE_SEGMENT_EDIT = NAME_PREFIX + "segment_edit.gif"; //$NON-NLS-1$

	public static final String IMG_MENU_OPEN_INCLUDE = NAME_PREFIX + "open_include.gif"; //$NON-NLS-1$

	private static ImageDescriptor createManaged(String prefix, String name) {
		return createManaged(imageRegistry, prefix, name);
	}

	private static ImageDescriptor createManaged(ImageRegistry registry, String prefix, String name) {
		ImageDescriptor result = ImageDescriptor
				.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
		registry.put(name, result);
		return result;
	}

	public static Image getImage(String key) {
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
			AutotoolsUIPlugin.log(e);
			return null;
		}
	}

	/**
	 * Sets all available image descriptors for the given action.
	 */
	public static void setImageDescriptors(IAction action, String type, String relPath) {
		relPath = relPath.substring(NAME_PREFIX_LENGTH);
		action.setDisabledImageDescriptor(create("d" + type + "/", relPath)); //$NON-NLS-1$ //$NON-NLS-2$
		//		action.setHoverImageDescriptor(create("e" + type + "/", relPath)); //$NON-NLS-1$ //$NON-NLS-2$
		action.setImageDescriptor(create("e" + type + "/", relPath)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}
}
