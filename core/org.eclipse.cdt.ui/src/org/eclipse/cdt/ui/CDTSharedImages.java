/*******************************************************************************
 * Copyright (c) 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *    Patrick Chuong (Texas Instruments) - Add support for icon overlay in the debug view (Bug 334566)
 *******************************************************************************/
package org.eclipse.cdt.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

/**
 * A repository for common images used by the CDT which may be useful to other plug-ins.
 * <p>
 * This class provides {@link Image} and {@link ImageDescriptor}
 * for each named image in the interface.  All {@code Image} objects provided
 * by this class are managed by this class and must never be disposed
 * by other clients.
 * </p>
 * <p>
 * For common platform images see {@link org.eclipse.ui.ISharedImages}
 * ({@code org.eclipse.ui.PlatformUI.getWorkbench().getSharedImages()})
 * <br>
 * and {@link org.eclipse.ui.ide.IDE.SharedImages}.
 * </p>
 * <p>
 * Note that org.eclipse.cdt.ui.tests.misc.CDTSharedImagesTests will verify
 * existence of the images defined here.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 *
 * @since 5.3
 */
public class CDTSharedImages {
	private static final char OVERLAY_SEPARATOR = '.';
	private static ImageRegistry imageRegistry = new ImageRegistry(CUIPlugin.getStandardDisplay());
	private static Map<String, URL> urlMap = new HashMap<String, URL>();

	public static final String IMG_OBJS_TEMPLATE = "icons/obj16/template_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_VARIABLE = "icons/obj16/variable_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_LOCAL_VARIABLE = "icons/obj16/variable_local_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CLASS = "icons/obj16/class_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CLASS_ALT = "icons/obj16/classfo_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_NAMESPACE = "icons/obj16/namespace_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_USING = "icons/obj16/using_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_STRUCT = "icons/obj16/struct_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_STRUCT_ALT = "icons/obj16/structfo_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_UNION = "icons/obj16/union_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_UNION_ALT = "icons/obj16/unionfo_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TYPEDEF = "icons/obj16/typedef_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TYPEDEF_ALT = "icons/obj16/typedeffo_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ENUMERATION = "icons/obj16/enum_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ENUMERATION_ALT = "icons/obj16/enumfo_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_UNKNOWN_TYPE = "icons/obj16/unknown_type_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ENUMERATOR = "icons/obj16/enumerator_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_FUNCTION = "icons/obj16/function_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_PUBLIC_METHOD = "icons/obj16/method_public_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_PROTECTED_METHOD = "icons/obj16/method_protected_obj.gif"; //$NON-NLS-1$

	public static final String IMG_OBJS_PRIVATE_METHOD = "icons/obj16/method_private_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_PUBLIC_FIELD = "icons/obj16/field_public_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_PROTECTED_FIELD = "icons/obj16/field_protected_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_PRIVATE_FIELD = "icons/obj16/field_private_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_KEYWORD = "icons/obj16/keyword_obj.gif"; //$NON-NLS-1$

	public static final String IMG_OBJS_DECLARATION = "icons/obj16/cdeclaration_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_VAR_DECLARATION = "icons/obj16/var_declaration_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDE = "icons/obj16/include_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_MACRO = "icons/obj16/define_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_LABEL = "icons/obj16/label_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TUNIT = "icons/obj16/c_file_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TUNIT_HEADER = "icons/obj16/h_file_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TUNIT_ASM = "icons/obj16/s_file_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TUNIT_RESOURCE = "icons/obj16/c_resource_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TUNIT_RESOURCE_H = "icons/obj16/ch_resource_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_TUNIT_RESOURCE_A = "icons/obj16/asm_resource_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_SOURCE_ROOT = "icons/obj16/sroot_obj.gif"; // $NON-NLS-1$  //$NON-NLS-1$
	public static final String IMG_OBJS_SOURCE2_ROOT = "icons/obj16/sroot2_obj.gif"; // $NON-NLS-1$  //$NON-NLS-1$
	public static final String IMG_OBJS_FOLDER = "icons/obj16/fldr_obj.gif"; // $NON-NLS-1$  //$NON-NLS-1$
	public static final String IMG_OBJS_CFOLDER = "icons/obj16/cfolder_obj.gif"; // $NON-NLS-1$  //$NON-NLS-1$
	public static final String IMG_OBJS_CONFIG = "icons/obj16/config.gif"; // $NON-NLS-1$  //$NON-NLS-1$
	public static final String IMG_OBJS_ARCHIVE = "icons/obj16/ar_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_BINARY = "icons/obj16/bin_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_SHLIB = "icons/obj16/shlib_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CEXEC = "icons/obj16/exec_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CEXEC_DEBUG = "icons/obj16/exec_dbg_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CORE = "icons/obj16/core_obj.gif"; //$NON-NLS-1$

	public static final String IMG_OBJS_CONTAINER = "icons/obj16/container_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ARCHIVES_CONTAINER = "icons/obj16/archives_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_BINARIES_CONTAINER = "icons/obj16/binaries_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_OUTPUT_FOLDER = "icons/obj16/output_folder_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_LIBRARY = "icons/obj16/lib_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDES_CONTAINER = "icons/obj16/includes_container.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDES_FOLDER = "icons/obj16/hfolder_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDES_FOLDER_PROJECT = "icons/obj16/hfolder_prj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDES_FOLDER_WORKSPACE = "icons/obj16/wsp_includefolder.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_QUOTE_INCLUDES_FOLDER = "icons/obj16/hfolder_quote_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCLUDES_FOLDER_SYSTEM = "icons/obj16/fldr_sys_obj.gif"; //$NON-NLS-1$
	/** @since 5.4 */
	public static final String IMG_OBJS_FRAMEWORKS_FOLDER = "icons/obj16/frameworks.png"; //$NON-NLS-1$
	public static final String IMG_OBJS_MACROS_FILE= "icons/obj16/macros_file.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_LIBRARY_FOLDER=  "icons/obj16/fldr_lib_obj.gif"; // $NON-NLS-1$  //$NON-NLS-1$
	public static final String IMG_OBJS_ORDER = "icons/obj16/cp_order_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_EXCLUSION_FILTER_ATTRIB = "icons/obj16/exclusion_filter_attrib.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_SOURCE_ATTACH_ATTRIB = "icons/obj16/source_attach_attrib.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_IMPORT_SETTINGS = "icons/obj16/import_settings_wiz.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_EXPORT_SETTINGS = "icons/obj16/export_settings_wiz.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_INCCONT = "icons/obj16/incc_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_EXTENSION = "icons/obj16/extension_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_USER = "icons/obj16/person-me.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CDT_TESTING = "icons/obj16/flask.png"; //$NON-NLS-1$

	public static final String IMG_OBJS_NLS_NEVER_TRANSLATE = "icons/obj16/never_translate.gif"; //$NON-NLS-1$

	// Breakpoint images
	public static final String IMG_OBJS_BREAKPOINT = "icons/obj16/breakpoint.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_BREAKPOINT_DISABLED = "icons/obj16/breakpoint_disabled.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_BREAKPOINT_ACTIVE = "icons/obj16/breakpoint_active.gif"; //$NON-NLS-1$

	public static final String IMG_OBJS_FIXABLE_PROBLEM = "icons/obj16/quickfix_warning_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_FIXABLE_ERROR = "icons/obj16/quickfix_error_obj.gif"; //$NON-NLS-1$

	// unknown type
	public static final String IMG_OBJS_UNKNOWN = "icons/obj16/unknown_obj.gif"; //$NON-NLS-1$

	// For the build image
	public static final String IMG_OBJS_BUILD = "icons/obj16/build_menu.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_FILESYSTEM = "icons/obj16/filesyst.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_WORKSPACE = "icons/obj16/workspace.gif"; //$NON-NLS-1$

	//for search
	public static final String IMG_OBJS_SEARCH_REF = "icons/obj16/search_ref_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_SEARCH_DECL = "icons/obj16/search_decl_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_SEARCH_LINE = "icons/obj16/searchm_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CSEARCH = "icons/obj16/csearch_obj.gif"; //$NON-NLS-1$

	public static final String IMG_OBJS_SEARCHFOLDER = "icons/obj16/fldr_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_SEARCHPROJECT = "icons/obj16/cprojects.gif"; //$NON-NLS-1$

	// refactoring
	public static final String IMG_OBJS_REFACTORING_FATAL = "icons/obj16/fatalerror_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_REFACTORING_ERROR = "icons/obj16/error_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_REFACTORING_WARNING = "icons/obj16/warning_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_REFACTORING_INFO = "icons/obj16/info_obj.gif"; //$NON-NLS-1$

	public static final String IMG_OBJS_PREFERRED = "icons/obj16/tc_preferred.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_EMPTY = "icons/obj16/tc_empty.gif"; //$NON-NLS-1$

	public static final String IMG_OBJS_QUICK_ASSIST = "icons/obj16/quickassist_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CORRECTION_ADD = "icons/obj16/correction_add.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CORRECTION_CHANGE = "icons/obj16/correction_change.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CORRECTION_RENAME = "icons/obj16/correction_rename.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_CORRECTION_LINKED_RENAME = "icons/obj16/correction_linked_rename.gif"; //$NON-NLS-1$

	public static final String IMG_VIEW_BUILD_CONSOLE = "icons/view16/buildconsole.gif"; //$NON-NLS-1$

	// Images for file list control
	public static final String IMG_FILELIST_ADD = "icons/elcl16/list-add.gif"; //$NON-NLS-1$
	public static final String IMG_FILELIST_DEL = "icons/elcl16/list-delete.gif"; //$NON-NLS-1$
	public static final String IMG_FILELIST_EDIT = "icons/elcl16/list-edit.gif"; //$NON-NLS-1$
	public static final String IMG_FILELIST_MOVEUP = "icons/elcl16/list-moveup.gif"; //$NON-NLS-1$
	public static final String IMG_FILELIST_MOVEDOWN = "icons/elcl16/list-movedown.gif"; //$NON-NLS-1$

	// overlays
	public static final String IMG_OVR_WARNING = "icons/ovr16/warning_co.gif"; //$NON-NLS-1$
	public static final String IMG_OVR_ERROR = "icons/ovr16/error_co.gif"; //$NON-NLS-1$
	public static final String IMG_OVR_SETTING = "icons/ovr16/setting_nav.gif"; //$NON-NLS-1$
	public static final String IMG_OVR_INACTIVE = "icons/ovr16/inactive_co.gif"; //$NON-NLS-1$

	// Pin & Clone
    public static final String IMG_THREAD_SUSPENDED_R_PINNED = "icons/obj16/threads_obj_r.gif"; //$NON-NLS-1$
    public static final String IMG_THREAD_SUSPENDED_G_PINNED = "icons/obj16/threads_obj_g.gif"; //$NON-NLS-1$
    public static final String IMG_THREAD_SUSPENDED_B_PINNED = "icons/obj16/threads_obj_b.gif"; //$NON-NLS-1$

    public static final String IMG_THREAD_RUNNING_R_PINNED = "icons/obj16/thread_obj_r.gif"; //$NON-NLS-1$
    public static final String IMG_THREAD_RUNNING_G_PINNED = "icons/obj16/thread_obj_g.gif"; //$NON-NLS-1$
    public static final String IMG_THREAD_RUNNING_B_PINNED = "icons/obj16/thread_obj_b.gif"; //$NON-NLS-1$

    public static final String IMG_CONTAINER_SUSPENDED_R_PINNED = "icons/obj16/debugts_obj_r.gif"; //$NON-NLS-1$
    public static final String IMG_CONTAINER_SUSPENDED_G_PINNED = "icons/obj16/debugts_obj_g.gif"; //$NON-NLS-1$
    public static final String IMG_CONTAINER_SUSPENDED_B_PINNED = "icons/obj16/debugts_obj_b.gif"; //$NON-NLS-1$

    public static final String IMG_CONTAINER_RUNNING_R_PINNED = "icons/obj16/debugt_obj_r.gif"; //$NON-NLS-1$
    public static final String IMG_CONTAINER_RUNNING_G_PINNED = "icons/obj16/debugt_obj_g.gif"; //$NON-NLS-1$
    public static final String IMG_CONTAINER_RUNNING_B_PINNED = "icons/obj16/debugt_obj_b.gif"; //$NON-NLS-1$

    public static final String IMG_VIEW_PIN_ACTION = "icons/obj16/toolbar_pinned.gif"; //$NON-NLS-1$
    public static final String IMG_VIEW_PIN_ACTION_R = "icons/obj16/toolbar_pinned_r.gif"; //$NON-NLS-1$
    public static final String IMG_VIEW_PIN_ACTION_G = "icons/obj16/toolbar_pinned_g.gif"; //$NON-NLS-1$
    public static final String IMG_VIEW_PIN_ACTION_B = "icons/obj16/toolbar_pinned_b.gif"; //$NON-NLS-1$
    public static final String IMG_VIEW_PIN_ACTION_MULTI = "icons/obj16/toolbar_pinned_multi.gif"; //$NON-NLS-1$

	/**
	 * The method finds URL of the image corresponding to the key which could be project-relative path
	 * of the image in org.eclipse.cdt.ui plugin or a (previously registered) string representation of URL
	 * in a bundle.
	 * For project-relative paths a check on existence and variables expansion (such as "$NL$")
	 * is done using {@link FileLocator}.
	 *
	 * @param key - the key which could be project-relative path of the image in org.eclipse.cdt.ui plugin
	 *     or a previously registered string representation of URL in a bundle.
	 * @return the URL or {@code null} if image was not found.
	 */
	private static URL getUrl(String key) {
		// Note that the map can keep null URL in order not to search again
		if (urlMap.containsKey(key))
			return urlMap.get(key);

		IPath projectRelativePath = new Path(key);
		URL url = FileLocator.find(CUIPlugin.getDefault().getBundle(), projectRelativePath, null);
		if (url==null) {
			Exception e = new Exception(NLS.bind(Messages.CDTSharedImages_MissingImage, key, CUIPlugin.PLUGIN_ID));
			CUIPlugin.log(e.getMessage(), e);
		}
		urlMap.put(key, url);
		return url;
	}

	/**
	 * Internal method. It lets register image URL from a bundle directly to the map.
	 * It is user responsibility to ensure that a valid URL is passed.
	 *
	 * @param url - URL of the image pointing to its location in a bundle (bundle entry).
	 *
	 * @noreference This is internal method which is not intended to be referenced by clients.
	 */
	public static void register(URL url) {
		urlMap.put(url.toString(), url);
	}

	/**
	 * The method retrieves an image from the internal repository according to the given key.
	 * The image is managed by image registry and the caller must not dispose it.
	 *
	 * @param key - one of {@code CDTSharedImages.IMG_} constants.
	 * <p>
	 * Reserved for internal usage: the key could be a string representation of URL pointing to location
	 * of the image in the bundle. Such URL key must be registered first with {@code register(URL url)}.
	 * </p>
	 * @return the image from the repository or the default image for missing image descriptor.
	 */
	public static Image getImage(String key) {
		URL url = getUrl(key);
		String registryKey = url!=null ? url.toString() : null;
		Image image = imageRegistry.get(registryKey);
		if (image==null) {
			ImageDescriptor descriptor= ImageDescriptor.createFromURL(url);
			imageRegistry.put(registryKey, descriptor);
			image = imageRegistry.get(registryKey);
		}

		return image;
	}

	/**
	 * The method retrieves an image descriptor from the internal repository according to the given key.
	 * See also {@link #getImage(String)}.
	 *
	 * @param key - one of {@code CDTSharedImages.IMG_} constants.
	 * @return the image from the repository or {@link ImageDescriptor#getMissingImageDescriptor()}.
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		URL url = getUrl(key);
		String registryKey = url!=null ? url.toString() : null;
		ImageDescriptor descriptor = imageRegistry.getDescriptor(registryKey);
		if (descriptor==null) {
			descriptor = ImageDescriptor.createFromURL(url);
			imageRegistry.put(registryKey, descriptor);
		}
		return descriptor;
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
		Assert.isTrue(overlayKeys.length==5);

		String suffix=""; //$NON-NLS-1$
		for (int i=0;i<5;i++) {
			String overlayKey=""; //$NON-NLS-1$
			if (i<overlayKeys.length && overlayKeys[i]!=null) {
				overlayKey=overlayKeys[i];
			}
			suffix=suffix+OVERLAY_SEPARATOR+overlayKey;
		}
		if (suffix.length()==5) {
			// No overlays added
			Image result = getImage(baseKey);
			return result;
		}
		String compositeKey=baseKey+suffix;

		Image result = imageRegistry.get(compositeKey);
		if (result!=null)
			return result;

		Image baseImage = getImage(baseKey);
		ImageDescriptor[] overlayDescriptors = new ImageDescriptor[5];
		for (int i=0;i<5;i++) {
			String overlayKey = overlayKeys[i];
			if (overlayKey!=null) {
				overlayDescriptors[i] = getImageDescriptor(overlayKey);
			}
		}
		ImageDescriptor compositeDescriptor = new DecorationOverlayIcon(baseImage, overlayDescriptors);
		imageRegistry.put(compositeKey, compositeDescriptor);
		result = imageRegistry.get(compositeKey);
		return result;
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
		String[] overlayKeys = new String[5];
		overlayKeys[quadrant]=overlayKey;
		return getImageOverlaid(baseKey, overlayKeys);
	}

	/**
	 * Helper method to return an image with warning overlay.
	 *
	 * @param baseKey - key of the base image. Expected to be in repository.
	 * @return an image with warning overlay.
	 */
	public static Image getImageWithWarning(String baseKey) {
		return CDTSharedImages.getImageOverlaid(baseKey, CDTSharedImages.IMG_OVR_WARNING, IDecoration.BOTTOM_LEFT);
	}

	/**
	 * Helper method to return an image with error overlay.
	 *
	 * @param baseKey - key of the base image. Expected to be in repository.
	 * @return an image with error overlay.
	 */
	public static Image getImageWithError(String baseKey) {
		return CDTSharedImages.getImageOverlaid(baseKey, CDTSharedImages.IMG_OVR_ERROR, IDecoration.BOTTOM_LEFT);
	}
}

