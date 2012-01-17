/*******************************************************************************
 * Copyright (c) 2007, 2010 ENEA Software AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     ENEA Software AB - CLI command extension - fix for bug 190277
 *     Marc-Andre Laperle - Replace info proc with info pid, patch for bug 294538
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.macos;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.command.CLIInfoProc;
import org.eclipse.cdt.debug.mi.core.output.CLIInfoProcInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIResult;

/**
 * GDB/CLI info proc parsing. 18 info pid &"info pid\n"
 * 18^done,process-id="89643"
 * 
 */
class MacOSCLIInfoPID extends CLIInfoProc {

	// apple-gdb doesn't have info proc but has info pid
	// Since info proc is only used to get the pid, it is valid to use info pid
	// as a replacement
	public MacOSCLIInfoPID() {
		setOperation("info pid"); //$NON-NLS-1$
	}

	@Override
	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MacOSCLIInfoPIDOutput(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}

	class MacOSCLIInfoPIDOutput extends CLIInfoProcInfo {

		int pid;

		public MacOSCLIInfoPIDOutput(MIOutput out) {
			super(out);
			parsePID();
		}

		void parsePID() {
			if (isDone()) {
				MIOutput out = getMIOutput();
				MIResult[] rr = out.getMIResultRecord().getMIResults();
				for (int i = 0; i < rr.length; i++) {
					parsePIDLine(rr[i].toString());
				}
			}
		}

		void parsePIDLine(String str) {
			if (str != null && str.length() > 0) {
				str = str.trim();
				if (!str.startsWith("process-id=")) { //$NON-NLS-1$
					return;
				}

				pid = Integer.decode(str.substring(12, str.length() - 1))
						.intValue();
			}
		}

		@Override
		public int getPID() {
			return pid;
		}

	}
}
