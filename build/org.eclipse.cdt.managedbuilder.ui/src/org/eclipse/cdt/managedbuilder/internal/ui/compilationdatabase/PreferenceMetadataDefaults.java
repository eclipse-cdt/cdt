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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class PreferenceMetadataDefaults implements PreferencesMetadata {

	@Reference
	private PreferenceOptionsDefaults defaults;

	@Override
	public PreferenceMetadata<Boolean> generateCDBFile() {
		return new PreferenceMetadata<>(Boolean.class, //
				"generateCDB", defaults.generateCDB(), // //$NON-NLS-1$
				Messages.JsonCdbGeneratorPreferencePage_generateCompilationdatabase,
				Messages.JsonCdbGeneratorPreferencePage_description);
	}

}
