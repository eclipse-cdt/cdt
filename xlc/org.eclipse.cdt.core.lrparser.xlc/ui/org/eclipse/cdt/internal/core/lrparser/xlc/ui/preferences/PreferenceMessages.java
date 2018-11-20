/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.lrparser.xlc.ui.preferences;

import java.lang.reflect.Field;

import org.eclipse.osgi.util.NLS;

public class PreferenceMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.core.lrparser.xlc.ui.preferences.PreferenceMessages"; //$NON-NLS-1$

	private PreferenceMessages() {
	}

	static {
		initializeMessages(BUNDLE_NAME, PreferenceMessages.class);
	}

	public static final String PREFIX = "XlcLanguageOptionsPreferencePage_";

	public static String getMessage(String suffix) {
		try {
			Field field = PreferenceMessages.class.getDeclaredField(PREFIX + suffix);
			return (String) field.get(null);

		} catch (NoSuchFieldException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	public static String XlcLanguageOptionsPreferencePage_link, XlcLanguageOptionsPreferencePage_group,

			XlcLanguageOptionsPreferencePage_SUPPORT_VECTOR_TYPES,
			XlcLanguageOptionsPreferencePage_SUPPORT_DECIMAL_FLOATING_POINT_TYPES,
			XlcLanguageOptionsPreferencePage_SUPPORT_COMPLEX_IN_CPP,
			XlcLanguageOptionsPreferencePage_SUPPORT_RESTRICT_IN_CPP,
			XlcLanguageOptionsPreferencePage_SUPPORT_STATIC_ASSERT;
}
