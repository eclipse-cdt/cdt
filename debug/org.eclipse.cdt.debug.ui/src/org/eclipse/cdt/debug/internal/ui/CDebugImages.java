/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;

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
		String pathSuffix = "icons/full/"; //$NON-NLS-1$
		try
		{
			fgIconBaseURL = new URL( CDebugUIPlugin.getDefault().getDescriptor().getInstallURL(), pathSuffix );
		}
		catch( MalformedURLException e )
		{
			CDebugUIPlugin.log( e );
		}
	}

	// The plugin registry
	private final static ImageRegistry IMAGE_REGISTRY = new ImageRegistry( CDebugUIPlugin.getStandardDisplay() );

	/*
	 * Available cached Images in the C/C++ debug plug-in image registry.
	 */	
	public static final String IMG_OBJS_BREAKPOINT_INSTALLED = NAME_PREFIX + "installed_ovr.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_BREAKPOINT_INSTALLED_DISABLED = NAME_PREFIX + "installed_ovr_disabled.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ADDRESS_BREAKPOINT_ENABLED = NAME_PREFIX + "addrbrkp_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_ADDRESS_BREAKPOINT_DISABLED = NAME_PREFIX + "addrbrkpd_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_WATCHPOINT_ENABLED = NAME_PREFIX + "readwrite_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_WATCHPOINT_DISABLED = NAME_PREFIX + "readwrite_obj_disabled.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_READ_WATCHPOINT_ENABLED = NAME_PREFIX + "read_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_READ_WATCHPOINT_DISABLED = NAME_PREFIX + "read_obj_disabled.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_WRITE_WATCHPOINT_ENABLED = NAME_PREFIX + "write_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_WRITE_WATCHPOINT_DISABLED = NAME_PREFIX + "write_obj_disabled.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_VARIABLE_SIMPLE = NAME_PREFIX + "var_simple.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_VARIABLE_AGGREGATE = NAME_PREFIX + "var_aggr.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_VARIABLE_POINTER = NAME_PREFIX + "var_pointer.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_VARIABLE_STRING = NAME_PREFIX + "var_string.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_REGISTER_GROUP = NAME_PREFIX + "registergroup_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_REGISTER = NAME_PREFIX + "register_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_DISASSEMBLY = NAME_PREFIX + "disassembly_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_PROJECT = NAME_PREFIX + "project_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_FOLDER = NAME_PREFIX + "folder_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_LOADED_SHARED_LIBRARY = NAME_PREFIX + "library_syms_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_SHARED_LIBRARY = NAME_PREFIX + "library_obj.gif";	//$NON-NLS-1$
	public static final String IMG_OBJS_SIGNAL = NAME_PREFIX + "signal_obj.gif";	//$NON-NLS-1$

	public static final String IMG_LCL_TYPE_NAMES = NAME_PREFIX + "tnames_co.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_CHANGE_REGISTER_VALUE = NAME_PREFIX + "change_reg_value_co.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_AUTO_REFRESH = NAME_PREFIX + "autorefresh_mem.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_REFRESH = NAME_PREFIX + "refresh_mem.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_MEMORY_SAVE = NAME_PREFIX + "memory_update.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_MEMORY_CLEAR = NAME_PREFIX + "memory_clear.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_SHOW_ASCII = NAME_PREFIX + "show_ascii.gif";	//$NON-NLS-1$
	public static final String IMG_LCL_LOAD_ALL_SYMBOLS = NAME_PREFIX + "load_all_symbols_co.gif";	//$NON-NLS-1$

	public static final String IMG_TOOLS_ADD_DIR_SOURCE_LOCATION = NAME_PREFIX + "adddirsource_wiz.gif";	//$NON-NLS-1$
	public static final String IMG_TOOLS_ADD_PRJ_SOURCE_LOCATION = NAME_PREFIX + "addprjsource_wiz.gif";	//$NON-NLS-1$

	public static final String IMG_WIZBAN_ADD_SOURCE_LOCATION = NAME_PREFIX + "add_source_location_wiz.gif";	//$NON-NLS-1$
	public static final String IMG_WIZBAN_ADD_DIR_SOURCE_LOCATION = NAME_PREFIX + "add_dir_source_location_wiz.gif";	//$NON-NLS-1$
	public static final String IMG_WIZBAN_ADD_PRJ_SOURCE_LOCATION = NAME_PREFIX + "add_prj_source_location_wiz.gif";	//$NON-NLS-1$

	/*
	 * Set of predefined Image Descriptors.
	 */
	private static final String T_OBJ = "obj16"; //$NON-NLS-1$
	private static final String T_OVR = "ovr16"; //$NON-NLS-1$
	private static final String T_WIZBAN = "wizban"; //$NON-NLS-1$
	private static final String T_LCL = "lcl16"; //$NON-NLS-1$
	private static final String T_CTOOL = "ctool16"; //$NON-NLS-1$
	private static final String T_CVIEW = "cview16"; //$NON-NLS-1$
	private static final String T_DTOOL = "dtool16"; //$NON-NLS-1$
	private static final String T_ETOOL = "etool16"; //$NON-NLS-1$

	public static final ImageDescriptor DESC_OBJS_BREAKPOINT_INSTALLED = createManaged( T_OVR, IMG_OBJS_BREAKPOINT_INSTALLED );
	public static final ImageDescriptor DESC_OBJS_BREAKPOINT_INSTALLED_DISABLED = createManaged( T_OVR, IMG_OBJS_BREAKPOINT_INSTALLED_DISABLED );
	public static final ImageDescriptor DESC_OBJS_ADDRESS_BREAKPOINT_ENABLED = createManaged( T_OBJ, IMG_OBJS_ADDRESS_BREAKPOINT_ENABLED );
	public static final ImageDescriptor DESC_OBJS_ADDRESS_BREAKPOINT_DISABLED = createManaged( T_OBJ, IMG_OBJS_ADDRESS_BREAKPOINT_DISABLED );
	public static final ImageDescriptor DESC_OBJS_WATCHPOINT_ENABLED = createManaged( T_OBJ, IMG_OBJS_WATCHPOINT_ENABLED );
	public static final ImageDescriptor DESC_OBJS_WATCHPOINT_DISABLED = createManaged( T_OBJ, IMG_OBJS_WATCHPOINT_DISABLED );
	public static final ImageDescriptor DESC_OBJS_READ_WATCHPOINT_ENABLED = createManaged( T_OBJ, IMG_OBJS_READ_WATCHPOINT_ENABLED );
	public static final ImageDescriptor DESC_OBJS_READ_WATCHPOINT_DISABLED = createManaged( T_OBJ, IMG_OBJS_READ_WATCHPOINT_DISABLED );
	public static final ImageDescriptor DESC_OBJS_WRITE_WATCHPOINT_ENABLED = createManaged( T_OBJ, IMG_OBJS_WRITE_WATCHPOINT_ENABLED );
	public static final ImageDescriptor DESC_OBJS_WRITE_WATCHPOINT_DISABLED = createManaged( T_OBJ, IMG_OBJS_WRITE_WATCHPOINT_DISABLED );
	public static final ImageDescriptor DESC_OBJS_VARIABLE_SIMPLE = createManaged( T_OBJ, IMG_OBJS_VARIABLE_SIMPLE );
	public static final ImageDescriptor DESC_OBJS_VARIABLE_AGGREGATE = createManaged( T_OBJ, IMG_OBJS_VARIABLE_AGGREGATE );
	public static final ImageDescriptor DESC_OBJS_VARIABLE_POINTER = createManaged( T_OBJ, IMG_OBJS_VARIABLE_POINTER );
	public static final ImageDescriptor DESC_OBJS_VARIABLE_STRING = createManaged( T_OBJ, IMG_OBJS_VARIABLE_STRING );
	public static final ImageDescriptor DESC_OBJS_REGISTER_GROUP = createManaged( T_OBJ, IMG_OBJS_REGISTER_GROUP );
	public static final ImageDescriptor DESC_OBJS_REGISTER = createManaged( T_OBJ, IMG_OBJS_REGISTER );
	public static final ImageDescriptor DESC_OBJS_DISASSEMBLY = createManaged( T_OBJ, IMG_OBJS_DISASSEMBLY );
	public static final ImageDescriptor DESC_OBJS_PROJECT = createManaged( T_OBJ, IMG_OBJS_PROJECT );
	public static final ImageDescriptor DESC_OBJS_FOLDER = createManaged( T_OBJ, IMG_OBJS_FOLDER );
	public static final ImageDescriptor DESC_OBJS_LOADED_SHARED_LIBRARY = createManaged( T_OBJ, IMG_OBJS_LOADED_SHARED_LIBRARY );
	public static final ImageDescriptor DESC_OBJS_SHARED_LIBRARY = createManaged( T_OBJ, IMG_OBJS_SHARED_LIBRARY );
	public static final ImageDescriptor DESC_OBJS_SIGNAL = createManaged( T_OBJ, IMG_OBJS_SIGNAL );
	public static final ImageDescriptor DESC_WIZBAN_ADD_SOURCE_LOCATION = createManaged( T_WIZBAN, IMG_WIZBAN_ADD_SOURCE_LOCATION ); 		//$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_ADD_PRJ_SOURCE_LOCATION = createManaged( T_WIZBAN, IMG_WIZBAN_ADD_PRJ_SOURCE_LOCATION ); 		//$NON-NLS-1$
	public static final ImageDescriptor DESC_WIZBAN_ADD_DIR_SOURCE_LOCATION = createManaged( T_WIZBAN, IMG_WIZBAN_ADD_DIR_SOURCE_LOCATION ); 		//$NON-NLS-1$
	public static final ImageDescriptor DESC_TOOLS_ADD_PRJ_SOURCE_LOCATION = createManaged( T_CTOOL, IMG_TOOLS_ADD_PRJ_SOURCE_LOCATION ); 		//$NON-NLS-1$
	public static final ImageDescriptor DESC_TOOLS_ADD_DIR_SOURCE_LOCATION = createManaged( T_CTOOL, IMG_TOOLS_ADD_DIR_SOURCE_LOCATION ); 		//$NON-NLS-1$

	/**
	 * Returns the image managed under the given key in this registry.
	 * 
	 * @param key the image's key
	 * @return the image managed under the given key
	 */ 
	public static Image get( String key )
	{
		return IMAGE_REGISTRY.get( key );
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
	/* package */
	static ImageRegistry getImageRegistry()
	{
		return IMAGE_REGISTRY;
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
			IMAGE_REGISTRY.put( name, result );
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
