/*******************************************************************************
 * Copyright (c) 2011 Texas Instruments, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dobrin Alexiev (Texas Instruments) - initial API and implementation (bug 336876)
********************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.debugview.layout;

import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.expressions.PropertyTester;

/**
 * Property tester for debug view related commands - group, ungroup, hide, etc. 
 * 
 * @since 2.2
 */

public class DebugViewLayoutTester extends PropertyTester{
	
	public DebugViewLayoutTester() {
	}

	protected static final String IS_GROUP_VISIBLE = "isGroupDebugContextsVisible"; //$NON-NLS-1$
	protected static final String IS_UNGROUP_VISIBLE = "isUngroupDebugContextsVisible"; //$NON-NLS-1$
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		if( IS_GROUP_VISIBLE.equals(property) || IS_UNGROUP_VISIBLE.equals(property)) {
    		if (receiver instanceof IDMVMContext) {
				return test((IDMVMContext)receiver);
    		}
    	}
    	return false;
    }   
    
    private boolean test(IDMVMContext dmContext) {
		String sessionId = dmContext.getDMContext().getSessionId();
		return DsfSession.isSessionActive(sessionId);
    }
}
