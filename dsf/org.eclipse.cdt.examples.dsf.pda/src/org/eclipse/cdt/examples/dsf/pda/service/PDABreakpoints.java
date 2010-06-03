/*******************************************************************************
 * Copyright (c) 2007, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.examples.dsf.pda.service;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.cdt.examples.dsf.pda.breakpoints.PDAWatchpoint;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAClearBreakpointCommand;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDACommandResult;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDASetBreakpointCommand;
import org.eclipse.cdt.examples.dsf.pda.service.commands.PDAWatchCommand;
import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.model.IBreakpoint;
import org.osgi.framework.BundleContext;

/**
 * Initial breakpoint service implementation.
 * Implements the IBreakpoints interface.
 */
public class PDABreakpoints extends AbstractDsfService implements IBreakpoints
{
    /**
     * Context representing a PDA line breakpoint.  In PDA debugger, since there is only 
     * one file being debugged at a time, a breakpoint is uniquely identified using the 
     * line number only.
     */
    @Immutable
    private static class BreakpointDMContext extends AbstractDMContext implements IBreakpointDMContext {

        final Integer fLine;

        public BreakpointDMContext(String sessionId, PDAVirtualMachineDMContext commandControlCtx, Integer line) {
            super(sessionId, new IDMContext[] { commandControlCtx });
            fLine = line;
        }

        @Override
        public boolean equals(Object obj) {
            return baseEquals(obj) && (fLine.equals(((BreakpointDMContext) obj).fLine));
        }

        @Override
        public int hashCode() {
            return baseHashCode() + fLine.hashCode();
        }

        @Override
        public String toString() {
            return baseToString() + ".breakpoint(" + fLine + ")";  //$NON-NLS-1$//$NON-NLS-2$*/
        }
    }

    /**
     * Context representing a watch point.  In PDA debugger, a watchpoint is 
     * uniquely identified using the function and variable.
     */
    @Immutable
    private static class WatchpointDMContext extends AbstractDMContext implements IBreakpointDMContext {
        final String fFunction;
        final String fVariable; 

        public WatchpointDMContext(String sessionId, PDAVirtualMachineDMContext commandControlCtx, String function, 
            String variable) 
        {
            super(sessionId, new IDMContext[] { commandControlCtx });
            fFunction = function;
            fVariable = variable;
        }

        @Override
        public boolean equals(Object obj) {
            if (baseEquals(obj)) {
                WatchpointDMContext watchpointCtx = (WatchpointDMContext)obj;
                return fFunction.equals(watchpointCtx.fFunction) && fVariable.equals(watchpointCtx.fVariable);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return baseHashCode() + fFunction.hashCode() + fVariable.hashCode();
        }

        @Override
        public String toString() {
            return baseToString() + ".watchpoint(" + fFunction + "::" + fVariable + ")";
        }
    }

    // Attribute names
    public static final String ATTR_BREAKPOINT_TYPE = PDAPlugin.PLUGIN_ID + ".pdaBreakpointType";      //$NON-NLS-1$
    public static final String PDA_LINE_BREAKPOINT = "breakpoint";                 //$NON-NLS-1$
    public static final String PDA_WATCHPOINT = "watchpoint";                 //$NON-NLS-1$
    public static final String ATTR_PROGRAM_PATH = PDAPlugin.PLUGIN_ID + ".pdaProgramPath";      //$NON-NLS-1$

    // Services
    private PDACommandControl fCommandControl;

    // Breakpoints currently installed
    private Set<IBreakpointDMContext> fBreakpoints = new HashSet<IBreakpointDMContext>();

    /**
     * The service constructor
     * 
     * @param session The debugging session this service belongs to.
     */
    public PDABreakpoints(DsfSession session) {
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
        // Get the services references
        fCommandControl = getServicesTracker().getService(PDACommandControl.class);

        // Register this service
        register(new String[] { IBreakpoints.class.getName(), PDABreakpoints.class.getName() },
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
        return PDAPlugin.getBundleContext();
    }

    public void getBreakpoints(final IBreakpointsTargetDMContext context, final DataRequestMonitor<IBreakpointDMContext[]> rm) {
        // Validate the context
        if (!fCommandControl.getContext().equals(context)) {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid breakpoints target context");
            return;
        }

        rm.setData(fBreakpoints.toArray(new IBreakpointDMContext[fBreakpoints.size()]));
        rm.done();
    }

    public void getBreakpointDMData(IBreakpointDMContext dmc, DataRequestMonitor<IBreakpointDMData> rm) {
        PDAPlugin.failRequest(rm, NOT_SUPPORTED, "Retrieving breakpoint data is not supported");
    }

    public void insertBreakpoint(IBreakpointsTargetDMContext context, Map<String, Object> attributes, 
        DataRequestMonitor<IBreakpointDMContext> rm) 
    {
        Boolean enabled = (Boolean)attributes.get(IBreakpoint.ENABLED);
        if (enabled != null && !enabled.booleanValue()) {
            // If the breakpoint is disabled, just fail the request. 
            PDAPlugin.failRequest(rm, REQUEST_FAILED, "Breakpoint is disabled");
        } else {
            String type = (String) attributes.get(ATTR_BREAKPOINT_TYPE);

            if (PDA_LINE_BREAKPOINT.equals(type)) {
                // Retrieve the PDA program context from the context given in the 
                // argument.  This service is typically only called by the 
                // breakpoints mediator, which was called with the program context
                // in the services initialization sequence.  So checking if 
                // programCtx != null is mostly a formality.
                PDAVirtualMachineDMContext programCtx = DMContexts.getAncestorOfType(context, PDAVirtualMachineDMContext.class);
                if (programCtx != null) {
                    doInsertBreakpoint(programCtx, attributes, rm);
                } else {
                    PDAPlugin.failRequest(rm, INVALID_HANDLE, "Unknown breakpoint type");
                }
            }
            else if (PDA_WATCHPOINT.equals(type)) {
                doInsertWatchpoint(attributes, rm);
            }
            else {
                PDAPlugin.failRequest(rm, REQUEST_FAILED, "Unknown breakpoint type");
            }
        }
    }

    private void doInsertBreakpoint(PDAVirtualMachineDMContext programCtx, final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> rm) 
    {
        // Compare the program path in the breakpoint with the path in the PDA 
        // program context. Only insert the breakpoint if the program matches. 
        String program = (String)attributes.get(ATTR_PROGRAM_PATH);
        if (!programCtx.getProgram().equals(program)) {
            PDAPlugin.failRequest(rm, REQUEST_FAILED, "Invalid file name");
            return;
        }

        // Retrieve the line.
        Integer line = (Integer)attributes.get(IMarker.LINE_NUMBER);
        if (line == null) {
            PDAPlugin.failRequest(rm, REQUEST_FAILED, "No breakpoint line specified");
            return;
        }

        // Create a new breakpoint context object and check that it's not 
        // installed already. PDA can only track a single breakpoint at a 
        // given line, attempting to set the second breakpoint should fail.
        final BreakpointDMContext breakpointCtx = 
            new BreakpointDMContext(getSession().getId(), fCommandControl.getContext(), line);
        if (fBreakpoints.contains(breakpointCtx)) {
            PDAPlugin.failRequest(rm, REQUEST_FAILED, "Breakpoint already set");
            return;
        }

        // Add the new breakpoint context to the list of known breakpoints.  
        // Adding it here, before the set command is completed will prevent 
        // a possibility of a second breakpoint being installed in the same 
        // location while this breakpoint is being processed.  It will also
        // allow the breakpoint to be removed or updated even while it is 
        // still being processed here.
        fBreakpoints.add(breakpointCtx);
        fCommandControl.queueCommand(
            new PDASetBreakpointCommand(fCommandControl.getContext(), line, false), 
            new DataRequestMonitor<PDACommandResult>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    rm.setData(breakpointCtx);
                    rm.done();
                }

                @Override
                protected void handleFailure() {
                    // If inserting of the breakpoint failed, remove it from
                    // the set of installed breakpoints.
                    fBreakpoints.remove(breakpointCtx);
                    super.handleFailure();
                }
            });
    }

    private void doInsertWatchpoint(final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> rm) 
    {
        String function = (String)attributes.get(PDAWatchpoint.FUNCTION_NAME);
        if (function == null) {
            PDAPlugin.failRequest(rm, REQUEST_FAILED, "No function specified");
            return;
        }

        String variable = (String)attributes.get(PDAWatchpoint.VAR_NAME);
        if (variable == null) {
            PDAPlugin.failRequest(rm, REQUEST_FAILED, "No variable specified");
            return;
        }

        Boolean isAccess = (Boolean)attributes.get(PDAWatchpoint.ACCESS);
        isAccess = isAccess != null ? isAccess : Boolean.FALSE; 

        Boolean isModification = (Boolean)attributes.get(PDAWatchpoint.MODIFICATION);
        isModification = isModification != null ? isModification : Boolean.FALSE; 

        // Create a new watchpoint context object and check that it's not 
        // installed already. PDA can only track a single watchpoint for a given
        // function::variable, attempting to set the second breakpoint should fail.
        final WatchpointDMContext watchpointCtx = 
            new WatchpointDMContext(getSession().getId(), fCommandControl.getContext(), function, variable);
        if (fBreakpoints.contains(watchpointCtx)) {
            PDAPlugin.failRequest(rm, REQUEST_FAILED, "Watchpoint already set");
            return;
        }

        // Determine the watch operation to perform.
        PDAWatchCommand.WatchOperation watchOperation = PDAWatchCommand.WatchOperation.NONE;
        if (isAccess && isModification) {
            watchOperation = PDAWatchCommand.WatchOperation.BOTH;
        } else if (isAccess) {
            watchOperation = PDAWatchCommand.WatchOperation.READ;
        } else if (isModification) {
            watchOperation = PDAWatchCommand.WatchOperation.WRITE;
        }

        // Add the new breakpoint context to the list of known breakpoints.  
        // Adding it here, before the set command is completed will prevent 
        // a possibility of a second breakpoint being installed in the same 
        // location while this breakpoint is being processed.  It will also
        // allow the breakpoint to be removed or updated even while it is 
        // still being processed here.
        fBreakpoints.add(watchpointCtx);
        fCommandControl.queueCommand(
            new PDAWatchCommand(fCommandControl.getContext(), function, variable, watchOperation), 
            new DataRequestMonitor<PDACommandResult>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    rm.setData(watchpointCtx);
                    rm.done();
                }

                @Override
                protected void handleFailure() {
                    // Since the command failed, we need to remove the breakpoint from 
                    // the existing breakpoint set.
                    fBreakpoints.remove(watchpointCtx);
                    super.handleFailure();
                }
            });
    }

    public void removeBreakpoint(IBreakpointDMContext bpCtx, RequestMonitor rm) {
        if (!fBreakpoints.contains(bpCtx)) {
            PDAPlugin.failRequest(rm, REQUEST_FAILED, "Breakpoint already removed");
            return;
        }

        if (bpCtx instanceof BreakpointDMContext) {
            doRemoveBreakpoint((BreakpointDMContext)bpCtx, rm);
        } else if (bpCtx instanceof WatchpointDMContext) {
            doRemoveWatchpoint((WatchpointDMContext)bpCtx, rm);
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid breakpoint");
        }
    }

    private void doRemoveBreakpoint(BreakpointDMContext bpCtx, RequestMonitor rm) {
        // Remove the breakpoint from the table right away, so that even when 
        // the remove is being processed, a new breakpoint can be created at the same 
        // location.
        fBreakpoints.remove(bpCtx);

        fCommandControl.queueCommand(
            new PDAClearBreakpointCommand(fCommandControl.getContext(), bpCtx.fLine), 
            new DataRequestMonitor<PDACommandResult>(getExecutor(), rm));        
    }

    private void doRemoveWatchpoint(WatchpointDMContext bpCtx, RequestMonitor rm) {
        fBreakpoints.remove(bpCtx);

        // Watchpoints are cleared using the same command, but with a "no watch" operation
        fCommandControl.queueCommand(
            new PDAWatchCommand(
                fCommandControl.getContext(), bpCtx.fFunction, bpCtx.fVariable, PDAWatchCommand.WatchOperation.NONE), 
                new DataRequestMonitor<PDACommandResult>(getExecutor(), rm));        
    }

    public void updateBreakpoint(final IBreakpointDMContext bpCtx, Map<String, Object> attributes, final RequestMonitor rm) {
        if (!fBreakpoints.contains(bpCtx)) {
            PDAPlugin.failRequest(rm, REQUEST_FAILED, "Breakpoint not installed");
            return;
        }

        if (bpCtx instanceof BreakpointDMContext) {
            PDAPlugin.failRequest(rm, NOT_SUPPORTED, "Modifying PDA breakpoints is not supported");
        } else if (bpCtx instanceof WatchpointDMContext) {
            WatchpointDMContext wpCtx = (WatchpointDMContext)bpCtx;
            if (!wpCtx.fFunction.equals(attributes.get(PDAWatchpoint.FUNCTION_NAME)) || 
                !wpCtx.fVariable.equals(attributes.get(PDAWatchpoint.VAR_NAME)) )
            {
                PDAPlugin.failRequest(rm, REQUEST_FAILED, "Cannot modify watchpoint function or variable");
                return;
            }
            
            // PDA debugger can only track one watchpoint in the same location, 
            // so we can simply remove the existing context from the set and 
            // call insert again.  
            fBreakpoints.remove(bpCtx);
            doInsertWatchpoint(
                attributes, 
                new DataRequestMonitor<IBreakpointDMContext>(getExecutor(), rm) {
                    @Override
                    protected void handleSuccess() {
                        // The inserted watchpoint context will equal the 
                        // current context.
                        assert bpCtx.equals(getData());
                        rm.done();
                    }
                });
        } else {
            PDAPlugin.failRequest(rm, INVALID_HANDLE, "Invalid breakpoint");
        }
    }
}
