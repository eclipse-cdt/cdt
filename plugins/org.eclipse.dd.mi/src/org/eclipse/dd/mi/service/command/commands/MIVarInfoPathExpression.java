/*******************************************************************************
 * Copyright (c) 2007 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.dd.mi.service.command.MIControlDMContext;
import org.eclipse.dd.mi.service.command.output.MIOutput;
import org.eclipse.dd.mi.service.command.output.MIVarInfoPathExpressionInfo;

/**
 * 
 *     -var-info-path-expression NAME
 *     
 *     as of GDB 6.7
 *
 *  Print full expression that this variable object represents:
 *
 *     (gdb) -var-info-path-expression C.Base.public.m_size
 *     ^done,path_expr=((Base)c).m_size)
 * 
 */

public class MIVarInfoPathExpression extends MICommand<MIVarInfoPathExpressionInfo> 
{
	/**
     * @since 1.1
     */
	public MIVarInfoPathExpression(ICommandControlDMContext dmc, String name) {
		super(dmc, "-var-info-path-expression", new String[]{name}); //$NON-NLS-1$
	}

	public MIVarInfoPathExpression(MIControlDMContext dmc, String name) {
	    this ((ICommandControlDMContext)dmc, name);
	}
	
    @Override
    public MIVarInfoPathExpressionInfo getResult(MIOutput out) {
        return new MIVarInfoPathExpressionInfo(out);
    }
}

