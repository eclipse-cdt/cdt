/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
