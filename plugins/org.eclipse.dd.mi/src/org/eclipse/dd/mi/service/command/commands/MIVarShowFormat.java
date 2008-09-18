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

package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.dd.mi.service.command.MIControlDMContext;
import org.eclipse.dd.mi.service.command.output.MIOutput;
import org.eclipse.dd.mi.service.command.output.MIVarShowFormatInfo;

/**
 * 
 *    -var-show-format NAME
 *
 *  Returns the format used to display the value of the object NAME.
 *
 *     FORMAT ==>
 *     FORMAT-SPEC
 * 
 */
public class MIVarShowFormat extends MICommand<MIVarShowFormatInfo> 
{
	/**
     * @since 1.1
     */
	public MIVarShowFormat(ICommandControlDMContext ctx, String name) {
		super(ctx, "-var-show-format", new String[]{name}); //$NON-NLS-1$
	}
    
    @Deprecated
	public MIVarShowFormat(MIControlDMContext ctx, String name) {
	    this ((ICommandControlDMContext)ctx, name);
	}

    @Override
    public MIVarShowFormatInfo getResult(MIOutput out) {
        return new MIVarShowFormatInfo(out);
    }
}
