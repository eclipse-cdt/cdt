/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLIRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 4.8
 */
public class GDBRunControl_7_10 extends GDBRunControl_7_6 implements IReverseRunControl2 {

    private IMICommandControl fCommandControl;
    private CommandFactory fCommandFactory;

    private String fReverseTraceMethod = CLIRecord.STOP_TRACE; // default: no trace

    public GDBRunControl_7_10(DsfSession session) {
        super(session);
    }

    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(
            new ImmediateRequestMonitor(requestMonitor) {
                @Override
                public void handleSuccess() {
                    doInitialize(requestMonitor);
                }});
    }

    private void doInitialize(final RequestMonitor requestMonitor) {

        fCommandControl = getServicesTracker().getService(IMICommandControl.class);
        fCommandFactory = fCommandControl.getCommandFactory();

        if (fCommandControl == null) {
            requestMonitor.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Service is not available")); //$NON-NLS-1$
            return;
        }

        fCommandControl.addEventListener(this);

        register(new String[]{IRunControl.class.getName(),
                              IRunControl2.class.getName(),
                              IMIRunControl.class.getName(),
                              MIRunControl.class.getName(),
                              GDBRunControl_7_0.class.getName(),
                              GDBRunControl_7_6.class.getName(),
                              IReverseRunControl.class.getName(),
                              IReverseRunControl2.class.getName()},
                 new Hashtable<String,String>());
        requestMonitor.done();
    }

    /**
     * @since 5.0
     */
    @Override
    public void getReverseTraceMethod(ICommandControlDMContext context, DataRequestMonitor<String> rm) {
        rm.setData(fReverseTraceMethod);
        rm.done();
    }

    @Override
    public void enableReverseMode(final ICommandControlDMContext context,final String traceMethod, final RequestMonitor rm) {
        if (!fReverseSupported) {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Reverse mode is not supported.", null)); //$NON-NLS-1$
            rm.done();
            return;
        }

        if (fReverseTraceMethod == traceMethod) {
            rm.done();
            return;
        }

        if(fReverseTraceMethod == CLIRecord.STOP_TRACE || traceMethod == CLIRecord.STOP_TRACE) {
            getConnection().queueCommand(
                fCommandFactory.createCLIRecord(context, traceMethod),
                new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
                    @Override
                    public void handleSuccess() {
                        boolean enabled = false;
                        fReverseTraceMethod = traceMethod;
                        if(fReverseTraceMethod != CLIRecord.STOP_TRACE)
                            enabled = true;
                            setReverseModeEnabled(enabled );
                            rm.done();
                        }
                        @Override
                        public void handleFailure() {
                            //rm.cancel();
                            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Trace method could not be selected", null)); //$NON-NLS-1$
                            rm.done();
                        }
                    });
            return;
        }
            getConnection().queueCommand(
                    fCommandFactory.createCLIRecord(context, CLIRecord.STOP_TRACE),
                    new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
                        @Override
                        public void handleSuccess() {
                            setReverseModeEnabled(false);
                            getConnection().queueCommand(
                                    fCommandFactory.createCLIRecord(context, traceMethod),
                                    new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
                                        @Override
                                        public void handleSuccess() {
                                            fReverseTraceMethod = traceMethod;
                                            setReverseModeEnabled(true);
                                            rm.done();
                                        }
                                        @Override
                                        public void handleFailure() {
                                            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_STATE, "Trace method could not be selected", null)); //$NON-NLS-1$
                                            setReverseModeEnabled(false);
                                            fReverseTraceMethod = CLIRecord.STOP_TRACE;
                                            rm.done();
                                        }
                                    });
                        }
                    });
            return;
        }
}
