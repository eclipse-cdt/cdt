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
 * Marc Khouzam (Ericsson) - Modified for Remote launch (bug 450080)
 *******************************************************************************/

package org.eclipse.cdt.debug.application;

/**
 * This class provides information required to start debugging a remote executable.
 */
public class RemoteExecutableInfo {
	private final String fHostPath;
	private final String fBuildLog;
	private final String fAddress;
	private final String fPort;
	private final boolean fAttach;

	public RemoteExecutableInfo(String hostPath, String buildLog, String address, String port, boolean attach) {
		super();
		fHostPath = hostPath;
		fBuildLog = buildLog;
		fAddress = address;
		fPort = port;
		fAttach = attach;
	}

	public RemoteExecutableInfo(RemoteExecutableInfo info) {
		fHostPath = info.getHostPath();
		fBuildLog = info.getBuildLog();
		fAddress = info.getAddress();
		fPort = info.getPort();
		fAttach = info.isAttach();
	}

	/**
	 * Returns the path of the executable on the host
	 */
	public String getHostPath() {
		return fHostPath;
	}

	public String getAddress() {
		return fAddress;
	}

	public String getPort() {
		return fPort;
	}

	public boolean isAttach() {
		return fAttach;
	}

	/**
	 * Get the build log path.
	 *
	 * @return the build log path or null
	 */
	public String getBuildLog() {
		return fBuildLog;
	}
}