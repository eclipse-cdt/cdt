/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIDataEvaluateExpressionInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;

/**
 * 
 *      -data-evaluate-expression EXPR
 *
 *   Evaluate EXPR as an expression.  The expression could contain an
 *inferior function call.  The function call will execute synchronously.
 *If the expression contains spaces, it must be enclosed in double quotes.
 *
 */
public class MIDataEvaluateExpression extends MICommand 
{
	public MIDataEvaluateExpression(String expr) {
		super("-data-evaluate-expression", new String[]{expr});
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIDataEvaluateExpressionInfo(out);
			if (info.isError()) {
				throw new MIException(info.getErrorMsg());
			}
		}
		return info;
	}
}
