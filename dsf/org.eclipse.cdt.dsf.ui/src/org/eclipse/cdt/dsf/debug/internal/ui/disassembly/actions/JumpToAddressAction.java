/*******************************************************************************
 * Copyright (c) 2009, 2010 Texas Instruments, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Texas Instruments[nmehregani] - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Bug fix (326670)
 *     Patrick Chuong (Texas Instruments) - Bug fix (329682)
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import java.math.BigInteger;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyMessages;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyPart;
import org.eclipse.jface.action.Action;

public class JumpToAddressAction extends Action {

	DisassemblyPart fDisassemblyPart = null;

	public JumpToAddressAction(DisassemblyPart disassemblyPart) {
		fDisassemblyPart = disassemblyPart;
	}

	@Override
	public void run() {
		AddressBarContributionItem addressBar = fDisassemblyPart.getAddressBar();
		if (addressBar != null && addressBar.isEnabled() && fDisassemblyPart.isSuspended()) {
			String locationTxt = addressBar.getText();

			if (locationTxt == null || locationTxt.trim().length() == 0)
				return;

			locationTxt = locationTxt.trim();

			if (locationTxt.equals(DisassemblyMessages.Disassembly_GotoLocation_initial_text)) {
				fDisassemblyPart.gotoActiveFrameByUser();
				return;
			}

			BigInteger address = fDisassemblyPart.eval(locationTxt, false);
			if (address.compareTo(BigInteger.ZERO) < 0) {
				addressBar.setWarningIconVisible(true);
			} else {
				fDisassemblyPart.gotoLocationByUser(address, locationTxt);
				addressBar.setWarningIconVisible(false);
			}
		}
	}

	protected void activateDisassemblyContext() {
		fDisassemblyPart.activateDisassemblyContext();
	}

	protected void deactivateDisassemblyContext() {
		fDisassemblyPart.deactivateDisassemblyContext();
	}
}
