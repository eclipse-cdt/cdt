/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *     Marc Dumais (Ericsson) - Bug 414959
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

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
	 * @param ctx indicates which inferior should be used when doing the attach
	 * @param id the pid of the process to attach to
	 * 
	 * @since 4.0
	 */
	public MITargetAttach(IMIContainerDMContext ctx, String pid) {
		this(ctx, pid, true);
	}

	/**
	 * @param ctx indicates which inferior should be used when doing the attach
	 * @param id the pid of the process to attach to
	 * @param interrupt indicates if the process should be interrupted once the attach is done
	 *                  Leaving the process running is only support with target-async on, which
	 *                  we currently only use in non-stop mode
	 * 
	 * @since 4.0
	 */
	public MITargetAttach(IMIContainerDMContext ctx, String pid, boolean interrupt) {
		super(ctx, "-target-attach", new String[] { pid + (interrupt ? "" : "&") }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
