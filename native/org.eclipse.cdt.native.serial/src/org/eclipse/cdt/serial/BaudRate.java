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

	BaudRate() {
		this.rate = Integer.parseInt(toString().substring(1));
	}

	public int getRate() {
		return rate;
	}

	/**
	 * @since 1.2
	 */
	private String getSpeedString() {
		return toString().substring(1);
	}

	public static String[] getStrings() {
		return Arrays.asList(values()).stream().map(BaudRate::getSpeedString).toArray(String[]::new);
	}

	/**
	 * @since 1.2
	 */
	public static String[] getLinuxStrings() {
		return Arrays.asList(values()).stream().filter(b -> b != B14400).map(BaudRate::getSpeedString)
				.toArray(String[]::new);
	}

	/**
	 * @deprecated Migrate to stop using BaudRate to store baud rates
	 */
	@Deprecated
	public static BaudRate fromStringIndex(int rate) {
		if (rate < values().length && rate >= 0) {
			return values()[rate];
		}
		return getDefault();
	}

	/**
	 * @deprecated Migrate to stop using BaudRate to store baud rates
	 */
	@Deprecated
	public static int getStringIndex(BaudRate rate) {
		return rate.ordinal();
	}

	/**
	 * @since 1.2
	 * @deprecated Migrate to stop using BaudRate to store baud rates
	 */
	@Deprecated
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
