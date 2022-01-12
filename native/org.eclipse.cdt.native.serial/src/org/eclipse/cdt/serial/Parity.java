/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.serial;

/**
 * @since 5.8
 */
public enum Parity {

	None, Even, Odd;

	private static final String[] strings = { "None", //$NON-NLS-1$
			"Even", //$NON-NLS-1$
			"Odd" //$NON-NLS-1$
	};

	public static String[] getStrings() {
		return strings;
	}

	private static final Parity[] parities = { None, Even, Odd };

	public static Parity fromStringIndex(int index) {
		return parities[index];
	}

	public static int getStringIndex(Parity parity) {
		for (int i = 0; i < parities.length; ++i) {
			if (parity.equals(parities[i])) {
				return i;
			}
		}
		return getStringIndex(getDefault());
	}

	public static Parity getDefault() {
		return None;
	}

}
