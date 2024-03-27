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

import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IScopeContext;

public class PreferencePreferredOptions extends PreferredOptions implements PreferenceOptions, GenerateCDBEnable {
	private final PreferencesMetadata metadata;
	private final GenerateCDBEnable enable;

	public PreferencePreferredOptions(String qualifier, IScopeContext[] scopes, PreferencesMetadata metadata,
			GenerateCDBEnable enable) {
		super(qualifier, scopes);
		this.metadata = Objects.requireNonNull(metadata);
		this.enable = enable;
	}

	@Override
	public boolean generateCDB() {
		return booleanValue(metadata.generateCDBFile());
	}

	@Override
	public boolean isEnabledFor(IProject project) {
		if (enable != null) {
			return enable.isEnabledFor(project);
		}
		return booleanValue(metadata.generateCDBFile());
	}

}
