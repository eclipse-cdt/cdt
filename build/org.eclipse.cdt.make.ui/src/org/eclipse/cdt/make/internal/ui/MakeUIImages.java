/*
 * Created on 19-Aug-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;

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
			fgIconBaseURL = new URL(MakeUIPlugin.getDefault().getDescriptor().getInstallURL(), "icons/"); //$NON-NLS-1$
		} catch (MalformedURLException e) {
			MakeUIPlugin.log(e);
		}
	}
	private static final String NAME_PREFIX = MakeUIPlugin.getUniqueIdentifier() + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	public static final String OBJ = "obj16/"; //$NON-NLS-1$

	// For the build image
	public static final String IMG_OBJS_BUILD_TARGET = NAME_PREFIX + "target_obj.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_BUILD_TARGET = createManaged(OBJ, IMG_OBJS_BUILD_TARGET);

	public static final String IMG_OBJS_ERROR = NAME_PREFIX + "error_obj.gif";
	public static final ImageDescriptor DESC_OBJ_ERROR = createManaged(OBJ, IMG_OBJS_ERROR);


	public static final String IMG_TOOLS_MAKE_TARGET_BUILD = NAME_PREFIX + "target_build.gif"; //$NON-NLS-1$
	public static final String IMG_TOOLS_MAKE_TARGET_ADD = NAME_PREFIX + "target_add.gif"; //$NON-NLS-1$
	public static final String IMG_TOOLS_MAKE_TARGET_DELETE = NAME_PREFIX + "target_delete.gif"; //$NON-NLS-1$
	public static final String IMG_TOOLS_MAKE_TARGET_EDIT = NAME_PREFIX + "target_edit.gif"; //$NON-NLS-1$


	private static ImageDescriptor createManaged(String prefix, String name) {
		return createManaged(imageRegistry, prefix, name);
	}

	private static ImageDescriptor createManaged(ImageRegistry registry, String prefix, String name) {
		ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
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
		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append(name);
		try {
			return new URL(fgIconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			MakeUIPlugin.log(e);
			return null;
		}
	}

	/**
	 * Sets all available image descriptors for the given action.
	 */
	public static void setImageDescriptors(IAction action, String type, String relPath) {
		relPath = relPath.substring(NAME_PREFIX_LENGTH);
		action.setDisabledImageDescriptor(create("d" + type + "/", relPath)); //$NON-NLS-1$
		action.setHoverImageDescriptor(create("c" + type + "/", relPath)); //$NON-NLS-1$
		action.setImageDescriptor(create("e" + type + "/", relPath)); //$NON-NLS-1$
	}

	/**
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}
}
