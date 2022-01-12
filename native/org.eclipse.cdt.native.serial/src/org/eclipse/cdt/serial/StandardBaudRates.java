/*******************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
= *******************************************************************************/
package org.eclipse.cdt.serial;

/**
 * Define the set of baud rates that are standard are generally supported.
 *
 * @since 1.2
 */
public final class StandardBaudRates {
	/**
	 * Return an array of the standard values for baud rates.
	 *
	 * Note: Especially on Linux these values are special as they can be set
	 * without requiring operations on the serial port that are not universally
	 * supported.
	 *
	 * The contents of this array may be changed from time to time and therefore
	 * the order of the elements and length of this array should not be used
	 * for anything. In particular, if storing a baud rate preference, store the
	 * integer value of that preference, not the index in this table.
	 *
	 * @return array of standard values
	 */
	public static int[] asArray() {
		// This list comes from what linux supports without custom rates.
		return new int[] { 110, 300, 600, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400, 460800, 500000,
				576000, 921600, 1000000, 1152000, 1500000, 2000000, 2500000, 3000000, 3500000, 4000000 };
	}

	/**
	 * Return an array of the standard values for baud rates, as strings for
	 * display in the UI.
	 * @see #asArray()
	 */
	public static String[] asStringArray() {
		int[] rates = asArray();
		String[] rateStrings = new String[rates.length];
		for (int i = 0; i < rateStrings.length; i++) {
			rateStrings[i] = Integer.toString(rates[i]);
		}
		return rateStrings;
	}

	/**
	 * Return the default speed used by the {@link SerialPort}
	 */
	public static int getDefault() {
		return 115200;
	}
}
