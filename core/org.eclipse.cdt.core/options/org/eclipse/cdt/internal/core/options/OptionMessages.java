/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.options;

import org.eclipse.osgi.util.NLS;

public class OptionMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.core.options.OptionMessages"; //$NON-NLS-1$
	public static String BaseOption_e_null_default_value;
	public static String BaseOption_e_null_description;
	public static String BaseOption_e_null_identifier;
	public static String BaseOption_e_null_name;
	public static String BaseOption_e_null_value_type;
	public static String OsgiPreferenceStorage_e_null_preference_node;
	public static String PreferenceStorage_e_load_unsupported;
	public static String PreferenceStorage_e_save_unsupported;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, OptionMessages.class);
	}

	private OptionMessages() {
	}
}
