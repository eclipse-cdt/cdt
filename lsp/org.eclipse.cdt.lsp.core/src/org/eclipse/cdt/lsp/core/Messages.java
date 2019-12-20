/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.core;

import org.eclipse.cdt.lsp.internal.core.LspCoreMessages;
import org.eclipse.cdt.lsp.internal.ui.LspUiMessages;
import org.eclipse.osgi.util.NLS;

@Deprecated
public class Messages extends NLS {

	public static String PreferencePageDescription = LspUiMessages.CPPLanguageServerPreferencePage_description;
	public static String ServerChoiceLabel = LspUiMessages.CPPLanguageServerPreferencePage_server_selector;
	public static String ServerPathLabel = LspUiMessages.CPPLanguageServerPreferencePage_server_path;
	public static String ServerOptionsLabel = LspUiMessages.CPPLanguageServerPreferencePage_server_options;

	public static String CqueryStateIdle = LspCoreMessages.Server2ClientProtocolExtension_cquery_idle;
	public static String CqueryStateBusy = LspCoreMessages.Server2ClientProtocolExtension_cquery_busy;

}
