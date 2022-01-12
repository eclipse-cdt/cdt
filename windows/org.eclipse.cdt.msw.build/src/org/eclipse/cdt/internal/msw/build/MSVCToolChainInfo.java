/*******************************************************************************
 * Copyright (c) 2020 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.msw.build;

/**
 * Basic information about a MSVC toolchain as found under a VS installation.
 * More information could be added here in order to support users selecting a specific
 * toolchain (architecture, etc).
 */
public class MSVCToolChainInfo {
	private String fPathEnvVar;
	private String fIncludeEnvVar;
	private String fLibEnvVar;

	MSVCToolChainInfo(String pathEnvVar, String includeEnvVar, String libEnvVar) {
		fPathEnvVar = pathEnvVar;
		fIncludeEnvVar = includeEnvVar;
		fLibEnvVar = libEnvVar;
	}

	/**
	 * @return The PATH environment variable containing paths needed for this toolchain. Delimited with ';' for multiple paths.
	 */
	public String getPathEnvVar() {
		return fPathEnvVar;
	}

	/**
	 * @return The INCLUDE environment variable containing paths needed for this toolchain. Delimited with ';' for multiple paths.
	 */
	public String getIncludeEnvVar() {
		return fIncludeEnvVar;
	}

	/**
	 * @return The LIB environment variable containing paths needed for this toolchain. Delimited with ';' for multiple paths.
	 */
	public String getLibEnvVar() {
		return fLibEnvVar;
	}
}