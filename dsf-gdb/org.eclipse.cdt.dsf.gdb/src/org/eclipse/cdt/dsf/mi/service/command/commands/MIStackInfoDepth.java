/*******************************************************************************
 * Copyright (c) 2007, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIStackInfoDepthInfo;

/**
 * 
 *     -stack-info-depth [maxDepth]
 *
 * 
 */
public class MIStackInfoDepth extends MICommand<MIStackInfoDepthInfo> 
{
	
    public MIStackInfoDepth(IMIExecutionDMContext ctx) {
    	super(ctx, "-stack-info-depth"); //$NON-NLS-1$
    }    	

    public MIStackInfoDepth(IMIExecutionDMContext ctx, int maxDepth) {
        super(ctx, "-stack-info-depth", new String[]{Integer.toString(maxDepth)}); //$NON-NLS-1$
    }

    @Override
    public MIStackInfoDepthInfo getResult(MIOutput out) {
        return new MIStackInfoDepthInfo(out);
    }
}
