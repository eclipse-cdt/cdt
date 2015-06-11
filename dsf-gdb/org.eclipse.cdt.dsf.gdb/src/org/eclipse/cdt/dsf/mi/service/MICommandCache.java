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
package org.eclipse.cdt.dsf.mi.service;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.internal.provisional.service.IMIExecutionContextTranslator;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Execution contexts are no longer immutable objects.
 * Clients should base their caches on the Thread context instead. 
 * This class makes the transition from Execution context to Thread context 
 * transparent to the user.  
 * @since 4.8
 */
public class MICommandCache extends CommandCache {

    private DsfServicesTracker fTracker;
	
	public MICommandCache(DsfSession session, ICommandControl control, DsfServicesTracker tracker) {
		super(session, control);
		fTracker = tracker;
	}

    @Override
	public void setContextAvailable(IDMContext context, boolean isAvailable) {
    	IDMContext stableContext = getStableContext(context);
    	super.setContextAvailable(stableContext, isAvailable);
    }

    private IDMContext getStableContext( IDMContext context) {
    	IDMContext ret = context;
    	if( context instanceof IMIExecutionDMContext) {
    		IMIExecutionContextTranslator translator = 
    			fTracker.getService(IMIExecutionContextTranslator.class);
    		if(translator != null)
    			ret = translator.getStableContext((IExecutionDMContext)context);
    	}
		return ret;
    }
}
