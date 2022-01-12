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
import java.util.Optional;

/**
 * Standard BaudRates that are generally supported by serial driver.
 * @since 1.0
 * @deprecated Baud Rates are not a fixed set. Instead use {@link StandardBaudRates} for
 * typical values and use an int to represent baud rates. This deprecation goes
 * along with {@link SerialPort#setBaudRate(BaudRate)'s deprecation. Use
 * SerialPort#setBaudRateValue(int) instead.
 */
@Deprecated
public enum BaudRate {

	B110, //
	B300, //
	B600, //
	B1200, //
	B2400, //
	B4800, //
	B9600,
	/**
	 * 14,400 is not standard on Linux and requires custom baud rate support.
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

	BaudRate() {
		this.rate = Integer.parseInt(toString().substring(1));
	}

	public int getRate() {
		return rate;
	}

	private String getSpeedString() {
		return toString().substring(1);
	}

	public static String[] getStrings() {
		return Arrays.asList(values()).stream().map(BaudRate::getSpeedString).toArray(String[]::new);
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

	/**
	 * This method allows some amount of translation between new API that uses ints
	 * for baud rate and those that use BaudRate. It attempts to get the closest
	 * value.
	 *
	 * @since 1.2
	 */
	public static BaudRate getClosest(int baudRate) {
		Optional<BaudRate> reduce = Arrays.asList(BaudRate.values()).stream().reduce((result, current) -> {
			if (Math.abs(baudRate - current.getRate()) < Math.abs(baudRate - result.getRate()))
				return current;
			else
				return result;
		});
		return reduce.get();
	}

	public static BaudRate getDefault() {
		return B115200;
	}
}
