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
public enum StopBits {

	S1, S2;

	private static final String[] strings = { "1", //$NON-NLS-1$
			"2" //$NON-NLS-1$
	};

	public static String[] getStrings() {
		return strings;
	}

	private static final StopBits[] stopBits = { S1, S2 };

	public static StopBits fromStringIndex(int index) {
		return stopBits[index];
	}

	public static int getStringIndex(StopBits sb) {
		for (int i = 0; i < stopBits.length; ++i) {
			if (sb.equals(stopBits[i])) {
				return i;
			}
		}
		return getStringIndex(getDefault());
	}

	public static StopBits getDefault() {
		return S1;
	}

}
