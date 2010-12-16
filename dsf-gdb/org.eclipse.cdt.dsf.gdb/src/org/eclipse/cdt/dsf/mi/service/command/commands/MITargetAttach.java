/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * -target-attach < PID | THREAD_GROUP_ID >
 * 
 * This command attaches to the process specified by the PID
 * or THREAD_GROUP_ID. If attaching to a thread group, the id
 * previously returned by `-list-thread-groups --available' must be used.
 * 
 * @since 1.1
 */
public class MITargetAttach extends MICommand<MIInfo> {


	/**
	 *  @deprecated Replaced with MITargetAttach(IMIContainerDMContext, String)
     * since this command is container-specific
     */
	@Deprecated
	public MITargetAttach(ICommandControlDMContext ctx, String groupId) {
		super(ctx, "-target-attach", new String[] {groupId}); //$NON-NLS-1$
	}
	
	/**
	 * @param ctx indicates which inferior should be used when doing the attach
	 * @param id the pid of the process to attach to
	 * 
	 * @since 4.0
	 */
	public MITargetAttach(IMIContainerDMContext ctx, String pid) {
		super(ctx, "-target-attach", new String[] { pid }); //$NON-NLS-1$
	}
	
    @Override
    public boolean supportsThreadGroupOption() {
    	return true;
    }
}
