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

import org.eclipse.cdt.cmake.is.core.language.settings.providers.IParserPreferences;
import org.eclipse.cdt.cmake.is.core.language.settings.providers.IParserPreferencesAccess;
import org.eclipse.cdt.cmake.is.core.language.settings.providers.IParserPreferencesMetadata;
import org.eclipse.cdt.core.options.OptionStorage;
import org.eclipse.cdt.core.options.OsgiPreferenceStorage;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.prefs.Preferences;

/**
 * @author weber
 */
@Component
public class ParserPreferencesAccess implements IParserPreferencesAccess {

	private final ParserPreferencesMetadata metadata;

	public ParserPreferencesAccess() {
		this.metadata = new ParserPreferencesMetadata();
	}

	private OptionStorage workspaceStorage() {
		return new OsgiPreferenceStorage(preferences(InstanceScope.INSTANCE));
	}

	@Override
	public IParserPreferences getWorkspacePreferences() {
		return new ParserPreferences(workspaceStorage(), metadata);
	}

	@Override
	public IParserPreferencesMetadata metadata() {
		return metadata;
	}

	private Preferences preferences(IScopeContext scope) {
		return scope.getNode(nodeQualifier()).node(nodePath());
	}

	private String nodeQualifier() {
		return Plugin.PLUGIN_ID;
	}

	private String nodePath() {
		return "parser"; //$NON-NLS-1$
	}
}
