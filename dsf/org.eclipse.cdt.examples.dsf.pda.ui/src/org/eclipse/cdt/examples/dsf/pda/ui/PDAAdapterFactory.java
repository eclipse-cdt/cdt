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
package org.eclipse.cdt.examples.dsf.pda.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfResumeCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepIntoCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepOverCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepReturnCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfSuspendCommand;
import org.eclipse.cdt.dsf.debug.ui.sourcelookup.DsfSourceDisplayAdapter;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.DefaultDsfModelSelectionPolicyFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.cdt.examples.dsf.pda.launch.PDALaunch;
import org.eclipse.cdt.examples.dsf.pda.ui.actions.PDATerminateCommand;
import org.eclipse.cdt.examples.dsf.pda.ui.viewmodel.PDAVMAdapter;
import org.eclipse.core.runtime.IAdapterFactory;
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicyFactory;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;

/**
 * The adapter factory is the central point of control of view model and other
 * UI adapters of a DSF-based debugger.  As new launches are created and
 * old ones removed, this factory manages the life cycle of the associated
 * UI adapters.
 * <p>
 * As a platform adapter factory, this factory only  provides adapters for
 * the launch object.  Adapters for all other objects in the DSF model hierarchy
 * are registered with the DSF session.
 * </p>
 */
@ThreadSafe
@SuppressWarnings({"restriction"})
public class PDAAdapterFactory implements IAdapterFactory, ILaunchesListener2
{
    /**
     * Contains the set of adapters that are created for each launch instance.
     */
    @Immutable
    private static class LaunchAdapterSet {
        // View Model adapter
        final PDAVMAdapter fViewModelAdapter;
        
        // Source lookup and positioning adapter
        final DsfSourceDisplayAdapter fSourceDisplayAdapter;
        
        // Command adapters
        final DsfStepIntoCommand fStepIntoCommand;
        final DsfStepOverCommand fStepOverCommand;
        final DsfStepReturnCommand fStepReturnCommand;
        final DsfSuspendCommand fSuspendCommand;
        final DsfResumeCommand fResumeCommand;
        final PDATerminateCommand fTerminateCommand;
        
        // Adapters for integration with other UI actions
        final IDebugModelProvider fDebugModelProvider;
        final PDALaunch fLaunch;

		final SteppingController fSteppingController;

		private IModelSelectionPolicyFactory fModelSelectionPolicyFactory;

        LaunchAdapterSet(PDALaunch launch) {
            // Initialize launch and session.
            fLaunch = launch;
            DsfSession session = launch.getSession();
            
            // register stepping controller
            fSteppingController = new SteppingController(session);
            session.registerModelAdapter(SteppingController.class, fSteppingController);

            // Initialize VM
            fViewModelAdapter = new PDAVMAdapter(session, fSteppingController);

            // Initialize source lookup
            fSourceDisplayAdapter = new DsfSourceDisplayAdapter(session, (ISourceLookupDirector)launch.getSourceLocator(), fSteppingController);
            session.registerModelAdapter(ISourceDisplay.class, fSourceDisplayAdapter);

            // Default selection policy
            fModelSelectionPolicyFactory = new DefaultDsfModelSelectionPolicyFactory();
            session.registerModelAdapter(IModelSelectionPolicyFactory.class, fModelSelectionPolicyFactory);
            
            // Initialize retargetable command handler.
            fStepIntoCommand = new DsfStepIntoCommand(session, null);
            fStepOverCommand = new DsfStepOverCommand(session, null);
            fStepReturnCommand = new DsfStepReturnCommand(session);
            fSuspendCommand = new DsfSuspendCommand(session);
            fResumeCommand = new DsfResumeCommand(session);
            fTerminateCommand = new PDATerminateCommand(session);
            session.registerModelAdapter(IStepIntoHandler.class, fStepIntoCommand);
            session.registerModelAdapter(IStepOverHandler.class, fStepOverCommand);
            session.registerModelAdapter(IStepReturnHandler.class, fStepReturnCommand);
            session.registerModelAdapter(ISuspendHandler.class, fSuspendCommand);
            session.registerModelAdapter(IResumeHandler.class, fResumeCommand);
            session.registerModelAdapter(ITerminateHandler.class, fTerminateCommand);

            // Initialize debug model provider
            fDebugModelProvider = new IDebugModelProvider() {
                public String[] getModelIdentifiers() {
                    return new String[] { PDAPlugin.ID_PDA_DEBUG_MODEL };
                }
            };
            session.registerModelAdapter(IDebugModelProvider.class, fDebugModelProvider);
            
            // Register the launch as an adapter This ensures that the launch,
            // and debug model ID will be associated with all DMContexts from this
            // session.
            session.registerModelAdapter(ILaunch.class, fLaunch);
        }
        
        void dispose() {
            DsfSession session = fLaunch.getSession();

            fViewModelAdapter.dispose();

            session.unregisterModelAdapter(ISourceDisplay.class);
            if (fSourceDisplayAdapter != null) fSourceDisplayAdapter.dispose();
            
            session.unregisterModelAdapter(SteppingController.class);
            fSteppingController.dispose();

            session.unregisterModelAdapter(IModelSelectionPolicyFactory.class);

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
        }
    }

    /**
     * Active adapter sets.  They are accessed using the launch instance 
     * which owns the debug services session. 
     */
    private static Map<PDALaunch, LaunchAdapterSet> fgLaunchAdapterSets =
        Collections.synchronizedMap(new HashMap<PDALaunch, LaunchAdapterSet>());
 
    /**
     * Map of launches for which adapter sets have already been disposed.
     * This map (used as a set) is maintained in order to avoid re-creating an 
     * adapter set after the launch was removed from the launch manager, but 
     * while the launch is still being held by other classes which may 
     * request its adapters.  A weak map is used to avoid leaking 
     * memory once the launches are no longer referenced.
     * <p>
     * Access to this map is synchronized using the fgLaunchAdapterSets 
     * instance.
     * </p>
     */
    private static Map<ILaunch, Object> fgDisposedLaunchAdapterSets =
        new WeakHashMap<ILaunch, Object>();

	static void disposeAdapterSet(ILaunch launch) {
		synchronized(fgLaunchAdapterSets) {
            if ( fgLaunchAdapterSets.containsKey(launch) ) {
                fgLaunchAdapterSets.remove(launch).dispose();
                fgDisposedLaunchAdapterSets.put(launch, null);
            }
		}
	}

    public PDAAdapterFactory() {
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
    }

    // This IAdapterFactory method returns adapters for the PDA launch object only.
    @SuppressWarnings("unchecked") // IAdapterFactory is Java 1.3
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!(adaptableObject instanceof PDALaunch)) return null;

        PDALaunch launch = (PDALaunch)adaptableObject;

        // Check for valid session.  
        // Note: even if the session is no longer active, the adapter set 
        // should still be returned.  This is because the view model may still
        // need to show elements representing a terminated process/thread/etc.
        DsfSession session = launch.getSession();
        if (session == null) return null;

        // Find the correct set of adapters based on the launch.  If not found
        // it means that we have a new launch, and we have to create a
        // new set of adapters.
        LaunchAdapterSet adapterSet;
        synchronized(fgLaunchAdapterSets) {
            // The adapter set for the given launch was already disposed.  
            // Return a null adapter.
            if (fgDisposedLaunchAdapterSets.containsKey(launch)) {
                return null;
            }
            adapterSet = fgLaunchAdapterSets.get(launch);
            if (adapterSet == null) {
                adapterSet = new LaunchAdapterSet(launch);
                fgLaunchAdapterSets.put(launch, adapterSet);
            }
        }
        
        // Returns the adapter type for the launch object.
        if (adapterType.equals(IElementContentProvider.class)) return adapterSet.fViewModelAdapter;
        else if (adapterType.equals(IModelProxyFactory.class)) return adapterSet.fViewModelAdapter;
        else return null;
    }

    @SuppressWarnings("unchecked") // IAdapterFactory is Java 1.3
    public Class[] getAdapterList() {
        return new Class[] { IElementContentProvider.class, IModelProxyFactory.class, IColumnPresentationFactory.class };
    }

    public void launchesRemoved(ILaunch[] launches) {
        // Dispose the set of adapters for a launch only after the launch is
        // removed from the view.  If the launch is terminated, the adapters
        // are still needed to populate the contents of the view.
        for (ILaunch launch : launches) {
            if (launch instanceof PDALaunch) {
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
