/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.useractions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * Defines the standard icons for user actions, user types, and compile commands.
 * Images and image descriptions may be extracted from these.
 * There is no public constructor. Use the predefined instances of this class
 * to access the images and descriptors.
 */
public class UserActionsIcon {
	/**
	 * A new user defined action.
	 */
	public static final UserActionsIcon USERACTION_NEW = new UserActionsIcon("user_action_new_obj"); //$NON-NLS-1$
	/**
	 * An existing user defined user action.
	 */
	public static final UserActionsIcon USERACTION_USR = new UserActionsIcon("user_action_obj"); //$NON-NLS-1$
	/**
	 * A predefined user defined action.
	 */
	public static final UserActionsIcon USERACTION_IBM = new UserActionsIcon("user_action_ibm_obj"); //$NON-NLS-1$
	/**
	 * A predefined user defined action that has been modified.
	 */
	public static final UserActionsIcon USERACTION_IBMUSR = new UserActionsIcon("user_action_ibm_user_obj"); //$NON-NLS-1$
	/**
	 * A new user defined type.
	 */
	public static final UserActionsIcon USERTYPE_NEW = new UserActionsIcon("user_type_new_obj"); //$NON-NLS-1$
	/**
	 * An existing user defined type.
	 */
	public static final UserActionsIcon USERTYPE_USR = new UserActionsIcon("user_type_obj"); //$NON-NLS-1$
	/**
	 * A predefined user defined type.
	 */
	public static final UserActionsIcon USERTYPE_IBM = new UserActionsIcon("user_type_ibm_obj"); //$NON-NLS-1$
	/**
	 * A predefined user defined type that has been modified.
	 */
	public static final UserActionsIcon USERTYPE_IBMUSR = new UserActionsIcon("user_type_ibm_user_obj"); //$NON-NLS-1$
	/**
	 * A new user defined compile command.
	 */
	public static final UserActionsIcon COMPILE_NEW = new UserActionsIcon("compcmd_new_obj"); //$NON-NLS-1$
	/**
	 * An existing user defined compile command.
	 */
	public static final UserActionsIcon COMPILE_USR = new UserActionsIcon("compcmd_user_obj"); //$NON-NLS-1$
	/**
	 * A predefined user defined compile command.
	 */
	public static final UserActionsIcon COMPILE_IBM = new UserActionsIcon("compcmd_ibm_obj"); //$NON-NLS-1$
	/**
	 * A predefined user defined compile command that has been edited.
	 */
	public static final UserActionsIcon COMPILE_IBMUSR = new UserActionsIcon("compcmd_ibmuser_obj"); //$NON-NLS-1$	
	private static final String PREFIX = "icon."; //$NON-NLS-1$
	private static final String ICON_DIR = "icons/full/obj16/"; //$NON-NLS-1$
	private static final String ICON_EXT = ".gif"; //$NON-NLS-1$
	private String name;
	private String id;
	private String location;

	private UserActionsIcon(String name) {
		this.name = name;
		this.id = PREFIX + name;
		this.location = ICON_DIR + name + ICON_EXT;
	}

	/**
	 * Gets the image associated with this icon. This image is stored in the image registry of the 
	 * user actions plugin and therefore the image must not be disposed when its use is completed.
	 * @return the image
	 */
	public Image getImage() {
		ImageRegistry registry = Activator.getDefault().getImageRegistry();
		Image image = registry.get(id);
		if (image == null) {
			ImageDescriptor descriptor = getImageDescriptor();
			image = descriptor.createImage();
			registry.put(id, image);
		}
		return image;
	}

	/**
	 * Gets the image descriptor associated with this icon.
	 * @return the image descriptor
	 */
	public ImageDescriptor getImageDescriptor() {
		ImageDescriptor descriptor = Activator.getImageDescriptor(location);
		return descriptor;
	}

	/**
	 * @return the name of the icon
	 */
	public String getName() {
		return name;
	}
}
