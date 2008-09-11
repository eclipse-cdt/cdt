/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.SteppingController;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.DefaultDsfModelSelectionPolicyFactory;
import org.eclipse.dd.dsf.debug.ui.actions.DsfResumeCommand;
import org.eclipse.dd.dsf.debug.ui.actions.DsfStepIntoCommand;
import org.eclipse.dd.dsf.debug.ui.actions.DsfStepOverCommand;
import org.eclipse.dd.dsf.debug.ui.actions.DsfStepReturnCommand;
import org.eclipse.dd.dsf.debug.ui.actions.DsfSteppingModeTarget;
import org.eclipse.dd.dsf.debug.ui.actions.DsfSuspendCommand;
import org.eclipse.dd.dsf.debug.ui.contexts.DsfSuspendTrigger;
import org.eclipse.dd.dsf.debug.ui.sourcelookup.DsfSourceDisplayAdapter;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.internal.provisional.actions.IConnect;
import org.eclipse.dd.gdb.internal.provisional.launching.GdbLaunch;
import org.eclipse.dd.gdb.internal.provisional.launching.GdbLaunchDelegate;
import org.eclipse.dd.gdb.internal.ui.actions.DsfTerminateCommand;
import org.eclipse.dd.gdb.internal.ui.actions.GdbConnectCommand;
import org.eclipse.dd.gdb.internal.ui.actions.GdbDisconnectCommand;
import org.eclipse.dd.gdb.internal.ui.actions.GdbRestartCommand;
import org.eclipse.dd.gdb.internal.ui.viewmodel.GdbViewModelAdapter;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.core.commands.IResumeHandler;
import org.eclipse.debug.core.commands.IStepIntoHandler;
import org.eclipse.debug.core.commands.IStepOverHandler;
import org.eclipse.debug.core.commands.IStepReturnHandler;
import org.eclipse.debug.core.commands.ISuspendHandler;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicyFactory;
import org.eclipse.debug.ui.contexts.ISuspendTrigger;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;

/**
 * This implementation of platform adapter factory only retrieves the adapters
 * for the launch object.  But it also manages the creation and destruction
 * of the session-based adapters which are returned by the
 * IDMContext.getAdapter() methods.
 */
@ThreadSafe
@SuppressWarnings({"restriction"})
public class GdbAdapterFactory
    implements IAdapterFactory, ILaunchesListener2
{
    @Immutable
    class SessionAdapterSet {
        final GdbLaunch fLaunch;
        final GdbViewModelAdapter fViewModelAdapter;
        final DsfSourceDisplayAdapter fSourceDisplayAdapter;
        final DsfStepIntoCommand fStepIntoCommand;
        final DsfStepOverCommand fStepOverCommand;
        final DsfStepReturnCommand fStepReturnCommand;
        final DsfSuspendCommand fSuspendCommand;
        final DsfResumeCommand fResumeCommand;
        final GdbRestartCommand fRestartCommand;
        final DsfTerminateCommand fTerminateCommand;
        final GdbConnectCommand fConnectCommand;
        final GdbDisconnectCommand fDisconnectCommand;
        final IDebugModelProvider fDebugModelProvider;
        final DsfSuspendTrigger fSuspendTrigger;
		final DsfSteppingModeTarget fSteppingModeTarget;
		final IModelSelectionPolicyFactory fModelSelectionPolicyFactory;
		final SteppingController fSteppingController;

        SessionAdapterSet(GdbLaunch launch) {
            fLaunch = launch;
            DsfSession session = launch.getSession();
            
            // register stepping controller
            fSteppingController = new SteppingController(session);
            session.registerModelAdapter(SteppingController.class, fSteppingController);

            fViewModelAdapter = new GdbViewModelAdapter(session, fSteppingController);

            if (launch.getSourceLocator() instanceof ISourceLookupDirector) {
                fSourceDisplayAdapter = new DsfSourceDisplayAdapter(session, (ISourceLookupDirector)launch.getSourceLocator(), fSteppingController);
            } else {
                fSourceDisplayAdapter = null;
            }
            session.registerModelAdapter(ISourceDisplay.class, fSourceDisplayAdapter);
            
            fSteppingModeTarget= new DsfSteppingModeTarget();
            fStepIntoCommand = new DsfStepIntoCommand(session, fSteppingModeTarget);
            fStepOverCommand = new DsfStepOverCommand(session, fSteppingModeTarget);
            fStepReturnCommand = new DsfStepReturnCommand(session);
            fSuspendCommand = new DsfSuspendCommand(session);
            fResumeCommand = new DsfResumeCommand(session);
            fRestartCommand = new GdbRestartCommand(session, fLaunch);
            fTerminateCommand = new DsfTerminateCommand(session);
            fConnectCommand = new GdbConnectCommand(session);
            fDisconnectCommand = new GdbDisconnectCommand(session);
            fSuspendTrigger = new DsfSuspendTrigger(session, fLaunch);
            fModelSelectionPolicyFactory = new DefaultDsfModelSelectionPolicyFactory();

            session.registerModelAdapter(ISteppingModeTarget.class, fSteppingModeTarget);
            session.registerModelAdapter(IStepIntoHandler.class, fStepIntoCommand);
            session.registerModelAdapter(IStepOverHandler.class, fStepOverCommand);
            session.registerModelAdapter(IStepReturnHandler.class, fStepReturnCommand);
            session.registerModelAdapter(ISuspendHandler.class, fSuspendCommand);
            session.registerModelAdapter(IResumeHandler.class, fResumeCommand);
            session.registerModelAdapter(IRestart.class, fRestartCommand);
            session.registerModelAdapter(ITerminateHandler.class, fTerminateCommand);
            session.registerModelAdapter(IConnect.class, fConnectCommand);
            session.registerModelAdapter(IDisconnectHandler.class, fDisconnectCommand);
            session.registerModelAdapter(IModelSelectionPolicyFactory.class, fModelSelectionPolicyFactory);

            fDebugModelProvider = new IDebugModelProvider() {
                // @see org.eclipse.debug.core.model.IDebugModelProvider#getModelIdentifiers()
                public String[] getModelIdentifiers() {
                    return new String[] { GdbLaunchDelegate.GDB_DEBUG_MODEL_ID };
                }
            };
            session.registerModelAdapter(IDebugModelProvider.class, fDebugModelProvider);

            /*
             * Registering the launch as an adapter, ensures that this launch,
             * and debug model ID will be associated with all DMContexts from this
             * session.
             */
            session.registerModelAdapter(ILaunch.class, fLaunch);
        }
        
        void dispose() {
            DsfSession session = fLaunch.getSession();
            
            fViewModelAdapter.dispose();

            session.unregisterModelAdapter(ISourceDisplay.class);
            if (fSourceDisplayAdapter != null) fSourceDisplayAdapter.dispose();

            session.unregisterModelAdapter(SteppingController.class);
            fSteppingController.dispose();

            session.unregisterModelAdapter(ISteppingModeTarget.class);
            session.unregisterModelAdapter(IStepIntoHandler.class);
            session.unregisterModelAdapter(IStepOverHandler.class);
            session.unregisterModelAdapter(IStepReturnHandler.class);
            session.unregisterModelAdapter(ISuspendHandler.class);
            session.unregisterModelAdapter(IResumeHandler.class);
            session.unregisterModelAdapter(IRestart.class);
            session.unregisterModelAdapter(ITerminateHandler.class);
            session.unregisterModelAdapter(IConnect.class);
            session.unregisterModelAdapter(IDisconnectHandler.class);
            session.unregisterModelAdapter(IModelSelectionPolicyFactory.class);
            
            fStepIntoCommand.dispose();
            fStepOverCommand.dispose();
            fStepReturnCommand.dispose();
            fSuspendCommand.dispose();
            fResumeCommand.dispose();
            fRestartCommand.dispose();
            fTerminateCommand.dispose();
            fConnectCommand.dispose();
            fDisconnectCommand.dispose();
            fSuspendTrigger.dispose();
        }
        
        
    }

    private static Map<GdbLaunch, SessionAdapterSet> fgLaunchAdapterSets =
        Collections.synchronizedMap(new HashMap<GdbLaunch, SessionAdapterSet>());
    
	static void disposeAdapterSet(ILaunch launch) {
		synchronized(fgLaunchAdapterSets) {
		    if ( fgLaunchAdapterSets.containsKey(launch) ) {
		        fgLaunchAdapterSets.remove(launch).dispose();
		    }
		}
	}

    public GdbAdapterFactory() {
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
    }

    /**
     * This method only actually returns adapters for the launch object.
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!(adaptableObject instanceof GdbLaunch)) return null;

        GdbLaunch launch = (GdbLaunch)adaptableObject;

        // Find the correct set of adapters based on the launch session-ID.  If not found
        // it means that we have a new launch and new session, and we have to create a
        // new set of adapters.
        DsfSession session = launch.getSession();
        if (session == null) return null;

        SessionAdapterSet adapterSet;
        synchronized(fgLaunchAdapterSets) {
            adapterSet = fgLaunchAdapterSets.get(launch);
            if (adapterSet == null) {
                adapterSet = new SessionAdapterSet(launch);
                fgLaunchAdapterSets.put(launch, adapterSet);
            }
        }
        
        // Returns the adapter type for the launch object.
        if (adapterType.equals(IElementContentProvider.class)) return adapterSet.fViewModelAdapter;
        else if (adapterType.equals(IModelProxyFactory.class)) return adapterSet.fViewModelAdapter;
        else if (adapterType.equals(IColumnPresentationFactory.class)) return adapterSet.fViewModelAdapter;
        else if (adapterType.equals(ISuspendTrigger.class)) return adapterSet.fSuspendTrigger;
        else return null;
    }

    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        return new Class[] {
            IElementContentProvider.class, IModelProxyFactory.class, ISuspendTrigger.class,
            IColumnPresentationFactory.class
            };
    }

    public void launchesRemoved(ILaunch[] launches) {
        // Dispose the set of adapters for a launch only after the launch is
        // removed.
        for (ILaunch launch : launches) {
            if (launch instanceof GdbLaunch) {
                disposeAdapterSet(launch);
            }
        }
    }

    public void launchesTerminated(ILaunch[] launches) {
    }

    public void launchesAdded(ILaunch[] launches) {
    }
    
    public void launchesChanged(ILaunch[] launches) {
    }
    
}
