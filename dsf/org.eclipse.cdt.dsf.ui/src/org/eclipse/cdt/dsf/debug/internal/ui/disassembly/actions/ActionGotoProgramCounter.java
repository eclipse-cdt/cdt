/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyMessages;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;

public final class ActionGotoProgramCounter extends AbstractDisassemblyAction {
	public ActionGotoProgramCounter(IDisassemblyPart disassemblyPart) {
		super(disassemblyPart);
		setText(DisassemblyMessages.Disassembly_action_GotoPC_label);
		setToolTipText(DisassemblyMessages.Disassembly_action_GotoPC_tooltip);
	}

	@Override
	public void run() {
		getDisassemblyPart().gotoProgramCounter();
	}
}
