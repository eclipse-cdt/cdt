/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Enter type comment.
 *
 * @since: Feb 23, 2004
 */
public class ActionMessages {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.internal.ui.actions.ActionMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

	private ActionMessages() {
	}

	public static String getString( String key ) {
		try {
			return RESOURCE_BUNDLE.getString( key );
		}
		catch( MissingResourceException e ) {
			return '!' + key + '!';
		}
	}
}
