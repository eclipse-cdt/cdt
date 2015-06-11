/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import java.util.Vector;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractContainerVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ExecutionContextLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.FullStackRefreshEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ILaunchVMConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.provisional.service.IMIExecutionContextTranslator;
import org.eclipse.cdt.dsf.gdb.internal.provisional.service.IMIExecutionContextTranslator.IContainerLayoutChangedEvent;
import org.eclipse.cdt.dsf.gdb.internal.provisional.service.IMIExecutionContextTranslator.IGroupDMContext;
import org.eclipse.cdt.dsf.gdb.internal.provisional.service.IMIExecutionContextTranslator.IGroupDMData;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IThreadRemovedDMEvent;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerCountingRequestMonitor;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelAttribute;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelColumnInfo;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelImage;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesBasedLabelProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.VMDelegatingPropertiesUpdate;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;


public class GroupVMNode extends AbstractContainerVMNode
    implements IElementLabelProvider, IElementMementoProvider 
{
	/** Indicator that we should not display running threads */
	private boolean fHideRunningThreadsProperty = false;
	
	/** PropertyChangeListener to keep track of the PREF_HIDE_RUNNING_THREADS preference */
	private IPropertyChangeListener fPropertyChangeListener = new IPropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(IGdbDebugPreferenceConstants.PREF_HIDE_RUNNING_THREADS)) {
				fHideRunningThreadsProperty = (Boolean)event.getNewValue();
			}
		}
	};
	
	public GroupVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session);

        IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
        store.addPropertyChangeListener(fPropertyChangeListener);
        fHideRunningThreadsProperty = store.getBoolean(IGdbDebugPreferenceConstants.PREF_HIDE_RUNNING_THREADS);
	}
	
    @Override
    public void dispose() {
		GdbUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPropertyChangeListener);
    	super.dispose();
    }
 
	@Override
	public String toString() {
	    return "GroupVMNode(" + getSession().getId() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
    protected IElementLabelProvider createLabelProvider() {
        PropertiesBasedLabelProvider provider = new PropertiesBasedLabelProvider();
        
        provider.setColumnInfo(
            PropertiesBasedLabelProvider.ID_COLUMN_NO_COLUMNS, 
            new LabelColumnInfo(new LabelAttribute[] { 
            	
            	/* ACTIVE GROUP LABEL */
                new GdbExecutionContextLabelText(
                MessagesForGdbLaunchVM.GroupVMNode_No_columns__text_format,
                    new String[] { 
                        ExecutionContextLabelText.PROP_NAME_KNOWN, 
                        PROP_NAME,  
                        IGdbLaunchVMConstants.PROP_THREAD_SUMMARY_KNOWN, 
                        IGdbLaunchVMConstants.PROP_THREAD_SUMMARY }), 
                        
                new LabelText(MessagesForGdbLaunchVM.ContainerVMNode_No_columns__Error__label, new String[0]),
                
				/* RUNNING GROUP */
				new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET)) {
                    { setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED }); }
                    
                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                        return Boolean.FALSE.equals(properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED));
                    };
                },
                
                /* SUSPENDED GROUP */
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET_SUSPENDED)),
            }));
        
        return provider;
    }
    
	@Override
	protected void updateElementsInSessionThread(final IChildrenUpdate update) {
		IRunControl runControl = getServicesTracker().getService(IRunControl.class);
		if (runControl == null) {
			handleFailedUpdate(update);
			return;
		}

		// If there is already a container (group or process) in the path, we ask for its children,
		// as they could be groups. If there is not, we'll get a null and then the service will 
		// give us the top groups.
		IContainerDMContext container = findDmcInPath(update.getViewerInput(), update.getElementPath(), IContainerDMContext.class);
		runControl.getExecutionContexts(
			container,
			new ViewerDataRequestMonitor<IExecutionDMContext[]>(getExecutor(), update) {
				@Override
				public void handleCompleted() {
					if (!isSuccess()) {
						handleFailedUpdate(update);
						return;
					}
					
					IExecutionDMContext[] execDmcs = getData();
					if (execDmcs != null) {
						// Extract the groups as there could be processes or threads in the answer
						Vector<IGroupDMContext> groupDmcs = new Vector<>();
						for (IExecutionDMContext exec : execDmcs) {
							if (exec instanceof IGroupDMContext) {
								groupDmcs.add((IGroupDMContext)exec);
							}
						}
						fillUpdateWithVMCs(
								update,
								groupDmcs.toArray(new IGroupDMContext[groupDmcs.size()]));
					}
					update.done();
				}
			});
	}
    
    @Override
    protected void updatePropertiesInSessionThread(IPropertiesUpdate[] updates) {
        IPropertiesUpdate[] parentUpdates = new IPropertiesUpdate[updates.length]; 
        
        for (int i = 0; i < updates.length; i++) {
        	final IPropertiesUpdate update = updates[i];
        	
            final ViewerCountingRequestMonitor countingRm = 
                    new ViewerCountingRequestMonitor(ImmediateExecutor.getInstance(), update);
            int count = 0;

            // Create a delegating update which will let the super-class fill in the 
            // standard container properties.
            parentUpdates[i] = new VMDelegatingPropertiesUpdate(update, countingRm);
            count++;

    		if (update.getProperties().contains(PROP_NAME)) {
    			IGroupDMContext groupDmc = (IGroupDMContext)((IDMVMContext)update.getElement()).getDMContext();
    			IMIExecutionContextTranslator execTranslator = getServicesTracker().getService(IMIExecutionContextTranslator.class);
    			if (execTranslator == null) {
    				update.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Service or handle invalid", null)); //$NON-NLS-1$            			
    			} else {
    				execTranslator.getExecutionData(
    					groupDmc,
    					new ViewerDataRequestMonitor<IGroupDMData>(getExecutor(), update) {
    						@Override
    						public void handleCompleted() {
    							// A group only has a name
    							if (isSuccess()) {
    								update.setProperty(PROP_NAME, getData().getName());
    							} else {
    								update.setStatus(getStatus());
    							}
    							countingRm.done();
    						}
    					});
    				count++;
    			}
    		}
    		
            if (update.getProperties().contains(IGdbLaunchVMConstants.PROP_THREAD_SUMMARY)) {
            	// TODO We should only show the filtered threads belonging to the group
            	// What about the process node?  Should it count all its filtered threads
            	// or only ones that are not in a sub group?
            	// fillThreadSummary(update, countingRm);
            	// count++;
            }
                        
            countingRm.setDoneCount(count);
        }
        
        super.updatePropertiesInSessionThread(parentUpdates);
    }
    
//    protected void fillThreadSummary(final IPropertiesUpdate update, final RequestMonitor rm) {
    // Can imitate code from ContainerVMNode.fillThreadSummary if needed
//    }
    
	@Override
	public int getDeltaFlags(Object e) {
		// TODO
		
		if (e instanceof IContainerLayoutChangedEvent) {
			return IModelDelta.CONTENT;
		}
		
		if (e instanceof ISuspendedDMEvent) {
			return IModelDelta.STATE;
		}

		if (e instanceof ICommandControlShutdownDMEvent) {
			return IModelDelta.CONTENT;
		}
		if (e instanceof IThreadRemovedDMEvent) {
		    IDMContext dmc = e instanceof IDMEvent<?> ? ((IDMEvent<?>)e).getDMContext() : null;
		    if (dmc instanceof IProcessDMContext) {
				return IModelDelta.CONTENT;		    	
		    }
		    return IModelDelta.NO_CHANGE;
	    }
	    return super.getDeltaFlags(e);
	}

	@Override
	public void buildDelta(Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
		// TODO

		if (buildDeltaForRecursiveVMNode(e, parentDelta, nodeOffset, requestMonitor))
			return;

		if (e instanceof IStartedDMEvent) {
			parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
			requestMonitor.done();
			return;
		}

		if (e instanceof IContainerLayoutChangedEvent) {
			parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
			requestMonitor.done();
			return;
		}
		
		if (e instanceof ICommandControlShutdownDMEvent) {
	        parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
	    } else if (e instanceof IThreadRemovedDMEvent) {
		    IDMContext dmc = e instanceof IDMEvent<?> ? ((IDMEvent<?>)e).getDMContext() : null;
		    if (dmc instanceof IProcessDMContext) {
		    	// A process was removed, refresh the parent
		    	parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
		    }
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
    
    @Override
    public void compareElements(IElementCompareRequest[] requests) {
		// TODO

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
    				                @Override
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
    @Override
    public void encodeElements(IElementMementoRequest[] requests) {
		// TODO

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
    			                @Override
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

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractContainerVMNode#getContextsForEvent(org.eclipse.cdt.dsf.ui.viewmodel.VMDelta, java.lang.Object, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
    @Override
    public void getContextsForEvent(VMDelta parentDelta, Object event, final DataRequestMonitor<IVMContext[]> rm) {
		// TODO

    	if (event instanceof FullStackRefreshEvent &&
    			((FullStackRefreshEvent)event).getDMContext() instanceof IContainerDMContext)
    	{
    		// The step sequence end event occurred on a container and not on a thread.  Do not
    		// return a context for this event, which will force the view model to generate
    		// a delta for all the threads.
    		rm.done(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "", null)); //$NON-NLS-1$
    		return;
    	}

    	if( getContextsForRecursiveVMNode( parentDelta, event, rm))
    		return;

    	super.getContextsForEvent(parentDelta, event, rm);
    }
}
