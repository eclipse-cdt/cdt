/********************************************************************************
 * Copyright (c) 2023, 2024 Renesas Electronics Corp. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase.messages"; //$NON-NLS-1$
	public static String JsonCdbGeneratorPreferencePage_description;
	public static String JsonCdbGeneratorPreferencePage_generateCompilationdatabase;
	public static String JsonCdbGeneratorPropertyPage_configureWorkspace;
	public static String JsonCdbGeneratorPropertyPage_enableProjectSpecific;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
