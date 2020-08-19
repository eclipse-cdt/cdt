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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.lsp.LanguageProtocolExtension;
import org.eclipse.cdt.lsp.SupportedProtocolExtensions;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;

@SuppressWarnings("restriction")
public final class DelegatingLauncherBuilder extends Launcher.Builder<LanguageClientImpl> {

	private final ResolvePreferredServer server;

	public DelegatingLauncherBuilder() {
		this.server = new ResolvePreferredServer();
	}

	@Override
	protected Map<String, JsonRpcMethod> getSupportedMethods() {
		Map<String, JsonRpcMethod> methods = new LinkedHashMap<>(super.getSupportedMethods());
		extensions().stream()//
				.map(x -> x.getClass())//
				.map(ServiceEndpoints::getSupportedMethods)//
				.forEach(methods::putAll);
		return methods;
	}

	private List<LanguageProtocolExtension> extensions() {
		List<LanguageProtocolExtension> extensions = new ArrayList<>();
		ServiceCaller.callOnce(getClass(), SupportedProtocolExtensions.class,
				x -> extensions.addAll(x.applicable(server.apply(getClass()))));
		return extensions;
	}

}
