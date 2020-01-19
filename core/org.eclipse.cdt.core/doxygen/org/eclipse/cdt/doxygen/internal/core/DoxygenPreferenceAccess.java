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
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.doxygen.internal.core;

import java.util.Objects;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.options.OptionStorage;
import org.eclipse.cdt.core.options.OsgiPreferenceStorage;
import org.eclipse.cdt.doxygen.DoxygenMetadata;
import org.eclipse.cdt.doxygen.DoxygenOptions;
import org.eclipse.cdt.doxygen.core.DoxygenConfiguration;
import org.eclipse.cdt.doxygen.core.DoxygenPreferences;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.prefs.Preferences;

@Component
public class DoxygenPreferenceAccess implements DoxygenConfiguration, DoxygenPreferences {

	private final DoxygenMetadata doxygenMetadata;

	public DoxygenPreferenceAccess() {
		this.doxygenMetadata = new DoxygenMetadataDefaults();
	}

	@Override
	public OptionStorage workspaceStorage() {
		return new OsgiPreferenceStorage(preferences(InstanceScope.INSTANCE));
	}

	@Override
	public OptionStorage projectStorage(IProject project) {
		Objects.requireNonNull(DoxygenCoreMessages.DoxygenPreferenceAccess_e_null_project);
		return new OsgiPreferenceStorage(preferences(new ProjectScope(project)));
	}

	@Override
	public DoxygenMetadata metadata() {
		return doxygenMetadata;
	}

	@Override
	public DoxygenOptions workspaceOptions() {
		return new DoxygenOptionsAccess(workspaceStorage(), doxygenMetadata);
	}

	@Override
	public DoxygenOptions projectOptions(IProject project) {
		Objects.requireNonNull(DoxygenCoreMessages.DoxygenPreferenceAccess_e_null_project);
		return new DoxygenOptionsAccess(projectStorage(project), doxygenMetadata);
	}

	private Preferences preferences(IScopeContext scope) {
		return scope.getNode(nodeQualifier()).node(nodePath());
	}

	private String nodeQualifier() {
		return CCorePlugin.PLUGIN_ID;
	}

	private String nodePath() {
		return "doxygen"; //$NON-NLS-1$
	}
}
