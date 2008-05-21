/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for multi threaded functionality	
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.ui.viewmodel.launch;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.debug.service.StepQueueManager.ISteppingTimedOutEvent;
import org.eclipse.dd.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.ModelProxyInstalledEvent;
import org.eclipse.dd.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.dd.gdb.internal.provisional.service.GDBRunControl;
import org.eclipse.dd.gdb.internal.provisional.service.GDBRunControl.GDBThreadData;
import org.eclipse.dd.mi.service.IMIExecutionDMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IMemento;


@SuppressWarnings("restriction")
public class ThreadVMNode extends AbstractDMVMNode 
    implements IElementLabelProvider, IElementMementoProvider
{
    public ThreadVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, IExecutionDMContext.class);
    }

    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
    	IRunControl runControl = getServicesTracker().getService(IRunControl.class);
    	final IContainerDMContext contDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IContainerDMContext.class);
    	if (runControl == null || contDmc == null) {
    		handleFailedUpdate(update);
    		return;
    	} 

    	runControl.getExecutionContexts(contDmc, 
    			new ViewerDataRequestMonitor<IExecutionDMContext[]>(getSession().getExecutor(), update){
    				@Override
    				public void handleCompleted() {
    					if (!isSuccess()) {
    						handleFailedUpdate(update);
    						return;
    					}
    					fillUpdateWithVMCs(update, getData());
    					update.done();
    				}
    			});
    }


    public void update(final ILabelUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    updateLabelInSessionThread(updates);
                }});
        } catch (RejectedExecutionException e) {
            for (ILabelUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }

    @Override
    public void getContextsForEvent(VMDelta parentDelta, Object e, final DataRequestMonitor<IVMContext[]> rm) {
        if(e instanceof IContainerResumedDMEvent) {
            IExecutionDMContext[] triggerContexts = ((IContainerResumedDMEvent)e).getTriggeringContexts();
            if (triggerContexts.length != 0) {
                rm.setData(new IVMContext[] { createVMContext(triggerContexts[0]) });
                rm.done();
                return;
            }
        } else if(e instanceof IContainerSuspendedDMEvent) {
            IExecutionDMContext[] triggerContexts = ((IContainerSuspendedDMEvent)e).getTriggeringContexts();
            if (triggerContexts.length != 0) {
                rm.setData(new IVMContext[] { createVMContext(triggerContexts[0]) });
                rm.done();
                return;
            }
        } else if (e instanceof ISteppingTimedOutEvent && 
                   ((ISteppingTimedOutEvent)e).getDMContext() instanceof IContainerDMContext) 
        {
            // The timed out event occured on a container and not on a thread.  Do not
            // return a context for this event, which will force the view model to generate
            // a delta for all the threads.
            rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "", null)); //$NON-NLS-1$
            rm.done();
        } else if (e instanceof ModelProxyInstalledEvent) {
            getThreadVMCForModelProxyInstallEvent(
                parentDelta, 
                new DataRequestMonitor<VMContextInfo>(getExecutor(), rm) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            rm.setData(new IVMContext[] { getData().fVMContext });
                        } else {
                            rm.setData(new IVMContext[0]);
                        }
                        rm.done();
                    }
                });
            return;
        }
        super.getContextsForEvent(parentDelta, e, rm);
    }
    
    private static class VMContextInfo {
        final IVMContext fVMContext;
        final int fIndex;
        final boolean fIsSuspended;
        VMContextInfo(IVMContext vmContext, int index, boolean isSuspended) {
            fVMContext = vmContext;
            fIndex = index;
            fIsSuspended = isSuspended;
        }
    }
    
    private void getThreadVMCForModelProxyInstallEvent(VMDelta parentDelta, final DataRequestMonitor<VMContextInfo> rm) {
        getVMProvider().updateNode(this, new VMChildrenUpdate(
            parentDelta, getVMProvider().getPresentationContext(), -1, -1, 
            new DataRequestMonitor<List<Object>>(getExecutor(), rm) {
                @Override
                protected void handleSuccess() {
                    try {
                        getSession().getExecutor().execute(new DsfRunnable() {
                            public void run() {
                                final GDBRunControl runControl = getServicesTracker().getService(GDBRunControl.class);
                                if (runControl != null) {
                                    int vmcIdx = -1;
                                    int suspendedVmcIdx = -1;
                                    
                                    for (int i = 0; i < getData().size(); i++) {
                                        if (getData().get(i) instanceof IDMVMContext) {
                                            IDMVMContext vmc = (IDMVMContext)getData().get(i);
                                            IExecutionDMContext execDmc = DMContexts.getAncestorOfType(
                                                vmc.getDMContext(), IExecutionDMContext.class);
                                            if (execDmc != null) {
                                                vmcIdx = vmcIdx < 0 ? i : vmcIdx; 
                                                if (runControl.isSuspended(execDmc)) {
                                                    suspendedVmcIdx = suspendedVmcIdx < 0 ? i : suspendedVmcIdx;                                                     
                                                }
                                            }
                                        }
                                    }
                                    if (suspendedVmcIdx >= 0) {
                                        rm.setData(new VMContextInfo(
                                            (IVMContext)getData().get(suspendedVmcIdx), suspendedVmcIdx, true));
                                    } else if (vmcIdx >= 0) {
                                        rm.setData(new VMContextInfo((IVMContext)getData().get(vmcIdx), vmcIdx, false));
                                    } else {
                                        rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "No threads available", null)); //$NON-NLS-1$
                                    }
                                    rm.done();
                                } else {
                                    rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "No threads available", null)); //$NON-NLS-1$
                                    rm.done();
                                }
                            }
                        });
                    } catch (RejectedExecutionException e) {
                        rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "", null)); //$NON-NLS-1$
                        rm.done();
                    }
                }
            }));
    }
    
    protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
        	final GDBRunControl runControl = getServicesTracker().getService(GDBRunControl.class);
            if ( runControl == null ) {
                    handleFailedUpdate(update);
                    continue;
            }
            
            final IMIExecutionDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IMIExecutionDMContext.class);

            String imageKey = null;
            if (getServicesTracker().getService(IRunControl.class).isSuspended(dmc)) {
                imageKey = IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
            } else {
                imageKey = IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
            }
            update.setImageDescriptor(DebugUITools.getImageDescriptor(imageKey), 0);

            // Find the Reason for the State
            runControl.getExecutionData(dmc, 
            		new ViewerDataRequestMonitor<IExecutionDMData>(getSession().getExecutor(), update) { 
            	@Override
				public void handleCompleted(){
                    if (!isSuccess()) {
                        handleFailedUpdate(update);
                        return;
                    }

                    // We're in a new dispatch cycle, and we have to check whether the 
                    // service reference is still valid.
                    final GDBRunControl runControl = getServicesTracker().getService(GDBRunControl.class);
                    if ( runControl == null ) {
                        handleFailedUpdate(update);
                        return;
                    }

                    final StateChangeReason reason = getData().getStateChangeReason();

                    // Retrieve the rest of the thread information
                    runControl.getThreadData(
                        dmc, 
                        new ViewerDataRequestMonitor<GDBThreadData>(getSession().getExecutor(), update) { 
                            @Override
                            public void handleCompleted() {
                                if (!isSuccess()) {
                                    update.done();
                                    return;
                                }
                                // Create Labels of type Thread[GDBthreadId]RealThreadID/Name (State: Reason)
                                // Thread[1] 3457 (Suspended:BREAKPOINT)
                                final StringBuilder builder = new StringBuilder("Thread["); //$NON-NLS-1$
                                builder.append(dmc.getThreadId());
                                builder.append("] "); //$NON-NLS-1$
                                builder.append(getData().getId());
                                builder.append(getData().getName());
                                if(getServicesTracker().getService(IRunControl.class).isSuspended(dmc))
                                    builder.append(" (Suspended"); //$NON-NLS-1$
                                else
                                    builder.append(" (Running"); //$NON-NLS-1$
                                // Reason will be null before ContainerSuspendEvent is fired
                                if(reason != null) {
                                    builder.append(" : "); //$NON-NLS-1$
                                    builder.append(reason);
                                }
                                builder.append(")"); //$NON-NLS-1$
                                update.setLabel(builder.toString(), 0);
                                update.done();
                            }
                        });
            	}
            });
            
        }
    }

    public int getDeltaFlags(Object e) {
        if(e instanceof IResumedDMEvent || e instanceof ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        } 
        if (e instanceof ModelProxyInstalledEvent) {
            return IModelDelta.SELECT | IModelDelta.EXPAND;
        }
        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor rm) {
        if(e instanceof IContainerResumedDMEvent) {
            IExecutionDMContext[] triggeringContexts = ((IContainerResumedDMEvent)e).getTriggeringContexts();
            if (triggeringContexts.length != 0) {
                parentDelta.addNode(createVMContext(triggeringContexts[0]), IModelDelta.CONTENT);
            }
            rm.done();
        } else if (e instanceof IContainerSuspendedDMEvent) {
            IExecutionDMContext[] triggeringContexts = ((IContainerSuspendedDMEvent)e).getTriggeringContexts();
            if (triggeringContexts.length != 0) {
                parentDelta.addNode(createVMContext(triggeringContexts[0]), IModelDelta.CONTENT);
            }
            rm.done();
        } else if(e instanceof IResumedDMEvent || e instanceof ISuspendedDMEvent) {
            parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.CONTENT);
            rm.done();
        } else if (e instanceof ModelProxyInstalledEvent) {
            getThreadVMCForModelProxyInstallEvent(
                parentDelta, 
                new DataRequestMonitor<VMContextInfo>(getExecutor(), rm) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            parentDelta.addNode(
                                getData().fVMContext, nodeOffset + getData().fIndex, 
                                IModelDelta.EXPAND | (getData().fIsSuspended ? 0 : IModelDelta.SELECT));
                        }
                        rm.done();
                    }
                });
        } else {
            
            rm.done();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#compareElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest[])
     */
    
    private String produceThreadElementName( String viewName , IMIExecutionDMContext execCtx ) {
    	return "Thread." + execCtx.getThreadId(); //$NON-NLS-1$
    }
    
    private final String MEMENTO_NAME = "THREAD_MEMENTO_NAME"; //$NON-NLS-1$
    
    public void compareElements(IElementCompareRequest[] requests) {
        
        for ( IElementCompareRequest request : requests ) {
        	
            Object element = request.getElement();
            IMemento memento = request.getMemento();
            String mementoName = memento.getString(MEMENTO_NAME);
            
            if (mementoName != null) {
                if (element instanceof IDMVMContext) {
                	
                    IDMContext dmc = ((IDMVMContext)element).getDMContext();
                    
                    if ( dmc instanceof IMIExecutionDMContext) {
                    	
                    	String elementName = produceThreadElementName( request.getPresentationContext().getId(), (IMIExecutionDMContext) dmc );
                    	request.setEqual( elementName.equals( mementoName ) );
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
    	
    	for ( IElementMementoRequest request : requests ) {
    		
            Object element = request.getElement();
            IMemento memento = request.getMemento();
            
            if (element instanceof IDMVMContext) {

            	IDMContext dmc = ((IDMVMContext)element).getDMContext();

            	if ( dmc instanceof IMIExecutionDMContext) {
                	
            		String elementName = produceThreadElementName( request.getPresentationContext().getId(), (IMIExecutionDMContext) dmc );
                	memento.putString(MEMENTO_NAME, elementName);
                } 
            }
            request.done();
        }
    }
}
