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
package org.eclipse.cdt.debug.mi.core.output;

/**
 * Represent a GDB Tuple MI assembly response.
 */
public class MIAsm {
	long address;
	String function = ""; //$NON-NLS-1$
	String opcode = ""; //$NON-NLS-1$
	String args = ""; //$NON-NLS-1$
	long offset;

	public MIAsm (MITuple tuple) {
		parse(tuple);
	}

	public long getAddress() {
		return address;
	}

	public String getFunction() {
		return function;
	}

	public long getOffset() {
		return offset;
	}

	public String getInstruction() {
		return opcode + " " + args; //$NON-NLS-1$
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append('{');
		buffer.append("address=\"" + Long.toHexString(address) +"\"");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append(",func-name=\"" + function + "\"");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append(",offset=\"").append(offset).append('"'); //$NON-NLS-1$
		buffer.append(",inst=\"" + getInstruction() + "\"");  //$NON-NLS-1$//$NON-NLS-2$
		buffer.append('}');
		return buffer.toString();
	}

	void parse(MITuple tuple) {
		MIResult[] results = tuple.getMIResults();
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = ""; //$NON-NLS-1$

			if (value != null && value instanceof MIConst) {
				str = ((MIConst)value).getCString();
			}

			if (var.equals("address")) { //$NON-NLS-1$
				try {
					address = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("func-name")) { //$NON-NLS-1$
				function = str;
			} else if (var.equals("offset")) { //$NON-NLS-1$
				try {
					offset = Long.decode(str.trim()).longValue();
				} catch (NumberFormatException e) {
				}
			} else if (var.equals("inst")) { //$NON-NLS-1$
				/* for the instruction, we do not want the C string but the
				translated string since the only thing we are doing is
				displaying it. */
				str = ((MIConst)value).getString();

				char chars[] = str.toCharArray();
				int index = 0;
 
				// count the non-whitespace characters.
				while( (index < chars.length) && (chars[index] > '\u0020'))
					index++;

				opcode = str.substring( 0, index );

				// skip any whitespace characters
				while( index < chars.length && chars[index] >= '\u0000' && chars[index] <= '\u0020')
					index++;

				// guard no argument
				if( index < chars.length )
					args = str.substring( index );
 			}
		}
	}

	/**
	 * @return String
	 */
	public String getArgs() {
		return args;
	}

	/**
	 * @return String
	 */
	public String getOpcode() {
		return opcode;
	}
}
