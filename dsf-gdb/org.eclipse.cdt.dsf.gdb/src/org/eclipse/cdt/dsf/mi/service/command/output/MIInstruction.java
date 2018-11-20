/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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
 *     Ericsson - Adapted for DSF
 *     Dmitry Kozlov (Mentor Graphics) - Add tab symbols parsing (Bug 391115)
 *     William Riley (Renesas) - Add raw Opcode parsing (Bug 357270)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.math.BigInteger;

import org.eclipse.cdt.dsf.debug.service.AbstractInstruction;

public class MIInstruction extends AbstractInstruction {

	// The parsed information
	BigInteger address;
	String function = ""; //$NON-NLS-1$
	long offset;
	String opcode = ""; //$NON-NLS-1$
	String args = ""; //$NON-NLS-1$
	BigInteger rawOpcodes = null;
	Integer opcodeSize = null;

	public MIInstruction(MITuple tuple) {
		parse(tuple);
	}

	@Override
	public BigInteger getAdress() {
		return address;
	}

	@Override
	public String getFuntionName() {
		return function;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public String getInstruction() {
		return opcode + "\t" + args; //$NON-NLS-1$;
	}

	@Override
	public String getOpcode() {
		return opcode;
	}

	@Override
	public String getArgs() {
		return args;
	}

	@Override
	public BigInteger getRawOpcodes() {
		return rawOpcodes;
	}

	/**
	 *  Parse the assembly instruction result. Each instruction has the following
	 *  fields:
	 *   - Address
	 *   - Function name
	 *   - Offset
	 *   - Instruction
	 *
	 *   {address="0x000107c0",func-name="main",offset="4",inst="mov 2, %o0"},
	 *   {address="0x000107c4",func-name="main",offset="8",inst="sethi %hi(0x11800), %o2"},
	 *   ...,
	 *   {address="0x00010820",func-name="main",offset="100",inst="restore "}
	 *
	 * 	An instruction may also contain:
	 *    - Opcode bytes
	 *
	 *  {address="0x004016b9",func-name="main",offset="9",opcodes="e8 a2 05 00 00",
	 *  	inst="call   0x401c60 <__main>"},
	 *  ...,
	 *
	 *  In addition, the opcode and arguments are extracted form the assembly instruction.
	 */
	private void parse(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = ""; //$NON-NLS-1$

			if (value instanceof MIConst) {
				str = ((MIConst) value).getCString();
			}

			if (var.equals("address")) { //$NON-NLS-1$
				try {
					address = decodeAddress(str.trim());
				} catch (NumberFormatException e) {
				}
				continue;
			}

			if (var.equals("func-name")) { //$NON-NLS-1$
				function = str;
				continue;
			}

			if (var.equals("offset")) { //$NON-NLS-1$
				try {
					offset = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
				}
				continue;
			}

			if (var.equals("inst")) { //$NON-NLS-1$
				/* for the instruction, we do not want the C string but the
				translated string since the only thing we are doing is
				displaying it. */
				if (value instanceof MIConst) {
					str = ((MIConst) value).getString();
				}
				/* to avoid improper displaying of instructions we need to translate tabs */
				str = str.replace("\\t", "\t"); //$NON-NLS-1$ //$NON-NLS-2$

				char chars[] = str.toCharArray();
				int index = 0;

				// count the non-whitespace characters.
				while ((index < chars.length) && (chars[index] > '\u0020'))
					index++;

				opcode = str.substring(0, index);

				// skip any whitespace characters
				while (index < chars.length && chars[index] >= '\u0000' && chars[index] <= '\u0020')
					index++;

				// guard no argument
				if (index < chars.length)
					args = str.substring(index);

				continue;
			}

			if (var.equals("opcodes")) { //$NON-NLS-1$
				try {
					rawOpcodes = decodeOpcodes(str);
					opcodeSize = Integer.valueOf(str.replace(" ", "").length() / 2); //$NON-NLS-1$//$NON-NLS-2$
				} catch (NumberFormatException e) {
				}
				continue;
			}
		}

	}

	/**
	 * Decode given string representation of a non-negative integer. A
	 * hexadecimal encoded integer is expected to start with <code>0x</code>.
	 *
	 * @param string
	 *            decimal or hexadecimal representation of an non-negative integer
	 * @return address value as <code>BigInteger</code>
	 */
	private static BigInteger decodeAddress(String string) {
		if (string.startsWith("0x")) { //$NON-NLS-1$
			return new BigInteger(string.substring(2), 16);
		}
		return new BigInteger(string);
	}

	/**
	 * Decode given string representation of a space separated hex encoded byte
	 * array
	 *
	 * @param string
	 *            space separated hexadecimal byte array
	 * @return opcode bytes as <code>BigInteger</code>
	 */
	private static BigInteger decodeOpcodes(String string) {
		// Removing space separation and parse as single big integer
		return new BigInteger(string.replace(" ", ""), 16); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.AbstractInstruction#getSize()
	 */
	@Override
	public Integer getSize() {
		return opcodeSize;
	}
}
