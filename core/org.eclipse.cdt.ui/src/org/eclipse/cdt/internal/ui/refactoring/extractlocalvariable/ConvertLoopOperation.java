/*******************************************************************************
 * Copyright (c) 2005, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Taiming Wang - Initial implementation copied from JDT and extended for CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConvertLoopOperation {
	protected static final String FOR_LOOP_ELEMENT_IDENTIFIER = "element"; //$NON-NLS-1$

	private static final Map<String, String> IRREG_NOUNS = Stream
			.of(new AbstractMap.SimpleImmutableEntry<>("Children", "Child"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Entries", "Entry"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Proxies", "Proxy"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Indices", "Index"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("People", "Person"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Properties", "Property"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Factories", "Factory"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Archives", "archive"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Aliases", "Alias"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Alternatives", "Alternative"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Capabilities", "Capability"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Hashes", "Hash"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Directories", "Directory"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Statuses", "Status"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Instances", "Instance"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Classes", "Class"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Deliveries", "Delivery"), //$NON-NLS-1$ //$NON-NLS-2$
					new AbstractMap.SimpleImmutableEntry<>("Vertices", "Vertex")) //$NON-NLS-1$ //$NON-NLS-2$
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	private static final Set<String> NO_BASE_TYPES = Stream.of("integers", //$NON-NLS-1$
			"floats", //$NON-NLS-1$
			"doubles", //$NON-NLS-1$
			"booleans", //$NON-NLS-1$
			"bytes", //$NON-NLS-1$
			"chars", //$NON-NLS-1$
			"shorts", //$NON-NLS-1$
			"longs") //$NON-NLS-1$
			.collect(Collectors.toSet());

	private static final Set<String> CUT_PREFIX = Stream.of("all") //$NON-NLS-1$
			.collect(Collectors.toSet());

	private static final Set<String> IRREG_ENDINGS = Stream.of("xes", //$NON-NLS-1$
			"ies", //$NON-NLS-1$
			"oes", //$NON-NLS-1$
			"ses", //$NON-NLS-1$
			"hes", //$NON-NLS-1$
			"zes", //$NON-NLS-1$
			"ves", //$NON-NLS-1$
			"ces", //$NON-NLS-1$
			"ss", //$NON-NLS-1$
			"is", //$NON-NLS-1$
			"us", //$NON-NLS-1$
			"os", //$NON-NLS-1$
			"as") //$NON-NLS-1$
			.collect(Collectors.toSet());

	public static String modifyBaseName(String suggestedName) {
		String name = suggestedName;
		for (String prefix : CUT_PREFIX) {
			if (prefix.length() >= suggestedName.length()) {
				continue;
			}
			char afterPrefix = suggestedName.charAt(prefix.length());
			if (Character.isUpperCase(afterPrefix) || afterPrefix == '_') {
				if (suggestedName.toLowerCase().startsWith(prefix)) {
					String nameWithoutPrefix = suggestedName.substring(prefix.length());
					if (nameWithoutPrefix.startsWith("_") && nameWithoutPrefix.length() > 1) { //$NON-NLS-1$
						name = nameWithoutPrefix.substring(1);
					} else {
						name = nameWithoutPrefix;
					}
					if (name.length() == 1) {
						return name;
					}
					break;
				}
			}
		}
		for (Map.Entry<String, String> entry : IRREG_NOUNS.entrySet()) {
			String suffix = entry.getKey();
			if (name.toLowerCase().endsWith(suffix.toLowerCase())) {
				String firstPart = name.substring(0, name.length() - suffix.length());
				return firstPart + entry.getValue();
			}
		}
		for (String varname : NO_BASE_TYPES) {
			if (name.equalsIgnoreCase(varname)) {
				return FOR_LOOP_ELEMENT_IDENTIFIER;
			}
		}
		for (String suffix : IRREG_ENDINGS) {
			if (name.toLowerCase().endsWith(suffix)) {
				return FOR_LOOP_ELEMENT_IDENTIFIER;
			}
		}
		if (name.length() > 2 && name.endsWith("s")) { //$NON-NLS-1$
			return name.substring(0, name.length() - 1);
		}
		return FOR_LOOP_ELEMENT_IDENTIFIER;
	}
}
