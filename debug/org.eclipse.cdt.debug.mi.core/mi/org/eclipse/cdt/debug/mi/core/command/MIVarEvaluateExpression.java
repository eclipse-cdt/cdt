/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

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
		super("-var-evaluate-expression", new String[] { expression }); //$NON-NLS-1$
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
				throwMIException(info, out);
			}
		}
		return info;
	}
}
