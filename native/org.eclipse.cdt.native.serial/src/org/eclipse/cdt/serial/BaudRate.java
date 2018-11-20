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
 * @since 1.0
 */
public enum BaudRate {

	B110(110), B300(300), B600(600), B1200(1200), B2400(2400), B4800(4800), B9600(9600), B14400(14400), B19200(19200),
	B38400(38400), B57600(57600), B115200(115200);

	private final int rate;

	private BaudRate(int rate) {
		this.rate = rate;
	}

	public int getRate() {
		return rate;
	}

	private static final String[] strings = { "110", //$NON-NLS-1$
			"300", //$NON-NLS-1$
			"600", //$NON-NLS-1$
			"1200", //$NON-NLS-1$
			"2400", //$NON-NLS-1$
			"4800", //$NON-NLS-1$
			"9600", //$NON-NLS-1$
			"14400", //$NON-NLS-1$
			"19200", //$NON-NLS-1$
			"38400", //$NON-NLS-1$
			"57600", //$NON-NLS-1$
			"115200" //$NON-NLS-1$
	};

	public static String[] getStrings() {
		return strings;
	}

	private static final BaudRate[] rates = { B110, B300, B600, B1200, B2400, B4800, B9600, B14400, B19200, B38400,
			B57600, B115200 };

	public static BaudRate fromStringIndex(int rate) {
		return rates[rate];
	}

	public static int getStringIndex(BaudRate rate) {
		for (int i = 0; i < rates.length; ++i) {
			if (rate.equals(rates[i])) {
				return i;
			}
		}
		return getStringIndex(getDefault());
	}

	public static BaudRate getDefault() {
		return B115200;
	}

}
