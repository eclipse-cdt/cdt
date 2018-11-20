/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.qt.core.IQtInstall;

public class QtInstall implements IQtInstall {

	private final Path qmakePath;
	private String spec;
	private Map<String, String> properties = new HashMap<>();

	public QtInstall(Path qmakePath) {
		this.qmakePath = qmakePath;
	}

	@Override
	public Path getQmakePath() {
		return qmakePath;
	}

	@Override
	public Path getLibPath() {
		return qmakePath.resolve("../lib"); //$NON-NLS-1$
	}

	@Override
	public Path getQmlPath() {
		return qmakePath.resolve("../../qml"); //$NON-NLS-1$
	}

	public static String getSpec(Path qmakePath) throws IOException {
		if (Files.exists(qmakePath)) {
			Process proc = new ProcessBuilder(qmakePath.toString(), "-query", "QMAKE_XSPEC").start(); //$NON-NLS-1$ //$NON-NLS-2$
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
				String line = reader.readLine();
				if (line != null) {
					return line.trim();
				}
			}
		}
		return null;
	}

	@Override
	public String getSpec() {
		if (spec == null) {
			try {
				spec = getSpec(getQmakePath());
			} catch (IOException e) {
				Activator.log(e);
			}
		}
		return spec;
	}

	@Override
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}

	@Override
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

}
