/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson				- Modified for handling of frame contexts
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;


import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIVarListChildrenInfo;

/**
 * 
 *     -var-list-children NAME
 *
 *  Returns a list of the children of the specified variable object:
 *
 *     numchild=N,children={{name=NAME,
 *     numchild=N,type=TYPE},(repeats N times)}
 * 
 */
public class MIVarListChildren extends MICommand<MIVarListChildrenInfo> 
{
	/**
     * @since 1.1
     */
	public MIVarListChildren(ICommandControlDMContext ctx, String name) {
		super(ctx, "-var-list-children", new String[]{name}); //$NON-NLS-1$
	}
	
    @Override
    public MIVarListChildrenInfo getResult(MIOutput out) {
        return new MIVarListChildrenInfo(out);
    }
}
