/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIBreakInsertInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *    -break-insert [ -t ] [ -h ] [ -r ]
 *       [ -c CONDITION ] [ -i IGNORE-COUNT ]
 *       [ -p THREAD ] [ LINE | ADDR ]
 * 
 * If specified, LINE, can be one of:
 * 
 *  * function
 *
 *  * filename:linenum
 *
 *  * filename:function
 *
 *  * *address
 * 
 *  The possible optional parameters of this command are:
 *
 * `-t'
 *     Insert a tempoary breakpoint.
 *
 * `-h'
 *     Insert a hardware breakpoint.
 *
 * `-c CONDITION'
 *     Make the breakpoint conditional on CONDITION.
 *
 * `-i IGNORE-COUNT'
 *     Initialize the IGNORE-COUNT.
 *
 * `-r'
 *
 *     Insert a regular breakpoint in all the functions whose names match
 *     the given regular expression.  Other flags are not applicable to
 *     regular expresson.
 *
 *  The result is in the form:
 *
 *     ^done,bkptno="NUMBER",func="FUNCNAME",
 *      file="FILENAME",line="LINENO"
 * 
 */
public class MIBreakInsert extends MICommand 
{
	public MIBreakInsert(String func) {
		this(false, false, null, 0, func);
	}

	public MIBreakInsert(boolean isTemporary, boolean isHardware,
			 String condition, int ignoreCount, String line) {
		super("-break-insert");

		int i = 0;
		if (isTemporary || isHardware) {
			i++;
		}
		if (condition != null && condition.length() > 0) {
			i += 2;
		}
		if (ignoreCount > 0) {
			i += 2;
		}

		String[] opts = new String[i];
		
		i = 0;
		if (isTemporary) {
			opts[i] = "-t";
			i++;
		} else if (isHardware) {
			opts[i] = "-h";
			i++;
		}
		if (condition != null && condition.length() > 0) {
			opts[i] = "-c";
			i++;
			opts[i] = condition;
			i++;
		}
		if (ignoreCount > 0) {
			opts[i] = "-i";
			i++;
			opts[i] = Integer.toString(ignoreCount);
			i++;
		}

		if (opts.length > 0) {
			setOptions(opts);
		}
		setParameters(new String[]{line});
	}

	public MIBreakInsertInfo getMIBreakInsertInfo() throws MIException {
		return (MIBreakInsertInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIBreakInsertInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
