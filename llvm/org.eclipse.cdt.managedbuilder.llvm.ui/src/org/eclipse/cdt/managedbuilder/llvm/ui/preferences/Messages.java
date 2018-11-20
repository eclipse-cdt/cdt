/*******************************************************************************
 * Copyright (c) 2010-2013 Nokia Siemens Networks Oyj, Finland.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
