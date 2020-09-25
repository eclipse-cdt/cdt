/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.internal;

import org.eclipse.osgi.util.NLS;

/**
 * @author weber
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.cmake.is.core.internal.messages"; //$NON-NLS-1$
	public static String ParserPreferencesAccess_e_get_preferences;
	public static String ParserPreferencesMetadata_label_console;
	public static String ParserPreferencesMetadata_label_suffix;
	public static String ParserPreferencesMetadata_label_try_suffix;
	public static String ParserPreferencesMetadata_ttip_suffix;
	public static String ParserPreferencesMetadata_ttip_try_suffix;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
