/*******************************************************************************
 * Copyright (c) 2015 Patrick Hofer
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Hofer - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Static methods for use in tests.
 */
public class TestUtils {
	public static File saveFile(InputStream st, File testFile) throws FileNotFoundException, IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(st));
		String line;
		try (PrintStream wr = new PrintStream(testFile)) {
			while ((line = r.readLine()) != null) {
				wr.println(line);
			}
		}
		return testFile;
	}
}
