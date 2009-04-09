/*******************************************************************************
 * Copyright (c) 2003 Berthold Daum.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Berthold Daum
 *******************************************************************************/

package org.eclipse.cdt.codan.internal.ui.preferences;

import java.util.ResourceBundle;


public class Messages {

	private final static String RESOURCE_BUNDLE = "org.eclipse.cdt.codan.internal.ui.preferences.Messages";//$NON-NLS-1$
	
	private static ResourceBundle fgResourceBundle = null;
	
	private static boolean notRead = true;

	public Messages() {
	}
	public static ResourceBundle getResourceBundle() {
		if (notRead) {
			notRead = false;
			try {
				fgResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
			}
			catch (Exception e) {
			}
		}
		
		return fgResourceBundle;
	}
	public static String getString(String key) {
		try {
			return getResourceBundle().getString(key);
		} catch (Exception e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}
}

