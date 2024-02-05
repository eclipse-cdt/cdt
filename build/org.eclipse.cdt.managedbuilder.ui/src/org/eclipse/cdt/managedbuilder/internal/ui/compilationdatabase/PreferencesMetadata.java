/*******************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase;

import org.eclipse.core.runtime.preferences.PreferenceMetadata;

public interface PreferencesMetadata {

	/**
	 * Returns the metadata for the "Generate compile_commands.json file" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Generate compile_commands.json file" option
	 *
	 * @see PreferenceOptions#generateCDB()
	 */
	PreferenceMetadata<Boolean> generateCDBFile();

}
