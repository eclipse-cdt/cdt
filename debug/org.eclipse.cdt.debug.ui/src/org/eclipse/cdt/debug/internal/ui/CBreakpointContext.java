/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson           - Added tracepoint support (284286)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionFilter;

/**
 * Input for breakpoint properties dialog.  It captures both the 
 * selected breakpoint object as well as the selected debug context.
 * This combined context can then be used by breakpoint property
 * pages to access model and target specific breakpoint settings.  
 */
public class CBreakpointContext extends PlatformObject {

    // Register an adapter factory for the class when it is first loaded.
    static {
        Platform.getAdapterManager().registerAdapters(new CBreakpointContextAdapterFactory(), CBreakpointContext.class);
    }
    
    /**
     * Breakpoint object held by this context.
     */
    private final ICBreakpoint fBreakpoint;
    
    /**
     * The active debug context held by this context.
     */
    private final ISelection fDebugContext;
    
    /**
     * Creates a new breakpoint context with given breakpoint and debbug 
     * context selection.
     */
    public CBreakpointContext(ICBreakpoint breakpoint, ISelection debugContext) {
        fBreakpoint = breakpoint;
        fDebugContext = debugContext;
    }

    /**
     * Returns the breakpoint.
     */
    public ICBreakpoint getBreakpoint() { return fBreakpoint; }
    
    /**
     * Returns the debug context.
     */
    public ISelection getDebugContext() { return fDebugContext; }
}

/**
 * Action filter for the breakpoint context, which allows property
 * pages to filter their activation based on the debug model ID of 
 * the active debug context.
 */
class CBreakpointContextActionFilter implements IActionFilter {

    private static String[] EMPTY_IDENTIFIERS_ARRAY = new String[0];
    
    public boolean testAttribute(Object target, String name, String value) {
        if (target instanceof CBreakpointContext) {
            if ("debugModelId".equals(name)) { //$NON-NLS-1$
                String[] targetModelIds = getDebugModelIds( (CBreakpointContext)target );
                for (int i = 0; i < targetModelIds.length; i++) {
                    if (targetModelIds[i].equals(value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private String[] getDebugModelIds(CBreakpointContext bpContext) {
        ISelection debugContext = bpContext.getDebugContext();
        if (debugContext instanceof IStructuredSelection) {
            Object debugElement = ((IStructuredSelection)debugContext).getFirstElement();
            if (debugElement instanceof IAdaptable) {
                IDebugModelProvider debugModelProvider = 
                    (IDebugModelProvider)((IAdaptable)debugElement).getAdapter(IDebugModelProvider.class);
                if (debugModelProvider != null) {
                    return debugModelProvider.getModelIdentifiers();
                } else if (debugElement instanceof IDebugElement) {
                    return new String[] { ((IDebugElement)debugElement).getModelIdentifier() };
                }
            }
        }
        return EMPTY_IDENTIFIERS_ARRAY; 
    }
}

/**
 * Adapter factory which returns the breakpoint object and the action
 * filter for the CBreakpointContext type.
 */
class CBreakpointContextAdapterFactory implements IAdapterFactory {
    
    private static final Class[] fgAdapterList = new Class[] {
        IBreakpoint.class, ICBreakpoint.class, ICTracepoint.class, IActionFilter.class
    };

    private static final IActionFilter fgActionFilter = new CBreakpointContextActionFilter();
    
    public Object getAdapter(Object obj, Class adapterType) {
        if (adapterType.isInstance( ((CBreakpointContext)obj).getBreakpoint() )) {
            return ((CBreakpointContext)obj).getBreakpoint();
        }
        
        if (IActionFilter.class.equals(adapterType)) {
            return fgActionFilter;
        }
        return null;
    }
    
    public Class[] getAdapterList() {
        return fgAdapterList;
    }
}

