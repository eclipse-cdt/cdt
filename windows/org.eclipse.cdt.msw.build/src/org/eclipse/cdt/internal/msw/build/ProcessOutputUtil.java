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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple util for getting output out of a command. Only meant to be used in a few places to help detect things in the environment.
 */
final class ProcessOutputUtil {

	static String[] getAllOutputFromCommand(String... command) {
		try {
			Process p = new ProcessBuilder(command).start();
			List<String> lines = new ArrayList<>();
			try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				String line = input.readLine();
				while (line != null) {
					lines.add(line);
					line = input.readLine();
				}
			}
			return lines.toArray(new String[0]);
		} catch (IOException e) {
			// Since this is mostly used to detect the presence of things in the environment,
			// if anything goes wrong we just return null and we will fallback to undetected VS/MSVC.
		}

		return null;
	}
}
