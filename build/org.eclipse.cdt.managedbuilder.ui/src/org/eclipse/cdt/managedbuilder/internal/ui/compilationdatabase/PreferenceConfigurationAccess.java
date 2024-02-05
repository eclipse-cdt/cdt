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

import java.util.Optional;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IPreferenceMetadataStore;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.OsgiPreferenceMetadataStore;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

@Component
public class PreferenceConfigurationAccess extends ConfigurationAccess implements Configuration {

	@Reference
	private PreferencesMetadata metadata;

	@Reference
	private IWorkspace workspace;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	private GenerateCDBEnable enable;

	public PreferenceConfigurationAccess() {
		super(new Qualifier().get());
	}

	@Override
	public PreferenceOptions defaults() {
		return new PreferencePreferredOptions(qualifier, new IScopeContext[] { DefaultScope.INSTANCE }, metadata,
				enable);
	}

	@Override
	public PreferenceOptions options(Object context) {
		Optional<ProjectScope> project = projectScope(workspace, context);
		IScopeContext[] scopes;
		if (project.isPresent()) {
			scopes = new IScopeContext[] { project.get(), InstanceScope.INSTANCE, DefaultScope.INSTANCE };
		} else {
			scopes = new IScopeContext[] { InstanceScope.INSTANCE, DefaultScope.INSTANCE };
		}
		return new PreferencePreferredOptions(qualifier, scopes, metadata, enable);
	}

	@Override
	public IPreferenceMetadataStore storage(Object context) {
		return new OsgiPreferenceMetadataStore(//
				preferences(//
						projectScope(workspace, context)//
								.map(IScopeContext.class::cast)//
								.orElse(InstanceScope.INSTANCE)));
	}

	@Override
	public PreferencesMetadata metadata() {
		return metadata;
	}

	@Override
	public String qualifier() {
		return qualifier;
	}

}
