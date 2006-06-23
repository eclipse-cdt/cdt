/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
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
	public MIDataEvaluateExpression(String miVersion, String expr) {
		super(miVersion, "-data-evaluate-expression", new String[]{expr}); //$NON-NLS-1$
	}

	public MIDataEvaluateExpressionInfo getMIDataEvaluateExpressionInfo() throws MIException {
		return (MIDataEvaluateExpressionInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIDataEvaluateExpressionInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}
}
