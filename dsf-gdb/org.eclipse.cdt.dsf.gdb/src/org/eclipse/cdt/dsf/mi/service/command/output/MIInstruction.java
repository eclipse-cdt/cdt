/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson - Adapted for DSF
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

import java.math.BigInteger;

import org.eclipse.cdt.dsf.debug.service.IInstruction;

public class MIInstruction implements IInstruction {

    // The parsed information
    BigInteger address;
    String     function = ""; //$NON-NLS-1$
    long       offset;
    String     opcode   = ""; //$NON-NLS-1$
    String     args     = ""; //$NON-NLS-1$

    public MIInstruction(MITuple tuple) {
        parse(tuple);
    }

    public BigInteger getAdress() {
        return address;
    }

    public String getFuntionName() {
        return function;
    }

    public long getOffset() {
        return offset;
    }

    public String getInstruction() {
        return opcode + " " + args; //$NON-NLS-1$;
    }

    public String getOpcode() {
        return opcode;
    }

    public String getArgs() {
        return args;
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
     *  In addition, the opcode and arguments are extracted form the assembly instruction.
     */
    private void parse(MITuple tuple) {
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
                str = ((MIConst) value).getString();

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

}
