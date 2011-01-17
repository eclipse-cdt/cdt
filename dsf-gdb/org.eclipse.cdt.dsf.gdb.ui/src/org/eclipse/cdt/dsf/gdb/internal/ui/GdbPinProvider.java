/*****************************************************************
 * Copyright (c) 2010 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Pin and Clone Supports (331781)
 *****************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui;

import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.ui.IPinProvider;
import org.eclipse.cdt.debug.ui.PinElementHandle;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkbenchPart;

/**
 * GDB pin provider implementation.
 */
public class GdbPinProvider implements IPinProvider {

	private IMIExecutionDMContext getExecutionDmc(IDMContext dmc) {		
		return DMContexts.getAncestorOfType(dmc, IMIExecutionDMContext.class);		

	}
	
	private IProcessDMContext getProcessDmc(IDMContext dmc) {
		return DMContexts.getAncestorOfType(dmc, IProcessDMContext.class);
	}
	
	private IThreadDMData getData(final IThreadDMContext threadDmc) {
		if (threadDmc == null) 
			return null;
		
		IThreadDMData data = null;		
		try {
			String sessionId = threadDmc.getSessionId();
			DsfSession session = DsfSession.getSession(sessionId);
			final DsfServicesTracker tracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), sessionId);
			
			try {
				if (tracker != null) {				
					Query<IThreadDMData> query = new Query<IThreadDMData>() {
						@Override
						protected void execute(DataRequestMonitor<IThreadDMData> rm) {
							final IProcesses processes = tracker.getService(IProcesses.class);
							if (processes != null) {			
								processes.getExecutionData(threadDmc, rm);
							}
						}
					};
					
					session.getExecutor().execute(query);
					data = query.get(1, TimeUnit.SECONDS);
				}
			} finally {
				if (tracker != null)
					tracker.dispose();
			}			
		} catch (Exception e) {			
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
	public boolean isPinnable(IWorkbenchPart part, Object debugContext) {
		if (debugContext instanceof IAdaptable) {
			return ((IAdaptable) debugContext).getAdapter(IDMContext.class) != null;
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.IPinProvider#pin(org.eclipse.ui.IWorkbenchPart, java.lang.Object)
	 */
	public IPinElementHandle pin(IWorkbenchPart part, Object debugContext) {
		Object pinContext = debugContext;
		String label = ""; //$NON-NLS-1$
		
		if (debugContext instanceof IAdaptable) {
			IDMContext dmc = (IDMContext) ((IAdaptable) debugContext).getAdapter(IDMContext.class);
			if (dmc != null) {
				IMIExecutionDMContext execDmc = getExecutionDmc(dmc);
				IProcessDMContext processDmc = getProcessDmc(dmc);
				
				label = getCombinedLabels(processDmc, execDmc);
				if (execDmc != null)
					pinContext = execDmc;
			}
		}
		return new PinElementHandle(pinContext, label);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.IPinProvider#unpin(org.eclipse.ui.IWorkbenchPart, org.eclipse.cdt.debug.ui.IPinProvider.IPinElementHandle)
	 */
	public void unpin(IWorkbenchPart part, IPinElementHandle handle) {
		// do nothing for now.
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.IPinProvider#isPinnedTo(java.lang.Object, org.eclipse.cdt.debug.ui.IPinProvider.IPinElementHandle)
	 */
	public boolean isPinnedTo(Object debugContext, IPinElementHandle handle) {
		if (debugContext instanceof IAdaptable) {
			IDMContext dmc = (IDMContext) ((IAdaptable) debugContext).getAdapter(IDMContext.class);
			if (dmc != null) {
				IMIExecutionDMContext execDmc = getExecutionDmc(dmc);
				IProcessDMContext processDmc = getProcessDmc(dmc);

				if (execDmc != null && processDmc != null ) {
					String label = getCombinedLabels(processDmc, execDmc);
					if (handle instanceof IPinHandleLabelProvider)
						return label.equals( ((IPinHandleLabelProvider)handle).getLabel() );	
				}
			}
		}
		return false;
	}
}
