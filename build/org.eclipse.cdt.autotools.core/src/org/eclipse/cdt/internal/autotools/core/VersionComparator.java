/*******************************************************************************
 * Copyright (c) 2011, 2012 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.core;

public class VersionComparator {

	/**
	 * Compare two version numbers if
	 * return -1 if v1 is older than v2 0 if they are the same and +1
	 * if v1 is newer than v2
	 *
	 * Version numbers are expected to be in the format x.y.z...
	 *
	 * So:
	 * 	VersionComparator.compare("1.0", "1.2") return -1
	 *  VersionComparator.compare("1.5", "1.2") returns 1
	 *  VersionComparator.compare("1.5.1", "1.5.5") returns -1
	 *  VersionComparator.compare("1.5", "1.5.1")  returns 1
	 *  VersionComparator.compare("1.5.1", "1.5.1")  returns 0
	 */
	public static int compare(String v1, String v2) {
		String[] v1digits = v1.split("\\.");
		String[] v2digits = v2.split("\\.");

		for (int i = 0; i < v1digits.length && i < v2digits.length; i++) {
			int d1 = Integer.valueOf(v1digits[i]);
			int d2 = Integer.valueOf(v2digits[i]);

			if (d1 < d2)
				return -1;

			if (d1 > d2)
				return 1;
		}

		// At this point all digits have the same value
		// so the version with the longer string wins

		if (v1digits.length < v2digits.length)
			return -1;

		if (v1digits.length > v2digits.length)
			return 1;

		return 0;
	}

}
