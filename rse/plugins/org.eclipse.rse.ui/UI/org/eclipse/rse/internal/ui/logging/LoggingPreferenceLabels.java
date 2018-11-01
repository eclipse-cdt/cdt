/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * David Dykstal (IBM) - 148434 Better F1 help.
 *******************************************************************************/
package org.eclipse.rse.internal.ui.logging;

import org.eclipse.osgi.util.NLS;

public class LoggingPreferenceLabels extends NLS {

	private static String BUNDLE_NAME = "org.eclipse.rse.internal.ui.logging.LoggingPreferenceLabels";//$NON-NLS-1$

	public static String LOGGING_PREFERENCE_PAGE_TOPLABEL;
	public static String LOGGING_PREFERENCE_PAGE_ERRORS_ONLY; 
	public static String LOGGING_PREFERENCE_PAGE_WARNINGS_ERRORS;
	public static String LOGGING_PREFERENCE_PAGE_INFO_DEBUG;
	public static String LOGGING_PREFERENCE_PAGE_FULL_DEBUG;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, LoggingPreferenceLabels.class);
	}

}
