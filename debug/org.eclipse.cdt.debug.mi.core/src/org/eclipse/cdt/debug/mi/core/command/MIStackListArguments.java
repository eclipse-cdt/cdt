/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

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
		super("-stack-list-arguments");
		if (showValues) {
			setParameters(new String[]{"1"});
		} else {
			setParameters(new String[]{"0"});
		}
	}

	public MIStackListArguments(boolean showValues, int low, int high) {
		super("-stack-list-arguments");
		String[] params = new String[3];
		if (showValues) {
			params[0] = "1";
		} else {
			params[0] = "0";
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
				throw new MIException(info.getErrorMsg());
			}
		}
		return info;
	}
}
