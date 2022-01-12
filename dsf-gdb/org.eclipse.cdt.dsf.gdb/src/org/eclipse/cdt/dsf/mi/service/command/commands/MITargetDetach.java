/*******************************************************************************
 * Copyright (c) 2008, 2011 Ericsson and others.
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
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * -target-detach < PID | THREAD_GROUP_ID >
 *
 * This command detaches from the process specified by the PID
 * or THREAD_GROUP_ID
 * @since 1.1
 */
public class MITargetDetach extends MICommand<MIInfo> {

	/*
	 * No need to specify an IMIContainerDMContext because
	 * only one such context is associated with ID; therefore,
	 * GDB will know which inferior to detach using only ID.
	 */
	public MITargetDetach(ICommandControlDMContext ctx, String groupId) {
		super(ctx, "-target-detach", new String[] { groupId }); //$NON-NLS-1$
	}

	/*
	 * This method does not follow our convention of passing the highest required
	 * context and proper parameters.  The proper way is handled by the method above
	 * MITargetDetach(ICommandControlDMContext, String)
	 * However, this method here will trigger the command in the form
	 * Form 1: -target-detach --thread-group i2
	 * instead of the way the above method does, which is
	 * Form 2: -target-detach i2
	 * Because of a bug in GDB 7.2, form 2 does not work.
	 * However, this has been fixed with GDB 7.2.1, which is why we keep both
	 * approaches.
	 */
	/** @since 4.0 */
	public MITargetDetach(IMIContainerDMContext ctx) {
		super(ctx, "-target-detach"); //$NON-NLS-1$
	}
}
