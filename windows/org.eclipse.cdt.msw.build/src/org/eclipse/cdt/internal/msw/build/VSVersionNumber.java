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
 * A VS version number (Integer tuple).
 */
public class VSVersionNumber implements Comparable<VSVersionNumber> {
	private Integer[] fIntegers;

	VSVersionNumber(Integer... integers) {
		fIntegers = integers;
	}

	@Override
	public int compareTo(VSVersionNumber o) {
		for (int i = 0; i < fIntegers.length; i++) {
			if (i >= o.fIntegers.length) {
				// All numbers are the same up to now but the other tuple
				// has less
				return 1;
			}

			int compareTo = fIntegers[i].compareTo(o.fIntegers[i]);
			if (compareTo != 0) {
				return compareTo;
			}
		}

		// All numbers are the same up to now but this tuple has less than
		// the other
		if (fIntegers.length < o.fIntegers.length) {
			return -1;
		}

		return 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fIntegers.length; i++) {
			sb.append(fIntegers[i]);
			if (i != fIntegers.length - 1) {
				sb.append("."); //$NON-NLS-1$
			}
		}
		return sb.toString();
	}

	Integer get(int index) {
		return fIntegers[index];
	}
}