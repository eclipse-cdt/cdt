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
import java.util.Optional;

import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

public abstract class PreferredOptions {
	protected final String qualifier;
	protected final IScopeContext[] scopes;

	public PreferredOptions(String qualifier, IScopeContext[] scopes) {
		this.qualifier = Objects.requireNonNull(qualifier);
		this.scopes = Objects.requireNonNull(scopes);
	}

	protected String stringValue(PreferenceMetadata<?> meta) {
		String actual = String.valueOf(meta.defaultValue());
		for (int i = scopes.length - 1; i >= 0; i--) {
			IScopeContext scope = scopes[i];
			String previous = actual;
			actual = scope.getNode(qualifier).get(meta.identifer(), previous);
		}
		return actual;
	}

	protected boolean booleanValue(PreferenceMetadata<Boolean> meta) {
		return Optional.of(meta)//
				.map(this::stringValue)//
				.map(Boolean::valueOf)//
				.orElseGet(meta::defaultValue);
	}

}
