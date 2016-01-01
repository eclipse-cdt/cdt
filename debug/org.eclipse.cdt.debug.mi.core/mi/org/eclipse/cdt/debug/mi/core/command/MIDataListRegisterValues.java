/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.output.MIDataListRegisterValuesInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *       -data-list-register-values FMT [ ( REGNO )*]
 * 
 *    Display the registers' contents.  FMT is the format according to
 * which the registers' contents are to be returned, followed by an
 * optional list of numbers specifying the registers to display.  A
 * missing list of numbers indicates that the contents of all the
 * registers must be returned.
 *
 */
public class MIDataListRegisterValues extends MICommand 
{
	public MIDataListRegisterValues(String miVersion, int fmt) {
		this(miVersion, fmt, null);
	}

	public MIDataListRegisterValues(String miVersion, int fmt, int [] regnos) {
		super(miVersion, "-data-list-register-values"); //$NON-NLS-1$

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

		if (regnos != null && regnos.length > 0) {
			String[] array = new String[regnos.length];
			for (int i = 0; i < regnos.length; i++) {
				array[i] = Integer.toString(regnos[i]);
			}
			setParameters(array);
		}
	}

	public MIDataListRegisterValuesInfo getMIDataListRegisterValuesInfo() throws MIException {
		return (MIDataListRegisterValuesInfo)getMIInfo();
	}

	@Override
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIDataListRegisterValuesInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
