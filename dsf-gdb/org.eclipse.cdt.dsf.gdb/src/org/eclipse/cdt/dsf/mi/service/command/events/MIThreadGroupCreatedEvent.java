/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
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
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.events;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;

/**
 * This can only be detected by gdb/mi after GDB 6.8.
 * @since 1.1
 *
 */
@Immutable
public class MIThreadGroupCreatedEvent extends MIEvent<IProcessDMContext> {

	final private String fGroupId;

	public MIThreadGroupCreatedEvent(IProcessDMContext ctx, int token, String groupId) {
		super(ctx, token, null);
		fGroupId = groupId;
	}

	public String getGroupId() {
		return fGroupId;
	}

}
