package org.eclipse.cdt.serial;

/**
 * Define the set of baud rates that are standard are generally supported.
 *
 * @since 1.2
 */
public class BaudRateValues {
	public static int[] getStandardBaudRates() {
		// This list comes from what linux supports without custom rates.
		return new int[] { 110, 300, 600, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400, 460800, 500000,
				576000, 921600, 1000000, 1152000, 1500000, 2000000, 2500000, 3000000, 3500000, 4000000 };
	}

	public static String[] getStandardBaudRatesAsStrings() {
		int[] rates = getStandardBaudRates();
		String[] rateStrings = new String[rates.length];
		for (int i = 0; i < rateStrings.length; i++) {
			rateStrings[i] = Integer.toString(rates[i]);
		}
		return rateStrings;
	}

	public static int getDefaultBaudRate() {
		return 115200;
	}

}
