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
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIStackListArgumentsInfo;

/**
 * 
 *    -stack-list-arguments SHOW-VALUES
 *        [ LOW-FRAME HIGH-FRAME ]
 *
 *  Display a list of the arguments for the frames between LOW-FRAME and
 * HIGH-FRAME (inclusive).  If LOW-FRAME and HIGH-FRAME are not provided,
 * list the arguments for the whole call stack.
 *
 *   The SHOW-VALUES argument must have a value of 0 or 1.  A value of 0
 * means that only the names of the arguments are listed, a value of 1
 * means that both names and values of the arguments are printed.
 * 
 */
public class MIStackListArguments extends MICommand 
{
	public MIStackListArguments(boolean showValues) {
		super("-stack-list-arguments"); //$NON-NLS-1$
		if (showValues) {
			setParameters(new String[]{"1"}); //$NON-NLS-1$
		} else {
			setParameters(new String[]{"0"}); //$NON-NLS-1$
		}
	}

	public MIStackListArguments(boolean showValues, int low, int high) {
		super("-stack-list-arguments"); //$NON-NLS-1$
		String[] params = new String[3];
		if (showValues) {
			params[0] = "1"; //$NON-NLS-1$
		} else {
			params[0] = "0"; //$NON-NLS-1$
		}
		params[1] = Integer.toString(low);
		params[2] = Integer.toString(high);
		setParameters(params);
	}

	public MIStackListArgumentsInfo getMIStackListArgumentsInfo() throws MIException {
		return (MIStackListArgumentsInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIStackListArgumentsInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
