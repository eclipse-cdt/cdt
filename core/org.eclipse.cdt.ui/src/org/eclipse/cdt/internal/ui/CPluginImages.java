package org.eclipse.cdt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.ui.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * Bundle of all images used by the C plugin.
 */
public class CPluginImages {
	
	// The plugin registry
	private static ImageRegistry imageRegistry = new ImageRegistry();

	// Subdirectory (under the package containing this class) where 16 color images are
	private static URL fgIconBaseURL;
	static {
		try {
			fgIconBaseURL= new URL(CUIPlugin.getDefault().getDescriptor().getInstallURL(), "icons/" );
		} catch (MalformedURLException e) {
			CUIPlugin.getDefault().log(e);
		}
	}	
	private static final String NAME_PREFIX= CUIPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH= NAME_PREFIX.length();
	private static final String T= "full/";

	public static final String T_OBJ= T + "obj16/";
	public static final String T_WIZBAN= T + "wizban/";
	public static final String T_LCL=  "lcl16/";
	public static final String T_TOOL= "tool16/";
	public static final String T_VIEW= "view16/";
	public static final String T_OVR= T + "ovr16/";

	public static final String IMG_OBJS_TEMPLATE= NAME_PREFIX + "template_obj.gif";
	public static final String IMG_OBJS_FIELD= NAME_PREFIX + "field_obj.gif";
	public static final String IMG_OBJS_CLASS= NAME_PREFIX + "class_obj.gif";
	public static final String IMG_OBJS_STRUCT= NAME_PREFIX + "struct_obj.gif";
	public static final String IMG_OBJS_UNION= NAME_PREFIX + "union_obj.gif";
	public static final String IMG_OBJS_TYPEDEF= NAME_PREFIX + "typedef_obj.gif";
	public static final String IMG_OBJS_ENUMERATION= NAME_PREFIX + "enum_obj.gif";
	public static final String IMG_OBJS_ENUMERATOR= NAME_PREFIX + "enumerator_obj.gif";
	public static final String IMG_OBJS_FUNCTION= NAME_PREFIX + "function_obj.gif";
	public static final String IMG_OBJS_PUBLIC_METHOD= NAME_PREFIX + "method_public_obj.gif";
	public static final String IMG_OBJS_PROTECTED_METHOD= NAME_PREFIX + "method_protected_obj.gif";
	public static final String IMG_OBJS_PRIVATE_METHOD= NAME_PREFIX + "method_private_obj.gif";	
	public static final String IMG_OBJS_PUBLIC_FIELD= NAME_PREFIX + "field_public_obj.gif";
	public static final String IMG_OBJS_PROTECTED_FIELD= NAME_PREFIX + "field_protected_obj.gif";
	public static final String IMG_OBJS_PRIVATE_FIELD= NAME_PREFIX + "field_private_obj.gif";	
	public static final String IMG_OBJS_DECLARATION= NAME_PREFIX + "cdeclaration_obj.gif";
	public static final String IMG_OBJS_VAR_DECLARATION= NAME_PREFIX + "var_declaration_obj.gif";
	public static final String IMG_OBJS_INCLUDE= NAME_PREFIX + "include_obj.gif";
	public static final String IMG_OBJS_MACRO= NAME_PREFIX + "define_obj.gif";
	public static final String IMG_OBJS_TUNIT= NAME_PREFIX + "c_file_obj.gif";
	public static final String IMG_OBJS_ARCHIVE= NAME_PREFIX + "ar_obj.gif";
	public static final String IMG_OBJS_BINARY= NAME_PREFIX + "bin_obj.gif";
	public static final String IMG_OBJS_SHLIB= NAME_PREFIX + "shlib_obj.gif";
	public static final String IMG_OBJS_CEXEC= NAME_PREFIX + "exec_obj.gif";
	public static final String IMG_OBJS_CEXEC_DEBUG= NAME_PREFIX + "exec_dbg_obj.gif";
	public static final String IMG_OBJS_CORE= NAME_PREFIX + "core_obj.gif";
	public static final String IMG_OBJS_CONTAINER= NAME_PREFIX + "container_obj.gif";
	public static final String IMG_OBJS_LIBRARY= NAME_PREFIX + "lib_obj.gif";

	// Breakpoint images
	public static final String IMG_OBJS_BREAKPOINT = NAME_PREFIX + "breakpoint.gif";
	public static final String IMG_OBJS_BREAKPOINT_DISABLED = NAME_PREFIX + "breakpoint_disabled.gif";
	public static final String IMG_OBJS_BREAKPOINT_ACTIVE = NAME_PREFIX + "breakpoint_active.gif";

	public static final ImageDescriptor DESC_OBJS_FIELD= createManaged(T_OBJ, IMG_OBJS_FIELD);
	public static final ImageDescriptor DESC_OBJS_CLASS= createManaged(T_OBJ, IMG_OBJS_CLASS);
	public static final ImageDescriptor DESC_OBJS_STRUCT= createManaged(T_OBJ, IMG_OBJS_STRUCT);
	public static final ImageDescriptor DESC_OBJS_UNION= createManaged(T_OBJ, IMG_OBJS_UNION);
	public static final ImageDescriptor DESC_OBJS_TYPEDEF= createManaged(T_OBJ, IMG_OBJS_TYPEDEF);
	public static final ImageDescriptor DESC_OBJS_ENUMERATION= createManaged(T_OBJ, IMG_OBJS_ENUMERATION);
	public static final ImageDescriptor DESC_OBJS_ENUMERATOR= createManaged(T_OBJ, IMG_OBJS_ENUMERATOR);
	public static final ImageDescriptor DESC_OBJS_FUNCTION= createManaged(T_OBJ, IMG_OBJS_FUNCTION);
	public static final ImageDescriptor DESC_OBJS_PUBLIC_METHOD= createManaged(T_OBJ, IMG_OBJS_PUBLIC_METHOD);
	public static final ImageDescriptor DESC_OBJS_PROTECTED_METHOD= createManaged(T_OBJ, IMG_OBJS_PROTECTED_METHOD);
	public static final ImageDescriptor DESC_OBJS_PRIVATE_METHOD= createManaged(T_OBJ, IMG_OBJS_PRIVATE_METHOD);
	public static final ImageDescriptor DESC_OBJS_PUBLIC_FIELD= createManaged(T_OBJ, IMG_OBJS_PUBLIC_FIELD);
	public static final ImageDescriptor DESC_OBJS_PROTECTED_FIELD= createManaged(T_OBJ, IMG_OBJS_PROTECTED_FIELD);
	public static final ImageDescriptor DESC_OBJS_PRIVATE_FIELD= createManaged(T_OBJ, IMG_OBJS_PRIVATE_FIELD);			
	public static final ImageDescriptor DESC_OBJS_DECLARARION= createManaged(T_OBJ, IMG_OBJS_DECLARATION);
	public static final ImageDescriptor DESC_OBJS_VAR_DECLARARION= createManaged(T_OBJ, IMG_OBJS_VAR_DECLARATION);
	public static final ImageDescriptor DESC_OBJS_INCLUDE= createManaged(T_OBJ, IMG_OBJS_INCLUDE);
	public static final ImageDescriptor DESC_OBJS_MACRO= createManaged(T_OBJ, IMG_OBJS_MACRO);
	public static final ImageDescriptor DESC_OBJS_TUNIT= createManaged(T_OBJ, IMG_OBJS_TUNIT);
	public static final ImageDescriptor DESC_OBJS_ARCHIVE= createManaged(T_OBJ, IMG_OBJS_ARCHIVE);
	public static final ImageDescriptor DESC_OBJS_BINARY= createManaged(T_OBJ, IMG_OBJS_BINARY);
	public static final ImageDescriptor DESC_OBJS_SHLIB= createManaged(T_OBJ, IMG_OBJS_SHLIB);
	public static final ImageDescriptor DESC_OBJS_CEXEC= createManaged(T_OBJ, IMG_OBJS_CEXEC);
	public static final ImageDescriptor DESC_OBJS_CEXEC_DEBUG= createManaged(T_OBJ, IMG_OBJS_CEXEC_DEBUG);
	public static final ImageDescriptor DESC_OBJS_CORE= createManaged(T_OBJ, IMG_OBJS_CORE);
	public static final ImageDescriptor DESC_OBJS_CONTAINER= createManaged(T_OBJ, IMG_OBJS_CONTAINER);
	public static final ImageDescriptor DESC_OBJS_LIBRARY= createManaged(T_OBJ, IMG_OBJS_LIBRARY);
	
	// Breakpoint image descriptors
	public static final ImageDescriptor DESC_OBJS_BREAKPOINT = createManaged( T_OBJ, IMG_OBJS_BREAKPOINT );
	public static final ImageDescriptor DESC_OBJS_BREAKPOINT_DISABLED = createManaged( T_OBJ, IMG_OBJS_BREAKPOINT_DISABLED );
	public static final ImageDescriptor DESC_OBJS_BREAKPOINT_ACTIVE = createManaged( T_OBJ, IMG_OBJS_BREAKPOINT_ACTIVE );

	public static final String IMG_MENU_SHIFT_RIGHT= NAME_PREFIX + "shift_r_edit.gif";
	public static final String IMG_MENU_SHIFT_LEFT= NAME_PREFIX + "shift_l_edit.gif";
	public static final String IMG_MENU_OPEN_INCLUDE= NAME_PREFIX + "open_incl.gif";
	public static final String IMG_MENU_SEGMENT_EDIT= NAME_PREFIX + "segment_edit.gif";
	public static final String IMG_MENU_CODE_ASSIST= NAME_PREFIX + "metharg_obj.gif";
	public static final String IMG_MENU_COLLAPSE_ALL= NAME_PREFIX + "collapseall.gif";
	public static final String IMG_CLEAR_CONSOLE= NAME_PREFIX + "clear_co.gif";
	public static final String IMG_ALPHA_SORTING= NAME_PREFIX + "alphab_sort_co.gif";
	public static final String IMG_TOOL_GOTO_PREV_ERROR= NAME_PREFIX + "prev_error_nav.gif";
	public static final String IMG_TOOL_GOTO_NEXT_ERROR= NAME_PREFIX + "next_error_nav.gif";
	public static final String IMG_EDIT_PROPERTIES= NAME_PREFIX + "prop_edt.gif";	

	public static final String IMG_ACTION_SHOW_FIELDS= NAME_PREFIX + "fields_co.gif";
	public static final String IMG_ACTION_SHOW_PUBLIC= NAME_PREFIX + "public_co.gif";
	public static final String IMG_ACTION_SHOW_STATIC= NAME_PREFIX + "static_co.gif";

	public static final ImageDescriptor DESC_OBJS_TEMPLATE= createManaged(T_OBJ, IMG_OBJS_TEMPLATE);
	
	public static final ImageDescriptor DESC_OVR_STATIC= create(T_OVR, "static_co.gif");
	public static final ImageDescriptor DESC_OVR_CONSTANT= create(T_OVR, "c_ovr.gif");
	public static final ImageDescriptor DESC_OVR_VOLATILE= create(T_OVR, "volatile_co.gif");
	public static final ImageDescriptor DESC_OVR_TEMPLATE= create(T_OVR, "template_co.gif");

	public static final ImageDescriptor DESC_OVR_WARNING= create(T_OVR, "warning_co.gif");
	public static final ImageDescriptor DESC_OVR_ERROR= create(T_OVR, "error_co.gif");

	public static final ImageDescriptor DESC_WIZABAN_NEW_PROJ= create(T_WIZBAN, "newcprj_wiz.gif");
	public static final ImageDescriptor DESC_WIZBAN_NEWCLASS= create(T_WIZBAN, "newclass_wiz.gif");	
	public static final ImageDescriptor DESC_WIZABAN_C_APP= create(T_WIZBAN, "c_app_wiz.gif");
	public static final String IMG_OBJS_PROJECT = NAME_PREFIX + "prj_obj.gif";
	public static final ImageDescriptor DESC_PROJECT= createManaged(T_WIZBAN, IMG_OBJS_PROJECT);

	public static final ImageDescriptor DESC_TOOL_NEWCLASS= create(T_TOOL, "newclass_wiz.gif"); 				//$NON-NLS-1$

	// For the build image
	public static final String IMG_OBJS_BUILD= NAME_PREFIX + "build_menu.gif";
	public static final ImageDescriptor DESC_BUILD_MENU = createManaged(T_OBJ, IMG_OBJS_BUILD);
	
	//for search
	public static final String IMG_OBJS_SEARCH_REF  = NAME_PREFIX + "search_ref_obj.gif";
	public static final String IMG_OBJS_SEARCH_DECL = NAME_PREFIX + "search_decl_obj.gif";
	public static final String IMG_OBJS_CSEARCH     = NAME_PREFIX + "csearch_obj.gif";
	
	public static final ImageDescriptor DESC_OBJS_SEARCH_DECL = createManaged(T_OBJ, IMG_OBJS_SEARCH_DECL);
	public static final ImageDescriptor DESC_OBJS_SEARCH_REF  = createManaged(T_OBJ, IMG_OBJS_SEARCH_REF);
	public static final ImageDescriptor DESC_OBJS_CSEARCH     = createManaged(T_OBJ, IMG_OBJS_CSEARCH);
	
	public static void initialize() {
		//createManaged(registry, T_OBJ, IMG_OBJS_TUNIT);
		//createManaged(registry, T_OBJ, IMG_OBJS_FIELD);
		//createManaged(registry, T_OBJ, IMG_OBJS_CLASS);
		//createManaged(registry, T_OBJ, IMG_OBJS_STRUCT);
		//createManaged(registry, T_OBJ, IMG_OBJS_UNION);
		//createManaged(registry, T_OBJ, IMG_OBJS_FUNCTION);
		//createManaged(registry, T_OBJ, IMG_OBJS_INCLUDE);
		//createManaged(registry, T_OBJ, IMG_OBJS_DEFINE);

		//createManaged(registry, T_OBJ, IMG_OBJS_ARCHIVE);
		//createManaged(registry, T_OBJ, IMG_OBJS_SHLIB);
		//createManaged(registry, T_OBJ, IMG_OBJS_BINARY);
		//createManaged(registry, T_OBJ, IMG_OBJS_CEXEC);
		//createManaged(registry, T_OBJ, IMG_OBJS_CEXEC_DEBUG);
		//createManaged(registry, T_OBJ, IMG_OBJS_CONTAINER);

		//createManaged(registry, T_OBJ, IMG_OBJS_TEMPLATE);
	}
	
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
			return new URL(fgIconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			CUIPlugin.getDefault().log(e);
			return null;
		}
	}
	
	/**
	 * Sets all available image descriptors for the given action.
	 */	
	public static void setImageDescriptors(IAction action, String type, String relPath) {
		relPath= relPath.substring(NAME_PREFIX_LENGTH);
		action.setDisabledImageDescriptor(create(T + "d" + type, relPath));
		action.setHoverImageDescriptor(create(T + "c" + type, relPath));
		action.setImageDescriptor(create(T + "e" + type, relPath));
	}
	
	/**
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}
}
