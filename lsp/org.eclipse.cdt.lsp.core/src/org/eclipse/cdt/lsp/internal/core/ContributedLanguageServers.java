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
package org.eclipse.cdt.lsp.internal.core;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.lsp.LanguageServerConfiguration;
import org.eclipse.cdt.lsp.SupportedLanguageServers;
import org.eclipse.cdt.lsp.core.PreferenceConstants;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

@Component
public final class ContributedLanguageServers implements SupportedLanguageServers {

	private final LanguageServerConfiguration undefined;
	private final Map<String, LanguageServerConfiguration> configs;

	public ContributedLanguageServers() {
		configs = new LinkedHashMap<>();
		undefined = new UndefinedLanguageServer();
	}

	@Override
	public Collection<LanguageServerConfiguration> all() {
		return configs.values();
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE)
	public void register(LanguageServerConfiguration configuration) {
		configs.put(configuration.identifier(), configuration);
	}

	public void unregister(LanguageServerConfiguration configuration) {
		configs.remove(configuration.identifier());
	}

	@Override
	public LanguageServerConfiguration preferred() {
		return configs.getOrDefault(preferredIdentifier(), undefined);
	}

	private String preferredIdentifier() {
		return Platform.getPreferencesService().getString(nodeQualifier(), nodePath(), undefined.identifier(),
				new IScopeContext[] { InstanceScope.INSTANCE });
	}

	private String nodeQualifier() {
		return FrameworkUtil.getBundle(getClass()).getSymbolicName();
	}

	private String nodePath() {
		//FIXME: rework
		return PreferenceConstants.P_SERVER_CHOICE;
	}

}
