/*****************************************************************
 * Copyright (c) 2010, 2011 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Pin and Clone Supports (331781)
 *     Patrick Chuong (Texas Instruments) - Add support for icon overlay in the debug view (Bug 334566)
 *****************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.ui.IPinProvider;
import org.eclipse.cdt.debug.ui.PinElementHandle;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateInDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.StateChangedEvent;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * GDB pin provider implementation.
 */
public class GdbPinProvider implements IPinProvider {
	private static class GdbPinElementColorDescriptor implements IPinElementColorDescriptor {
		int fColor = GREEN;
		
		GdbPinElementColorDescriptor(int color) {
			fColor = color;
		}
        @Override
		public int getOverlayColor() {
			return fColor;
		}
        @Override
		public ImageDescriptor getToolbarIconDescriptor() {
			return null;
		}
	}
	
	/**
	 * A set of pinned element handles and their callback.
	 */
	private static Map<IPinElementHandle, IPinModelListener> gsPinnedHandles = 
		Collections.synchronizedMap(new HashMap<IPinElementHandle, IPinModelListener>());
	
	/**
	 * Dsf session.
	 */
	private final DsfSession fSession;
	
	/**
	 * Constructor.
	 * 
	 * @param session
	 */
	public GdbPinProvider(DsfSession session) {
		fSession = session;
		
		session.getExecutor().execute(new Runnable() {			
            @Override
			public void run() {
				fSession.addServiceEventListener(GdbPinProvider.this, null);
			}
		});		
	}
	
	/**
	 * Dispose all resources.
	 */
	public void dispose() {
		try {
			fSession.getExecutor().execute(new Runnable() {			
                @Override
				public void run() {
					fSession.removeServiceEventListener(GdbPinProvider.this);
				}
			});
		} catch (RejectedExecutionException e) {
			// Session already gone.
		}
	}
	
	/**
	 * Returns the pinned element handles.
	 * 
	 * @return the element handles.
	 */
	public static Set<IPinElementHandle> getPinnedHandles() {
		return gsPinnedHandles.keySet();
	}
	
	private static IMIExecutionDMContext getExecutionDmc(IDMContext dmc) {		
		return DMContexts.getAncestorOfType(dmc, IMIExecutionDMContext.class);		
	}
	
	private static IProcessDMContext getProcessDmc(IDMContext dmc) {
		return DMContexts.getAncestorOfType(dmc, IProcessDMContext.class);
	}
	
	private IThreadDMData getData(final IThreadDMContext threadDmc) {
		if (threadDmc == null || !fSession.isActive()) 
			return null;		
		
		IThreadDMData data = null;
		final DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), fSession.getId());
		try {			
			Query<IThreadDMData> query = new Query<IThreadDMData>() {
				@Override
				protected void execute(final DataRequestMonitor<IThreadDMData> rm) {
					final IProcesses processes = tracker.getService(IProcesses.class);
					if (processes != null) {
						processes.getExecutionData(threadDmc, rm);
					} else {
						rm.setData(null);
						rm.done();
					}
				}
			};
			
			ImmediateInDsfExecutor immediateExecutor = new ImmediateInDsfExecutor(fSession.getExecutor());			
			immediateExecutor.execute(query);
			data = query.get(2, TimeUnit.SECONDS); // timeout in 2 seconds, in case the call to execute got stuck 
		} catch (Exception e) {
			GdbUIPlugin.log(e);
		} finally {
			if (tracker != null)
				tracker.dispose();
		}
		return data;
	}
	
	private String getLabel(IThreadDMData data) {
		String label = ""; //$NON-NLS-1$
		if (data != null) {
			String name = data.getName();
			String id = data.getId();
			if (name != null && name.length() > 0)
				label = name;
			else if (id != null && id.length() > 0)
				label = id;
		}
		return label;
	}
	
	private String getCombinedLabels(IThreadDMContext processDmc, IMIExecutionDMContext execDmc) {
		// get the process label
		IThreadDMData processData = getData(processDmc);
		String label = getLabel(processData);
		
		// get the execution (thread) context label
		if (execDmc != null) {
			int threadId = execDmc.getThreadId();
			label += label.length() > 0 ? ": " : "";   //$NON-NLS-1$//$NON-NLS-2$
			label += "Thread [" + Integer.toString(threadId) + "]";   //$NON-NLS-1$//$NON-NLS-2$
		}
		return label;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.IPinProvider#isPinnable(org.eclipse.ui.IWorkbenchPart, java.lang.Object)
	 */
    @Override
	public boolean isPinnable(IWorkbenchPart part, Object debugContext) {
		if (debugContext instanceof IAdaptable) {
			return ((IAdaptable) debugContext).getAdapter(IDMContext.class) != null;
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.IPinProvider#pin(org.eclipse.ui.IWorkbenchPart, java.lang.Object, org.eclipse.cdt.debug.ui.IPinModelListener)
	 */
    @Override
	public IPinElementHandle pin(IWorkbenchPart part, Object debugContext, IPinModelListener listener) {
		Object pinContext = debugContext;
		String label = ""; //$NON-NLS-1$
		String sessionId = ""; //$NON-NLS-1$
		
		IDMContext dmc = null;
		if (debugContext instanceof IAdaptable) {
			dmc = (IDMContext) ((IAdaptable) debugContext).getAdapter(IDMContext.class);
			sessionId = dmc.getSessionId() + "."; //$NON-NLS-1$
			
			if (dmc != null) {
				IMIExecutionDMContext execDmc = getExecutionDmc(dmc);
				IProcessDMContext processDmc = getProcessDmc(dmc);
				
				label = getCombinedLabels(processDmc, execDmc);
				
				// set the pin context to a thread if it exist
				if (execDmc != null) {
					dmc = execDmc;
					pinContext = execDmc;
					
				// otherwise, set it to the DM context
				} else {
					pinContext = dmc;
				}
			}
		}

		IPinElementColorDescriptor colorDesc = 
			new GdbPinElementColorDescriptor(GdbPinColorTracker.INSTANCE.addRef(sessionId + label));
		PinElementHandle handle = new PinElementHandle(pinContext, label, colorDesc);
		gsPinnedHandles.put(handle, listener);
		dispatchChangedEvent(dmc);
		
		return handle;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.IPinProvider#unpin(org.eclipse.ui.IWorkbenchPart, org.eclipse.cdt.debug.ui.IPinProvider.IPinElementHandle)
	 */
    @Override
	public void unpin(IWorkbenchPart part, IPinElementHandle handle) {
		// remove the handle from the cache
		gsPinnedHandles.remove(handle);		
		
		// dispatch the event to update the handle DM context
		Object debugContext = handle.getDebugContext();
		if (debugContext instanceof IAdaptable) {
			IDMContext dmc = (IDMContext) ((IAdaptable) debugContext).getAdapter(IDMContext.class);
			GdbPinColorTracker.INSTANCE.removeRef(dmc.getSessionId() + "." + handle.getLabel()); //$NON-NLS-1$
			dispatchChangedEvent(dmc);
			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.IPinProvider#isPinnedTo(java.lang.Object, org.eclipse.cdt.debug.ui.IPinProvider.IPinElementHandle)
	 */
    @Override
	public boolean isPinnedTo(Object debugContext, IPinElementHandle handle) {
		Object handleDebugContext = handle.getDebugContext();
		
		if (debugContext instanceof IAdaptable && handleDebugContext instanceof IAdaptable) {			
			IDMContext dmc = (IDMContext) ((IAdaptable) debugContext).getAdapter(IDMContext.class);
			IDMContext hDmc = (IDMContext) ((IAdaptable) handleDebugContext).getAdapter(IDMContext.class);
			
			if (dmc != null && hDmc != null) {
				if (dmc.getSessionId().equals(hDmc.getSessionId())) {				
					IMIExecutionDMContext execDmc = getExecutionDmc(dmc);
					IProcessDMContext processDmc = getProcessDmc(dmc);
					
					String label = getCombinedLabels(processDmc, execDmc);
						return label.equals(handle.getLabel());
				}
			}
		}
		return false;
	}
	
	/**
	 * Dispatch the change event for the given DM context.
	 * 
	 * @param dmc the DM context
	 */
	private void dispatchChangedEvent(IDMContext dmc) {
		if (dmc == null)
			return;
		
		try {			
			DsfSession session = DsfSession.getSession(dmc.getSessionId());
			if (session != null && session.isActive())
				session.dispatchEvent(new StateChangedEvent(dmc), null);
		} catch (RejectedExecutionException e) {
			// Session already gone.
		}
	}
	
	/**
	 * Handle start event and re-attach the DM context to the pinned handles. The DM context
	 * is used for dispatching event to update the element label.
	 */
	@DsfServiceEventHandler
	public void handleEvent(final IStartedDMEvent event) { 
		final IDMContext eventDmc = event.getDMContext(); 
		final IMIExecutionDMContext eventExecDmc = getExecutionDmc(eventDmc); 
		final IProcessDMContext eventProcessDmc = getProcessDmc(eventDmc); 
	
		if (eventProcessDmc != null) { 
			for (final IPinElementHandle h : getPinnedHandles()) {
				new Job("Updating pin handler debug context") { //$NON-NLS-1$
					{setPriority(INTERACTIVE);}
					@Override
					protected IStatus run(IProgressMonitor monitor) {						
						// only attach to the same pin handle if the session is not active
						PinElementHandle handle = ((PinElementHandle)h);						
						Object handleDebugContext = handle.getDebugContext();
						if (handleDebugContext instanceof IAdaptable) {						
							IDMContext handleDmc = (IDMContext) ((IAdaptable) handleDebugContext).getAdapter(IDMContext.class);
							if (handleDmc != null) {
								DsfSession session = DsfSession.getSession(handleDmc.getSessionId());
								if (session == null || !session.isActive()) {							
									String handleLabel = handle.getLabel();
									String label = getCombinedLabels(eventProcessDmc, eventExecDmc);
									
									if (label.equals(handleLabel)) {
										IDMContext newDmc = eventExecDmc != null ? eventExecDmc : eventDmc;
										handle.setDebugContext(newDmc);
										dispatchChangedEvent(newDmc);
									}
								}
							}
						}
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		}
	}
	
	@DsfServiceEventHandler
	public void handleEvent(final ICommandControlShutdownDMEvent event) {
		handleInvalidModelContext(event);
	}

	@DsfServiceEventHandler
	public void handleEvent(final IExitedDMEvent event) {
		handleInvalidModelContext(event);
	}
	
	@DsfServiceEventHandler
	public void handleEvent(final IResumedDMEvent event) {
		handleInvalidModelContext(event);
	}
	
	private void handleInvalidModelContext(IDMEvent<?> event) {
		Set<Entry<IPinElementHandle, IPinModelListener>> entries = gsPinnedHandles.entrySet();
		for (final Entry<IPinElementHandle, IPinModelListener> e : entries) {			
			final IPinModelListener listener = e.getValue();
			if (listener != null) {
				IPinElementHandle handle = e.getKey();
				
				Object handleObject = handle.getDebugContext();
				if (handleObject instanceof IDMContext) {
					IDMContext handleDmc = (IDMContext)handleObject;
					IDMContext eventDmc = event.getDMContext();
					
					// First check if we have a thread.  We must use IMIExecutionDMContext and not
					// IExecutionDMContext because IExecutionDMContext also represents a process
					IMIExecutionDMContext execEventDmc = DMContexts.getAncestorOfType(eventDmc, IMIExecutionDMContext.class);
					IMIExecutionDMContext execHandleDmc = DMContexts.getAncestorOfType(handleDmc, IMIExecutionDMContext.class);
					
					// Make sure both dmcs are not null to know if we should compare thread dmcs or container dmcs
					if (execEventDmc != null && execHandleDmc != null) {
						// It is a thread event, but is it the same as the pin handle?
						if (execEventDmc.equals(execHandleDmc)) {
							fireModleChangeEvent(listener, null);
						}
						continue;
					}
					
					// If we weren't dealing with a thread for either the event or the handle,
					// let's check for IMIContainerDMContext
					IMIContainerDMContext procEventDmc = DMContexts.getAncestorOfType(eventDmc, IMIContainerDMContext.class);
					IMIContainerDMContext procHandleDmc = DMContexts.getAncestorOfType(handleDmc, IMIContainerDMContext.class);
					if (procEventDmc != null && procHandleDmc != null) {
						if (procEventDmc.equals(procHandleDmc)) {
							fireModleChangeEvent(listener, null);
						}
						continue;
					}
					
					// If we got a shutdown event 
					if (eventDmc instanceof ICommandControlDMContext) {
						fireModleChangeEvent(listener, null);
						continue;
					}
				}
			}
		}
	}
	
	private void fireModleChangeEvent(final IPinModelListener listener, final ISelection selection) {
		new Job("Model Changed") { //$NON-NLS-1$
			{ setSystem(true); }
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				listener.modelChanged(selection);
				return Status.OK_STATUS;
			}							
		}.schedule();
	}
}
