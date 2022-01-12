/*******************************************************************************
 * Copyright (c) 2010,2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.ui;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

public class RemoteUIImages {

	public static final IPath ICONS_PATH = new Path("icons"); //$NON-NLS-1$

	private static final String NAME_PREFIX = RemoteUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

	private static ImageRegistry fgImageRegistry = new ImageRegistry();

	private static final String T_ELCL = "elcl16"; //$NON-NLS-1$
	private static final String T_DLCL = "dlcl16"; //$NON-NLS-1$
	private static final String T_OVR = "ovr16"; //$NON-NLS-1$

	/*
	 * Keys for images available from the plug-in image registry.
	 */
	public static final String IMG_ELCL_UP_NAV = NAME_PREFIX + T_ELCL + ".up_nav.gif"; //$NON-NLS-1$
	public static final String IMG_DLCL_UP_NAV = NAME_PREFIX + T_DLCL + ".up_nav.gif"; //$NON-NLS-1$
	public static final String IMG_OVR_SYMLINK = NAME_PREFIX + T_OVR + ".symlink_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_ELCL_NEW_FOLDER = NAME_PREFIX + T_ELCL + ".new_folder.gif"; //$NON-NLS-1$
	public static final String IMG_DLCL_NEW_FOLDER = NAME_PREFIX + T_DLCL + ".new_folder.gif"; //$NON-NLS-1$
	public static final String IMG_DEFAULT_TYPE = NAME_PREFIX + "defaultType"; //$NON-NLS-1$
	
	/*
	 * Set of predefined Image Descriptors.
	 */
	public static final ImageDescriptor DESC_ELCL_UP_NAV = createManaged(T_ELCL, "up_nav.gif", IMG_ELCL_UP_NAV); //$NON-NLS-1$
	public static final ImageDescriptor DESC_DLCL_UP_NAV = createManaged(T_DLCL, "up_nav.gif", IMG_ELCL_UP_NAV); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_SYMLINK = createManaged(T_OVR, "symlink_ovr.gif", IMG_OVR_SYMLINK); //$NON-NLS-1$
	public static final ImageDescriptor DESC_ELCL_NEW_FOLDER = createManaged(T_ELCL, "new_folder.gif", IMG_ELCL_NEW_FOLDER); //$NON-NLS-1$
	public static final ImageDescriptor DESC_DLCL_NEW_FOLDER = createManaged(T_DLCL, "new_folder.gif", IMG_DLCL_NEW_FOLDER); //$NON-NLS-1$
	public static final ImageDescriptor DESC_DEFAULT_TYPE = createManaged(ICONS_PATH.append("console.png"), IMG_DEFAULT_TYPE); //$NON-NLS-1$

	/**
	 * Returns the image managed under the given key in this registry.
	 * 
	 * @param key
	 *            the image's key
	 * @return the image managed under the given key
	 */
	public static Image get(String key) {
		return fgImageRegistry.get(key);
	}

	/**
	 * Returns the image descriptor for the given key in this registry. Might be called in a non-UI thread.
	 * 
	 * @param key
	 *            the image's key
	 * @return the image descriptor for the given key
	 */
	public static ImageDescriptor getDescriptor(String key) {
		return fgImageRegistry.getDescriptor(key);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions
	 * are retrieved from the *lcl16 folders.
	 * 
	 * @param action
	 *            the action
	 * @param iconName
	 *            the icon name
	 */
	public static void setLocalImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "lcl16", iconName); //$NON-NLS-1$
	}

	// ---- Helper methods to access icons on the file system --------------------------------------

	private static void setImageDescriptors(IAction action, String type, String relPath) {
		ImageDescriptor id = create("d" + type, relPath, false); //$NON-NLS-1$
		if (id != null) {
			action.setDisabledImageDescriptor(id);
		}

		/*
		 * id= create("c" + type, relPath, false); //$NON-NLS-1$
		 * if (id != null)
		 * action.setHoverImageDescriptor(id);
		 */

		ImageDescriptor descriptor = create("e" + type, relPath, true); //$NON-NLS-1$
		action.setHoverImageDescriptor(descriptor);
		action.setImageDescriptor(descriptor);
	}

	private static ImageDescriptor createManaged(IPath path, String key) {
		ImageDescriptor desc = createImageDescriptor(RemoteUIPlugin.getDefault().getBundle(), path, true);
		fgImageRegistry.put(key, desc);
		return desc;
	}

	private static ImageDescriptor createManaged(String prefix, String name, String key) {
		ImageDescriptor image = create(prefix, name, true);
		fgImageRegistry.put(key, image);
		return image;
	}

	/*
	 * Creates an image descriptor for the given prefix and name in the JDT UI bundle. The path can
	 * contain variables like $NL$.
	 * If no image could be found, <code>useMissingImageDescriptor</code> decides if either
	 * the 'missing image descriptor' is returned or <code>null</code>.
	 * or <code>null</code>.
	 */
	private static ImageDescriptor create(String prefix, String name, boolean useMissingImageDescriptor) {
		IPath path = ICONS_PATH.append(prefix).append(name);
		return createImageDescriptor(RemoteUIPlugin.getDefault().getBundle(), path, useMissingImageDescriptor);
	}

	/*
	 * Creates an image descriptor for the given path in a bundle. The path can contain variables
	 * like $NL$.
	 * If no image could be found, <code>useMissingImageDescriptor</code> decides if either
	 * the 'missing image descriptor' is returned or <code>null</code>.
	 * Added for 3.1.1.
	 */
	public static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path, boolean useMissingImageDescriptor) {
		URL url = FileLocator.find(bundle, path, null);
		if (url != null) {
			return ImageDescriptor.createFromURL(url);
		}
		if (useMissingImageDescriptor) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
		return null;
	}

}
