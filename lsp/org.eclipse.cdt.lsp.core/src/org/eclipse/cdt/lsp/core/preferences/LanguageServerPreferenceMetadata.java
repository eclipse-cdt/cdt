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
package org.eclipse.cdt.lsp.core.preferences;

import org.eclipse.core.runtime.preferences.PreferenceMetadata;

/**
 * The metadata for preferences to configure language server
 *
 */
public interface LanguageServerPreferenceMetadata {

	/**
	 * Returns the metadata for the "Prefer Language Server" preference, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Prefer Language Server" preference
	 *
	 * @see LanguageServerPreferences#preferLanguageServer()
	 */
	PreferenceMetadata<Boolean> preferLanguageServer();

}
