/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.signals;

import java.util.MissingResourceException;
//import java.util.ResourceBundle;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;

public class SignalsMessages {

//	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.internal.ui.views.signals.SignalsMessages";//$NON-NLS-1$

//	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

	private SignalsMessages() {
	}

	public static String getString( String key ) {
		try {
			return CDebugUIPlugin.getResourceString( key );
//			return RESOURCE_BUNDLE.getString( key );
		} catch( MissingResourceException e ) {
			return '!' + key + '!';
		}
	}
}