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
	private static final String NAME_PREFIX = "org.eclipse.jdt.debug.ui."; //$NON-NLS-1$
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

	/*
	 * Set of predefined Image Descriptors.
	 */
	private static final String T_OBJ = "obj16"; //$NON-NLS-1$
	private static final String T_OVR = "ovr16"; //$NON-NLS-1$
	private static final String T_WIZBAN = "wizban"; //$NON-NLS-1$
	private static final String T_LCL = "clcl16"; //$NON-NLS-1$
	private static final String T_CTOOL = "ctool16"; //$NON-NLS-1$
	private static final String T_CVIEW = "cview16"; //$NON-NLS-1$
	private static final String T_DTOOL = "dtool16"; //$NON-NLS-1$
	private static final String T_ETOOL = "etool16"; //$NON-NLS-1$

	public static final ImageDescriptor DESC_OBJS_BREAKPOINT_INSTALLED = createManaged( T_OVR, IMG_OBJS_BREAKPOINT_INSTALLED );
	public static final ImageDescriptor DESC_OBJS_BREAKPOINT_INSTALLED_DISABLED = createManaged( T_OVR, IMG_OBJS_BREAKPOINT_INSTALLED_DISABLED );

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
		setImageDescriptors( action, "lcl16", iconName ); //$NON-NLS-1$
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
