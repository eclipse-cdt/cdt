/*******************************************************************************
 * Copyright (c) 2019 Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - initial API and implementatin
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.ui;

import org.eclipse.osgi.util.NLS;

public class LspUiMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.lsp.internal.ui.LspUiMessages"; //$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, LspUiMessages.class);
	}

	public static String CPPLanguageServerPreferencePage_clangd;
	public static String CPPLanguageServerPreferencePage_cquery;

	public static String CPPLanguageServerPreferencePage_description;
	public static String CPPLanguageServerPreferencePage_server_options;
	public static String CPPLanguageServerPreferencePage_server_path;
	public static String CPPLanguageServerPreferencePage_server_selector;

	private LspUiMessages() {
	}
}
