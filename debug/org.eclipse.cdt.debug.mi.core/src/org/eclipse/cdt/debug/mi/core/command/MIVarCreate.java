/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIVarCreateInfo;

/**
 * 
 *    -var-create {NAME | "-"}
 *       {FRAME-ADDR | "*"} EXPRESSION
 *
 * This operation creates a variable object, which allows the
 * monitoring of a variable, the result of an expression, a memory cell or
 * a CPU register.
 *
 *   The NAME parameter is the string by which the object can be
 * referenced.  It must be unique.  If `-' is specified, the varobj system
 * will generate a string "varNNNNNN" automatically.  It will be unique
 * provided that one does not specify NAME on that format.  The command
 * fails if a duplicate name is found.
 *
 *  The frame under which the expression should be evaluated can be
 * specified by FRAME-ADDR.  A `*' indicates that the current frame should
 * be used.
 *
 *   EXPRESSION is any expression valid on the current language set (must
 * not begin with a `*'), or one of the following:
 *
 *  * `*ADDR', where ADDR is the address of a memory cell
 *
 *   * `*ADDR-ADDR' -- a memory address range (TBD)
 *
 *   * `$REGNAME' -- a CPU register name
 * 
 */
public class MIVarCreate extends MICommand 
{
	public MIVarCreate(String expression) {
		this("-", "*", expression);
	}

	public MIVarCreate(String name, String expression) {
		this(name, "*", expression);
	}

	public MIVarCreate(String name, String frameAddr, String expression) {
		super("-var-name", new String[]{name, frameAddr, expression});
	}

	public MIVarCreateInfo getMIVarCreateInfo() throws MIException {
		return (MIVarCreateInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIVarCreateInfo(out);
			if (info.isError()) {
				throw new MIException(info.getErrorMsg());
			}
		}
		return info;
	}
}
