/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
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
import org.eclipse.cdt.debug.mi.core.output.MIDataListRegisterNamesInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *     -data-list-register-names [ ( REGNO )+ ]
 *
 *   Show a list of register names for the current target.  If no
 * arguments are given, it shows a list of the names of all the registers.
 * If integer numbers are given as arguments, it will print a list of the
 * names of the registers corresponding to the arguments.  To ensure
 * consistency between a register name and its number, the output list may
 * include empty register names.
 *
 */
public class MIDataListRegisterNames extends MICommand 
{
	public MIDataListRegisterNames(String miVersion) {
		super(miVersion, "-data-list-register-names"); //$NON-NLS-1$
	}

	public MIDataListRegisterNames(String miVersion, int [] regnos) {
		this(miVersion);
		if (regnos != null && regnos.length > 0) {
			String[] array = new String[regnos.length];
			for (int i = 0; i < regnos.length; i++) {
				array[i] = Integer.toString(regnos[i]);
			}
			setParameters(array);
		}
	}

	public MIDataListRegisterNamesInfo getMIDataListRegisterNamesInfo() throws MIException {
		return (MIDataListRegisterNamesInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIDataListRegisterNamesInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
