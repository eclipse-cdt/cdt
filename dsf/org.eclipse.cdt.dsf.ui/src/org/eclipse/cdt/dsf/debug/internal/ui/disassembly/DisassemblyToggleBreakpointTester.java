/*****************************************************************
 * Copyright (c) 2011 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - 
 *     	Update CDT ToggleBreakpointTargetFactory enablement (340177 )
 *****************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.ui.DebugUITools;

/**
 * Disassembly toggle breakpoint factory enablement tester.
 * 
 * @since 2.2
 */
public class DisassemblyToggleBreakpointTester extends PropertyTester {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("isDisassemblyViewSupportsCBreakpoint".equals(property) && (receiver instanceof IDisassemblyPart)) { //$NON-NLS-1$
			IDisassemblyPart view = ((IDisassemblyPart) receiver);
			if (!CDebugUtils.isCustomToggleBreakpointFactory())
				return true;			
			
			IAdaptable element = DebugUITools.getPartDebugContext(view.getSite());			
	        if (element != null) {
	            IDebugModelProvider modelProvider = (IDebugModelProvider)element.getAdapter(IDebugModelProvider.class);
	            if (modelProvider != null) {
	                String[] models = modelProvider.getModelIdentifiers();
	                for (String model : models) {
	                    if (CDIDebugModel.getPluginIdentifier().equals(model) ||
	                        ICBreakpoint.C_BREAKPOINTS_DEBUG_MODEL_ID.equals(model)) {
	                        return true;
	                    }
	                }
	            } else if (element instanceof IDebugElement) {
	                if (CDIDebugModel.getPluginIdentifier().equals(((IDebugElement)element).getModelIdentifier()) ) {
	                    return true;
	                }
	            }
	        }
		}
		return false;
	}
}