/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	public static String DsfDebugPreferencePage_description;
	public static String DsfDebugPreferencePage_limitStackFrames_label;

	public static String DsfDebugPreferencePage_minStepInterval_label;
	public static String DsfDebugPreferencePage_performanceGroup_label;

	public static String DsfDebugPreferencePage_waitForViewUpdate_label;

	static {
		// initialize resource bundle
		NLS.initializeMessages(MessagesForPreferences.class.getName(), MessagesForPreferences.class);
	}

	private MessagesForPreferences() {
	}
}
