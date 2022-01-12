/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui;

import java.net.URL;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.SharedImagesFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

/**
 * A repository for common images used by org.eclipse.cdt.make.ui plugin.
 * <p>
 * This class provides {@link Image} and {@link ImageDescriptor}
 * for each named image in the interface.  All {@code Image} objects provided
 * by this class are managed by this class and must never be disposed
 * by other clients.
 * </p>
 * <p>
 * For common CDT images see {@link CDTSharedImages}.
 * <p>
 * For common platform images see {@link org.eclipse.ui.ISharedImages}
 * ({@code org.eclipse.ui.PlatformUI.getWorkbench().getSharedImages()})
 * <br>
 * and {@link org.eclipse.ui.ide.IDE.SharedImages}.
 * </p>
 * <p>
 * Note that org.eclipse.cdt.ui.tests.misc.MakeUIImagesTest will verify
 * existence of the images defined here.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MakeUIImages {
	// images
	public static final String IMG_OBJS_TARGET = "icons/obj16/target_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INFERENCE_RULE = "icons/obj16/irule_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_RELATION = "icons/obj16/relation_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_COMMAND = "icons/obj16/command_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDE = "icons/obj16/include_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ENVIRONMENT = "icons/obj16/environment_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ENV_VAR = "icons/obj16/envvar_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_AUTO_VARIABLE = "icons/obj16/var_auto.png"; //$NON-NLS-1$
	public static final String IMG_OBJS_FUNCTION = "icons/obj16/builtin_func.png"; //$NON-NLS-1$

	public static final String IMG_ETOOL_MAKEFILE = "icons/etool16/makefile.gif"; //$NON-NLS-1$
	public static final String IMG_ETOOL_TARGET_BUILD = "icons/etool16/target_build.png"; //$NON-NLS-1$
	public static final String IMG_DTOOL_TARGET_BUILD = "icons/dtool16/target_build.png"; //$NON-NLS-1$
	public static final String IMG_ETOOL_TARGET_ADD = "icons/etool16/target_add.gif"; //$NON-NLS-1$
	public static final String IMG_DTOOL_TARGET_ADD = "icons/dtool16/target_add.gif"; //$NON-NLS-1$
	public static final String IMG_ETOOL_TARGET_EDIT = "icons/etool16/target_edit.gif"; //$NON-NLS-1$
	public static final String IMG_DTOOL_TARGET_EDIT = "icons/dtool16/target_edit.gif"; //$NON-NLS-1$
	public static final String IMG_ETOOL_TARGET_DELETE = "icons/etool16/target_delete.gif"; //$NON-NLS-1$
	public static final String IMG_DTOOL_TARGET_DELETE = "icons/dtool16/target_delete.gif"; //$NON-NLS-1$
	public static final String IMG_ETOOL_TARGET_FILTER = "icons/etool16/target_filter.gif"; //$NON-NLS-1$
	public static final String IMG_DTOOL_TARGET_FILTER = "icons/dtool16/target_filter.gif"; //$NON-NLS-1$
	public static final String IMG_ETOOL_ALPHA_SORTING = "icons/etool16/alphab_sort_co.gif"; //$NON-NLS-1$
	public static final String IMG_DTOOL_ALPHA_SORTING = "icons/dtool16/alphab_sort_co.gif"; //$NON-NLS-1$
	public static final String IMG_ETOOL_SEGMENT_EDIT = "icons/etool16/segment_edit.gif"; //$NON-NLS-1$
	public static final String IMG_DTOOL_SEGMENT_EDIT = "icons/dtool16/segment_edit.gif"; //$NON-NLS-1$

	// overlays
	public static final String IMG_OVR_AUTOMATIC = "icons/ovr16/auto_co.gif"; //$NON-NLS-1$
	public static final String IMG_OVR_SPECIAL = "icons/ovr16/special_co.gif"; //$NON-NLS-1$

	private static SharedImagesFactory imagesFactory = new SharedImagesFactory(MakeUIPlugin.getDefault());

	/**
	 * Internal method. It lets register image URL from a bundle directly to the map.
	 * It is user responsibility to ensure that a valid URL is passed.
	 *
	 * @param url - URL of the image pointing to its location in a bundle (bundle entry).
	 *
	 * @noreference This is internal method which is not intended to be referenced by clients.
	 */
	public static void register(URL url) {
		imagesFactory.register(url);
	}

	/**
	 * The method retrieves an image from the internal repository according to the given key.
	 * The image is managed by image registry and the caller must not dispose it.
	 *
	 * @param key - one of {@code MakeUISharedImages.IMG_} constants.
	 * <p>
	 * Reserved for internal usage: the key could be a string representation of URL pointing to location
	 * of the image in the bundle. Such URL key must be registered first with {@code register(URL url)}.
	 * </p>
	 * @return the image from the repository or the default image for missing image descriptor.
	 */
	public static Image getImage(String key) {
		return imagesFactory.getImage(key);
	}

	/**
	 * The method retrieves an image descriptor from the internal repository according to the given key.
	 * See also {@link #getImage(String)}.
	 *
	 * @param key - one of {@code MakeUISharedImages.IMG_} constants.
	 * @return the image from the repository or {@link ImageDescriptor#getMissingImageDescriptor()}.
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return imagesFactory.getImageDescriptor(key);
	}

	/**
	 * Retrieves an overlaid image from the internal repository of images.
	 * If there is no image one will be created.
	 *
	* The decoration overlay for the base image will use the array of
	 * provided overlays. The indices of the array correspond to the values
	 * of the 5 overlay constants defined on {@link IDecoration}, i.e.
	 * {@link IDecoration#TOP_LEFT},
	 * {@link IDecoration#TOP_RIGHT},
	 * {@link IDecoration#BOTTOM_LEFT},
	 * {@link IDecoration#BOTTOM_RIGHT} or
	 * {@link IDecoration#UNDERLAY}.
	 *
	 * @param baseKey the base image key.
	 * @param overlayKeys the keys for the overlay images. Must be
	 *    String[5], i.e. string array of 5 elements. Put {@code null} as
	 *    an element to the array if no overlay should be added in given quadrant.
	 */
	public static Image getImageOverlaid(String baseKey, String[] overlayKeys) {
		return imagesFactory.getImageOverlaid(baseKey, overlayKeys);
	}

	/**
	 * Retrieves an overlaid image descriptor from the repository of images.
	 * If there is no image one will be created.
	 *
	 * @param baseKey - key of the base image. Expected to be in repository.
	 * @param overlayKey - key of overlay image. Expected to be in repository as well.
	 * @param quadrant - location of overlay, one of those:
	 *        {@link IDecoration#TOP_LEFT},
	 *        {@link IDecoration#TOP_RIGHT},
	 *        {@link IDecoration#BOTTOM_LEFT},
	 *        {@link IDecoration#BOTTOM_RIGHT}
	 *
	 * @return image overlaid with smaller image in the specified quadrant.
	 */
	public static Image getImageOverlaid(String baseKey, String overlayKey, int quadrant) {
		return imagesFactory.getImageOverlaid(baseKey, overlayKey, quadrant);
	}
}
