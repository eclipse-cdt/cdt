/*******************************************************************************
 * Copyright (c) 2006, 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial API and implementation
 *     Wind River Systems - Factored out AbstractContainerVMNode
 *******************************************************************************/

package org.eclipse.dd.gdb.internal.ui.viewmodel.launch;


import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.AbstractContainerVMNode;
import org.eclipse.dd.dsf.debug.service.IProcesses;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.dd.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.command.ICommandControlService;
import org.eclipse.dd.dsf.debug.service.command.ICommandControlService.ICommandControlInitializedDMEvent;
import org.eclipse.dd.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IMemento;


@SuppressWarnings("restriction")
public class ContainerVMNode extends AbstractContainerVMNode
    implements IElementMementoProvider
{
	public ContainerVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session);
	}
	
	@Override
	public String toString() {
	    return "ContainerVMNode(" + getSession().getId() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void updateElementsInSessionThread(final IChildrenUpdate update) {
		IProcesses processService = getServicesTracker().getService(IProcesses.class);
		ICommandControlService controlService = getServicesTracker().getService(ICommandControlService.class);
		if (processService == null || controlService == null) {
			handleFailedUpdate(update);
			return;
		}

		processService.getProcessesBeingDebugged(
				controlService.getContext(),
				new ViewerDataRequestMonitor<IDMContext[]>(getExecutor(), update) {
					@Override
					public void handleCompleted() {
						if (!isSuccess()) {
							handleFailedUpdate(update);
							return;
						}
						if (getData() != null) fillUpdateWithVMCs(update, getData());
						update.done();
					}
				});
	}

	
	@Override
	protected void updateLabelInSessionThread(final ILabelUpdate update) {
		IProcesses processService = getServicesTracker().getService(IProcesses.class);
		IRunControl runControl = getServicesTracker().getService(IRunControl.class);
		if (processService == null || runControl == null) {
			handleFailedUpdate(update);
			return;
		}

		final IProcessDMContext procDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IProcessDMContext.class);
		final IContainerDMContext contDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IContainerDMContext.class);

		String imageKey = null;
		if (runControl.isSuspended(contDmc)) {
			imageKey = IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
		} else {
			imageKey = IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
		}
		update.setImageDescriptor(DebugUITools.getImageDescriptor(imageKey), 0);

		processService.getExecutionData(
				procDmc,
				new ViewerDataRequestMonitor<IThreadDMData>(getExecutor(), update) {
					@Override
					public void handleCompleted() {
						if (!isSuccess()) {
							update.setLabel("<unavailable>", 0); //$NON-NLS-1$
							update.done();
							return;
						}

						// Create Labels of type Name[PID] if the pid is available
						final StringBuilder builder = new StringBuilder();
						builder.append(getData().getName());
						if (getData().getId() != null && getData().getId().length() > 0) {
							builder.append("[" + getData().getId()+ "]"); //$NON-NLS-1$//$NON-NLS-2$
						}
						update.setLabel(builder.toString(), 0);
						update.done();
					}
				});
	}

	@Override
	public int getDeltaFlags(Object e) {
		if (e instanceof ICommandControlShutdownDMEvent) {
	        return IModelDelta.CONTENT;
	    } else if (e instanceof ICommandControlInitializedDMEvent) {
	        return IModelDelta.EXPAND;
	    }
	    return super.getDeltaFlags(e);
	}

	@Override
	public void buildDelta(Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
		if (e instanceof ICommandControlShutdownDMEvent) {
	        parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
	    } else if (e instanceof ICommandControlInitializedDMEvent) {
	        parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.EXPAND);
	    } else {
	    	super.buildDelta(e, parentDelta, nodeOffset, requestMonitor);
	    	return;
	    }
		requestMonitor.done();
	 }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#compareElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest[])
     */
    private final String MEMENTO_NAME = "CONTAINER_MEMENTO_NAME"; //$NON-NLS-1$
    
    public void compareElements(IElementCompareRequest[] requests) {
    	for (final IElementCompareRequest request : requests) {

    		Object element = request.getElement();
    		final IMemento memento = request.getMemento();
    		final String mementoName = memento.getString(MEMENTO_NAME);

    		if (mementoName != null) {
    			if (element instanceof IDMVMContext) {

    				IDMContext dmc = ((IDMVMContext)element).getDMContext();

    				if (dmc instanceof IContainerDMContext)
    				{
    					final IProcessDMContext procDmc = findDmcInPath(request.getViewerInput(), request.getElementPath(), IProcessDMContext.class);

    					if (procDmc != null) {
    						try {
    							getSession().getExecutor().execute(new DsfRunnable() {
    								public void run() {
    									final IProcesses processService = getServicesTracker().getService(IProcesses.class);
    									if (processService != null) {
    										processService.getExecutionData(
    												procDmc,
    												new ViewerDataRequestMonitor<IThreadDMData>(processService.getExecutor(), request) {
    													@Override
    													protected void handleCompleted() {
    														if ( getStatus().isOK() ) {
    															memento.putString(MEMENTO_NAME, "Container." + getData().getName() + getData().getId()); //$NON-NLS-1$
    														}
    														request.done();
    													}
    												});
    									}
    									else {
    										request.done();
    									}
    								}
    							});
    						} catch (RejectedExecutionException e) {
    							request.done();
    						}

    						continue;
    					}
    				}
    			}
    		}
    		request.done();
    	}
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#encodeElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest[])
     */
    public void encodeElements(IElementMementoRequest[] requests) {
    	for (final IElementMementoRequest request : requests) {

    		Object element = request.getElement();
    		final IMemento memento = request.getMemento();

    		if (element instanceof IDMVMContext) {

    			IDMContext dmc = ((IDMVMContext)element).getDMContext();

    			if (dmc instanceof IContainerDMContext)
    			{
    				final IProcessDMContext procDmc = findDmcInPath(request.getViewerInput(), request.getElementPath(), IProcessDMContext.class);

    				if (procDmc != null) {
    					try {
    						getSession().getExecutor().execute(new DsfRunnable() {
    							public void run() {
    								final IProcesses processService = getServicesTracker().getService(IProcesses.class);
    								if (processService != null) {
    									processService.getExecutionData(
    											procDmc,
    											new ViewerDataRequestMonitor<IThreadDMData>(processService.getExecutor(), request) {
    												@Override
    												protected void handleCompleted() {
    													if ( getStatus().isOK() ) {
    														memento.putString(MEMENTO_NAME, "Container." + getData().getName() + getData().getId()); //$NON-NLS-1$
    													}
    													request.done();
    												}
    											});
    								} else {
    									request.done();
    								}
    							}
    						});
    					} catch (RejectedExecutionException e) {
    						request.done();
    					}

    					continue;
    				}
    			}
    		}
    		request.done();
    	}
    }
}
