/*******************************************************************************
 * Copyright (c) 2010-2013 Nokia Siemens Networks Oyj, Finland.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Petri Tuononen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.ui.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.managedbuilder.llvm.ui.preferences.messages"; //$NON-NLS-1$
	public static String IncludePathListEditor_0;
	public static String IncludePathListEditor_1;
	public static String LibraryListEditor_0;
	public static String LibraryPathListEditor_0;
	public static String LibraryPathListEditor_1;
	public static String LlvmPreferencePage_0;
	public static String LlvmPreferencePage_1;
	public static String LlvmPreferencePage_2;
	public static String LlvmPreferencePage_3;
	public static String LlvmPreferencePage_4;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
