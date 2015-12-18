/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import org.eclipse.cdt.qt.core.IQtInstall;

public class QtInstall implements IQtInstall {

	private final String name;
	private final Path qmakePath;
	private String spec;

	public QtInstall(String name, Path qmakePath) {
		this.name = name;
		this.qmakePath = qmakePath;
	}

	@Override
	public String getName() {
		return name;
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

	public static String getSpec(String qmakePath) throws IOException {
		Process proc = new ProcessBuilder(qmakePath, "-query", "QMAKE_XSPEC").start(); //$NON-NLS-1$ //$NON-NLS-2$
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
			String line = reader.readLine();
			if (line != null) {
				return line.trim();
			}
		}
		return null;
	}

	@Override
	public String getSpec() {
		if (spec == null) {
			try {
				spec = getSpec(getQmakePath().toString());
			} catch (IOException e) {
				Activator.log(e);
			}
		}
		return spec;
	}

}
