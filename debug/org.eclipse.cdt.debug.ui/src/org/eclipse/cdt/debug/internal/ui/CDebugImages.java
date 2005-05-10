/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * 
 * Bundle of most images used by the C/C++ debug plug-in.
 * 
 * @since Aug 30, 2002
 */
public class CDebugImages
{
	private static final String NAME_PREFIX = "org.eclipse.cdt.debug.ui."; //$NON-NLS-1$
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	private static URL fgIconBaseURL = null;

	static 
	{
		String pathSuffix = "icons/"; //$NON-NLS-1$
		try
		{
			fgIconBaseURL = new URL( CDebugUIPlugin.getDefault().getBundle().getEntry( "/" ), pathSuffix ); //$NON-NLS-1$
		}
		catch( MalformedURLException e )
		{
			CDebugUIPlugin.log( e );
		}
	}

	// The plugin registry
	private static ImageRegistry fgImageRegistry = null;
	private static HashMap fgAvoidSWTErrorMap = null;

	/*
	 * Available cached Images in the C/C++ debug plug-in image registry.
	 */	
	public static final String IMG_OVRS_BREAKPOINT_INSTALLED = NAME_PREFIX + "installed_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_OVRS_BREAKPOINT_INSTALLED_DISABLED = NAME_PREFIX + "installed_ovr_disabled.gif"; //$NON-NLS-1$
	public static final String IMG_OVRS_BREAKPOINT_CONDITIONAL = NAME_PREFIX + "conditional_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_OVRS_BREAKPOINT_CONDITIONAL_DISABLED = NAME_PREFIX + "conditional_ovr_disabled.gif"; //$NON-NLS-1$
	public static final String IMG_OVRS_ADDRESS_BREAKPOINT = NAME_PREFIX + "address_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_OVRS_ADDRESS_BREAKPOINT_DISABLED = NAME_PREFIX + "address_ovr_disabled.gif"; //$NON-NLS-1$
	public static final String IMG_OVRS_FUNCTION_BREAKPOINT = NAME_PREFIX + "function_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_OVRS_FUNCTION_BREAKPOINT_DISABLED = NAME_PREFIX + "function_ovr_disabled.gif"; //$NON-NLS-1$
	public static final String IMG_OVRS_ERROR = NAME_PREFIX + "error_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_OVRS_WARNING = NAME_PREFIX + "warning_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_OVRS_SYMBOLS = NAME_PREFIX + "symbols_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_OVRS_VARIABLE_CASTED = NAME_PREFIX + "casttype_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_OVRS_ARGUMENT = NAME_PREFIX + "argument_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_OVRS_GLOBAL = NAME_PREFIX + "global_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_BREAKPOINT_ENABLED = NAME_PREFIX + "brkp_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_BREAKPOINT_DISABLED = NAME_PREFIX + "brkpd_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_ADDRESS_BREAKPOINT_ENABLED = NAME_PREFIX + "addrbrkp_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_ADDRESS_BREAKPOINT_DISABLED = NAME_PREFIX + "addrbrkpd_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_FUNCTION_BREAKPOINT_ENABLED = NAME_PREFIX + "funbrkp_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_FUNCTION_BREAKPOINT_DISABLED = NAME_PREFIX + "funbrkpd_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_WATCHPOINT_ENABLED = NAME_PREFIX + "readwrite_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_WATCHPOINT_DISABLED = NAME_PREFIX + "readwrite_obj_disabled.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_READ_WATCHPOINT_ENABLED = NAME_PREFIX + "read_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_READ_WATCHPOINT_DISABLED = NAME_PREFIX + "read_obj_disabled.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_WRITE_WATCHPOINT_ENABLED = NAME_PREFIX + "write_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_WRITE_WATCHPOINT_DISABLED = NAME_PREFIX + "write_obj_disabled.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_VARIABLE_SIMPLE = NAME_PREFIX + "var_simple.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_VARIABLE_SIMPLE_DISABLED = NAME_PREFIX + "vard_simple.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_VARIABLE_AGGREGATE = NAME_PREFIX + "var_aggr.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_VARIABLE_AGGREGATE_DISABLED = NAME_PREFIX + "vard_aggr.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_VARIABLE_POINTER = NAME_PREFIX + "var_pointer.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_VARIABLE_POINTER_DISABLED = NAME_PREFIX + "vard_pointer.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_VARIABLE_STRING = NAME_PREFIX + "var_string.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_REGISTER_GROUP = NAME_PREFIX + "registergroup_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_REGISTER_GROUP_DISABLED = NAME_PREFIX + "registergroupd_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_REGISTER = NAME_PREFIX + "register_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_REGISTER_DISABLED = NAME_PREFIX + "registerd_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_DISASSEMBLY = NAME_PREFIX + "disassembly_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_PROJECT = NAME_PREFIX + "project_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_CLOSED_PROJECT = NAME_PREFIX + "cproject_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_FOLDER = NAME_PREFIX + "folder_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_EXECUTABLE_WITH_SYMBOLS = NAME_PREFIX + "exec_dbg_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_EXECUTABLE = NAME_PREFIX + "exec_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_SHARED_LIBRARY_WITH_SYMBOLS = NAME_PREFIX + "library_syms_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_SHARED_LIBRARY = NAME_PREFIX + "library_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_SIGNAL = NAME_PREFIX + "signal_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_WORKSPACE_SOURCE_FILE = NAME_PREFIX + "prj_file_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_EXTERNAL_SOURCE_FILE = NAME_PREFIX + "ext_file_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_PATH_MAPPING = NAME_PREFIX + "mapping_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_PATH_MAP_ENTRY = NAME_PREFIX + "mapentry_obj.gif";	//$NON-NLS-1$

	public static final String IMG_LCL_TYPE_NAMES = NAME_PREFIX + "tnames_co.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_CHANGE_REGISTER_VALUE = NAME_PREFIX + "change_reg_value_co.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_AUTO_REFRESH = NAME_PREFIX + "auto_refresh_co.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_REFRESH = NAME_PREFIX + "refresh_co.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_MEMORY_SAVE = NAME_PREFIX + "memory_update.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_MEMORY_CLEAR = NAME_PREFIX + "memory_clear.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_SHOW_ASCII = NAME_PREFIX + "show_ascii.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_LOAD_ALL_SYMBOLS = NAME_PREFIX + "load_all_symbols_co.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_CAST_TO_TYPE = NAME_PREFIX + "casttotype_co.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_DISPLAY_AS_ARRAY = NAME_PREFIX + "showasarray_co.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_RESUME_AT_LINE = NAME_PREFIX + "jump_co.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_DETAIL_PANE_UNDER = NAME_PREFIX + "det_pane_under.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_DETAIL_PANE_RIGHT = NAME_PREFIX + "det_pane_right.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_DETAIL_PANE_HIDE = NAME_PREFIX + "det_pane_hide.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_COLLAPSE_ALL = NAME_PREFIX + "collapseall.gif";	//$NON-NLS-1$

	public static final String IMG_WIZBAN_ADD_SOURCE = NAME_PREFIX + "addsrcloc_wiz.gif";	//$NON-NLS-1$

	/*
	 * Set of predefined Image Descriptors.
	 */
	private static final String T_OBJ = "obj16"; //$NON-NLS-1$
	private static final String T_OVR = "ovr16"; //$NON-NLS-1$
	private static final String T_WIZBAN = "wizban"; //$NON-NLS-1$
	private static final String T_LCL = "lcl16"; //$NON-NLS-1$
	private static final String T_ELCL = "elcl16"; //$NON-NLS-1$
	private static final String T_DLCL = "dlcl16"; //$NON-NLS-1$
//	private static final String T_DTOOL = "dtool16"; //$NON-NLS-1$
//	private static final String T_ETOOL = "etool16"; //$NON-NLS-1$

	public static final ImageDescriptor DESC_OVRS_ERROR = createManaged( T_OVR, IMG_OVRS_ERROR );
	public static final ImageDescriptor DESC_OVRS_WARNING = createManaged( T_OVR, IMG_OVRS_WARNING );
	public static final ImageDescriptor DESC_OVRS_BREAKPOINT_INSTALLED = createManaged( T_OVR, IMG_OVRS_BREAKPOINT_INSTALLED );
	public static final ImageDescriptor DESC_OVRS_BREAKPOINT_INSTALLED_DISABLED = createManaged( T_OVR, IMG_OVRS_BREAKPOINT_INSTALLED_DISABLED );
	public static final ImageDescriptor DESC_OVRS_BREAKPOINT_CONDITIONAL = createManaged( T_OVR, IMG_OVRS_BREAKPOINT_CONDITIONAL );
	public static final ImageDescriptor DESC_OVRS_BREAKPOINT_CONDITIONAL_DISABLED = createManaged( T_OVR, IMG_OVRS_BREAKPOINT_CONDITIONAL_DISABLED );
	public static final ImageDescriptor DESC_OVRS_ADDRESS_BREAKPOINT = createManaged( T_OVR, IMG_OVRS_ADDRESS_BREAKPOINT );
	public static final ImageDescriptor DESC_OVRS_ADDRESS_BREAKPOINT_DISABLED = createManaged( T_OVR, IMG_OVRS_ADDRESS_BREAKPOINT_DISABLED );
	public static final ImageDescriptor DESC_OVRS_FUNCTION_BREAKPOINT = createManaged( T_OVR, IMG_OVRS_FUNCTION_BREAKPOINT );
	public static final ImageDescriptor DESC_OVRS_FUNCTION_BREAKPOINT_DISABLED = createManaged( T_OVR, IMG_OVRS_FUNCTION_BREAKPOINT_DISABLED );
	public static final ImageDescriptor DESC_OVRS_SYMBOLS = createManaged( T_OVR, IMG_OVRS_SYMBOLS );
	public static final ImageDescriptor DESC_OVRS_VARIABLE_CASTED = createManaged( T_OVR, IMG_OVRS_VARIABLE_CASTED );
	public static final ImageDescriptor DESC_OVRS_ARGUMENT = createManaged( T_OVR, IMG_OVRS_ARGUMENT );
	public static final ImageDescriptor DESC_OVRS_GLOBAL = createManaged( T_OVR, IMG_OVRS_GLOBAL );
	public static final ImageDescriptor DESC_OBJS_BREAKPOINT_ENABLED = createManaged( T_OBJ, IMG_OBJS_BREAKPOINT_ENABLED );
	public static final ImageDescriptor DESC_OBJS_BREAKPOINT_DISABLED = createManaged( T_OBJ, IMG_OBJS_BREAKPOINT_DISABLED );
	public static final ImageDescriptor DESC_OBJS_WATCHPOINT_ENABLED = createManaged( T_OBJ, IMG_OBJS_WATCHPOINT_ENABLED );
	public static final ImageDescriptor DESC_OBJS_WATCHPOINT_DISABLED = createManaged( T_OBJ, IMG_OBJS_WATCHPOINT_DISABLED );
	public static final ImageDescriptor DESC_OBJS_READ_WATCHPOINT_ENABLED = createManaged( T_OBJ, IMG_OBJS_READ_WATCHPOINT_ENABLED );
	public static final ImageDescriptor DESC_OBJS_READ_WATCHPOINT_DISABLED = createManaged( T_OBJ, IMG_OBJS_READ_WATCHPOINT_DISABLED );
	public static final ImageDescriptor DESC_OBJS_WRITE_WATCHPOINT_ENABLED = createManaged( T_OBJ, IMG_OBJS_WRITE_WATCHPOINT_ENABLED );
	public static final ImageDescriptor DESC_OBJS_WRITE_WATCHPOINT_DISABLED = createManaged( T_OBJ, IMG_OBJS_WRITE_WATCHPOINT_DISABLED );
	public static final ImageDescriptor DESC_OBJS_VARIABLE_SIMPLE = createManaged( T_OBJ, IMG_OBJS_VARIABLE_SIMPLE );
	public static final ImageDescriptor DESC_OBJS_VARIABLE_SIMPLE_DISABLED = createManaged( T_OBJ, IMG_OBJS_VARIABLE_SIMPLE_DISABLED );
	public static final ImageDescriptor DESC_OBJS_VARIABLE_AGGREGATE = createManaged( T_OBJ, IMG_OBJS_VARIABLE_AGGREGATE );
	public static final ImageDescriptor DESC_OBJS_VARIABLE_AGGREGATE_DISABLED = createManaged( T_OBJ, IMG_OBJS_VARIABLE_AGGREGATE_DISABLED );
	public static final ImageDescriptor DESC_OBJS_VARIABLE_POINTER = createManaged( T_OBJ, IMG_OBJS_VARIABLE_POINTER );
	public static final ImageDescriptor DESC_OBJS_VARIABLE_POINTER_DISABLED = createManaged( T_OBJ, IMG_OBJS_VARIABLE_POINTER_DISABLED );
	public static final ImageDescriptor DESC_OBJS_VARIABLE_STRING = createManaged( T_OBJ, IMG_OBJS_VARIABLE_STRING );
	public static final ImageDescriptor DESC_OBJS_REGISTER_GROUP = createManaged( T_OBJ, IMG_OBJS_REGISTER_GROUP );
	public static final ImageDescriptor DESC_OBJS_REGISTER_GROUP_DISABLED = createManaged( T_OBJ, IMG_OBJS_REGISTER_GROUP_DISABLED );
	public static final ImageDescriptor DESC_OBJS_REGISTER = createManaged( T_OBJ, IMG_OBJS_REGISTER );
	public static final ImageDescriptor DESC_OBJS_REGISTER_DISABLED = createManaged( T_OBJ, IMG_OBJS_REGISTER_DISABLED );
	public static final ImageDescriptor DESC_OBJS_DISASSEMBLY = createManaged( T_OBJ, IMG_OBJS_DISASSEMBLY );
	public static final ImageDescriptor DESC_OBJS_PROJECT = createManaged( T_OBJ, IMG_OBJS_PROJECT );
	public static final ImageDescriptor DESC_OBJS_CLOSED_PROJECT = createManaged( T_OBJ, IMG_OBJS_CLOSED_PROJECT );
	public static final ImageDescriptor DESC_OBJS_FOLDER = createManaged( T_OBJ, IMG_OBJS_FOLDER );
	public static final ImageDescriptor DESC_OBJS_EXECUTABLE_WITH_SYMBOLS = createManaged( T_OBJ, IMG_OBJS_EXECUTABLE_WITH_SYMBOLS );
	public static final ImageDescriptor DESC_OBJS_EXECUTABLE = createManaged( T_OBJ, IMG_OBJS_EXECUTABLE );
	public static final ImageDescriptor DESC_OBJS_SHARED_LIBRARY_WITH_SYMBOLS = createManaged( T_OBJ, IMG_OBJS_SHARED_LIBRARY_WITH_SYMBOLS );
	public static final ImageDescriptor DESC_OBJS_SHARED_LIBRARY = createManaged( T_OBJ, IMG_OBJS_SHARED_LIBRARY );
	public static final ImageDescriptor DESC_OBJS_SIGNAL = createManaged( T_OBJ, IMG_OBJS_SIGNAL );
	public static final ImageDescriptor DESC_OBJS_WORKSPACE_SOURCE_FILE = createManaged( T_OBJ, IMG_OBJS_WORKSPACE_SOURCE_FILE );
	public static final ImageDescriptor DESC_OBJS_EXTERNAL_SOURCE_FILE = createManaged( T_OBJ, IMG_OBJS_EXTERNAL_SOURCE_FILE );
	public static final ImageDescriptor DESC_OBJS_PATH_MAPPING = createManaged( T_OBJ, IMG_OBJS_PATH_MAPPING );
	public static final ImageDescriptor DESC_OBJS_PATH_MAP_ENTRY = createManaged( T_OBJ, IMG_OBJS_PATH_MAP_ENTRY );
	public static final ImageDescriptor DESC_WIZBAN_ADD_SOURCE = createManaged( T_WIZBAN, IMG_WIZBAN_ADD_SOURCE );
	public static final ImageDescriptor DESC_LCL_CAST_TO_TYPE = createManaged( T_ELCL, IMG_LCL_CAST_TO_TYPE );
	public static final ImageDescriptor DESC_LCL_DISPLAY_AS_ARRAY = createManaged( T_ELCL, IMG_LCL_DISPLAY_AS_ARRAY );
	public static final ImageDescriptor DESC_LCL_RESUME_AT_LINE = createManaged( T_ELCL, IMG_LCL_RESUME_AT_LINE );
	public static final ImageDescriptor DESC_LCL_RESUME_AT_LINE_DISABLED = createManaged( T_DLCL, IMG_LCL_RESUME_AT_LINE );
	public static final ImageDescriptor DESC_LCL_DETAIL_PANE_UNDER = createManaged( T_ELCL, IMG_LCL_DETAIL_PANE_UNDER );
	public static final ImageDescriptor DESC_LCL_DETAIL_PANE_UNDER_DISABLED = createManaged( T_DLCL, IMG_LCL_DETAIL_PANE_UNDER );
	public static final ImageDescriptor DESC_LCL_DETAIL_PANE_RIGHT = createManaged( T_ELCL, IMG_LCL_DETAIL_PANE_RIGHT );
	public static final ImageDescriptor DESC_LCL_DETAIL_PANE_RIGHT_DISABLED = createManaged( T_DLCL, IMG_LCL_DETAIL_PANE_RIGHT );
	public static final ImageDescriptor DESC_LCL_DETAIL_PANE_HIDE = createManaged( T_ELCL, IMG_LCL_DETAIL_PANE_HIDE );
	public static final ImageDescriptor DESC_LCL_DETAIL_PANE_HIDE_DISABLED = createManaged( T_DLCL, IMG_LCL_DETAIL_PANE_HIDE );
	public static final ImageDescriptor DESC_LCL_COLLAPSE_ALL = createManaged( T_ELCL, IMG_LCL_COLLAPSE_ALL );
	public static final ImageDescriptor DESC_LCL_COLLAPSE_ALL_DISABLED = createManaged( T_DLCL, IMG_LCL_COLLAPSE_ALL );

	/**
	 * Returns the image managed under the given key in this registry.
	 * 
	 * @param key the image's key
	 * @return the image managed under the given key
	 */ 
	public static Image get( String key )
	{
		return getImageRegistry().get( key );
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions
	 * are retrieved from the *tool16 folders.
	 */
	public static void setToolImageDescriptors( IAction action, String iconName )
	{
		setImageDescriptors( action, "tool16", iconName ); //$NON-NLS-1$
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions
	 * are retrieved from the *lcl16 folders.
	 */
	public static void setLocalImageDescriptors( IAction action, String iconName )
	{
		setImageDescriptors( action, T_LCL, iconName );
	}

	/*
	 * Helper method to access the image registry from the JDIDebugUIPlugin class.
	 */
	/* package */ static ImageRegistry getImageRegistry()
	{
		if ( fgImageRegistry == null )
		{
			fgImageRegistry = new ImageRegistry();
			for ( Iterator iter = fgAvoidSWTErrorMap.keySet().iterator(); iter.hasNext(); )
			{
				String key = (String)iter.next();
				fgImageRegistry.put( key, (ImageDescriptor)fgAvoidSWTErrorMap.get( key ) );
			}
			fgAvoidSWTErrorMap = null;
		}
		return fgImageRegistry;
	}

	//---- Helper methods to access icons on the file system --------------------------------------

	private static void setImageDescriptors( IAction action, String type, String relPath )
	{
		relPath = relPath.substring( NAME_PREFIX_LENGTH );
		try
		{
			ImageDescriptor id = ImageDescriptor.createFromURL( makeIconFileURL( "d" + type, relPath ) ); //$NON-NLS-1$
			if ( id != null )
				action.setDisabledImageDescriptor( id );
		}
		catch( MalformedURLException e )
		{
			CDebugUIPlugin.log( e );
		}

		try
		{
			ImageDescriptor id = ImageDescriptor.createFromURL( makeIconFileURL( "c" + type, relPath ) ); //$NON-NLS-1$
			if ( id != null )
				action.setHoverImageDescriptor( id );
		}
		catch( MalformedURLException e )
		{
			CDebugUIPlugin.log( e );
		}

		action.setImageDescriptor( create( "e" + type, relPath ) ); //$NON-NLS-1$
	}

	private static ImageDescriptor createManaged( String prefix, String name )
	{
		try
		{
			ImageDescriptor result = ImageDescriptor.createFromURL( makeIconFileURL( prefix, name.substring( NAME_PREFIX_LENGTH ) ) );
			if ( fgAvoidSWTErrorMap == null )
			{
				fgAvoidSWTErrorMap = new HashMap();
			}
			fgAvoidSWTErrorMap.put( name, result );
			if ( fgImageRegistry != null )
			{
				CDebugUIPlugin.logErrorMessage( "Internal Error: Image registry already defined" ); //$NON-NLS-1$
			}
			return result;
		}
		catch( MalformedURLException e )
		{
			CDebugUIPlugin.log( e );
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static ImageDescriptor create( String prefix, String name )
	{
		try
		{
			return ImageDescriptor.createFromURL( makeIconFileURL( prefix, name ) );
		}
		catch( MalformedURLException e )
		{
			CDebugUIPlugin.log( e );
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL( String prefix, String name ) throws MalformedURLException
	{
		if ( fgIconBaseURL == null )
			throw new MalformedURLException();

		StringBuffer buffer = new StringBuffer( prefix );
		buffer.append( '/' );
		buffer.append( name );
		return new URL( fgIconBaseURL, buffer.toString() );
	}
}
