/*******************************************************************************
 * Copyright (c) 2008, 2014 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

class Messages {
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
