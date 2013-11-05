/*******************************************************************************
 * Copyright (c) 2013 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import java.util.List;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;

public class GDBBreakpointDetailTester extends PropertyTester {

	private static final String ARE_BREAKPOINT_DETAILS_AVAILABLE = "areBreakpointDetailsAvailable"; //$NON-NLS-1$
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (ARE_BREAKPOINT_DETAILS_AVAILABLE.equals(property) 
			&& (receiver instanceof List<?>) 
		    && ((List<?>)receiver).size() == 1
		    && ((List<?>)receiver).get(0) instanceof ICBreakpoint) {
			
			IAdaptable dc = DebugUITools.getDebugContext();
			if (dc instanceof IDMVMContext) {
				IBreakpointsTargetDMContext btDmc = 
					DMContexts.getAncestorOfType(((IDMVMContext)dc).getDMContext(), IBreakpointsTargetDMContext.class);
				if (btDmc != null)
					return true;
			}
		}
		return false;
	}

}
