/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
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
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Abstract implementation of a container view model node.
 * Clients need to implement {@link #updateLabelInSessionThread(ILabelUpdate[])}.
 * 
 * @since 1.1
 */
public abstract class AbstractContainerVMNode extends AbstractExecutionContextVMNode 
    implements IElementLabelProvider, IElementPropertiesProvider 
 {
    /**
     * The label provider delegate.  This VM node will delegate label updates to this provider
     * which can be created by sub-classes. 
     *  
     * @since 2.0
     */    
    private IElementLabelProvider fLabelProvider;
    
	public AbstractContainerVMNode(AbstractDMVMProvider provider, DsfSession session) {
		super(provider, session, IRunControl.IContainerDMContext.class);
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
                new ExecutionContextLabelText(
                    MessagesForLaunchVM.AbstractContainerVMNode_No_columns__text_format,
                    new String[] { 
                        ExecutionContextLabelText.PROP_NAME_KNOWN, 
                        PROP_NAME,  
                        ExecutionContextLabelText.PROP_ID_KNOWN, 
                        ILaunchVMConstants.PROP_ID }), 
                new LabelText(MessagesForLaunchVM.AbstractContainerVMNode_No_columns__Error__label, new String[0]),
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_SUSPENDED)) {
                    { setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED }); }
                    
                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                        return Boolean.TRUE.equals(properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED));
                    };
                },
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET)),
            }));
        
        return provider;
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
    	if (e instanceof ModelProxyInstalledEvent || e instanceof DataModelInitializedEvent) {
    		getContainerVMCForModelProxyInstallEvent(
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
    	} else {
    		super.getContextsForEvent(parentDelta, e, rm);
    	}
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
    
    private void getContainerVMCForModelProxyInstallEvent(VMDelta parentDelta, final DataRequestMonitor<VMContextInfo> rm) {
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
                                            IContainerDMContext containerDmc = DMContexts.getAncestorOfType(
                                                vmc.getDMContext(), IContainerDMContext.class);
                                            if (containerDmc != null) {
                                                vmcIdx = vmcIdx < 0 ? i : vmcIdx;
                                                if (runControl.isSuspended(containerDmc)) {
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
                                        rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "No container available", null)); //$NON-NLS-1$
                                    }
                                    rm.done();
                                } else {
                                    rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "No container available", null)); //$NON-NLS-1$
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

	    if (e instanceof IContainerResumedDMEvent) {
            if (((IContainerResumedDMEvent)e).getReason() != IRunControl.StateChangeReason.STEP) 
            {
                return IModelDelta.CONTENT;
            }
        } else if (e instanceof IContainerSuspendedDMEvent) {
            return IModelDelta.NO_CHANGE;
        } else if (e instanceof SteppingTimedOutEvent) {
	        if (dmc instanceof IContainerDMContext) 
	        {
	            return IModelDelta.CONTENT;
	        }
	    } else if (e instanceof IExitedDMEvent) {
	        return IModelDelta.CONTENT;
	    } else if (e instanceof IStartedDMEvent) {
	    	if (dmc instanceof IContainerDMContext) {
	    		return IModelDelta.EXPAND | IModelDelta.SELECT;
	    	} else {
		        return IModelDelta.CONTENT;
	    	}
        } else if (e instanceof ModelProxyInstalledEvent || e instanceof DataModelInitializedEvent) {
            return IModelDelta.SELECT | IModelDelta.EXPAND;
	    } else if (e instanceof StateChangedEvent) {
	    	return IModelDelta.STATE;
	    }
	    return IModelDelta.NO_CHANGE;
	}

	@Override
	public void buildDelta(Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
	    IDMContext dmc = e instanceof IDMEvent<?> ? ((IDMEvent<?>)e).getDMContext() : null;
	    
		if(e instanceof IContainerResumedDMEvent) {
            // Container resumed: 
		    // - If not stepping, update the container and the execution 
		    // contexts under it.  
		    // - If stepping, do nothing to avoid too many updates.  If a 
		    // time-out is reached before the step completes, the 
		    // ISteppingTimedOutEvent will trigger a full refresh.
		    if (((IContainerResumedDMEvent)e).getReason() != IRunControl.StateChangeReason.STEP) 
		    {
    	        parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.CONTENT);
		    } 
		} else if (e instanceof IContainerSuspendedDMEvent) {
            // Container suspended.  Do nothing here to give the stack the 
		    // priority in updating. The container and threads will update as 
		    // a result of FullStackRefreshEvent. 
		} else if (e instanceof SteppingTimedOutEvent) {
		    // Stepping time-out indicates that a step operation is taking 
		    // a long time, and the view needs to be refreshed to show 
		    // the user that the program is running.
		    // If the step was issued for the whole container refresh
		    // the whole container.
		    if (dmc instanceof IContainerDMContext) {
	            parentDelta.addNode(createVMContext(dmc), IModelDelta.CONTENT);
		    }
		} else if (e instanceof IExitedDMEvent) {
		    // An exited event could either be for a thread within a container
		    // or for the container itself.  
		    // If a container exited, refresh the parent element so that the 
		    // container may be removed.
		    // If a thread exited within a container, refresh that container.
			if (dmc instanceof IContainerDMContext) {
	    		parentDelta.setFlags(parentDelta.getFlags() |  IModelDelta.CONTENT);
	    	} else {
		        IContainerDMContext containerCtx = DMContexts.getAncestorOfType(dmc, IContainerDMContext.class);
		        if (containerCtx != null) {
		            parentDelta.addNode(createVMContext(containerCtx), IModelDelta.CONTENT);
		        }
	    	}
	    } else if (e instanceof IStartedDMEvent) {
            // A started event could either be for a thread within a container
            // or for the container itself.  
            // If a container started, issue an expand and select event to 
	        // show the threads in the new container. 
	        // Note: the EXPAND flag implies refreshing the parent element.
			if (dmc instanceof IContainerDMContext) {
		        parentDelta.addNode(createVMContext(dmc), IModelDelta.EXPAND | IModelDelta.SELECT);
			} else {
				IContainerDMContext containerCtx = DMContexts.getAncestorOfType(dmc, IContainerDMContext.class);
				if (containerCtx != null) {
					parentDelta.addNode(createVMContext(containerCtx), IModelDelta.CONTENT);
				}
			}
        } else if (e instanceof ModelProxyInstalledEvent || e instanceof DataModelInitializedEvent) {
            // Model Proxy install event is generated when the model is first 
            // populated into the view.  This happens when a new debug session
            // is started or when the view is first opened.  
            // In both cases, if there are already thread containers in the debug model, 
            // the desired user behavior is to show the containers and to select
            // the first thread.  
            // If the container is suspended, do not select it, instead, 
            // one of its threads will be selected.
            getContainerVMCForModelProxyInstallEvent(
                parentDelta,
                new DataRequestMonitor<VMContextInfo>(getExecutor(), requestMonitor) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            parentDelta.addNode(
                                getData().fVMContext, nodeOffset + getData().fIndex,
                                IModelDelta.EXPAND | (getData().fIsSuspended ? 0 : IModelDelta.SELECT));
                        }
                        requestMonitor.done();
                    }
                });
            return;
	    } else if (e instanceof StateChangedEvent) {	    	
	    	// If there is a state change needed on the container, update the container
	    	if (dmc instanceof IContainerDMContext)
	    		parentDelta.addNode(createVMContext(dmc), IModelDelta.STATE);
	    }
	
		requestMonitor.done();
	 }

}
