/*******************************************************************************
 * Copyright (c) 2007, 2009 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * GDB/MI var-info-path-expression.
 *
 * (gdb) -var-info-path-expression C.Base.public.m_size
 * ^done,path_expr=((Base)c).m_size)
 */
public class MIVarInfoPathExpressionInfo extends MIInfo {

	String exp = ""; //$NON-NLS-1$

	public MIVarInfoPathExpressionInfo(MIOutput record) {
		super(record);
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("path_expr")) { //$NON-NLS-1$
						MIValue val = results[i].getMIValue();
						if (val instanceof MIConst) {
							exp = ((MIConst) val).getString();
						}
					}
				}
			}
		}
	}

	public String getFullExpression() {
		return exp;
	}

}
