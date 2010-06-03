/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui;

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
import org.eclipse.debug.core.commands.IResumeHandler;
import org.eclipse.debug.core.commands.IStepIntoHandler;
import org.eclipse.debug.core.commands.IStepOverHandler;
import org.eclipse.debug.core.commands.IStepReturnHandler;
import org.eclipse.debug.core.commands.ISuspendHandler;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicyFactory;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;

/**
 * Contains the set of adapters that are created for each session instance.
 */
class SessionAdapterSet {
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
    final PDASuspendTrigger fSuspendTrigger;
    
    // Adapters for integration with other UI actions
    final IDebugModelProvider fDebugModelProvider;
    final PDALaunch fLaunch;

    final SteppingController fSteppingController;

    final IModelSelectionPolicyFactory fModelSelectionPolicyFactory;

    SessionAdapterSet(PDALaunch launch) {
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
        fSuspendTrigger = new PDASuspendTrigger(session, fLaunch);

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
        fSuspendTrigger.dispose();
    }
    
}
