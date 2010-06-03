/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.preferences;

import org.eclipse.osgi.util.NLS;

/**
 * Preference strings.
 */
class MessagesForPreferences extends NLS {
	private static final String BUNDLE_NAME= "org.eclipse.cdt.dsf.debug.internal.ui.preferences.messages"; //$NON-NLS-1$

	public static String DsfDebugPreferencePage_description;
	public static String DsfDebugPreferencePage_limitStackFrames_label;

	public static String DsfDebugPreferencePage_minStepInterval_label;
	public static String DsfDebugPreferencePage_performanceGroup_label;

	public static String DsfDebugPreferencePage_waitForViewUpdate_label;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, MessagesForPreferences.class);
	}

	private MessagesForPreferences() {
	}
}
