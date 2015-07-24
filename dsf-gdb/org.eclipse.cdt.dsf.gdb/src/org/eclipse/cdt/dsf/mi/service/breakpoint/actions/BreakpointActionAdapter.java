/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *     Marc Dumais (Ericsson) - Added support for reverse debug action (Bug 365776)
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.breakpoint.actions;

import org.eclipse.cdt.debug.core.breakpointactions.ILogActionEnabler;
import org.eclipse.cdt.debug.core.breakpointactions.IResumeActionEnabler;
import org.eclipse.cdt.debug.core.breakpointactions.IReverseDebugEnabler;
import org.eclipse.cdt.debug.core.breakpointactions.IScriptActionEnabler;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.service.breakpoint.actions.GDBScriptActionEnabler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.IAdaptable;

/**
 * @since 3.0
 */
public class BreakpointActionAdapter implements IAdaptable {

    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fServiceTracker;
    private final IDMContext fContext;

    public BreakpointActionAdapter(DsfExecutor executor, DsfServicesTracker serviceTracker, IDMContext context) {
        fExecutor = executor;
        fServiceTracker = serviceTracker;
        fContext = context;
    }

	@SuppressWarnings("unchecked")
	@Override
   	public <T> T getAdapter(Class<T> adapter) {
        if (adapter.equals(ILogActionEnabler.class)) {
            return (T)new MILogActionEnabler(fExecutor, fServiceTracker, fContext);
        }
        if (adapter.equals(IResumeActionEnabler.class)) {
            return (T)new MIResumeActionEnabler(fExecutor, fServiceTracker, fContext);
        }
        if (adapter.equals(IReverseDebugEnabler.class)) {
        	return (T)new MIReverseDebugEnabler(fExecutor, fServiceTracker, fContext);
        }
        if (adapter.equals(IScriptActionEnabler.class)) {
        	return (T)new GDBScriptActionEnabler(fExecutor, fServiceTracker, fContext);
        }
        return null;
    }

}
