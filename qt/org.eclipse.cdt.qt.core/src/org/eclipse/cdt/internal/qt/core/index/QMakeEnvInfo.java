/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;

/**
 * Holds data describing QMake environment (pro file, qmake file, env. vars.) for a specific QMake run provided by QMakeEnv instance.
 */
public final class QMakeEnvInfo {

	private final IFile proFile;
	private final String qmakeFilePath;
	private final Map<String, String> environment;
	private final Set<IFile> sensitiveFiles;

	/**
	 * Creates QMakeEnvInfo.
	 *
	 * @param proFile the root-level .pro file
	 * @param qmakeFilePath the absolute path of qmake executable
	 * @param environment environment variables
	 * @param sensitiveFiles the list of IFile that needs to be tracked by IQMakeProjectInfo since their change might affect
	 *                       an environment for running qmake.
	 */
	public QMakeEnvInfo(IFile proFile, String qmakeFilePath, Map<String, String> environment,
			Collection<IFile> sensitiveFiles) {
		this.proFile = proFile;
		this.qmakeFilePath = qmakeFilePath;
		this.environment = environment != null ? new HashMap<>(environment) : Collections.<String, String>emptyMap();
		this.sensitiveFiles = sensitiveFiles != null ? new HashSet<>(sensitiveFiles) : Collections.<IFile>emptySet();
	}

	/**
	 * Returns IFile of .pro file.
	 *
	 * @return the .pro file
	 */
	public IFile getProFile() {
		return proFile;
	}

	/**
	 * Returns an absolute path of qmake executable.
	 *
	 * @return the qmake path
	 */
	public String getQMakeFilePath() {
		return qmakeFilePath;
	}

	/**
	 * Returns a map of environment variables that are used for qmake run.
	 *
	 * @return the environment
	 */
	public Map<String, String> getEnvironment() {
		return environment;
	}

	/**
	 * Returns a list of IFile that might affect environment of qmake run.
	 *
	 * @return the list sensitive files
	 */
	public Set<IFile> getSensitiveFiles() {
		return sensitiveFiles;
	}

}
