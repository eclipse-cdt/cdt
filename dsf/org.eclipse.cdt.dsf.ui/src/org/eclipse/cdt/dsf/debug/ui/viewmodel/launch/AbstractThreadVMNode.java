/*******************************************************************************
 * Copyright (c) 2006, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for multi threaded functionality
 *     Patrick Chuong (Texas Instruments) - Add support for icon overlay in the debug view (Bug 334566)
 *     Dobrin Alexiev (Texas Instruments) - user groups support  (bug 240208)   
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMData2;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController.SteppingTimedOutEvent;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.ModelProxyInstalledEvent;
import org.eclipse.cdt.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelAttribute;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelColumnInfo;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelImage;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesBasedLabelProvider;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;


/**
 * Abstract implementation of a thread view model node.
 * Clients need to implement {@link #updateLabelInSessionThread(ILabelUpdate[])}.
 * 
 * @since 1.1
 */
public abstract class AbstractThreadVMNode extends AbstractExecutionContextVMNode
    implements IElementLabelProvider, IElementPropertiesProvider
{
    /**
     * The label provider delegate.  This VM node will delegate label updates to this provider
     * which can be created by sub-classes. 
     *  
     * @since 2.0
     */    
    private IElementLabelProvider fLabelProvider;

    public AbstractThreadVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, IExecutionDMContext.class);
        fLabelProvider = createLabelProvider();
    }

    
    /**
     * Creates the label provider delegate.  This VM node will delegate label 
     * updates to this provider which can be created by sub-classes.   
     *  
     * @return Returns the label provider for this node. 
     *  
     * @since 2.0
     */    
    protected IElementLabelProvider createLabelProvider() {
        PropertiesBasedLabelProvider provider = new PropertiesBasedLabelProvider();

        provider.setColumnInfo(
            PropertiesBasedLabelProvider.ID_COLUMN_NO_COLUMNS, 
            new LabelColumnInfo(new LabelAttribute[] { 
                // Text is made of the thread name followed by its state and state change reason. 
                new ExecutionContextLabelText(
                    MessagesForLaunchVM.AbstractThreadVMNode_No_columns__text_format,
                    new String[] { 
                        ExecutionContextLabelText.PROP_NAME_KNOWN, 
                        PROP_NAME, 
                        ExecutionContextLabelText.PROP_ID_KNOWN, 
                        ILaunchVMConstants.PROP_ID, 
                        ILaunchVMConstants.PROP_IS_SUSPENDED, 
                        ExecutionContextLabelText.PROP_STATE_CHANGE_REASON_KNOWN, 
                        ILaunchVMConstants.PROP_STATE_CHANGE_REASON,
                        ExecutionContextLabelText.PROP_STATE_CHANGE_DETAILS_KNOWN, 
                        ILaunchVMConstants.PROP_STATE_CHANGE_DETAILS }),
                new LabelText(MessagesForLaunchVM.AbstractThreadVMNode_No_columns__Error__label, new String[0]),
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING)) {
                    { setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED }); }
                    
                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                    	return Boolean.FALSE.equals(properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED));
                    };
                },
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED)),
            }));
        return provider;
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


    
    @Override
	public void update(final ILabelUpdate[] updates) {
        fLabelProvider.update(updates);
    }

    /**
     * @see IElementPropertiesProvider#update(IPropertiesUpdate[])
     * 
     * @since 2.0
     */    
    @Override
	public void update(final IPropertiesUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                @Override
				public void run() {
                    updatePropertiesInSessionThread(updates);
                }});
        } catch (RejectedExecutionException e) {
            for (IPropertiesUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }

    /**
     * @since 2.0
     */
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    protected void updatePropertiesInSessionThread(final IPropertiesUpdate[] updates) {
        IRunControl service = getServicesTracker().getService(IRunControl.class);
        
        for (final IPropertiesUpdate update : updates) {
            if (service == null) {
                handleFailedUpdate(update);
                continue;
            }

            IExecutionDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IExecutionDMContext.class);
            if (dmc == null) {
                handleFailedUpdate(update);
                continue;
            }

            update.setProperty(ILaunchVMConstants.PROP_IS_SUSPENDED, service.isSuspended(dmc));
            update.setProperty(ILaunchVMConstants.PROP_IS_STEPPING, service.isStepping(dmc));
            
            service.getExecutionData(
                dmc, 
                new ViewerDataRequestMonitor<IExecutionDMData>(getSession().getExecutor(), update) { 
                    @Override
                    protected void handleSuccess() {
                        fillExecutionDataProperties(update, getData());
                        update.done();
                    }
                });
        }        
    }
    
    protected void fillExecutionDataProperties(IPropertiesUpdate update, IExecutionDMData data) {
        StateChangeReason reason = data.getStateChangeReason();
        if (reason != null) {
            update.setProperty(ILaunchVMConstants.PROP_STATE_CHANGE_REASON, data.getStateChangeReason().name());
        }

        if (data instanceof IExecutionDMData2) {
        	String details = ((IExecutionDMData2)data).getDetails();
        	if (details != null) {
            	update.setProperty(ILaunchVMConstants.PROP_STATE_CHANGE_DETAILS, details);
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
        } else if (e instanceof SteppingTimedOutEvent && 
                ((SteppingTimedOutEvent)e).getDMContext() instanceof IContainerDMContext) 
     {
          // The timed out event occured on a container and not on a thread.  Do not
          // return a context for this event, which will force the view model to generate
          // a delta for all the threads.
          rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "", null)); //$NON-NLS-1$
          rm.done();
          return;
        } else if (e instanceof FullStackRefreshEvent &&
                ((FullStackRefreshEvent)e).getDMContext() instanceof IContainerDMContext)
        {
        	// The step sequence end event occured on a container and not on a thread.  Do not
        	// return a context for this event, which will force the view model to generate
        	// a delta for all the threads.
        	rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "", null)); //$NON-NLS-1$
        	rm.done();
        	return;
        } else if (e instanceof ModelProxyInstalledEvent || e instanceof DataModelInitializedEvent) {
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
                            @Override
							public void run() {
                                final IRunControl runControl = getServicesTracker().getService(IRunControl.class);
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
    
    
    @Override
	public int getDeltaFlags(Object e) {
        IDMContext dmc = e instanceof IDMEvent<?> ? ((IDMEvent<?>)e).getDMContext() : null;

        if (dmc instanceof IContainerDMContext) {
            return IModelDelta.NO_CHANGE;
        } else if (e instanceof IResumedDMEvent && 
                   ((IResumedDMEvent)e).getReason() != IRunControl.StateChangeReason.STEP) 
        {
            return IModelDelta.CONTENT;            
        } else if (e instanceof ISuspendedDMEvent) {
            return IModelDelta.NO_CHANGE;
        } else if (e instanceof SteppingTimedOutEvent) {
            return IModelDelta.CONTENT;            
        } else if (e instanceof ModelProxyInstalledEvent || e instanceof DataModelInitializedEvent) {
            return IModelDelta.SELECT | IModelDelta.EXPAND;
        } else if (e instanceof StateChangedEvent) {
        	return IModelDelta.STATE;
        }
        return IModelDelta.NO_CHANGE;
    }

    @Override
	public void buildDelta(Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor rm) {
        IDMContext dmc = e instanceof IDMEvent<?> ? ((IDMEvent<?>)e).getDMContext() : null;

        if(dmc instanceof IContainerDMContext) {
            // The IContainerDMContext sub-classes IExecutionDMContext.
            // Also IContainerResumedDMEvent sub-classes IResumedDMEvent and
            // IContainerSuspendedDMEvnet sub-classes ISuspendedEvent.
            // Because of this relationship, the thread VM node can be called
            // with data-model evnets for the containers.  This statement
            // filters out those event.
            rm.done();
        } else if(e instanceof IResumedDMEvent) {
            // Resumed: 
            // - If not stepping, update the thread and its content (its stack).
            // - If stepping, do nothing to avoid too many updates.  If a 
            // time-out is reached before the step completes, the 
            // ISteppingTimedOutEvent will trigger a refresh.
            if (((IResumedDMEvent)e).getReason() != IRunControl.StateChangeReason.STEP) {
                parentDelta.addNode(createVMContext(dmc), IModelDelta.CONTENT);
            }
            rm.done();
        } else if (e instanceof ISuspendedDMEvent) {
            // Container suspended.  Do nothing here to give the stack the 
            // priority in updating. The thread will update as a result of 
            // FullStackRefreshEvent. 
        	rm.done();
        } else if (e instanceof SteppingTimedOutEvent) {
            // Stepping time-out indicates that a step operation is taking 
            // a long time, and the view needs to be refreshed to show 
            // the user that the program is running.  
            parentDelta.addNode(createVMContext(dmc), IModelDelta.CONTENT);
            rm.done();            
        } else if (e instanceof ModelProxyInstalledEvent || e instanceof DataModelInitializedEvent) {
            // Model Proxy install event is generated when the model is first 
            // populated into the view.  This happens when a new debug session
            // is started or when the view is first opened.  
            // In both cases, if there are already threads in the debug model, 
            // the desired user behavior is to show the threads and to select
            // the first thread.  
            // If the thread is suspended, do not select the thread, instead, 
            // its top stack frame will be selected.
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
        } else if (e instanceof StateChangedEvent) {
        	parentDelta.addNode(createVMContext(dmc), IModelDelta.STATE);
        	rm.done();        	
        } else {            
            rm.done();
        }
    }

}
