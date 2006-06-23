/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
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
 * GDB/MI show parsing.
 * (gdb) 
 * -data-evaluate-expression $_exitcode
 * ^done,value="10"
 * (gdb)
 */
public class MIGDBShowExitCodeInfo extends MIDataEvaluateExpressionInfo {

	public MIGDBShowExitCodeInfo(MIOutput o) {
		super(o);
	}

	public int getCode() {
		int code = 0;
		String exp = getExpression();
		try {
			code = Integer.parseInt(exp);
		} catch (NumberFormatException e) {
		}
		return code;
	}

}
