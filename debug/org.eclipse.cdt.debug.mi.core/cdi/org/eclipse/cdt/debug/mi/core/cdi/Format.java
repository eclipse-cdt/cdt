/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIFormat;
import org.eclipse.cdt.debug.mi.core.MIFormat;

/**
 */
public class Format {

	private Format() {
	}

	public static int toMIFormat(int format) {
		int fmt = MIFormat.NATURAL;
		switch (format) {
			case ICDIFormat.NATURAL:
				fmt = MIFormat.NATURAL;
			break;

			case ICDIFormat.DECIMAL:
				fmt = MIFormat.DECIMAL;
			break;

			case ICDIFormat.BINARY:
				fmt = MIFormat.BINARY;
			break;

			case ICDIFormat.OCTAL:
				fmt = MIFormat.OCTAL;
			break;

			case ICDIFormat.HEXADECIMAL:
				fmt = MIFormat.HEXADECIMAL;
			break;
		}
		return fmt;
	}
}
