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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfResumeCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepIntoCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepIntoSelectionCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepOverCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepReturnCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfSteppingModeTarget;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfSuspendCommand;
import org.eclipse.cdt.dsf.debug.ui.sourcelookup.DsfSourceDisplayAdapter;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.DefaultRefreshAllTarget;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.IRefreshAllTarget;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.DefaultDsfModelSelectionPolicyFactory;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
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
import org.eclipse.cdt.dsf.ui.viewmodel.IVMAdapter;
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;
import org.eclipse.debug.ui.contexts.ISuspendTrigger;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;

/**
 * This class creates and holds the different adapters registered with the DSF session.
 */
@Immutable
public class GdbSessionAdapters {
    private final ILaunch fLaunch;
    private final DsfSession fSession;

    private Map<Class<?>, Object> fLaunchAdapters = new HashMap<Class<?>, Object>();

    private SteppingController fSteppingController;
    private IVMAdapter fViewModelAdapter;
    private DsfSteppingModeTarget fSteppingModeTarget;

    public GdbSessionAdapters(ILaunch launch, DsfSession session) {
		fLaunch = launch;
		fSession = session;
		createAdapters();
    }

    /**
     * Creates all model and launch adapters.
     */
    protected void createAdapters() {
    	for (Class<?> adapterType : getModelAdapters()) {
    		Object adapter = createModelAdapter(adapterType, getLaunch(), getSession());
    		if (adapter != null) {
    			getSession().registerModelAdapter(adapterType, adapter);
    		}
    	}
    	for (Class<?> adapterType : getLaunchAdapters()) {
    		Object adapter = createLaunchAdapter(adapterType, getLaunch(), getSession());
    		if (adapter != null) {
    			fLaunchAdapters.put(adapterType, adapter);
    		}
    	}
    }

    /**
     * Returns the adapter object registered with the model for the given adapter type 
     * or null if no adapter is registered.
     */
    public Object getModelAdapter(Class<?> adapterType) {
    	return fSession.getModelAdapter(adapterType);
    }

    /**
     * Returns the adapter object registered with {@link ILaunch} for the given adapter type 
     * or null if no adapter is registered.
     */
    public Object getLaunchAdapter(Class<?> adapterType) {
    	return fLaunchAdapters.get(adapterType);
    }

    public void dispose() {
    	for (Class<?> adapterType : getModelAdapters()) {
    		Object adapter = getSession().getModelAdapter(adapterType);
    		if (adapter != null) {
    			getSession().unregisterModelAdapter(adapterType);
    			disposeAdapter(adapter);
    		}
    	}
    	for (Class<?> adapterType : getLaunchAdapters()) {
    		Object adapter = fLaunchAdapters.remove(adapterType);
    		if (adapter != null) {
    			disposeAdapter(adapter);
    		}
    	}
    }

    /**
     * Returns all adapter types registered with the model.
     * Clients can override this method to add a new adapter type and 
     * then override {@link GdbSessionAdapters.createModelAdapter()} 
     * to provide the adapter object.
     */
    protected Class<?>[] getModelAdapters() {
    	return new Class<?>[] {
    		SteppingController.class,
    		IViewerInputProvider.class,
    		ISteppingModeTarget.class,
    		ISourceDisplay.class,
    		IStepIntoHandler.class,
			IStepIntoSelectionHandler.class,
			IReverseStepIntoHandler.class,
			IStepOverHandler.class,
			IReverseStepOverHandler.class,
			IStepReturnHandler.class,
			IUncallHandler.class,
			ISuspendHandler.class,
			IResumeHandler.class,
			IReverseResumeHandler.class,
			IResumeWithoutSignalHandler.class,
			IRestartHandler.class,
			ITerminateHandler.class,
			IDebugNewExecutableHandler.class,
			IConnectHandler.class,
			IDisconnectHandler.class,
			IModelSelectionPolicyFactory.class,
			IRefreshAllTarget.class,
			IReverseToggleHandler.class,
			IStartTracingHandler.class,
			IStopTracingHandler.class,
			ISaveTraceDataHandler.class,
			ISelectNextTraceRecordHandler.class,
			ISelectPrevTraceRecordHandler.class,
			IPinProvider.class,
			IDebugModelProvider.class, 
			ILaunch.class,
			ICEditorTextHover.class,
    	};
    }

    /**
     * Returns all adapter types registered with {@link ILaunch}.
     * Clients can override this method to add a new adapter type and 
     * then override {@link GdbSessionAdapters.createLaunchAdapter()} 
     * to provide the adapter object.
     */
    protected Class<?>[] getLaunchAdapters() {
    	return new Class<?>[] {
    		IElementContentProvider.class,
    		IModelProxyFactory.class,
    		IColumnPresentationFactory.class,
    		ISuspendTrigger.class,
    	};    	
    }

    /**
     * Creates the adapter object for the given adapter type to register it with {@link ILaunch}.
     * Clients can override this method to provide their own adapters.
     */
    protected Object createLaunchAdapter(Class<?> adapterType, ILaunch launch, DsfSession session) {
        if (adapterType.equals(IElementContentProvider.class)) {
        	return getViewModelAdapter();
        }
        if (adapterType.equals(IModelProxyFactory.class)) {
        	return getViewModelAdapter();
        }
        if (adapterType.equals(IColumnPresentationFactory.class)) {
        	return getViewModelAdapter();
        }
        if (adapterType.equals(ISuspendTrigger.class)) {
        	return new GdbSuspendTrigger(session, launch);
        }
    	return null;
    }

    /**
     * Creates the adapter object for the given adapter type to register it with the model.
     * Clients can override this method to provide their own adapters.
     */
    protected Object createModelAdapter(Class<?> adapterType, ILaunch launch, DsfSession session) {
		if (SteppingController.class.equals(adapterType)) {
			fSteppingController = createSteppingController(launch, session);
			return fSteppingController;
		}
		if (IViewerInputProvider.class.equals(adapterType)) {
			fViewModelAdapter = createViewModelAdapter(launch, session);
			return fViewModelAdapter;
		}
		if (ISteppingModeTarget.class.equals(adapterType)) {
			fSteppingModeTarget = createSteppingModeTarget(launch, session);
			return fSteppingModeTarget;
		}
		if (ISourceDisplay.class.equals(adapterType)) { 
			return launch.getSourceLocator() instanceof ISourceLookupDirector ? 
				new DsfSourceDisplayAdapter(
					session, 
					(ISourceLookupDirector)launch.getSourceLocator(), 
					getSteppingController()
				) : null;
		}
		if (IStepIntoHandler.class.equals(adapterType)) {
			return new DsfStepIntoCommand(session, getSteppingModeTarget());
		}
		if (IStepIntoSelectionHandler.class.equals(adapterType)) {
			return new DsfStepIntoSelectionCommand(session);
		}
		if (IReverseStepIntoHandler.class.equals(adapterType)) {
			return new GdbReverseStepIntoCommand(session, getSteppingModeTarget());
		}
		if (IStepOverHandler.class.equals(adapterType)) {
			return new DsfStepOverCommand(session, getSteppingModeTarget());
		}
		if (IReverseStepOverHandler.class.equals(adapterType)) {
			return new GdbReverseStepOverCommand(session, getSteppingModeTarget());
		}
		if (IStepReturnHandler.class.equals(adapterType)) {
			return new DsfStepReturnCommand(session);
		}
		if (IUncallHandler.class.equals(adapterType)) {
			return new GdbUncallCommand(session, getSteppingModeTarget());
		}
		if (ISuspendHandler.class.equals(adapterType)) {
			return new DsfSuspendCommand(session);
		}
		if (IResumeHandler.class.equals(adapterType)) {
			return new DsfResumeCommand(session);
		}
		if (IReverseResumeHandler.class.equals(adapterType)) {
			return new GdbReverseResumeCommand(session);
		}
		if (IResumeWithoutSignalHandler.class.equals(adapterType)) {
			return new GdbResumeWithoutSignalCommand(session);
		}
		if (IRestartHandler.class.equals(adapterType)) {
			return new GdbRestartCommand(session, getLaunch());
		}
		if (ITerminateHandler.class.equals(adapterType)) { 
			return new DsfTerminateCommand(session);
		}
		if (IDebugNewExecutableHandler.class.equals(adapterType)) {
			return new GdbDebugNewExecutableCommand(session, launch);
		}
		if (IConnectHandler.class.equals(adapterType)) { 
			return new GdbConnectCommand(session, launch);
		}
		if (IDisconnectHandler.class.equals(adapterType)) { 
			return new GdbDisconnectCommand(session);
		}
		if (IModelSelectionPolicyFactory.class.equals(adapterType)) { 
			return new DefaultDsfModelSelectionPolicyFactory();
		}
		if (IRefreshAllTarget.class.equals(adapterType)) { 
			return new DefaultRefreshAllTarget();
		}
		if (IReverseToggleHandler.class.equals(adapterType)) { 
			return new GdbReverseToggleCommand(session);
		}
		if (IStartTracingHandler.class.equals(adapterType)) { 
			return new GdbStartTracingCommand(session);
		}
		if (IStopTracingHandler.class.equals(adapterType)) {
			return new GdbStopTracingCommand(session);
		}
		if (ISaveTraceDataHandler.class.equals(adapterType)) { 
			return new GdbSaveTraceDataCommand(session);
		}
		if (ISelectNextTraceRecordHandler.class.equals(adapterType)) { 
			return new GdbSelectNextTraceRecordCommand(session);
		}
		if (ISelectPrevTraceRecordHandler.class.equals(adapterType)) { 
			return new GdbSelectPrevTraceRecordCommand(session);
		}
		if (IPinProvider.class.equals(adapterType)) { 
			return new GdbPinProvider(session);
		}
		if (IDebugModelProvider.class.equals(adapterType)) {
			return new IDebugModelProvider() {
			    // @see org.eclipse.debug.core.model.IDebugModelProvider#getModelIdentifiers()
			    @Override
			    public String[] getModelIdentifiers() {
			    	return new String[] { 
			    		GdbLaunchDelegate.GDB_DEBUG_MODEL_ID, 
			    		ICBreakpoint.C_BREAKPOINTS_DEBUG_MODEL_ID, 
			    		"org.eclipse.cdt.gdb"  //$NON-NLS-1$
			    	};
			    }
			};
		}

		/*
		 * Registering the launch as an adapter, ensures that this launch,
		 * and debug model ID will be associated with all DMContexts from this
		 * session.
		 */
		if (ILaunch.class.equals(adapterType)) {
			return launch;
		}

		/*
		 * Register debug hover adapter (bug 309001).
		 */
		if (ICEditorTextHover.class.equals(adapterType)) {
			return new GdbDebugTextHover();
		}
    	return null;
    }

    /**
     * Returns the method that will be called to dispose the given object.
     *  
     * @param adapter the object to dispose 
     * @return "dispose()" method or null if the given object doesn't have "dispose()" method
     * 
     * Clients can override this method to provide dispose methods different than "dispose()" 
     * for specific adapters.
     */
    protected Method getDisposeMethod(Object adapter) {
    	if (adapter != null) {
			try {
				return adapter.getClass().getMethod("dispose"); //$NON-NLS-1$
			}
			catch(NoSuchMethodException | SecurityException e) {
				// ignore
			}
    	}
		return null;
    }

    protected DsfSession getSession() {
    	return fSession;
    }
    
    
    protected ILaunch getLaunch() {
    	return fLaunch;
    }

    private void disposeAdapter(Object adapter) {
		try {
			Method dispose = getDisposeMethod(adapter);
			if (dispose != null) {
				dispose.invoke(adapter);
			}
		}
		catch(SecurityException | IllegalAccessException | IllegalArgumentException e) {
			// ignore
		}
		catch(InvocationTargetException e) {
			GdbPlugin.log(e.getTargetException());
		}
    }

    protected SteppingController createSteppingController(ILaunch launch, DsfSession session) {
    	return new SteppingController(session);
    }

    protected IVMAdapter createViewModelAdapter(ILaunch launch, DsfSession session) {
    	return new GdbViewModelAdapter(session, getSteppingController()); 
    }
    
    protected DsfSteppingModeTarget createSteppingModeTarget(ILaunch launch, DsfSession session) {
    	return new GdbSteppingModeTarget(session);    
    }
    
    protected DsfSteppingModeTarget getSteppingModeTarget() {
    	return fSteppingModeTarget;
    }

	protected SteppingController getSteppingController() {
		return fSteppingController;
	}

	protected IVMAdapter getViewModelAdapter() {
		return fViewModelAdapter;
	}
}
