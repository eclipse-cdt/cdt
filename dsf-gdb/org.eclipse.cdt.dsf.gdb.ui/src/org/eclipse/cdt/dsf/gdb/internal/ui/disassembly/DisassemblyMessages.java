/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Support for dynamic printf (Bug 400628)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.disassembly;

import org.eclipse.osgi.util.NLS;

public final class DisassemblyMessages extends NLS {
	public static String Disassembly_action_AddDynamicPrintf_label;
	public static String Disassembly_action_AddDynamicPrintf_errorMessage;
	public static String Disassembly_action_AddDynamicPrintf_errorTitle;
	public static String Disassembly_action_AddDynamicPrintf_accelerator;

	static {
		NLS.initializeMessages(DisassemblyMessages.class.getName(), DisassemblyMessages.class);
	}

	// Do not instantiate
	private DisassemblyMessages() {
	}
}
