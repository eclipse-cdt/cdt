/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.core.preferences;

import org.eclipse.cdt.lsp.core.preferences.LanguageServerPreferenceMetadata;
import org.eclipse.cdt.lsp.internal.core.LspCoreMessages;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

public class LanguageServerDefaults implements LanguageServerPreferenceMetadata {

	@Override
	public PreferenceMetadata<Boolean> preferLanguageServer() {
		return new PreferenceMetadata<>(Boolean.class, "prefer_language_server", false, //$NON-NLS-1$
				LspCoreMessages.LanguageServerDefaults_prefer_name,
				LspCoreMessages.LanguageServerDefaults_prefer_description);
	}

}
