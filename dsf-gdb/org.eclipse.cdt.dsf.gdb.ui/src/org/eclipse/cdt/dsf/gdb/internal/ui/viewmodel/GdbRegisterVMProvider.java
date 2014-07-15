/*****************************************************************
 * Copyright (c) 2012, 2014 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Dallaway - DSF-GDB register format persistence (bug 395909)
 *     Marc Khouzam (Ericsson) - Make use of base class methods for IElementFormatProvider (Bug 439624)
 *****************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterVMProvider;
import org.eclipse.cdt.dsf.mi.service.MIRegisters.MIRegisterDMC;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * GDB View Model provider for the registers view.
 */
public class GdbRegisterVMProvider extends RegisterVMProvider {

	public GdbRegisterVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
		super(adapter, context, session);
	}
	
	@Override
	public boolean supportFormat(IVMContext context) {
		if (context instanceof IDMVMContext) {
			// Make sure we are dealing with a register node and not a register group for example
			IDMContext dmc = ((IDMVMContext)context).getDMContext();
			if (dmc instanceof IRegisterDMContext) return true;
		}
		return false;
	}

	@Override
	protected String getElementKey(IVMContext context) {
		if (context instanceof IDMVMContext) {
			IDMContext dmc = ((IDMVMContext)context).getDMContext();
			if (dmc instanceof IRegisterDMContext) {
				if (dmc instanceof MIRegisterDMC) {
					return ((MIRegisterDMC) dmc).getName(); // use MI register name as persistence key
				} else {
					// Things won't work if we have a IRegisterDMC but not an MIRegisterDMC
					// We should have an id based on the IRegisterDMC instead...
					assert false;
				}
			}
		}
		return null;
	}
}
