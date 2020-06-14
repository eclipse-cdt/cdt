/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.cmake.is.core.internal;

import java.util.Optional;

import org.eclipse.cdt.cmake.is.core.IParserPreferences;
import org.eclipse.cdt.cmake.is.core.IParserPreferencesAccess;
import org.eclipse.cdt.cmake.is.core.IParserPreferencesMetadata;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceMetadataStore;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.OsgiPreferenceMetadataStore;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.component.annotations.Component;

/**
 * @author weber
 */
@Component
public class ParserPreferencesAccess implements IParserPreferencesAccess {

	private final ParserPreferencesMetadata metadata;

	public ParserPreferencesAccess() {
		this.metadata = new ParserPreferencesMetadata();
	}

	private IPreferenceMetadataStore workspaceStorage() {
		return new OsgiPreferenceMetadataStore(preferences(InstanceScope.INSTANCE));
	}

	@Override
	public IParserPreferences getWorkspacePreferences() {
		return new ParserPreferences(workspaceStorage(), metadata);
	}

	@Override
	public IParserPreferencesMetadata metadata() {
		return metadata;
	}

	private IEclipsePreferences preferences(IScopeContext scope) {
		return Optional.ofNullable(scope.getNode(nodeQualifier()))//
				.map(n -> n.node(nodePath()))//
				.filter(IEclipsePreferences.class::isInstance)//
				.map(IEclipsePreferences.class::cast)//
				.orElseThrow(() -> new IllegalStateException(//
						NLS.bind(Messages.ParserPreferencesAccess_e_get_preferences, //
								nodeQualifier(), nodePath())));
	}

	private String nodeQualifier() {
		return Plugin.PLUGIN_ID;
	}

	private String nodePath() {
		return "parser"; //$NON-NLS-1$
	}
}
