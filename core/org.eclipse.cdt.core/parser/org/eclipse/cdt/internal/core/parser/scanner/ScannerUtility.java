/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.io.File;

/**
 * @author jcamelon
 */
public class ScannerUtility {
	static final char DOT = '.';
	static final char SLASH = '/';
	static final char BSLASH = '\\';
	static final char QUOTE = '\"';

	/**
	 * This method is quick 1-pass path reconciler.
	 * Functions:
	 *   - replace "/" or "\" by system's separator
	 *   - replace multiple separators by single one
	 *   - skip "/./"
	 *   - skip quotes
	 *
	 * Note: "/../" is left untouched on purpose in order to work properly under
	 * circumstances such as this:
	 *
	 * header file at include_1/vector:
	 *   // Is supposed to find the STL vector header:
	 *   #include <ext/../vector>
	 *
	 * GCC include tree
	 *   include_gcc/ext/...
	 *              /vector
	 *
	 * (ls include_1/ext/../vector does not work either).
	 *
	 * @param originalPath - path to process
	 * @return             - reconciled path
	 */
	public static String reconcilePath(String originalPath) {
		int len = originalPath.length();
		int len1 = len - 1; // to avoid multiple calculations
		int j = 0; // index for output array
		boolean noSepBefore = true; // to avoid duplicate separators

		char[] ein = new char[len];
		char[] aus = new char[len + 1];

		originalPath.getChars(0, len, ein, 0);

		// allow double backslash at beginning for windows UNC paths, bug 233511
		// also allow Unix UNC paths
		if (ein.length >= 2) {
			if (ein[0] == BSLASH && ein[1] == BSLASH && File.separatorChar == BSLASH) {
				aus[j++] = BSLASH;
			} else if (ein[0] == SLASH && ein[1] == SLASH && File.separatorChar == SLASH) {
				aus[j++] = SLASH;
			}
		}

		for (int i = 0; i < len; i++) {
			char c = ein[i];
			switch (c) {
			case QUOTE: // quotes are removed
				noSepBefore = true;
				break;
			case SLASH: // both separators are processed
			case BSLASH: // in the same way
				if (noSepBefore) {
					noSepBefore = false;
					aus[j++] = File.separatorChar;
				}
				break;
			case DOT:
				// No separator before, not a 1st string symbol.
				if (noSepBefore && j > 0) {
					aus[j++] = c;
				} else { // Separator before "."
					if (i < len1) {
						c = ein[i + 1]; // Check for next symbol
						// Check for "/./" case
						if (c == SLASH || c == BSLASH) {
							// Write nothing to output, skip the next symbol
							i++;
							noSepBefore = false;
						} else { // Process as usual
							i++;
							noSepBefore = true;
							aus[j++] = DOT;
							aus[j++] = c;
						}
					}
				}
				break;
			default:
				noSepBefore = true;
				aus[j++] = c;
			}
		}
		return new String(aus, 0, j);
	}

	/**
	 * @param path     - include path
	 * @param fileName - include file name
	 * @return         - reconciled path
	 */
	public static String createReconciledPath(String path, String fileName) {
		boolean pathEmpty = (path == null || path.length() == 0);
		return pathEmpty ? fileName : reconcilePath(path + File.separatorChar + fileName);
	}
}
