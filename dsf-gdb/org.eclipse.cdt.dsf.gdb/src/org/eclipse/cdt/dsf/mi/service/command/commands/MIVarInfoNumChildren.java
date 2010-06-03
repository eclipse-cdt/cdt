/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarInfoNumChildrenInfo;

/**
 * 
 *     -var-info-num-children NAME
 *
 *  Returns the number of children of a variable object NAME:
 *
 *     numchild=N
 * 
 */
public class MIVarInfoNumChildren extends MICommand<MIVarInfoNumChildrenInfo> 
{
	public MIVarInfoNumChildren(IExpressionDMContext ctx, String name) {
		super(ctx, "-var-info-num-children", new String[]{name}); //$NON-NLS-1$
	}
    
    @Override
    public MIVarInfoNumChildrenInfo getResult(MIOutput out) {
        return new MIVarInfoNumChildrenInfo(out);
    }
}
