/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.mi.core.MIFormat;

/**
 * 
 * Write given values into registers. The registers and values are
 * given as pairs. The corresponding MI command is 
 * -data-write-register-values <format> [<regnum1> <value1>...<regnumN> <valueN>]
 *
 */
public class MIDataWriteRegisterValues extends MICommand {

	public MIDataWriteRegisterValues(int fmt, int[] regnos, String[] values) {
		super("-data-write-register-values"); //$NON-NLS-1$

		String format = "x"; //$NON-NLS-1$
		switch (fmt) {
			case MIFormat.NATURAL:
				format = "N"; //$NON-NLS-1$
			break;

			case MIFormat.RAW:
				format = "r"; //$NON-NLS-1$
			break;

			case MIFormat.DECIMAL:
				format = "d"; //$NON-NLS-1$
			break;

			case MIFormat.BINARY:
				format = "t"; //$NON-NLS-1$
			break;

			case MIFormat.OCTAL:
				format = "o"; //$NON-NLS-1$
			break;

			case MIFormat.HEXADECIMAL:
			default:
				format = "x"; //$NON-NLS-1$
			break;
		}

		setOptions(new String[]{format});

		if (regnos != null && values != null) {
			List aList = new ArrayList(regnos.length);
			for (int i = 0; i < regnos.length && i < values.length; i++) {
				aList.add(Integer.toString(regnos[i]));
				aList.add(values[i]);
			}
			String[] array = (String[])aList.toArray(new String[0]);
			setParameters(array);
		}
	}

}
