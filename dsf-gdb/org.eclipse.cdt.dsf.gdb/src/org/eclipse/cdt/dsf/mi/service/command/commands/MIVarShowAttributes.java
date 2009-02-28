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
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarShowAttributesInfo;

/**
 * 
 *    -var-show-attributes NAME
 *
 *  List attributes of the specified variable object NAME:
 *
 *    status=ATTR [ ( ,ATTR )* ]
 *
 * where ATTR is `{ { editable | noneditable } | TBD }'.
 * 
 */
//DsfMIVarShowAttributesInfo

public class MIVarShowAttributes extends MICommand<MIVarShowAttributesInfo> 
{
	/**
     * @since 1.1
     */
	public MIVarShowAttributes(ICommandControlDMContext ctx, String name) {
		super(ctx, "-var-show-attributes", new String[]{name}); //$NON-NLS-1$
	}
	
    @Override
    public MIVarShowAttributesInfo getResult(MIOutput out) {
        return new MIVarShowAttributesInfo(out);
    }
}
