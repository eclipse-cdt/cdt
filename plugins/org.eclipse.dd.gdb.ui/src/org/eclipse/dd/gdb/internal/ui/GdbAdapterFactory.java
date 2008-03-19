/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.debug.ui.actions.DsfResumeCommand;
import org.eclipse.dd.dsf.debug.ui.actions.DsfStepIntoCommand;
import org.eclipse.dd.dsf.debug.ui.actions.DsfStepOverCommand;
import org.eclipse.dd.dsf.debug.ui.actions.DsfStepReturnCommand;
import org.eclipse.dd.dsf.debug.ui.actions.DsfSuspendCommand;
import org.eclipse.dd.dsf.debug.ui.contexts.DsfSuspendTrigger;
import org.eclipse.dd.dsf.debug.ui.sourcelookup.MISourceDisplayAdapter;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.internal.ui.actions.DsfTerminateCommand;
import org.eclipse.dd.gdb.internal.ui.viewmodel.GdbViewModelAdapter;
import org.eclipse.dd.gdb.launch.launching.GdbLaunch;
import org.eclipse.dd.gdb.launch.launching.GdbLaunchDelegate;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
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
        final MISourceDisplayAdapter fSourceDisplayAdapter;
        final DsfStepIntoCommand fStepIntoCommand;
        final DsfStepOverCommand fStepOverCommand;
        final DsfStepReturnCommand fStepReturnCommand;
        final DsfSuspendCommand fSuspendCommand;
        final DsfResumeCommand fResumeCommand;
        final DsfTerminateCommand fTerminateCommand;
        final IDebugModelProvider fDebugModelProvider;
        final DsfSuspendTrigger fSuspendTrigger;

        SessionAdapterSet(GdbLaunch launch) {
            fLaunch = launch;
            DsfSession session = launch.getSession();
            
            fViewModelAdapter = new GdbViewModelAdapter(session);

            if (launch.getSourceLocator() instanceof ISourceLookupDirector) {
                fSourceDisplayAdapter = new MISourceDisplayAdapter(session, (ISourceLookupDirector)launch.getSourceLocator());
            } else {
                fSourceDisplayAdapter = null;
            }
            session.registerModelAdapter(ISourceDisplay.class, fSourceDisplayAdapter);
            
            fStepIntoCommand = new DsfStepIntoCommand(session);
            fStepOverCommand = new DsfStepOverCommand(session);
            fStepReturnCommand = new DsfStepReturnCommand(session);
            fSuspendCommand = new DsfSuspendCommand(session);
            fResumeCommand = new DsfResumeCommand(session);
            fTerminateCommand = new DsfTerminateCommand(session);
            fSuspendTrigger = new DsfSuspendTrigger(session, fLaunch);
            session.registerModelAdapter(IStepIntoHandler.class, fStepIntoCommand);
            session.registerModelAdapter(IStepOverHandler.class, fStepOverCommand);
            session.registerModelAdapter(IStepReturnHandler.class, fStepReturnCommand);
            session.registerModelAdapter(ISuspendHandler.class, fSuspendCommand);
            session.registerModelAdapter(IResumeHandler.class, fResumeCommand);
            session.registerModelAdapter(ITerminateHandler.class, fTerminateCommand);

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
            
            session.unregisterModelAdapter(IStepIntoHandler.class);
            session.unregisterModelAdapter(IStepOverHandler.class);
            session.unregisterModelAdapter(IStepReturnHandler.class);
            session.unregisterModelAdapter(ISuspendHandler.class);
            session.unregisterModelAdapter(IResumeHandler.class);
            session.unregisterModelAdapter(ITerminateHandler.class);            
            fStepIntoCommand.dispose();
            fStepOverCommand.dispose();
            fStepReturnCommand.dispose();
            fSuspendCommand.dispose();
            fResumeCommand.dispose();
            fTerminateCommand.dispose();
            fSuspendTrigger.dispose();
        }        
        
        
    }        

    private Map<GdbLaunch, SessionAdapterSet> fLaunchAdapterSets = 
        Collections.synchronizedMap(new HashMap<GdbLaunch, SessionAdapterSet>());
    
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
        synchronized(fLaunchAdapterSets) {
            adapterSet = fLaunchAdapterSets.get(launch);
            if (adapterSet == null) {
                adapterSet = new SessionAdapterSet(launch);
                fLaunchAdapterSets.put(launch, adapterSet);
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
                synchronized(fLaunchAdapterSets) {
                    if ( fLaunchAdapterSets.containsKey(launch) ) {
                        fLaunchAdapterSets.remove(launch).dispose();
                    }
                }
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
