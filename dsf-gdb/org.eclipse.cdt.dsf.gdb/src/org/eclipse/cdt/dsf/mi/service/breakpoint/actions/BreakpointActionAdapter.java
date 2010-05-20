/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.breakpoint.actions;

import org.eclipse.cdt.debug.core.breakpointactions.ILogActionEnabler;
import org.eclipse.cdt.debug.core.breakpointactions.IResumeActionEnabler;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
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

    @SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
        if (adapter.equals(ILogActionEnabler.class)) {
            return new MILogActionEnabler(fExecutor, fServiceTracker, fContext);
        }
        if (adapter.equals(IResumeActionEnabler.class)) {
            return new MIResumeActionEnabler(fExecutor, fServiceTracker, fContext);
        }
        return null;
    }

}
