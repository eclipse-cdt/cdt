/*******************************************************************************
 * Copyright (c) 2009, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson				- Modified for handling of frame contexts
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
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

	/**
	 * @param ctx
	 * @param name
	 * @param from
	 *            The index of the first child to be listed, if there is one
	 *            with this index.
	 * @param to
	 *            One behind the last child to be listed.
	 *            
	 * @since 4.0
	 */
	public MIVarListChildren(ICommandControlDMContext ctx, String name, int from, int to) {
		super(ctx, "-var-list-children", new String[]{name, String.valueOf(from), String.valueOf(to)}); //$NON-NLS-1$
	}
	
    @Override
    public MIVarListChildrenInfo getResult(MIOutput out) {
        return new MIVarListChildrenInfo(out);
    }
}
