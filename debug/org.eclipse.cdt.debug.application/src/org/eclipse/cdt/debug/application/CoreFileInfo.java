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
public class CoreFileInfo {
	private String fHostPath;
	private String fTargetPath;
	private String fCoreFilePath;

	public CoreFileInfo(String hostPath, String targetPath, String coreFilePath) {
		super();
		fHostPath = hostPath;
		fTargetPath = targetPath;
		fCoreFilePath = coreFilePath;
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
	public String getCoreFilePath() {
		return fCoreFilePath;
	}

	/**
	 * Sets the build log path.
	 *
	 * @param path
	 */
	public void setCoreFilePath(String path) {
		fCoreFilePath = path;
	}

}