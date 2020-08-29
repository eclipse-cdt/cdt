/*******************************************************************************
 * Copyright (c) 2019, 2020 Eclipse Contributors and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - ongoing support
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.core;

import org.eclipse.osgi.util.NLS;

public class LspCoreMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.lsp.internal.core.LspCoreMessages"; //$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, LspCoreMessages.class);
	}

	public static String LanguageServerDefaults_prefer_description;
	public static String LanguageServerDefaults_prefer_name;
	public static String ShowStatus_busy;
	public static String ShowStatus_idle;

	private LspCoreMessages() {
	}
}
