/*******************************************************************************
 * Copyright (c) 2009 Texas Instruments, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Texas Instruments[nmehregani] - initial API and implementation
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
		if (addressBar!=null && addressBar.isEnabled() && fDisassemblyPart.isSuspended()) {
        	String location = addressBar.getText();        	
        	        	
        	if (location==null || location.trim().length()==0)
        		return;
        	
        	location = location.trim();        	        	
        	BigInteger address = null; 
        	try {
        		address = DisassemblyPart.decodeAddress(location);
				if (address.compareTo(BigInteger.ZERO) < 0) {
					address = null;
					addressBar.setWarningIconVisible(true);
					fDisassemblyPart.generateErrorDialog(DisassemblyMessages.Disassembly_GotoAddressDialog_error_invalid_address);	
					return;
				}
			} catch (NumberFormatException x) {
				// This will be handled below.  location will be treated as a symbol
			}

			// hide warning icon if it was shown before
			addressBar.setWarningIconVisible(false);

			/* Location is an address */
			if (address!=null) {
				fDisassemblyPart.gotoAddress(address);
			}
			/* Location is a symbol */
			else {
				fDisassemblyPart.gotoSymbol(location);
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
