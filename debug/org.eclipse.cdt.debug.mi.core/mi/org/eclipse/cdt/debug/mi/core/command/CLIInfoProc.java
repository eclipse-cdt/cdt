/*******************************************************************************
 * Copyright (c) 2007 ENEA Software AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ENEA Software AB - CLI command extension - fix for bug 190277
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.CLIInfoProcInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *    info proc
 *
 */
public class CLIInfoProc extends CLICommand 
{
	public CLIInfoProc() {
		super("info proc");  //$NON-NLS-1$
	}

	public CLIInfoProcInfo getMIInfoProcInfo() throws MIException {
		return (CLIInfoProcInfo)getMIInfo();
	}

	@Override
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new CLIInfoProcInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
