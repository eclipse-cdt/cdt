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
public enum ByteSize {

	B5(5), B6(6), B7(7), B8(8);

	private final int size;

	private ByteSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	private static final String[] strings = { "5", //$NON-NLS-1$
			"6", //$NON-NLS-1$
			"7", //$NON-NLS-1$
			"8" //$NON-NLS-1$
	};

	public static String[] getStrings() {
		return strings;
	}

	private static final ByteSize[] sizes = { B5, B6, B7, B8 };

	public static ByteSize fromStringIndex(int size) {
		return sizes[size];
	}

	public static int getStringIndex(ByteSize size) {
		for (int i = 0; i < sizes.length; ++i) {
			if (size.equals(sizes[i])) {
				return i;
			}
		}
		return getStringIndex(getDefault());
	}

	public static ByteSize getDefault() {
		return B8;
	}

}
