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

package org.eclipse.cdt.debug.mi.core.output;


/**
 * GDB/MI Data evalue expression parsing response.
 */
public class MIDataEvaluateExpressionInfo extends MIInfo{

	String expr;

	public MIDataEvaluateExpressionInfo(MIOutput rr) {
		super(rr);
	}

	public String getExpression() {
		if (expr == null) {
			parse();
		}
		return expr;
	}

	void parse() {
		expr = ""; //$NON-NLS-1$
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results =  rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("value")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							expr = ((MIConst)value).getCString();
						}
					}
				}
			}
		}
	}
}
