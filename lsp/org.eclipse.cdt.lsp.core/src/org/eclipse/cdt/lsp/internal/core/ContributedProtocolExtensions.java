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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.cdt.lsp.LanguageProtocolExtension;
import org.eclipse.cdt.lsp.LanguageServerConfiguration;
import org.eclipse.cdt.lsp.SupportedProtocolExtensions;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

@Component
public final class ContributedProtocolExtensions implements SupportedProtocolExtensions {

	private final Map<String, List<LanguageProtocolExtension>> extensions;

	public ContributedProtocolExtensions() {
		extensions = new LinkedHashMap<>();
	}

	@Override
	public Collection<LanguageProtocolExtension> all() {
		return extensions.values().stream()//
				.flatMap(List::stream)//
				.collect(Collectors.toList());
	}

	@Override
	public Collection<LanguageProtocolExtension> applicable(LanguageServerConfiguration server) {
		return new ArrayList<>(extensions.getOrDefault(server.identifier(), Collections.emptyList()));
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE)
	public void register(LanguageProtocolExtension extension) {
		List<LanguageProtocolExtension> list = extensions.computeIfAbsent(extension.targetIdentifier(),
				x -> new ArrayList<>());
		if (!list.contains(extension)) {
			list.add(extension);
		}
	}

	public void unregister(LanguageProtocolExtension extension) {
		Optional<List<LanguageProtocolExtension>> optional = Optional
				.ofNullable(extensions.get(extension.targetIdentifier()));
		if (optional.isPresent()) {
			List<LanguageProtocolExtension> list = optional.get();
			list.remove(extension);
			if (list.isEmpty()) {
				extensions.remove(extension.targetIdentifier());
			}
		}
	}

}
