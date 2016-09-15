/*******************************************************************************
 * Copyright (c) 2014, 2016 Red Hat and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class SWTImagesFactory {
	// The plug-in registry
	private static ImageRegistry imageRegistry = DockerLaunchUIPlugin
			.getDefault()
			.getImageRegistry();

	// Sub-directory (under the package containing this class) where 16 color
	// images are
	private static URL fgIconBaseURL;

	static {
		try {
			fgIconBaseURL = new URL(
					DockerLaunchUIPlugin.getDefault().getBundle()
					.getEntry("/"), "icons/"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (MalformedURLException e) {
			DockerLaunchUIPlugin.log(e);
		}
	}

	private static final String NAME_PREFIX = DockerLaunchUIPlugin.PLUGIN_ID
			+ '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();
	public static final String IMG_CONTAINER = NAME_PREFIX
			+ "repository-middle.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_CONTAINER = createManaged("",
			IMG_CONTAINER);

	private static ImageDescriptor createManaged(String prefix, String name) {
		return createManaged(imageRegistry, prefix, name);
	}

	private static ImageDescriptor createManaged(ImageRegistry registry,
			String prefix, String name) {
		ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(
				prefix, name.substring(NAME_PREFIX_LENGTH)));
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
			DockerLaunchUIPlugin.log(e);
			return null;
		}
	}

	/**
	 * Sets all available image descriptors for the given action.
	 * 
	 * @param action
	 *            to set descriptor for
	 * @param type
	 *            of image descriptor
	 * @param relPath
	 *            relative path
	 */
	public static void setImageDescriptors(IAction action, String type,
			String relPath) {
		if (relPath.startsWith(NAME_PREFIX))
			relPath = relPath.substring(NAME_PREFIX_LENGTH);
		action.setDisabledImageDescriptor(create("d" + type, relPath)); //$NON-NLS-1$
		action.setImageDescriptor(create("e" + type, relPath)); //$NON-NLS-1$

	}

	/**
	 * Helper method to access the image registry from the CUIPlugin class.
	 */
	static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}

}
