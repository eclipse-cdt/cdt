/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
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
