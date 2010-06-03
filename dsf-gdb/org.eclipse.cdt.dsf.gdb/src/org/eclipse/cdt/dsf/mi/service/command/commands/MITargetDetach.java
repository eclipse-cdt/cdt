/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;

/**
 * -target-detach < --pid PID | THREAD_GROUP_ID >
 * 
 * This command detaches from the process specified by the PID
 * or THREAD_GROUP_ID
 * @since 1.1
 */
public class MITargetDetach extends MICommand<MIInfo> {
	
	public MITargetDetach(ICommandControlDMContext ctx, String groupId) {
		super(ctx, "-target-detach", new String[] {groupId}); //$NON-NLS-1$
	}
}
