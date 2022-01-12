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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Information about a single Visual Studio installation (paths, toolchains, etc).
 */
public class VSInstallation {
	private String fLocation;
	private List<MSVCToolChainInfo> fToolChains = null;

	VSInstallation(String location) {
		super();
		this.fLocation = location;
	}

	// Return a non-null String if there is a single non-empty line in the output of the command, null otherwise.
	private static String getSingleLineOutputFromCommand(String... command) {
		String allOutput[] = ProcessOutputUtil.getAllOutputFromCommand(command);
		if (allOutput == null || allOutput.length != 1 || allOutput[0].isEmpty()) {
			return null;
		}
		return allOutput[0];
	}

	private void detectToolchains() {
		fToolChains = new ArrayList<>();

		String vcVarsLocation = fLocation + "\\VC\\Auxiliary\\Build\\vcvarsall.bat"; //$NON-NLS-1$
		//TODO: Support more toolchains/architectures (host and target) when we start giving the choice to the user.
		final String arch = "amd64"; //$NON-NLS-1$
		String vcVarsLocationCommands = "\"" + vcVarsLocation + "\" " + arch; //$NON-NLS-1$ //$NON-NLS-2$
		String includeEnvVar = getSingleLineOutputFromCommand("cmd", "/v", "/c", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"\"" + vcVarsLocationCommands + " > nul && echo !INCLUDE!\""); //$NON-NLS-1$ //$NON-NLS-2$
		if (includeEnvVar == null) {
			return;
		}

		String libEnvVar = getSingleLineOutputFromCommand("cmd", "/v", "/c", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"\"" + vcVarsLocationCommands + " > nul && echo !LIB!\""); //$NON-NLS-1$//$NON-NLS-2$
		if (libEnvVar == null) {
			return;
		}

		// Get the normal PATH variable before calling vcvars then we'll extract the ones added by vcvars.
		String normalPathVar = getSingleLineOutputFromCommand("cmd", "/c", "echo %PATH%"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		if (normalPathVar == null) {
			return;
		}

		// In case PATH is not defined at all (quite unlikely!)
		if (normalPathVar.equals("%PATH%")) { //$NON-NLS-1$
			normalPathVar = ""; //$NON-NLS-1$
		}

		String vcEnvPath = getSingleLineOutputFromCommand("cmd", "/v", "/c", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"\"" + vcVarsLocationCommands + " > nul && echo !PATH!\""); //$NON-NLS-1$//$NON-NLS-2$
		if (vcEnvPath == null) {
			return;
		}

		// Vcvars can put the user environment in the middle of its PATH values, not before of after.
		String vcEnvPathOnly = vcEnvPath.replaceFirst(Pattern.quote(normalPathVar), ""); //$NON-NLS-1$
		if (vcEnvPathOnly.isEmpty())
			return;

		fToolChains.add(new MSVCToolChainInfo(vcEnvPathOnly, includeEnvVar, libEnvVar));
	}

	/**
	 * @return Get all toolchains bundled with this installation of VS.
	 */
	public List<MSVCToolChainInfo> getToolchains() {
		if (fToolChains == null) {
			detectToolchains();
		}

		return Collections.unmodifiableList(fToolChains);
	}
}