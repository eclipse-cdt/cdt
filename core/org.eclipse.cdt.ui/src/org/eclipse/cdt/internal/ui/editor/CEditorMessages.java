package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class CEditorMessages 
{
	private static final String RESOURCE_BUNDLE = "org.eclipse.cdt.internal.ui.editor.CEditorMessages";


	private static ResourceBundle fgResourceBundle = ResourceBundle.getBundle( RESOURCE_BUNDLE );


	private CEditorMessages() 
	{
	}


	public static ResourceBundle getResourceBundle() 
	{
		return fgResourceBundle;
	}
	
	public static String getString( String key ) 
	{
		try 
		{
			return fgResourceBundle.getString( key );
		} 
		catch( MissingResourceException e ) 
		{
			return "!" + key + "!";
		}
	}
}


