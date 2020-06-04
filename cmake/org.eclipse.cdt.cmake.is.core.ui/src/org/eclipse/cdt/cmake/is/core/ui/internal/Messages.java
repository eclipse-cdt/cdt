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

package org.eclipse.cdt.cmake.is.core.ui.internal;

import org.eclipse.osgi.util.NLS;

/**
 * @author weber
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.cmake.is.core.ui.internal.messages"; //$NON-NLS-1$
	public static String IndexerSupportPreferencePage_description;
	public static String IndexerSupportPreferencePage_errmsg_suffix_regex;
	public static String IndexerSupportPreferencePage_label_suffix_pattern;
	public static String IndexerSupportPreferencePage_label_version_suffix_group;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
