/*******************************************************************************
 * Copyright (c) 2008, 2017 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *     Marc Dumais (Ericsson) - Bug 414959
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.gdb.service.GDBProcesses_7_2;
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
	private boolean extraNewline;

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
		this(ctx, pid, interrupt, false);
	}

	/**
	 * @param ctx indicates which inferior should be used when doing the attach
	 * @param id the pid of the process to attach to
	 * @param interrupt indicates if the process should be interrupted once the attach is done
	 *                  Leaving the process running is only support with target-async on, which
	 *                  we currently only use in non-stop mode
	 * @param extraNewline force an extra newline
	 * @since 5.4
	 */
	public MITargetAttach(IMIContainerDMContext ctx, String pid, boolean interrupt, boolean extraNewline) {
		super(ctx, "-target-attach", new String[] { pid + (interrupt ? "" : "&") }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.extraNewline = extraNewline;
	}

	/**
	 * Add an extra newline to force GDB 7.11 to flush error response to the MI
	 * channel.
	 *
	 * @see GDBProcesses_7_2#targetAttachRequiresTrailingNewline
	 * @since 5.4
	 */
	@Override
	public String constructCommand(String groupId, String threadId, int frameId) {
		/*
		 * We need to add the newline in constructCommand because the newline has to be
		 * after the parameters. The newline can't be added as a parameter because
		 * parameters are trimmed before being added to the command.
		 */
		String command = super.constructCommand(groupId, threadId, frameId);
		if (extraNewline) {
			command += "\n"; //$NON-NLS-1$
		}
		return command;
	}
}
