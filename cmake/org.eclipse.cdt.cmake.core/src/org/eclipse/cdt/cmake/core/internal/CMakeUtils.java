/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Red Hat Inc. - initial version
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CMakeUtils {

	/**
	 * Parse a string containing environment variables into individual VAR=VALUE pairs.
	 *
	 * @param envString - String to parse
	 * @return List of var=value Strings
	 */
	public static List<String> stripEnvVars(String envString) {
		Pattern p1 = Pattern.compile("(\\w+[=]\\\".*?\\\").*"); //$NON-NLS-1$
		Pattern p2 = Pattern.compile("(\\w+[=]'.*?').*"); //$NON-NLS-1$
		Pattern p3 = Pattern.compile("(\\w+[=][^\\s]+).*"); //$NON-NLS-1$
		boolean finished = false;
		List<String> envVars = new ArrayList<>();
		while (!finished) {
			Matcher m1 = p1.matcher(envString);
			if (m1.matches()) {
				envString = envString.replaceFirst("\\w+[=]\\\".*?\\\"", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
				String s = m1.group(1).trim();
				envVars.add(s.replaceAll("\\\"", "")); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				Matcher m2 = p2.matcher(envString);
				if (m2.matches()) {
					envString = envString.replaceFirst("\\w+[=]'.*?'", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
					String s = m2.group(1).trim();
					envVars.add(s.replaceAll("'", "")); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					Matcher m3 = p3.matcher(envString);
					if (m3.matches()) {
						envString = envString.replaceFirst("\\w+[=][^\\s]+", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
						envVars.add(m3.group(1).trim());
					} else {
						finished = true;
					}
				}
			}
		}
		return envVars;
	}

}
