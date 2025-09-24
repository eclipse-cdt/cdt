/*******************************************************************************
 * Copyright (c) 2025 Renesas Electronics Europe and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.core.target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.launchbar.core.internal.Activator;

/**
 * @since 3.1
 */
public class LaunchTargetUtils {
	/**
	 * Used to detect names with invalid characters. _ is allowed.
	 * Invalid characters include:
	 * spaces, tabs, newlines, most punctuation (eg +-,.), control chars (eg \n, \t, \r)
	 */
	private static final Pattern INVALID_NAME_PATTERN = Pattern
			.compile("[^\\pL\\pM\\p{Nd}\\p{Nl}\\p{Pc}[\\p{InEnclosedAlphanumerics}&&\\p{So}]]"); //$NON-NLS-1$
	/**
	 * Disallowed characters for launch configuration names
	 * '@' and '&' are disallowed because they corrupt menu items.
	 * Copied from  org.eclipse.debug.internal.core.LaunchConfigurationManager
	 * @since 3.1
	 */
	private static final char[] DISALLOWED_CONFIG_NAME_CHARS = new char[] { '@', '&', '\\', '/', ':', '*', '?', '"',
			'<', '>', '|', '\0' };

	private LaunchTargetUtils() {
		// empty
	}

	/**
	 * Check for invalid characters.
	 * @param name The name to check.
	 * @return true if name contains characters in {@link #INVALID_NAME_PATTERN}.
	 */
	public static boolean isInvalidName(String name) {
		return INVALID_NAME_PATTERN.matcher(name).find();
	}

	/**
	 * Replace any invalid characters with a safe value.
	 * @param name The name to check.
	 * @return name with any character in the {@link #INVALID_NAME_PATTERN} replaced with "_"
	 */
	public static String sanitizeName(String name) {
		return INVALID_NAME_PATTERN.matcher(name).replaceAll("_"); //$NON-NLS-1$
	}

	/**
	 * Replace launch configuration name disallowed characters with underscores.
	 * Copied from  org.eclipse.debug.internal.core.LaunchConfigurationManager
	 * LaunchManager.isValidLaunchConfigurationName() can be used to verify a name.
	 *
	 * @param name the name to sanitize.
	 * @since 3.1
	 */
	public static String sanitizeLaunchConfigurationName(String name) {
		//blanket replace all invalid chars
		for (char element : DISALLOWED_CONFIG_NAME_CHARS) {
			name = name.replace(element, '_');
		}
		return name;
	}

	/**
	 * Get the names of all existing launch targets.
	 *
	 * @return List of existing launch target names. If the launch target manager service is not available, an empty list is returned.
	 * @since 3.1
	 */
	public static List<String> getExistingLaunchTargetNames() {
		List<String> retVal = new ArrayList<>();
		ILaunchTargetManager manager = Activator.getService(ILaunchTargetManager.class);
		if (manager != null) {
			retVal = Arrays.stream(manager.getLaunchTargets()).map(ILaunchTarget::getId).collect(Collectors.toList());
		}
		return retVal;
	}
}
