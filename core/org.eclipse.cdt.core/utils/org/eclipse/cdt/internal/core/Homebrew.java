/*******************************************************************************
 * Copyright (c) 2012, 2025 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Original MinGW API and implementation
 *     John Dallaway  - Initial Homebrew implementation (#1175)
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.util.Objects;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * A collection of Homebrew-related utility methods.
 */
public class Homebrew {
	public static final String ENV_HOMEBREW_HOME = "HOMEBREW_HOME"; //$NON-NLS-1$

	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$

	private static String envPathValueCached = null;
	private static String envHomebrewHomeValueCached = null;
	private static String homebrewLocation = null;
	private static boolean isHomebrewLocationCached = false;

	private static String findHomebrewRoot(String envPathValue, String envHomebrewHomeValue) {
		// Check $HOMEBREW_HOME which overrides default locations
		if (envHomebrewHomeValue != null && !envHomebrewHomeValue.isEmpty()) {
			String homebrewPath = envHomebrewHomeValue;
			if (installationExists(homebrewPath)) {
				return homebrewPath;
			}
		}
		String defaultHomebrewPath = getDefaultHomebrewPath();
		if ((defaultHomebrewPath != null) && installationExists(defaultHomebrewPath)) {
			return defaultHomebrewPath;
		}
		return null;
	}

	private static boolean installationExists(String homebrewHomeDir) {
		return new Path(homebrewHomeDir).append("bin").toFile().isDirectory(); //$NON-NLS-1$
	}

	private static String getDefaultHomebrewPath() {
		final String os = Platform.getOS();
		final String arch = Platform.getOSArch();
		if (Platform.OS_MACOSX.equals(os)) {
			if (Platform.ARCH_AARCH64.equals(arch)) {
				return "/opt/homebrew"; //$NON-NLS-1$
			} else if (Platform.ARCH_X86_64.equals(arch)) {
				return "/usr/local"; //$NON-NLS-1$
			}
		} else if (Platform.OS_LINUX.equals(os) && Platform.ARCH_X86_64.equals(arch)) {
			return "/home/linuxbrew/.linuxbrew"; //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Find location where Homebrew is installed. Do not cache returned value.
	 *
	 * @return Homebrew installation path.
	 */
	public static String getHomebrewHome() {
		IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(ENV_PATH,
				(ICConfigurationDescription) null, true);
		String envPathValue = varPath != null ? varPath.getValue() : null;
		IEnvironmentVariable varHomebrewHome = CCorePlugin.getDefault().getBuildEnvironmentManager()
				.getVariable(ENV_HOMEBREW_HOME, (ICConfigurationDescription) null, true);
		String envHomebrewHomeValue = varHomebrewHome != null ? varHomebrewHome.getValue() : null;

		if (isHomebrewLocationCached && Objects.equals(envPathValue, envPathValueCached)
				&& Objects.equals(envHomebrewHomeValue, envHomebrewHomeValueCached)) {
			return homebrewLocation;
		}

		homebrewLocation = findHomebrewRoot(envPathValue, envHomebrewHomeValue);
		envPathValueCached = envPathValue;
		envHomebrewHomeValueCached = envHomebrewHomeValue;
		isHomebrewLocationCached = true;

		return homebrewLocation;
	}

}
