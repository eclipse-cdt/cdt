/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowExitCodeInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *-data-evaluate-expression $_exitcode
 * ^done,value="10"
 *
 *   Show the current value of a $_exitcode
 * 
 */
public class MIGDBShowExitCode extends MIDataEvaluateExpression {

	public MIGDBShowExitCode() {
		super("$_exitcode"); //$NON-NLS-1$
	}

	public MIGDBShowExitCodeInfo getMIGDBShowExitCodeInfo() throws MIException {
		return (MIGDBShowExitCodeInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIGDBShowExitCodeInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
