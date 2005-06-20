/*******************************************************************************
 * Copyright (c) 2002, 2005 Rational Software Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui;


import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * Bundle of all images used by the C plugin.
 */
public class ManagedBuilderUIImages {
	
	// The plugin registry
	private static ImageRegistry imageRegistry = new ImageRegistry();

	// Subdirectory (under the package containing this class) where 16 color images are
	private static URL iconBaseURL = null;
	static {
		iconBaseURL = Platform.getBundle(ManagedBuilderUIPlugin.getUniqueIdentifier()).getEntry("icons/"); //$NON-NLS-1$
	}	

	private static final String NAME_PREFIX= ManagedBuilderUIPlugin.getUniqueIdentifier() + '.';
	private static final int NAME_PREFIX_LENGTH= NAME_PREFIX.length();

	public static final String T_BUILD= "elcl16/"; //$NON-NLS-1$
	// list icons dir
	public static final String T_LIST= "elcl16/"; //$NON-NLS-1$


	// For the managed build images
	public static final String IMG_BUILD_CONFIG = NAME_PREFIX + "build_configs.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_BUILD_CONFIG = createManaged(T_BUILD, IMG_BUILD_CONFIG);
	public static final String IMG_BUILD_COMPILER = NAME_PREFIX + "config-compiler.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_BUILD_COMPILER = createManaged(T_BUILD, IMG_BUILD_COMPILER);
	public static final String IMG_BUILD_LINKER = NAME_PREFIX + "config-linker.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_BUILD_LINKER = createManaged(T_BUILD, IMG_BUILD_LINKER);
	public static final String IMG_BUILD_LIBRARIAN = NAME_PREFIX + "config-librarian.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_BUILD_LIBRARIAN = createManaged(T_BUILD, IMG_BUILD_LIBRARIAN);
	public static final String IMG_BUILD_COMMAND = NAME_PREFIX + "config-command.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_BUILD_COMMAND = createManaged(T_BUILD, IMG_BUILD_COMMAND);
	public static final String IMG_BUILD_PREPROCESSOR = NAME_PREFIX + "config-preprocessor.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_BUILD_PREPROCESSOR = createManaged(T_BUILD, IMG_BUILD_PREPROCESSOR);
	public static final String IMG_BUILD_TOOL = NAME_PREFIX + "config-tool.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_BUILD_TOOL = createManaged(T_BUILD, IMG_BUILD_TOOL);
	public static final String IMG_BUILD_CAT = NAME_PREFIX + "config-category.gif"; //$NON-NLS-1$
	public static final ImageDescriptor DESC_BUILD_CAT = createManaged(T_BUILD, IMG_BUILD_CAT);
	
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
	
	private static ImageDescriptor create(String prefix, String name) {
		return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
	}
	
	private static URL makeIconFileURL(String prefix, String name) {
		StringBuffer buffer= new StringBuffer(prefix);
		buffer.append(name);
		try {
			return new URL(iconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			ManagedBuilderUIPlugin.log(e);
			return null;
		}
	}
	
	/**
	 * Sets all available image descriptors for the given action.
	 */	
//	public static void setImageDescriptors(IAction action, String type, String relPath) {
//		relPath= relPath.substring(NAME_PREFIX_LENGTH);
//		action.setDisabledImageDescriptor(create(T + "d" + type, relPath)); //$NON-NLS-1$
//		action.setHoverImageDescriptor(create(T + "c" + type, relPath)); //$NON-NLS-1$
//		action.setImageDescriptor(create(T + "e" + type, relPath)); //$NON-NLS-1$
//	}
	
	/**
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}
}
