/*******************************************************************************
 * Copyright (c) 2013, 2014 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 * Red Hat Inc. - modified for use in Standalone Debugger
 *******************************************************************************/

package org.eclipse.cdt.debug.application;

/**
 * This class provides information required to start
 * debugging an executable.
 */
public class NewExecutableInfo {
	private String fHostPath;
	private String fTargetPath;
	private String fBuildLog;
	private String fArguments;

	public NewExecutableInfo(String hostPath, String targetPath, String buildLog, String args) {
		super();
		fHostPath = hostPath;
		fTargetPath = targetPath;
		fBuildLog = buildLog;
		fArguments = args;
	}

	/**
	 * Returns the path of the executable on the host
	 */
	public String getHostPath() {
		return fHostPath;
	}

	/**
	 * Sets the path of the executable on the host
	 */
	public void setHostPath(String path) {
		fHostPath = path;
	}

	/**
	 * For remote sessions returns the path of the executable
	 * on the target. Otherwise returns null.
	 */
	public String getTargetPath() {
		return fTargetPath;
	}

	/**
	 * Sets the path of the executable on the target for remote executables
	 */
	public void setTargetPath(String path) {
		fTargetPath = path;
	}

	/**
	 * Get the build log path.
	 *
	 * @return the build log path or null
	 */
	public String getBuildLog() {
		return fBuildLog;
	}

	/**
	 * Sets the build log path.
	 *
	 * @param path
	 */
	public void setBuildLog(String path) {
		fBuildLog = path;
	}

	/**
	 * Returns the arguments to pass to the executable, or null
	 */
	public String getArguments() {
		return fArguments;
	}

	/**
	 * Sets the arguments to pass to the executable
	 */
	public void setArguments(String args) {
		fArguments = args;
	}

}