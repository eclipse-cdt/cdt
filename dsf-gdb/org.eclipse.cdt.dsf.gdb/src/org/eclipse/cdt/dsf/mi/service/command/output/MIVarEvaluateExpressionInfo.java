/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Mathias Kunter       - use MIConst.getString which is for human consumption (Bug 307311)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI var-evalute-expression
 */
public class MIVarEvaluateExpressionInfo extends MIInfo {

	String value = ""; //$NON-NLS-1$

	public MIVarEvaluateExpressionInfo(MIOutput record) {
		super(record);
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("value")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							value = ((MIConst) val).getString();
						}
					}
				}
			}
		}
	}

	public String getValue() {
		return value;
	}
}
