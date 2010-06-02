/*******************************************************************************
 * Copyright (c) 2007, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.breakpoints;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;
import org.osgi.framework.BundleContext;

/**
 * Initial breakpoint service implementation.
 * Implements the IBreakpoints interface.
 */
public class DsfTestBreakpoints extends AbstractDsfService implements IBreakpoints
{
    public static final String ATTR_DEBUGGER_PREFIX = DsfTestBreakpoint.DSF_TEST_BREAKPOINT_MODEL_ID + ".debugger."; 

    public static final String ATTR_ENABLED = ATTR_DEBUGGER_PREFIX + "enabled"; 
    public static final String ATTR_TRANSLATED = ATTR_DEBUGGER_PREFIX + "enabled"; 
    public static final String ATTR_SUB_ID = ATTR_DEBUGGER_PREFIX + "subId"; 

    @Immutable
    public static class BreakpointsTargetDMContext extends AbstractDMContext implements IBreakpointsTargetDMContext {
        
        private static int fIdCounter = 1;
        
        public final Integer fId = fIdCounter++;
        
        BreakpointsTargetDMContext (String sessionId) {
            super(sessionId, DMContexts.EMPTY_CONTEXTS_ARRAY);
        }
        
        @Override
        public boolean equals(Object obj) {
            return baseEquals(obj) && (fId.equals(((BreakpointsTargetDMContext) obj).fId));
                 
        }

        @Override
        public int hashCode() {
            return baseHashCode() + fId.hashCode() ;
        }

        @Override
        public String toString() {
            return "breakpointsTarget(" + fId + ")";  //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
        }
    }
    
    /**
     * Context representing a PDA line breakpoint.  In PDA debugger, since there is only 
     * one file being debugged at a time, a breakpoint is uniquely identified using the 
     * line number only.
     */
    @Immutable
    public static class BreakpointDMContext extends AbstractDMContext implements IBreakpointDMContext {
        public final Integer fId;
        public final Integer fSubId;

        public BreakpointDMContext(String sessionId, BreakpointsTargetDMContext commandControlCtx, Integer id, Integer subId) {
            super(sessionId, new IDMContext[] { commandControlCtx });
            fId = id;
            fSubId = subId;
        }

        @Override
        public boolean equals(Object obj) {
            return baseEquals(obj) && 
                (fId.equals(((BreakpointDMContext) obj).fId)) && 
                (fSubId.equals(((BreakpointDMContext) obj).fSubId));
        }

        @Override
        public int hashCode() {
            return baseHashCode() + fId.hashCode() + fSubId.hashCode();
        }

        @Override
        public String toString() {
            return baseToString() + ".breakpoint(" + fId + "-" + fSubId + ")";  //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
        }
    }

    @Immutable
    public static class BreakpointDMData implements IBreakpointDMData {
        public final Map<String, Object> fAttributes;
        
        public BreakpointDMData(Map<String, Object> attributes) {
            fAttributes = Collections.unmodifiableMap( new HashMap<String, Object>(attributes) );
        }
        
        public IAddress[] getAddresses() { return null; }
        public String getBreakpointType() { return null; }
        public String getFileName() { return null; }
        public int getLineNumber() { return 0; }
        public String getFunctionName() { return null; }
        public String getCondition() { return null; }
        public int getIgnoreCount() { return 0; }
        public String getExpression() { return null; }
        
        public boolean isEnabled() { return (Boolean)getAttributes().get(ATTR_ENABLED); }
        public Map<String, Object> getAttributes() { return fAttributes; }
    }
    
    public static class BreakpointsChangedEvent implements IBreakpointsChangedEvent {
        public final BreakpointDMContext fBreakpoint;
        public final IBreakpointDMContext [] fBreakpointArray;
        
        BreakpointsChangedEvent (BreakpointDMContext bp) {
            fBreakpoint = bp;
            fBreakpointArray = new IBreakpointDMContext[] { bp };
        }

        public IBreakpointsTargetDMContext getDMContext() {
            return DMContexts.getAncestorOfType(fBreakpoint, IBreakpointsTargetDMContext.class);
        }
        
        public IBreakpointDMContext[] getBreakpoints() {
            return fBreakpointArray; 
        }
    }
    
    public static class BreakpointsAddedEvent extends BreakpointsChangedEvent implements IBreakpointsAddedEvent {
        BreakpointsAddedEvent(BreakpointDMContext bp) {
            super(bp);
        }
    }

    public static class BreakpointsRemovedEvent extends BreakpointsChangedEvent implements IBreakpointsRemovedEvent {
        BreakpointsRemovedEvent(BreakpointDMContext bp) {
            super(bp);
        }
    }

    public static class BreakpointsUpdatedEvent extends BreakpointsChangedEvent implements IBreakpointsUpdatedEvent {
        BreakpointsUpdatedEvent(BreakpointDMContext bp) {
            super(bp);
        }
    }
    
    // Breakpoints currently installed
    private Map<BreakpointDMContext, BreakpointDMData> fBreakpoints = new HashMap<BreakpointDMContext, BreakpointDMData>();

    /**
     * The service constructor
     * 
     * @param session The debugging session this service belongs to.
     */
    public DsfTestBreakpoints(DsfSession session) {
        super(session);
    }

    @Override
    public void initialize(final RequestMonitor rm) {
        super.initialize(new RequestMonitor(getExecutor(), rm) {
            @Override
            protected void handleSuccess() {
                doInitialize(rm);
            }
        });
    }

    private void doInitialize(final RequestMonitor rm) {

        // Register this service
        register(new String[] { IBreakpoints.class.getName(), DsfTestBreakpoints.class.getName() },
            new Hashtable<String, String>());

        rm.done();
    }

    @Override
    public void shutdown(final RequestMonitor rm) {
        unregister();
        rm.done();
    }

    @Override
    protected BundleContext getBundleContext() {
        return DsfTestPlugin.getBundleContext();
    }

    public void getBreakpoints(final IBreakpointsTargetDMContext context, final DataRequestMonitor<IBreakpointDMContext[]> rm) {
        // Validate the context
        // TODO: check the target context
//        if (!fCommandControl.getContext().equals(context)) {
//            DsfTestPlugin.failRequest(rm, INVALID_HANDLE, "Invalid breakpoints target context");
//            return;
//        }

        rm.setData(fBreakpoints.keySet().toArray(new IBreakpointDMContext[fBreakpoints.size()]));
        rm.done();
    }

    public void getBreakpointDMData(IBreakpointDMContext dmc, DataRequestMonitor<IBreakpointDMData> rm) {
        DsfTestPlugin.failRequest(rm, NOT_SUPPORTED, "Retrieving breakpoint data is not supported");
    }

    public void insertBreakpoint(IBreakpointsTargetDMContext context, Map<String, Object> attributes, 
        DataRequestMonitor<IBreakpointDMContext> rm) 
    {
        Boolean enabled = (Boolean)attributes.get(IBreakpoint.ENABLED);
        if (enabled != null && !enabled.booleanValue()) {
            // If the breakpoint is disabled, just fail the request. 
            DsfTestPlugin.failRequest(rm, REQUEST_FAILED, "Breakpoint is disabled");
        } else {
            BreakpointsTargetDMContext targetCtx = DMContexts.getAncestorOfType(context, BreakpointsTargetDMContext.class);
            if (targetCtx != null) {
                doInsertBreakpoint(targetCtx, attributes, rm);
            } else {
                DsfTestPlugin.failRequest(rm, INVALID_HANDLE, "Unknown breakpoint type");
            }
        }
    }

    private void doInsertBreakpoint(BreakpointsTargetDMContext targetCtx, final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> rm) 
    {
        // Retrieve the id
        Integer id = (Integer)attributes.get(DsfTestBreakpoint.ATTR_ID);
        if (id == null) {
            DsfTestPlugin.failRequest(rm, REQUEST_FAILED, "No ID specified");
            return;
        }

        Integer subId = (Integer)attributes.get(ATTR_SUB_ID);
        if (id == null) {
            DsfTestPlugin.failRequest(rm, REQUEST_FAILED, "No Sub ID specified");
            return;
        }

        // Create a new breakpoint context object and check that it's not 
        // installed already. PDA can only track a single breakpoint at a 
        // given line, attempting to set the second breakpoint should fail.
        final BreakpointDMContext breakpointCtx = 
            new BreakpointDMContext(getSession().getId(), targetCtx, id, subId);
        if (fBreakpoints.containsKey(breakpointCtx)) {
            DsfTestPlugin.failRequest(rm, REQUEST_FAILED, "Breakpoint already set");
            return;
        }

        // Add the new breakpoint context to the list of known breakpoints.  
        // Adding it here, before the set command is completed will prevent 
        // a possibility of a second breakpoint being installed in the same 
        // location while this breakpoint is being processed.  It will also
        // allow the breakpoint to be removed or updated even while it is 
        // still being processed here.
        fBreakpoints.put(breakpointCtx, new BreakpointDMData(attributes));
        rm.setData(breakpointCtx);
        getSession().dispatchEvent(new BreakpointsAddedEvent(breakpointCtx), getProperties());
    }


    public void removeBreakpoint(IBreakpointDMContext bpCtx, RequestMonitor rm) {
        if (!fBreakpoints.containsKey(bpCtx)) {
            DsfTestPlugin.failRequest(rm, REQUEST_FAILED, "Breakpoint already removed");
            return;
        }

        if (bpCtx instanceof BreakpointDMContext) {
            if ( fBreakpoints.remove(bpCtx) == null ) {
                DsfTestPlugin.failRequest(rm, INVALID_STATE, "Breakpoint does not exist");
            } else {
                getSession().dispatchEvent(new BreakpointsRemovedEvent((BreakpointDMContext)bpCtx), getProperties());
            }
            rm.done();
        } else {
            rm.setStatus(new Status(IStatus.ERROR, DsfTestPlugin.PLUGIN_ID, INTERNAL_ERROR, "Invalid breakpoint type", null ));
            rm.done();
        }
    }

    public void updateBreakpoint(final IBreakpointDMContext bpCtx, Map<String, Object> attributes, final RequestMonitor rm) {
        if (!fBreakpoints.containsKey(bpCtx)) {
            DsfTestPlugin.failRequest(rm, REQUEST_FAILED, "Breakpoint not installed");
            return;
        }

        for (String attribute : attributes.keySet()) {
            if (!DsfTestBreakpoint.ATTR_UPDATABLE.equals(attribute)) {
                DsfTestPlugin.failRequest(rm, REQUEST_FAILED, "Attribute cannot be updated");
                return;
            }
        }
        
        if (bpCtx instanceof BreakpointDMContext) {
            Map<String, Object> newAttrs = new HashMap<String, Object>(fBreakpoints.get(bpCtx).getAttributes());
            newAttrs.putAll(attributes);
            fBreakpoints.put((BreakpointDMContext)bpCtx, new BreakpointDMData(newAttrs));
            getSession().dispatchEvent(new BreakpointsRemovedEvent((BreakpointDMContext)bpCtx), getProperties());

        } else {
            DsfTestPlugin.failRequest(rm, INVALID_HANDLE, "Invalid breakpoint type");
        }
    }
}
