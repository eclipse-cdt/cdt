/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders)- Initial API and implementation
 */
package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;

/**
 * -gdb-set remotetimeout num
 * @since 5.5
 */
public class MIGDBSetRemoteTimeout extends MIGDBSet {
	public MIGDBSetRemoteTimeout(ICommandControlDMContext ctx, String remoteTimeout) {
		super(ctx, new String[] { "remotetimeout", remoteTimeout });//$NON-NLS-1$
	}
}
