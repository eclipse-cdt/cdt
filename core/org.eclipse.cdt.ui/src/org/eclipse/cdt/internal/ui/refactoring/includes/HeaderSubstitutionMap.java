/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.internal.corext.codemanipulation.IncludeInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * A set of header file substitution rules.
 */
public class HeaderSubstitutionMap {
	private static final String TAG_HEADER_SUBSTITUTION_MAPS = "maps"; //$NON-NLS-1$
	private static final String TAG_HEADER_SUBSTITUTION_MAP = "map"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_CPP_ONLY = "cpp_only"; //$NON-NLS-1$
	private static final String TAG_UNCONDITIONAL_SUBSTITUTION_MAP = "unconditional_substitution_map"; //$NON-NLS-1$
	private static final String TAG_OPTIONAL_SUBSTITUTION_MAP = "optional_substitution_map"; //$NON-NLS-1$

	private String name;
	private boolean cppOnly;
	private final IncludeMap unconditionalSubstitutionMap;
	private final IncludeMap optionalSubstitutionMap;

	public HeaderSubstitutionMap(boolean cppOnly) {
		this.cppOnly = cppOnly;
		this.unconditionalSubstitutionMap = new IncludeMap(true);
		this.optionalSubstitutionMap = new IncludeMap(false);
	}

	public HeaderSubstitutionMap(String name, boolean cppOnly, IncludeMap unconditionalSubstitutionMap,
			IncludeMap optionalSubstitutionMap) {
		this.name = name;
		this.cppOnly = cppOnly;
		this.unconditionalSubstitutionMap = unconditionalSubstitutionMap;
		this.optionalSubstitutionMap = optionalSubstitutionMap;
	}

	/**
	 * Indicates that the header file {@code to} should be used instead of {@code from}.
	 *
	 * @param from The header file to be replaced.
	 * @param to The header file to be used instead.
	 * @param unconditionalSubstitution {@code true} if the header substitution is mandatory.
	 *     Otherwise substitution only if the {@code to} header has to be included for other
	 *     reasons.
	 */
	protected void addMapping(IncludeInfo from, IncludeInfo to, boolean unconditionalSubstitution) {
		IncludeMap map = unconditionalSubstitution ? unconditionalSubstitutionMap : optionalSubstitutionMap;
		map.addMapping(from, to);
	}

	/**
	 * Indicates that the header file {@code to} should be used instead of {@code from}.

	 * @param from The header file to be replaced. The header is represented by an include name
	 *     optionally surrounded by double quotes or angle brackets. Angle brackets indicate
	 *     a system include.
	 * @param to The header file to be used instead. The header is represented by an include name
	 *     optionally surrounded by double quotes or angle brackets. Angle brackets indicate
	 *     a system include.
	 * @param unconditionalSubstitution {@code true} if the header substitution is mandatory.
	 *     Otherwise substitution only if the {@code to} header has to be included for other
	 *     reasons.
	 */
	public void addMapping(String from, String to, boolean unconditionalSubstitution) {
		IncludeMap map = unconditionalSubstitution ? unconditionalSubstitutionMap : optionalSubstitutionMap;
		map.addMapping(from, to);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isCppOnly() {
		return cppOnly;
	}

	public void setCppOnly(boolean cppOnly) {
		this.cppOnly = cppOnly;
	}

	public void saveToMemento(IMemento memento) {
		if (name != null)
			memento.putString(TAG_NAME, name);
		memento.putBoolean(TAG_CPP_ONLY, cppOnly);
		unconditionalSubstitutionMap.saveToMemento(memento.createChild(TAG_UNCONDITIONAL_SUBSTITUTION_MAP));
		optionalSubstitutionMap.saveToMemento(memento.createChild(TAG_OPTIONAL_SUBSTITUTION_MAP));
	}

	public static HeaderSubstitutionMap fromMemento(IMemento memento) {
		String name = memento.getString(TAG_NAME);
		Boolean b = memento.getBoolean(TAG_CPP_ONLY);
		boolean cppOnly = b != null && b.booleanValue();
		IncludeMap unconditionalSubstitutionMap = IncludeMap.fromMemento(true,
				memento.getChild(TAG_UNCONDITIONAL_SUBSTITUTION_MAP));
		IncludeMap optionalSubstitutionMap = IncludeMap.fromMemento(false,
				memento.getChild(TAG_OPTIONAL_SUBSTITUTION_MAP));
		// Remove potential redundant substitutions from optionalSubstitutionMap.
		for (IncludeInfo header : unconditionalSubstitutionMap.getMap().keySet()) {
			optionalSubstitutionMap.removeMapping(header);
		}
		return new HeaderSubstitutionMap(name, cppOnly, unconditionalSubstitutionMap, optionalSubstitutionMap);
	}

	public static HeaderSubstitutionMap fromSerializedMemento(String str) {
		return fromSerializedMemento(new StringReader(str));
	}

	public static HeaderSubstitutionMap fromSerializedMemento(Reader reader) {
		XMLMemento memento;
		try {
			memento = XMLMemento.createReadRoot(reader);
		} catch (WorkbenchException e) {
			return null;
		}
		return fromMemento(memento);
	}

	public static String serializeMaps(List<HeaderSubstitutionMap> maps) {
		XMLMemento memento = XMLMemento.createWriteRoot(TAG_HEADER_SUBSTITUTION_MAPS);
		for (HeaderSubstitutionMap element : maps) {
			element.saveToMemento(memento.createChild(TAG_HEADER_SUBSTITUTION_MAP));
		}
		StringWriter writer = new StringWriter();
		try {
			memento.save(writer);
		} catch (IOException e) {
			CUIPlugin.log(e);
		}
		return writer.toString();
	}

	public static List<HeaderSubstitutionMap> deserializeMaps(String str) {
		StringReader reader = new StringReader(str);
		XMLMemento memento;
		try {
			memento = XMLMemento.createReadRoot(reader);
		} catch (WorkbenchException e) {
			return Collections.emptyList();
		}

		List<HeaderSubstitutionMap> maps = new ArrayList<>();
		for (IMemento element : memento.getChildren(TAG_HEADER_SUBSTITUTION_MAP)) {
			maps.add(fromMemento(element));
		}
		return maps;
	}

	public IncludeMap getUnconditionalSubstitutionMap() {
		return unconditionalSubstitutionMap;
	}

	public IncludeMap getOptionalSubstitutionMap() {
		return optionalSubstitutionMap;
	}
}