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
import java.util.Optional;

import org.eclipse.cdt.internal.cquery.core.CqueryLanguageServer;
import org.eclipse.cdt.lsp.LanguageServerConfiguration;
import org.eclipse.cdt.lsp.SupportedLanguageServers;
import org.eclipse.cdt.lsp.core.ClangdLanguageServer;
import org.eclipse.cdt.lsp.core.PreferenceConstants;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.FrameworkUtil;

//FIXME ok, not really contributed at the moment, but will be
public final class ContributedLanguageServers implements SupportedLanguageServers {

	private final Map<String, LanguageServerConfiguration> configs;

	public ContributedLanguageServers() {
		configs = new LinkedHashMap<>();
		register(new ClangdLanguageServer());
		register(new CqueryLanguageServer());
	}

	@Override
	public Collection<LanguageServerConfiguration> all() {
		return configs.values();
	}

	public void register(LanguageServerConfiguration configuration) {
		configs.put(configuration.identifier(), configuration);
	}

	public void unregister(LanguageServerConfiguration configuration) {
		configs.remove(configuration.identifier());
	}

	@Override
	public LanguageServerConfiguration preferred() throws IllegalStateException {
		return Optional.ofNullable(InstanceScope.INSTANCE.getNode(nodeQualifier()))//
				.filter(IEclipsePreferences.class::isInstance)//
				.map(IEclipsePreferences.class::cast)//
				.flatMap(x -> Optional.ofNullable(x.get(nodePath(), null)))//
				.map(configs::get)//
				.orElseThrow(() -> new IllegalStateException("No preferred server was found"));
	}

	private String nodeQualifier() {
		return FrameworkUtil.getBundle(getClass()).getSymbolicName();
	}

	private String nodePath() {
		//FIXME: rework
		return PreferenceConstants.P_SERVER_CHOICE;
	}

}
