/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.lrparser.xlc.ui.preferences;

import org.eclipse.osgi.util.NLS;

public class PreferenceMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.core.lrparser.xlc.ui.preferences.PreferenceMessages"; //$NON-NLS-1$

	private PreferenceMessages() {}
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, PreferenceMessages.class);
	}
	
	
	public static String
		XlcLanguageOptionsPreferencePage_link,
		XlcLanguageOptionsPreferencePage_group,
		XlcLanguageOptionsPreferencePage_preference_vectors;
	
}

