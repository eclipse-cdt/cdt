/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import com.ibm.icu.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class BuildModelMessages {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildModelMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private BuildModelMessages() {
	}

	public static String getString(String key) {
		return getResourceString(key);
	}
	
	public static String getResourceString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NullPointerException e) {
			return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), (Object[]) new String[] { arg });
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), (Object[]) args);
	}
}
