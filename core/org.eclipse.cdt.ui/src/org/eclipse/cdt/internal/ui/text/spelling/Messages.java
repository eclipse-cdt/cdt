/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = Messages.class.getName();

	public static String AbstractSpellingDictionary_encodingError;
	public static String Spelling_add_askToConfigure_ignoreMessage;
	public static String Spelling_add_askToConfigure_question;
	public static String Spelling_add_askToConfigure_title;
	public static String Spelling_add_info;
	public static String Spelling_add_label;
	public static String Spelling_case_label;
	public static String Spelling_correct_label;
	public static String Spelling_disable_info;
	public static String Spelling_disable_label;
	public static String Spelling_error_case_label;
	public static String Spelling_error_label;
	public static String Spelling_ignore_info;
	public static String Spelling_ignore_label;

	public static String Spelling_msgWithLocation;

	static {
		// Initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
