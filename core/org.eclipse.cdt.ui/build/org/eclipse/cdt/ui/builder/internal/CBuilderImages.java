/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui.builder.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author gene.sally
 *
 * Container for C Builder images. Defines constants to the images in the project
 * and creates the image descriptor objects for these files so we can have nice
 * cached image management the way Eclipse intended.
 * 
 * To add an image:
 * 1) Place it in the icons/all folder in this project.
 * 2) Create a IMG_* constant for it's name along.
 * 3) Create a DESC_IMG_* constant for the image.
 */
public class CBuilderImages {

	// NO I18N on these strings!!!
	private static final String NAME_PREFIX = "org.eclipse.cdt.ui.builder."; //$NON-NLS-1$
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	private static URL m_IconFolder = null;
	private static ImageRegistry m_ImageRegistry = null;

	/**
	 * the icon folder won't change and the other static
	 * methods/constants need this, so init as a static
	 */
	static 
	{
		try {
			m_IconFolder = new URL( CUIPlugin.getDefault().getDescriptor().getInstallURL(), "icons/"); //$NON-NLS-1$
		} catch( MalformedURLException e ) {
			/* do nothing right now, but we should be doing something */
		}
	}
	
	// the following lists all of the images in the system, 
	// the constants will be expanded to something on the filesystem when converted into 
	// image descroptors that are later turned into real Image Objects when needed.

	public final static String IMG_CONFIG_COMPILER = NAME_PREFIX + "config-compiler.gif"; //$NON-NLS-1$
	public final static String IMG_CONFIG_LINKER = NAME_PREFIX + "config-linker.gif"; //$NON-NLS-1$
	public final static String IMG_CONFIG_PREPOCESSOR = NAME_PREFIX + "config-preprocessor.gif"; //$NON-NLS-1$
	public final static String IMG_CONFIG_DEBUG = NAME_PREFIX + "config-debug.gif"; //$NON-NLS-1$
	public final static String IMG_CONFIG_PROFILE = NAME_PREFIX + "config-profile.gif"; //$NON-NLS-1$
	public final static String IMG_CONFIG_RELEASE = NAME_PREFIX + "config-release.gif"; //$NON-NLS-1$
	public final static String IMG_ACTION_NEW_CONFIG = NAME_PREFIX + "action-newconfig.gif"; //$NON-NLS-1$
	public final static String IMG_ACTION_EDIT_CONFIG = NAME_PREFIX + "action-editconfig.gif"; //$NON-NLS-1$
	public final static String IMG_ACTION_BUILD_CONFIG = NAME_PREFIX + "action-buildconfig.gif"; //$NON-NLS-1$
	public final static String IMG_ACTION_DELETE_CONFIG = NAME_PREFIX + "action-deleteconfig.gif"; //$NON-NLS-1$

	// image prefix.  for our purposes, slam all fo these into the same folder
	// but leave the opporiunity for us to create sub-folders if necessary

	public final static String PREFIX_ALL = "full/build16"; //$NON-NLS-1$

	// create the image descriptors from the above constants 	

	public final static ImageDescriptor DESC_IMG_CONFIG_COMPILER = getImageDescriptor(PREFIX_ALL, IMG_CONFIG_COMPILER);
	public final static ImageDescriptor DESC_IMG_CONFIG_LINKER = getImageDescriptor(PREFIX_ALL, IMG_CONFIG_LINKER);
	public final static ImageDescriptor DESC_IMG_CONFIG_PREPOCESSOR = getImageDescriptor(PREFIX_ALL, IMG_CONFIG_PREPOCESSOR);
	public final static ImageDescriptor DESC_IMG_CONFIG_DEBUG = getImageDescriptor(PREFIX_ALL, IMG_CONFIG_DEBUG);
	public final static ImageDescriptor DESC_IMG_CONFIG_PROFILE = getImageDescriptor(PREFIX_ALL, IMG_CONFIG_PROFILE);
	public final static ImageDescriptor DESC_IMG_CONFIG_RELEASE = getImageDescriptor(PREFIX_ALL, IMG_CONFIG_RELEASE);
	public final static ImageDescriptor DESC_IMG_ACTION_NEW_CONFIG = getImageDescriptor(PREFIX_ALL, IMG_ACTION_NEW_CONFIG);
	public final static ImageDescriptor DESC_IMG_ACTION_EDIT_CONFIG = getImageDescriptor(PREFIX_ALL, IMG_ACTION_EDIT_CONFIG);
	public final static ImageDescriptor DESC_IMG_ACTION_BUILD_CONFIG = getImageDescriptor(PREFIX_ALL, IMG_ACTION_BUILD_CONFIG);
	public final static ImageDescriptor DESC_IMG_ACTION_DELETE_CONFIG = getImageDescriptor(PREFIX_ALL, IMG_ACTION_DELETE_CONFIG);

	private static ImageRegistry getImageRegistry() {
		if (null == m_ImageRegistry) {
			Display display = (Display.getCurrent() != null) ? Display.getCurrent() : Display.getDefault();
			m_ImageRegistry = new ImageRegistry(display);
		}
		return m_ImageRegistry;
	}

	/** 
	 * Returns the image object from the image cache matching the requested name
	 * 
	 * @param strImageIdent		the identifier of the inmage, see one of the static IMG_ decls in this calss
	 * @return the image for this item, null if the image was not found
	 * 
	 */
	public static Image getImage(String strImageIdent) {
		return getImageRegistry().get(strImageIdent);
	}

	/**
	 * Gets the location of an image based on it's location on the file system,
	 * 
	 * @param	strPrefix		the folder under the icon folder where this file resides
	 * @param	strName			name of the image file
	 * 
	 * @return the URL to the image requested.
	 */
	private static URL getFilesystemName(String strPrefix, String strName) throws MalformedURLException {
		if (m_IconFolder == null) {
			throw new MalformedURLException();
		}
		StringBuffer buffer = new StringBuffer(strPrefix);
		buffer.append('/');
		buffer.append(strName);
		return new URL(m_IconFolder, buffer.toString());
		
	}
	
	/**
	 * Creates an image descriptor for the requrested image
	 * 
	 * @param	strPrefix		the folder under the icon folder where this file resides
	 * @param	strName			name of the image file
	 * 
	 * @return the requested image descriptor, or image not found if the image could not be located on the file system.
	 */
	private static ImageDescriptor getImageDescriptor(String strPrefix, String strName) {
		ImageDescriptor descriptor = null;
		
		try {
			descriptor = ImageDescriptor.createFromURL(getFilesystemName(strPrefix, strName.substring(NAME_PREFIX_LENGTH)));
			getImageRegistry().put(strName, descriptor);
		}
		catch(MalformedURLException e)	{
			descriptor = ImageDescriptor.getMissingImageDescriptor();
		}
		
		return descriptor;
	}
}