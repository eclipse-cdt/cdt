/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
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
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.output;

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
		String exp = getValue();
		try {
			code = Integer.parseInt(exp);
		} catch (NumberFormatException e) {
		}
		return code;
	}

}
