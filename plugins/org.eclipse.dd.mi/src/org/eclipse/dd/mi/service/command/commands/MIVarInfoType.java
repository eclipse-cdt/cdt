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
import org.eclipse.dd.mi.service.command.output.MIVarInfoTypeInfo;


/**
 * 
 *     -var-info-type NAME
 *
 *   Returns the type of the specified variable NAME.  The type is
 * returned as a string in the same format as it is output by the GDB CLI:
 *
 *     type=TYPENAME
 * 
 */
public class MIVarInfoType extends MICommand<MIVarInfoTypeInfo> 
{
	/**
     * @since 1.1
     */
	public MIVarInfoType(ICommandControlDMContext ctx, String name) {
		super(ctx, "-var-info-type", new String[]{name}); //$NON-NLS-1$
	}

	public MIVarInfoType(MIControlDMContext ctx, String name) {
	    this ((ICommandControlDMContext)ctx, name);
	}
	
    @Override
    public MIVarInfoTypeInfo getResult(MIOutput out) {
        return new MIVarInfoTypeInfo(out);
    }
}
