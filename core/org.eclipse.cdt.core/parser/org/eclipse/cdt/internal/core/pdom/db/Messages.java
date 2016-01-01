/*******************************************************************************
 * Copyright (c) 2005, 2014 Symbian Software Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Symbian Software Limited - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.db;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final ResourceBundle RESOURCE_BUNDLE =
			ResourceBundle.getBundle(Messages.class.getName());

	private Messages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
