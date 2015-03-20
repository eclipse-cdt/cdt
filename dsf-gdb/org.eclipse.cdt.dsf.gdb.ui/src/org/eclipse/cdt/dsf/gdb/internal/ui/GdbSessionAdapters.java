/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.IConnectHandler;
import org.eclipse.cdt.debug.core.model.IDebugNewExecutableHandler;
import org.eclipse.cdt.debug.core.model.IResumeWithoutSignalHandler;
import org.eclipse.cdt.debug.core.model.IReverseResumeHandler;
import org.eclipse.cdt.debug.core.model.IReverseStepIntoHandler;
import org.eclipse.cdt.debug.core.model.IReverseStepOverHandler;
import org.eclipse.cdt.debug.core.model.IReverseToggleHandler;
import org.eclipse.cdt.debug.core.model.ISaveTraceDataHandler;
import org.eclipse.cdt.debug.core.model.IStartTracingHandler;
import org.eclipse.cdt.debug.core.model.IStepIntoSelectionHandler;
import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.debug.core.model.IStopTracingHandler;
import org.eclipse.cdt.debug.core.model.IUncallHandler;
import org.eclipse.cdt.debug.ui.IPinProvider;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfResumeCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepIntoCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepIntoSelectionCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepOverCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepReturnCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfSuspendCommand;
import org.eclipse.cdt.dsf.debug.ui.sourcelookup.DsfSourceDisplayAdapter;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.DefaultRefreshAllTarget;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.IRefreshAllTarget;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.DefaultDsfModelSelectionPolicyFactory;
import org.eclipse.cdt.dsf.gdb.internal.commands.ISelectNextTraceRecordHandler;
import org.eclipse.cdt.dsf.gdb.internal.commands.ISelectPrevTraceRecordHandler;
import org.eclipse.cdt.dsf.gdb.internal.ui.actions.DsfTerminateCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.actions.GdbDisconnectCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.actions.GdbRestartCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.actions.GdbSteppingModeTarget;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.GdbConnectCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.GdbDebugNewExecutableCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.GdbResumeWithoutSignalCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.GdbReverseResumeCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.GdbReverseStepIntoCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.GdbReverseStepOverCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.GdbReverseToggleCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.GdbSaveTraceDataCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.GdbSelectNextTraceRecordCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.GdbSelectPrevTraceRecordCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.GdbStartTracingCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.GdbStopTracingCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.commands.GdbUncallCommand;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.GdbViewModelAdapter;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.core.commands.IRestartHandler;
import org.eclipse.debug.core.commands.IResumeHandler;
import org.eclipse.debug.core.commands.IStepIntoHandler;
import org.eclipse.debug.core.commands.IStepOverHandler;
import org.eclipse.debug.core.commands.IStepReturnHandler;
import org.eclipse.debug.core.commands.ISuspendHandler;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;

@ThreadSafe
public class GdbSessionAdapters {
        private ILaunch fLaunch;
        private DsfSession fSession;
        
        protected GdbViewModelAdapter fViewModelAdapter;
        protected DsfSourceDisplayAdapter fSourceDisplayAdapter;
        protected DsfStepIntoCommand fStepIntoCommand;
        protected DsfStepIntoSelectionCommand fStepIntoSelectionCommand;
        protected GdbReverseStepIntoCommand fReverseStepIntoCommand;
        protected DsfStepOverCommand fStepOverCommand;
        protected GdbReverseStepOverCommand fReverseStepOverCommand;
        protected DsfStepReturnCommand fStepReturnCommand;
        protected GdbUncallCommand fUncallCommand;
        protected DsfSuspendCommand fSuspendCommand;
        protected DsfResumeCommand fResumeCommand;
        protected GdbReverseResumeCommand fReverseResumeCommand;
        protected GdbResumeWithoutSignalCommand fResumeWithoutSignalCommand;
        protected GdbRestartCommand fRestartCommand;
        protected DsfTerminateCommand fTerminateCommand;
        protected GdbDebugNewExecutableCommand fDebugNewExecutableCommand;
        protected GdbConnectCommand fConnectCommand;
        protected GdbDisconnectCommand fDisconnectCommand;
        protected IDebugModelProvider fDebugModelProvider;
        protected GdbSuspendTrigger fSuspendTrigger;
		protected GdbSteppingModeTarget fSteppingModeTarget;
		protected IModelSelectionPolicyFactory fModelSelectionPolicyFactory;
		protected SteppingController fSteppingController;
        protected DefaultRefreshAllTarget fRefreshAllTarget;
        protected GdbReverseToggleCommand fReverseToggleTarget;
        protected GdbStartTracingCommand fStartTracingTarget;
        protected GdbStopTracingCommand fStopTracingTarget;
        protected GdbSaveTraceDataCommand fSaveTraceDataTarget;
        protected GdbSelectNextTraceRecordCommand fSelectNextRecordTarget;
        protected GdbSelectPrevTraceRecordCommand fSelectPrevRecordTarget;
        protected GdbDebugTextHover fDebugTextHover;
        protected GdbPinProvider fPinProvider;
        
        public GdbSessionAdapters(ILaunch launch, DsfSession session) {
            fLaunch = launch;
            fSession = session;
            
            // register stepping controller
            fSteppingController = new SteppingController(session);
            session.registerModelAdapter(SteppingController.class, fSteppingController);

            fViewModelAdapter = new GdbViewModelAdapter(session, fSteppingController);
            session.registerModelAdapter(IViewerInputProvider.class, fViewModelAdapter);
            
            if (launch.getSourceLocator() instanceof ISourceLookupDirector) {
                fSourceDisplayAdapter = new DsfSourceDisplayAdapter(session, (ISourceLookupDirector)launch.getSourceLocator(), fSteppingController);
            } else {
                fSourceDisplayAdapter = null;
            }
            session.registerModelAdapter(ISourceDisplay.class, fSourceDisplayAdapter);
            
            fSteppingModeTarget = new GdbSteppingModeTarget(session);
            fStepIntoCommand = new DsfStepIntoCommand(session, fSteppingModeTarget);
            fStepIntoSelectionCommand = new DsfStepIntoSelectionCommand(session);
            fReverseStepIntoCommand = new GdbReverseStepIntoCommand(session, fSteppingModeTarget);
            fStepOverCommand = new DsfStepOverCommand(session, fSteppingModeTarget);
            fReverseStepOverCommand = new GdbReverseStepOverCommand(session, fSteppingModeTarget);
            fStepReturnCommand = new DsfStepReturnCommand(session);
            fUncallCommand = new GdbUncallCommand(session, fSteppingModeTarget);
            fSuspendCommand = new DsfSuspendCommand(session);
            fResumeCommand = new DsfResumeCommand(session);
            fReverseResumeCommand = new GdbReverseResumeCommand(session);
            fResumeWithoutSignalCommand = new GdbResumeWithoutSignalCommand(session);
            fRestartCommand = new GdbRestartCommand(session, fLaunch);
            fTerminateCommand = new DsfTerminateCommand(session);
            fDebugNewExecutableCommand = new GdbDebugNewExecutableCommand(session, fLaunch);
            fConnectCommand = new GdbConnectCommand(session, fLaunch);
            fDisconnectCommand = new GdbDisconnectCommand(session);
            fSuspendTrigger = new GdbSuspendTrigger(session, fLaunch);
            fModelSelectionPolicyFactory = new DefaultDsfModelSelectionPolicyFactory();
            fRefreshAllTarget = new DefaultRefreshAllTarget();
            fReverseToggleTarget = new GdbReverseToggleCommand(session);
            fStartTracingTarget = new GdbStartTracingCommand(session);
            fStopTracingTarget = new GdbStopTracingCommand(session);
            fSaveTraceDataTarget = new GdbSaveTraceDataCommand(session);
            fSelectNextRecordTarget = new GdbSelectNextTraceRecordCommand(session);
            fSelectPrevRecordTarget = new GdbSelectPrevTraceRecordCommand(session);
            fPinProvider = new GdbPinProvider(session);

            session.registerModelAdapter(ISteppingModeTarget.class, fSteppingModeTarget);
            session.registerModelAdapter(IStepIntoHandler.class, fStepIntoCommand);
            session.registerModelAdapter(IStepIntoSelectionHandler.class, fStepIntoSelectionCommand);
            session.registerModelAdapter(IReverseStepIntoHandler.class, fReverseStepIntoCommand);
            session.registerModelAdapter(IStepOverHandler.class, fStepOverCommand);
            session.registerModelAdapter(IReverseStepOverHandler.class, fReverseStepOverCommand);
            session.registerModelAdapter(IStepReturnHandler.class, fStepReturnCommand);
            session.registerModelAdapter(IUncallHandler.class, fUncallCommand);
            session.registerModelAdapter(ISuspendHandler.class, fSuspendCommand);
            session.registerModelAdapter(IResumeHandler.class, fResumeCommand);
            session.registerModelAdapter(IReverseResumeHandler.class, fReverseResumeCommand);
            session.registerModelAdapter(IResumeWithoutSignalHandler.class, fResumeWithoutSignalCommand);
            session.registerModelAdapter(IRestartHandler.class, fRestartCommand);
            session.registerModelAdapter(ITerminateHandler.class, fTerminateCommand);
            session.registerModelAdapter(IConnectHandler.class, fConnectCommand);
            session.registerModelAdapter(IDebugNewExecutableHandler.class, fDebugNewExecutableCommand);
            session.registerModelAdapter(IDisconnectHandler.class, fDisconnectCommand);
            session.registerModelAdapter(IModelSelectionPolicyFactory.class, fModelSelectionPolicyFactory);
            session.registerModelAdapter(IRefreshAllTarget.class, fRefreshAllTarget);
            session.registerModelAdapter(IReverseToggleHandler.class, fReverseToggleTarget);
            session.registerModelAdapter(IStartTracingHandler.class, fStartTracingTarget);
            session.registerModelAdapter(IStopTracingHandler.class, fStopTracingTarget);
            session.registerModelAdapter(ISaveTraceDataHandler.class, fSaveTraceDataTarget);
            session.registerModelAdapter(ISelectNextTraceRecordHandler.class, fSelectNextRecordTarget);
            session.registerModelAdapter(ISelectPrevTraceRecordHandler.class, fSelectPrevRecordTarget);
            session.registerModelAdapter(IPinProvider.class, fPinProvider);

            fDebugModelProvider = new IDebugModelProvider() {
                // @see org.eclipse.debug.core.model.IDebugModelProvider#getModelIdentifiers()
                @Override
                public String[] getModelIdentifiers() {
                    return new String[] { GdbLaunchDelegate.GDB_DEBUG_MODEL_ID, ICBreakpoint.C_BREAKPOINTS_DEBUG_MODEL_ID, "org.eclipse.cdt.gdb" }; //$NON-NLS-1$
                }
            };
            session.registerModelAdapter(IDebugModelProvider.class, fDebugModelProvider);

            /*
             * Registering the launch as an adapter, ensures that this launch,
             * and debug model ID will be associated with all DMContexts from this
             * session.
             */
            session.registerModelAdapter(ILaunch.class, fLaunch);
            
            /*
             * Register debug hover adapter (bug 309001).
             */
            fDebugTextHover = new GdbDebugTextHover();
            session.registerModelAdapter(ICEditorTextHover.class, fDebugTextHover);
        }
        
        public void dispose() {
            fViewModelAdapter.dispose();
            fSession.unregisterModelAdapter(IViewerInputProvider.class);

            fSession.unregisterModelAdapter(ISourceDisplay.class);
            if (fSourceDisplayAdapter != null) fSourceDisplayAdapter.dispose();

            fSession.unregisterModelAdapter(SteppingController.class);
            fSteppingController.dispose();

            fSession.unregisterModelAdapter(ISteppingModeTarget.class);
            fSession.unregisterModelAdapter(IStepIntoHandler.class);
            fSession.unregisterModelAdapter(IStepIntoSelectionHandler.class);
            fSession.unregisterModelAdapter(IReverseStepIntoHandler.class);
            fSession.unregisterModelAdapter(IStepOverHandler.class);
            fSession.unregisterModelAdapter(IReverseStepOverHandler.class);
            fSession.unregisterModelAdapter(IStepReturnHandler.class);
            fSession.unregisterModelAdapter(IUncallHandler.class);
            fSession.unregisterModelAdapter(ISuspendHandler.class);
            fSession.unregisterModelAdapter(IResumeHandler.class);
            fSession.unregisterModelAdapter(IReverseResumeHandler.class);
            fSession.unregisterModelAdapter(IResumeWithoutSignalHandler.class);
            fSession.unregisterModelAdapter(IRestartHandler.class);
            fSession.unregisterModelAdapter(ITerminateHandler.class);
            fSession.unregisterModelAdapter(IConnectHandler.class);
            fSession.unregisterModelAdapter(IDebugNewExecutableHandler.class);
            fSession.unregisterModelAdapter(IDisconnectHandler.class);
            fSession.unregisterModelAdapter(IModelSelectionPolicyFactory.class);
            fSession.unregisterModelAdapter(IRefreshAllTarget.class);
            fSession.unregisterModelAdapter(IReverseToggleHandler.class);
            fSession.unregisterModelAdapter(IStartTracingHandler.class);
            fSession.unregisterModelAdapter(IStopTracingHandler.class);
            fSession.unregisterModelAdapter(ISaveTraceDataHandler.class);
            fSession.unregisterModelAdapter(ISelectNextTraceRecordHandler.class);
            fSession.unregisterModelAdapter(ISelectPrevTraceRecordHandler.class);
            fSession.unregisterModelAdapter(IPinProvider.class);
            
            fSession.unregisterModelAdapter(IDebugModelProvider.class);
            fSession.unregisterModelAdapter(ILaunch.class);

            fSession.unregisterModelAdapter(ICEditorTextHover.class);

            fSteppingModeTarget.dispose();
            fStepIntoCommand.dispose();
            fStepIntoSelectionCommand.dispose();
            fReverseStepIntoCommand.dispose();
            fStepOverCommand.dispose();
            fReverseStepOverCommand.dispose();
            fStepReturnCommand.dispose();
            fUncallCommand.dispose();
            fSuspendCommand.dispose();
            fResumeCommand.dispose();
            fReverseResumeCommand.dispose();
            fResumeWithoutSignalCommand.dispose();
            fRestartCommand.dispose();
            fTerminateCommand.dispose();
            fConnectCommand.dispose();
            fDebugNewExecutableCommand.dispose();
            fDisconnectCommand.dispose();
            fSuspendTrigger.dispose();
            fReverseToggleTarget.dispose();
            fStartTracingTarget.dispose();
            fStopTracingTarget.dispose();
            fSaveTraceDataTarget.dispose();
            fSelectNextRecordTarget.dispose();
            fSelectPrevRecordTarget.dispose();
            fPinProvider.dispose();
        }
}
