/*******************************************************************************
 * Copyright (c) 2010, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Freescale Semiconductor - refactoring
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.disassembly.dsf;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.Platform;

/**
 * Some general utilities used by the DSF Disassembly view and its backends
 */
public class DisassemblyUtils {

	/**
	 * Trace option. The view started out and continues to be in DSF but
	 * backends can be non-DSF.
	 */
	public final static boolean DEBUG = Boolean
			.parseBoolean(Platform.getDebugOption("org.eclipse.cdt.dsf.ui/debug/disassembly")); //$NON-NLS-1$

	public static String getAddressText(BigInteger address) {
		if (address == null) {
			return "<null>"; //$NON-NLS-1$
		}
		if (address.compareTo(BigInteger.ZERO) < 0) {
			return address.toString();
		}
		String hex = address.toString(16);
		return "0x" + "0000000000000000".substring(hex.length() + (address.bitLength() <= 32 ? 8 : 0)) + hex; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void internalError(Throwable e) {
		if (DEBUG) {
			System.err.println("Disassembly: Internal error"); //$NON-NLS-1$
			CDebugUIPlugin.log(e);
		}
	}

	public static BigInteger decodeAddress(String string) {
		// Handle case where address has type info, such as:
		// {int (const char *, ...)} 0x7ffff7a48e80 <__printf>
		if (string.startsWith("{")) { //$NON-NLS-1$
			int indexOf = string.indexOf('}');
			if (indexOf >= 0 && indexOf < string.length()) {
				string = string.substring(indexOf + 1);
			}
			indexOf = string.indexOf('<');
			if (indexOf >= 0) {
				string = string.substring(0, indexOf);
			}
			string = string.trim();
		}
		if (string.startsWith("0x")) { //$NON-NLS-1$
			return new BigInteger(string.substring(2), 16);
		}
		return new BigInteger(string);
	}

	/**
	 * Decode given string representation of a space separated hex encoded byte
	 * array
	 *
	 * @param value
	 *            space separated hexadecimal byte array
	 * @return opcode bytes as <code>Byte</code> array
	 * @throws IllegalNumberException if the given string value cannot be parsed into bytes
	 */
	public static Byte[] decodeOpcode(String value) {
		// Removing space separation and parse as bytes
		List<Byte> opcodeBytesList = new ArrayList<>();
		for (String opcodeStringValue : value.split("\\s+")) { //$NON-NLS-1$
			if (opcodeStringValue.length() > 0 && opcodeStringValue.length() <= 2) {
				byte byteValue = 0;
				char charAtIndexZero = opcodeStringValue.charAt(0);
				char charAtIndexOne = opcodeStringValue.length() > 1 ? opcodeStringValue.charAt(1) : 0;
				byteValue = (byte) ((Character.digit(charAtIndexZero, 16) << 4) + Character.digit(charAtIndexOne, 16));
				opcodeBytesList.add(Byte.valueOf(byteValue));
			}
		}
		return opcodeBytesList.toArray(new Byte[0]);
	}
}
