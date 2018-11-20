/*******************************************************************************
 * Copyright (c) 2002, 2010 Rational Software Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Images for {@link org.eclipse.cdt.utils.ui.controls.FileListControl}.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 *
 * @deprecated as of CDT 8.0. Use {@link CDTSharedImages}.
 */
@Deprecated
public class CDTUIImages {
	private static final String ICONS = "icons/"; //$NON-NLS-1$
	/** Converter from CPluginImages key to CDTSharedImages key */
	private static Map<String, String> fPathMap = new HashMap<>();

	private static final String NAME_PREFIX = CUIPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();
	public static final String T_LIST = "elcl16/"; //$NON-NLS-1$

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

	/**
	 * Creates an image descriptor which is managed by internal registry in CDTSharedImages.
	 * {@code name} is assumed to start with "org.eclipse.cdt.ui."
	 */
	private static ImageDescriptor createManaged(String prefix, String name) {
		try {
			String convertedKey = ICONS + prefix + name.substring(NAME_PREFIX_LENGTH);
			fPathMap.put(name, convertedKey);
			return CDTSharedImages.getImageDescriptor(convertedKey);
		} catch (Throwable e) {
			CUIPlugin.log(e);
		}
		return ImageDescriptor.getMissingImageDescriptor();
	}

	/**
	 * Get an image from internal image registry. The image is managed by the registry and
	 * must not be disposed by the caller.
	 *
	 * @param key - one of {@code CDTUIImages.IMG_} constants.
	 * @return the image corresponding the given key.
	 *
	 * @deprecated as of CDT 8.0. Use {@link CDTSharedImages#getImage(String)}.
	 */
	@Deprecated
	public static Image get(String key) {
		String pathKey = fPathMap.get(key);
		return CDTSharedImages.getImage(pathKey);
	}
}
