/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.mi.internal.ui.preferences;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PreferenceMessages {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.mi.internal.ui.preferences.PreferenceMessages";//$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

	private PreferenceMessages() {
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