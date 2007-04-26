/*******************************************************************************
 * Copyright (c) 2002, 2007 Rational Software Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;


import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;


/**
 * Bundle of all images used by the C plugin.
 */
public class CDTUIImages {
	
	// The plugin registry
	private static ImageRegistry imageRegistry = new ImageRegistry();

	// Subdirectory (under the package containing this class) where 16 color images are
	private static URL iconBaseURL = null;
	static {
		iconBaseURL = Platform.getBundle(CUIPlugin.PLUGIN_ID).getEntry("icons/"); //$NON-NLS-1$
	}	

	private static final String NAME_PREFIX= CUIPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH= NAME_PREFIX.length();
	public static final String T_LIST= "elcl16/"; //$NON-NLS-1$
	
	// Image for file list control
	public static final String IMG_FILELIST_ADD = NAME_PREFIX + "list-add.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_FILELIST_ADD = createManaged(T_LIST, IMG_FILELIST_ADD);
	public static final String IMG_FILELIST_DEL = NAME_PREFIX + "list-delete.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_FILELIST_DEL = createManaged(T_LIST, IMG_FILELIST_DEL);
	public static final String IMG_FILELIST_EDIT = NAME_PREFIX + "list-edit.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_FILELIST_EDIT = createManaged(T_LIST, IMG_FILELIST_EDIT);
	public static final String IMG_FILELIST_MOVEUP = NAME_PREFIX + "list-moveup.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_FILELIST_MOVEUP = createManaged(T_LIST, IMG_FILELIST_MOVEUP);
	public static final String IMG_FILELIST_MOVEDOWN = NAME_PREFIX + "list-movedown.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_FILELIST_MOVEDOWN = createManaged(T_LIST, IMG_FILELIST_MOVEDOWN);
	
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
			return new URL(iconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			CUIPlugin.getDefault().log(e);
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
