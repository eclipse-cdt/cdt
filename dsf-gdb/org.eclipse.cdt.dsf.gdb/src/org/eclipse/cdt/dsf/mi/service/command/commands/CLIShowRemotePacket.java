/*******************************************************************************
 * Copyright (c) 2016 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Teodor Madan (Freescale Semiconductor) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.command.commands;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIShowRemotePacketInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;

/**
 * Returns the remote packet enabled state
 * 
 * @since 5.0
 */
public class CLIShowRemotePacket extends MIInterpreterExecConsole<CLIShowRemotePacketInfo> {

	private static final String SHOW_PACKET = "show remote ";  //$NON-NLS-1$

	public CLIShowRemotePacket(IDMContext ctx, String packet_id) {
		super(ctx, SHOW_PACKET + packet_id);
		
		assert packet_id.endsWith("-packet"); //$NON-NLS-1$
	}

	@Override
	public CLIShowRemotePacketInfo getResult(MIOutput miResult) {
		return new CLIShowRemotePacketInfo(miResult);
	}
}
