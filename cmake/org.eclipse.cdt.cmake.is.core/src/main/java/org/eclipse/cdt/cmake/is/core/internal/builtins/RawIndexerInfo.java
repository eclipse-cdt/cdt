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

package org.eclipse.cdt.cmake.is.core.internal.builtins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.cdt.cmake.is.core.internal.Plugin;
import org.eclipse.cdt.cmake.is.core.participant.IRawIndexerInfo;
import org.eclipse.cdt.cmake.is.core.participant.IRawIndexerInfoCollector;
import org.eclipse.core.runtime.Platform;

/**
 * Default implementation of IRawIndexerInfo.
 *
 * @author weber
 */
public class RawIndexerInfo implements IRawIndexerInfo, IRawIndexerInfoCollector {

	@SuppressWarnings("nls")
	private static final boolean DEBUG = Boolean
			.parseBoolean(Platform.getDebugOption(Plugin.PLUGIN_ID + "/debug/detected.entries"));

	private final Map<String, String> defines = new HashMap<>();
	private final List<String> undefines = new ArrayList<>();
	private final List<String> includePaths = new ArrayList<>();
	private final List<String> systemIncludePaths = new ArrayList<>();
	private final List<String> macroFiles = new ArrayList<>(0);
	private final List<String> includeFiles = new ArrayList<>(0);

	@Override
	public void addDefine(String name, String value) {
		Objects.requireNonNull(name);
		value = Objects.toString(value, ""); //$NON-NLS-1$
		if (DEBUG)
			System.out.printf("    Added define: %s=%s%n", name, value); //$NON-NLS-1$
		defines.put(name, value);
	}

	@Override
	public void addUndefine(String name) {
		Objects.requireNonNull(name);
		if (DEBUG)
			System.out.printf("    Added undefine: %s%n", name); //$NON-NLS-1$
		undefines.add(name);
		// - The GCC man page states:
		// '-U name Cancel any previous definition of name, either built in or provided
		// with a -D option.'
		// - The POSIX c99 man page states:
		// '-U name Remove any initial definition of name.'
		for (Iterator<Map.Entry<String, String>> iter = defines.entrySet().iterator(); iter.hasNext();) {
			Entry<String, String> define = iter.next();
			if (define.getKey().equals(name)) {
				if (DEBUG)
					System.out.printf("      Removed define: %s=%s%n", define.getKey(), define.getValue()); //$NON-NLS-1$
				iter.remove();
			}
		}
	}

	@Override
	public void addIncludePath(String path) {
		Objects.requireNonNull(path);
		if (DEBUG)
			System.out.printf("    Added incl path: %s%n", path); //$NON-NLS-1$
		includePaths.add(path);
	}

	@Override
	public void addSystemIncludePath(String path) {
		Objects.requireNonNull(path);
		if (DEBUG)
			System.out.printf("    Added sys incl path: %s%n", path); //$NON-NLS-1$
		systemIncludePaths.add(path);
	}

	@Override
	public void addMacroFile(String path) {
		Objects.requireNonNull(path);
		if (DEBUG)
			System.out.printf("    Added macro file: %s%n", path); //$NON-NLS-1$
		macroFiles.add(path);
	}

	@Override
	public void addIncludeFile(String path) {
		Objects.requireNonNull(path);
		if (DEBUG)
			System.out.printf("    Added include file: %s%n", path); //$NON-NLS-1$
		includeFiles.add(path);
	}

	@Override
	public Map<String, String> getDefines() {
		return Collections.unmodifiableMap(defines);
	}

	@Override
	public List<String> getUndefines() {
		return Collections.unmodifiableList(undefines);
	}

	@Override
	public List<String> getIncludePaths() {
		return Collections.unmodifiableList(includePaths);
	}

	@Override
	public List<String> getSystemIncludePaths() {
		return Collections.unmodifiableList(systemIncludePaths);
	}

	@Override
	public List<String> getMacroFiles() {
		return Collections.unmodifiableList(macroFiles);
	}

	@Override
	public List<String> getIncludeFiles() {
		return Collections.unmodifiableList(includeFiles);
	}
}
