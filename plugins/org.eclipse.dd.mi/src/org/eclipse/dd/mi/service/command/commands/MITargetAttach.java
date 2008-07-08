/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.mi.service.command.commands;

import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.mi.service.command.output.MIInfo;

/**
 * -target-attach < --pid PID | THREAD_GROUP_ID >
 * 
 * This command attaches to the process specified by the PID
 * or THREAD_GROUP_ID
 */
public class MITargetAttach extends MICommand<MIInfo> {

	public MITargetAttach(IDMContext ctx, String threadGroupId) {
		super(ctx, "-target-attach", new String[] {threadGroupId}); //$NON-NLS-1$
	}
	
	public MITargetAttach(IDMContext ctx, int pid) {
		super(ctx, "-target-attach" , new String[] {"--pid " + pid}); //$NON-NLS-1$//$NON-NLS-2$
	}
}
