/*******************************************************************************
 * Copyright (c) 2007 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.dd.examples.pda.service.breakpoints;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.debug.service.IBreakpoints;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.service.IDsfService;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.dd.examples.pda.breakpoints.PDAWatchpoint;
import org.eclipse.dd.examples.pda.service.command.PDACommandControl;
import org.eclipse.dd.examples.pda.service.command.PDACommandResult;
import org.eclipse.dd.examples.pda.service.command.commands.PDAClearBreakpointCommand;
import org.eclipse.dd.examples.pda.service.command.commands.PDASetBreakpointCommand;
import org.eclipse.dd.examples.pda.service.command.commands.PDAWatchCommand;
import org.osgi.framework.BundleContext;

/**
 * Initial breakpoint service implementation.
 * Implements the IBreakpoints interface.
 */
public class PDABreakpoints extends AbstractDsfService implements IBreakpoints
{
    public static final String ATTR_BREAKPOINT_TYPE = PDAPlugin.PLUGIN_ID + ".pdaBreakpointType";      //$NON-NLS-1$
    public static final String PDA_LINE_BREAKPOINT = "breakpoint";                 //$NON-NLS-1$
    public static final String PDA_WATCHPOINT = "watchpoint";                 //$NON-NLS-1$
    public static final String ATTR_PROGRAM_PATH = PDAPlugin.PLUGIN_ID + ".pdaProgramPath";      //$NON-NLS-1$
    
    private final String fProgram;
    
    // Services
    private PDACommandControl fCommandControl;

    // Service breakpoints tracking
    // The breakpoints are stored per context and keyed on the back-end breakpoint reference
    private Set<IBreakpointDMContext> fBreakpoints = new HashSet<IBreakpointDMContext>();

	/**
	 * The service constructor
	 * 
	 * @param session The debugging session this service belongs to.
	 * @param program The name of the program of this PDA debugger.
	 */
	public PDABreakpoints(DsfSession session, String program) {
		super(session);
		fProgram = program;
	}

	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new RequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleOK() {
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
		if (!fCommandControl.getDMContext().equals(context)) {
       		PDAPlugin.failRequest(rm, IDsfService.INVALID_HANDLE, "Invalid breakpoints target context");
			return;
		}

		rm.setData(fBreakpoints.toArray(new IBreakpointDMContext[fBreakpoints.size()]));
		rm.done();
	}

	public void getBreakpointDMData(IBreakpointDMContext dmc, DataRequestMonitor<IBreakpointDMData> rm) {
   		PDAPlugin.failRequest(rm, IDsfService.NOT_SUPPORTED, "Retrieving breakpoint data is not supported");
	}

	public void insertBreakpoint(IBreakpointsTargetDMContext context, Map<String, Object> attributes, 
	    DataRequestMonitor<IBreakpointDMContext> rm) 
	{
		String type = (String) attributes.get(ATTR_BREAKPOINT_TYPE);

		if (PDA_LINE_BREAKPOINT.equals(type)) {
		    doInsertBreakpoint(attributes, rm);
		}
		else if (PDA_WATCHPOINT.equals(type)) {
		    doInsertWatchpoint(attributes, rm);
		}
		else {
            PDAPlugin.failRequest(rm, IDsfService.REQUEST_FAILED, "Unknown breakpoint type");
		}
	}

	private void doInsertBreakpoint(final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> rm) 
	{
        String program = (String)attributes.get(ATTR_PROGRAM_PATH);
		if (!fProgram.equals(program)) {
		    PDAPlugin.failRequest(rm, IDsfService.REQUEST_FAILED, "Invalid file name");
            return;
		}
		
		Integer line = (Integer)attributes.get(IMarker.LINE_NUMBER);
        if (line == null) {
            PDAPlugin.failRequest(rm, IDsfService.REQUEST_FAILED, "No breakpoint line specified");
            return;
        }

        final BreakpointDMContext breakpointCtx = 
            new BreakpointDMContext(getSession().getId(), fCommandControl.getDMContext(), line);
        if (fBreakpoints.contains(breakpointCtx)) {
            PDAPlugin.failRequest(rm, IDsfService.REQUEST_FAILED, "Breakpoint already set");
            return;
        }
        
        fBreakpoints.add(breakpointCtx);
        fCommandControl.queueCommand(
            new PDASetBreakpointCommand(fCommandControl.getDMContext(), line), 
            new DataRequestMonitor<PDACommandResult>(getExecutor(), rm) {
                @Override
                protected void handleOK() {
                    rm.setData(breakpointCtx);
                    rm.done();
                }
                
                @Override
                protected void handleErrorOrCancel() {
                    fBreakpoints.remove(breakpointCtx);
                    super.handleErrorOrCancel();
                }
            });
	}

	private void doInsertWatchpoint(final Map<String, Object> attributes, final DataRequestMonitor<IBreakpointDMContext> rm) 
	{
        String function = (String)attributes.get(PDAWatchpoint.FUNCTION_NAME);
        if (function == null) {
            PDAPlugin.failRequest(rm, IDsfService.REQUEST_FAILED, "No function specified");
            return;
        }

        String variable = (String)attributes.get(PDAWatchpoint.VAR_NAME);
        if (variable == null) {
            PDAPlugin.failRequest(rm, IDsfService.REQUEST_FAILED, "No variable specified");
            return;
        }

        Boolean isAccess = (Boolean)attributes.get(PDAWatchpoint.ACCESS);
        isAccess = isAccess != null ? isAccess : Boolean.FALSE; 

        Boolean isModification = (Boolean)attributes.get(PDAWatchpoint.MODIFICATION);
        isModification = isModification != null ? isModification : Boolean.FALSE; 

        final WatchpointDMContext watchpointCtx = 
            new WatchpointDMContext(getSession().getId(), fCommandControl.getDMContext(), function, variable);
        if (fBreakpoints.contains(watchpointCtx)) {
            PDAPlugin.failRequest(rm, IDsfService.REQUEST_FAILED, "Watchpoint already set");
            return;
        }
        
        PDAWatchCommand.WatchOperation watchOperation = PDAWatchCommand.WatchOperation.NONE;
        if (isAccess && isModification) {
            watchOperation = PDAWatchCommand.WatchOperation.BOTH;
        } else if (isAccess) {
            watchOperation = PDAWatchCommand.WatchOperation.READ;
        } else if (isModification) {
            watchOperation = PDAWatchCommand.WatchOperation.WRITE;
        }
        
        fBreakpoints.add(watchpointCtx);
        fCommandControl.queueCommand(
            new PDAWatchCommand(fCommandControl.getDMContext(), function, variable, watchOperation), 
            new DataRequestMonitor<PDACommandResult>(getExecutor(), rm) {
                @Override
                protected void handleOK() {
                    rm.setData(watchpointCtx);
                    rm.done();
                }
                
                @Override
                protected void handleErrorOrCancel() {
                    // Since the command failed, we need to remove the breakpoint from 
                    // the existing breakpoint set.
                    fBreakpoints.remove(watchpointCtx);
                    super.handleErrorOrCancel();
                }
            });
	}

	public void removeBreakpoint(IBreakpointDMContext bpCtx, RequestMonitor rm) {
	    if (!fBreakpoints.contains(bpCtx)) {
            PDAPlugin.failRequest(rm, IDsfService.REQUEST_FAILED, "Breakpoint already removed");
            return;
	    }
	    
		if (bpCtx instanceof BreakpointDMContext) {
		    doRemoveBreakpoint((BreakpointDMContext)bpCtx, rm);
		} else if (bpCtx instanceof WatchpointDMContext) {
            doRemoveWatchpoint((WatchpointDMContext)bpCtx, rm);
		} else {
       		PDAPlugin.failRequest(rm, IDsfService.INVALID_HANDLE, "Invalid breakpoint");
		}
	}

    private void doRemoveBreakpoint(BreakpointDMContext bpCtx, RequestMonitor rm) {
        // Remove the breakpoint from the table right away, so that even when 
        // the remove is being processed, a new breakpoint can be created at the same 
        // location.
        fBreakpoints.remove(bpCtx);
        
        fCommandControl.queueCommand(
            new PDAClearBreakpointCommand(fCommandControl.getDMContext(), bpCtx.fLine), 
            new DataRequestMonitor<PDACommandResult>(getExecutor(), rm));        
    }

    private void doRemoveWatchpoint(WatchpointDMContext bpCtx, RequestMonitor rm) {
        fBreakpoints.remove(bpCtx);
        
        // Watchpoints are cleared using the same command, but with a "no watch" operation
        fCommandControl.queueCommand(
            new PDAWatchCommand(fCommandControl.getDMContext(), bpCtx.fFunction, bpCtx.fVariable, 
                PDAWatchCommand.WatchOperation.NONE), 
            new DataRequestMonitor<PDACommandResult>(getExecutor(), rm));        
    }

	public void updateBreakpoint(IBreakpointDMContext dmc, Map<String, Object> properties, RequestMonitor rm) {
   		PDAPlugin.failRequest(rm, IDsfService.NOT_SUPPORTED, "Modifying PDA breakpoints is not supported");
	}
}
