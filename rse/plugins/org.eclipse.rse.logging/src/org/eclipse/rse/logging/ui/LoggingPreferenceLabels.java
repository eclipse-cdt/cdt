/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - 148434 Better F1 help.
 ********************************************************************************/
package org.eclipse.rse.logging.ui;

import org.eclipse.osgi.util.NLS;

public class LoggingPreferenceLabels extends NLS {

	private static String BUNDLE_NAME = "org.eclipse.rse.logging.ui.LoggingPreferenceLabels";//$NON-NLS-1$

	public static String LOGGING_PREFERENCE_PAGE_TOPLABEL1;
	public static String LOGGING_PREFERENCE_PAGE_TOPLABEL2;
	public static String LOGGING_PREFERENCE_PAGE_ERRORS_ONLY; 
	public static String LOGGING_PREFERENCE_PAGE_WARNINGS_ERRORS;
	public static String LOGGING_PREFERENCE_PAGE_INFO_DEBUG;
	public static String LOGGING_PREFERENCE_PAGE_FULL_DEBUG;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, LoggingPreferenceLabels.class);
	}

}
