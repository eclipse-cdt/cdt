/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarAssignInfo;

/**
 * 
 *    -var-assign NAME EXPRESSION
 *
 *  Assigns the value of EXPRESSION to the variable object specified by
 * NAME.  The object must be `editable'.
 * 
 */
public class MIVarAssign extends MICommand<MIVarAssignInfo> 
{
	/**
     * @since 1.1
     */
	public MIVarAssign(ICommandControlDMContext ctx, String name, String expression) {
		super(ctx, "-var-assign", new String[]{name, expression}); //$NON-NLS-1$
	}
    
    @Override
    public MIVarAssignInfo getResult(MIOutput out) {
        return new MIVarAssignInfo(out);
    }
}
