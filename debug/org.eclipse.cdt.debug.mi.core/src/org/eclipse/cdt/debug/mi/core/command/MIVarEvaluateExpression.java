/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIVarEvaluateExpressionInfo;

/**
 * 
 *     -var-evaluate-expression NAME
 *
 *  Evaluates the expression that is represented by the specified
 * variable object and returns its value as a string in the current format
 * specified for the object:
 *
 *      value=VALUE
 * 
 */
public class MIVarEvaluateExpression extends MICommand {
	public MIVarEvaluateExpression(String expression) {
		super("-var-evaluate-expression", new String[] { expression });
	}

	public MIVarEvaluateExpressionInfo getMIVarEvaluateExpressionInfo()
		throws MIException {
		return (MIVarEvaluateExpressionInfo) getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIVarEvaluateExpressionInfo(out);
			if (info.isError()) {
				throw new MIException(info.getErrorMsg());
			}
		}
		return info;
	}
}
