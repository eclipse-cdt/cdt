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

import java.util.Arrays;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Stores information about different Visual Studio installations detected on the system (paths, toolchains, etc).
 */
public class VSInstallationRegistry {
	private static TreeMap<VSVersionNumber, VSInstallation> fVsInstallations;

	/**
	 * @return Get all VS installations, ordered by ascending version numbers.
	 */
	public static NavigableMap<VSVersionNumber, VSInstallation> getVsInstallations() {
		if (fVsInstallations == null)
			detectVSInstallations();

		return Collections.unmodifiableNavigableMap(fVsInstallations);
	}

	private static void detectVSInstallations() {
		fVsInstallations = new TreeMap<>();
		// We are opting-in which versions to detect instead of trying to detect even unknown ones in order
		// to allow proper testing for a new version before exposing it to users.
		Arrays.asList(IVSVersionConstants.VS2017_BASE_VER, IVSVersionConstants.VS2019_BASE_VER,
				IVSVersionConstants.VS2022_BASE_VER).forEach(version -> {
					VSInstallation insllation = detectVSInstallation(version);
					if (insllation != null)
						fVsInstallations.put(version, insllation);
				});
	}

	private static VSInstallation detectVSInstallation(VSVersionNumber baseVersion) {
		VSVersionNumber upperBound = new VSVersionNumber(baseVersion.get(0) + 1);
		// E.g. [15,16) for 15 inclusive to 16 exclusive.
		String versionFilterRange = "[" + baseVersion.toString() + "," + upperBound + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String vsInstallationLocation[] = ProcessOutputUtil.getAllOutputFromCommand("cmd", "/c", //$NON-NLS-1$//$NON-NLS-2$
				"\"\"%ProgramFiles(x86)%\\Microsoft Visual Studio\\Installer\\vswhere.exe\"\"", //$NON-NLS-1$
				"-products", "*", "-requires", "Microsoft.VisualStudio.Component.VC.Tools.x86.x64", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"-version", versionFilterRange, "-property", "installationPath"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (vsInstallationLocation == null || vsInstallationLocation.length == 0
				|| vsInstallationLocation[0].isEmpty()) {
			return null;
		}

		// We only support one installation per base (major) version for now.
		// Supporting multiple is a bit of a niche case left to be determined if useful.
		return new VSInstallation(vsInstallationLocation[0]);
	}
}
