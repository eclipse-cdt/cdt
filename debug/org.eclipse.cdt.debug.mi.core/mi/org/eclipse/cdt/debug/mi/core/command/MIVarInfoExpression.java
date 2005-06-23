/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
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
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIOutput;
import org.eclipse.cdt.debug.mi.core.output.MIVarInfoExpressionInfo;

/**
 * 
 *     -var-info-expression NAME
 *
 *  Returns what is represented by the variable object NAME:
 *
 *     lang=LANG-SPEC,exp=EXPRESSION
 *
 * where LANG-SPEC is `{"C" | "C++" | "Java"}'.
 * 
 */
public class MIVarInfoExpression extends MICommand 
{
	public MIVarInfoExpression(String name) {
		super("-var-info-expression", new String[]{name}); //$NON-NLS-1$
	}
	
	public MIVarInfoExpressionInfo getMIVarInfoExpressionInfo() throws MIException {
		return (MIVarInfoExpressionInfo)getMIInfo();
	}

	public MIInfo getMIInfo() throws MIException {
		MIInfo info = null;
		MIOutput out = getMIOutput();
		if (out != null) {
			info = new MIVarInfoExpressionInfo(out);
			if (info.isError()) {
				throwMIException(info, out);
			}
		}
		return info;
	}

}
