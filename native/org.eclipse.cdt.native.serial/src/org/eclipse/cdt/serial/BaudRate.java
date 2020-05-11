/*******************************************************************************
 * Copyright (c) 2015, 2020 QNX Software Systems and others.
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

import java.util.Arrays;

/**
 * @since 1.0
 */
public enum BaudRate {

	B110, //
	B300, //
	B600, //
	B1200, //
	B2400, //
	B4800, //
	B9600,
	/**
	 * 14,400 is not actually supported on Linux using the
	 * current serial.c implementation. Using that speed
	 * will result in an exception.
	 */
	B14400, //
	B19200, //
	B38400, //
	B57600, //
	B115200,
	/**
	* @since 1.2
	*/
	B230400,
	/**
	* @since 1.2
	*/
	B460800,
	/**
	* @since 1.2
	*/
	B500000,
	/**
	* @since 1.2
	*/
	B576000,
	/**
	* @since 1.2
	*/
	B921600,
	/**
	* @since 1.2
	*/
	B1000000,
	/**
	* @since 1.2
	*/
	B1152000,
	/**
	* @since 1.2
	*/
	B1500000,
	/**
	* @since 1.2
	*/
	B2000000,
	/**
	* @since 1.2
	*/
	B2500000,
	/**
	* @since 1.2
	*/
	B3000000,
	/**
	* @since 1.2
	*/
	B3500000,
	/**
	* @since 1.2
	*/
	B4000000;

	private final int rate;
	private static String[] strings;
	private String speedString;

	BaudRate() {
		speedString = toString().substring(1);
		this.rate = Integer.parseInt(speedString);
	}

	public int getRate() {
		return rate;
	}

	/**
	 * @since 1.2
	 */
	public String getSpeedString() {
		return speedString;
	}

	static {
		strings = Arrays.asList(values()).stream().map(BaudRate::getSpeedString).toArray(String[]::new);
	}

	public static String[] getStrings() {
		return strings;
	}

	public static BaudRate fromStringIndex(int rate) {
		if (rate < values().length && rate >= 0) {
			return values()[rate];
		}
		return getDefault();
	}

	public static int getStringIndex(BaudRate rate) {
		return rate.ordinal();
	}

	public static BaudRate getDefault() {
		return B115200;
	}

}
