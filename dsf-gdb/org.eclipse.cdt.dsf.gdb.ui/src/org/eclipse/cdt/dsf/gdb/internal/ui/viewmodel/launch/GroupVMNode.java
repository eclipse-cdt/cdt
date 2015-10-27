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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractContainerVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ExecutionContextLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ILaunchVMConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBGrouping;
import org.eclipse.cdt.dsf.gdb.service.IGDBGrouping.IGroupAddedEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBGrouping.IGroupDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBGrouping.IGroupDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBGrouping.IGroupDeletedEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBGrouping.IGroupModifiedEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IThreadRemovedDMEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
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
        super(provider, session, IGroupDMContext.class);

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
		// Note that currently, we don't support groups as children of processes, but that is
		// up to the service and the LaunchVMProvider, so let's keep the code here more flexible
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
    			IGDBGrouping execTranslator = getServicesTracker().getService(IGDBGrouping.class);
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
    			fillThreadSummary(update, countingRm);
    			count++;
    		}
                        
            countingRm.setDoneCount(count);
        }
        
        super.updatePropertiesInSessionThread(parentUpdates);
    }
    
    protected void fillThreadSummary(final IPropertiesUpdate update, final RequestMonitor rm) {
    	if (!fHideRunningThreadsProperty) {
    		// Disable the thread summary when we are not hiding threads
            update.setProperty(IGdbLaunchVMConstants.PROP_THREAD_SUMMARY, null);  
            rm.done();
            return;
    	}
    	
    	// Fetch all the threads that are directly children of this group
		final IRunControl runControl = getServicesTracker().getService(IRunControl.class);
        final IGroupDMContext groupDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IGroupDMContext.class);
        if (runControl == null || groupDmc == null) {
            update.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Service or handle invalid", null)); //$NON-NLS-1$
        	return;
        }
        
        runControl.getExecutionContexts(
			groupDmc,
			new ViewerDataRequestMonitor<IExecutionDMContext[]>(getSession().getExecutor(), update){
				@Override
				public void handleCompleted() {
					if (!isSuccess()) {
			            update.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Unable to get threads summary", null)); //$NON-NLS-1$
                        rm.done();
						return;
					}
					
					IExecutionDMContext[] execDmcs = getData();
					
					// Extract the threads by removing any container
					Vector<IExecutionDMContext> threadDmcs = new Vector<>();
					for (IExecutionDMContext exec : execDmcs) {
						if (!(exec instanceof IContainerDMContext)) {
							threadDmcs.add(exec);
						}
					}
					execDmcs = threadDmcs.toArray(new IExecutionDMContext[threadDmcs.size()]);
					
                    // For each thread, count how many are running and therefore hidden
					// Remove running threads from the list
					int runningCount = 0;
					for (IExecutionDMContext execDmc : execDmcs) {
						// Keep suspended or stepping threads
						if (!runControl.isSuspended(execDmc) && !runControl.isStepping(execDmc)) {
							runningCount++;
						}
					}
			        update.setProperty(IGdbLaunchVMConstants.PROP_THREAD_SUMMARY,
			        		           String.format("(%d %s)", runningCount, MessagesForGdbLaunchVM.ContainerVMNode_filtered_running_threads)); //$NON-NLS-1$
			        rm.done();
				}
			});
    }

    @Override
    public void getContextsForEvent(final VMDelta parentDelta, final Object event, final DataRequestMonitor<IVMContext[]> rm) {
    	if (event instanceof IDMEvent<?>) {
    		try {
    			getSession().getExecutor().execute(new Runnable() { 
    				@Override public void run() {
    					IDMEvent<?> dmEvent = (IDMEvent<?>)event;
    					Object eventCtx = dmEvent.getDMContext();
    					IExecutionDMContext execCtx = null;
    					if (eventCtx instanceof IExecutionDMContext) {
    						execCtx = (IExecutionDMContext)eventCtx;
    					}
    					else {
    						rm.setData(new IVMContext[0]);
    						rm.done();
    						return;
    					}

    					final DsfServicesTracker tracker = getServicesTracker();
    					if (tracker != null) {
    						IGDBGrouping groupingService = tracker.getService(IGDBGrouping.class);
    						if (groupingService == null) {
    							rm.setData(new IVMContext[0]);
    							rm.done();
    							return;
    						}

    						// 
    						groupingService.getGroupsContainingExecutionContext(execCtx,
    								new DataRequestMonitor<IGroupDMContext[]>(getExecutor(), rm) {
    									@Override
    									public void handleCompleted() {
    										if (isSuccess()) {
    											IGroupDMContext[] groups = getData();
    											List<IVMContext> contextsForEvent = new ArrayList<IVMContext>();

    											for (int i = 0; i < groups.length; i++) {
    												// For the suspended event, we want only the first group 
    												// containing the thread to be expanded/selected. Other events
    												// should generate a delta for every group the thread appears in
    												// e.g. FullStackRefresh event
    												if (i == 0 || !(event instanceof ISuspendedDMEvent)) {
    													contextsForEvent.add(createVMContext(groups[i]));
    												} 
    												// collapse other groups containing thread?
    												/*else {
    													if (event instanceof FullStackRefreshEvent) {
    														parentDelta.addNode(createVMContext(groups[i]), IModelDelta.COLLAPSE);
    													}
    												}*/
    											}    											
    											rm.setData(contextsForEvent.toArray(new IVMContext[contextsForEvent.size()]));
    										}
    										else {
    											rm.setData(new IVMContext[0]);
    											rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "", null)); //$NON-NLS-1$
    										}
    										rm.done();
    									}
    								});
    					}
    					else {
    						rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "", null)); //$NON-NLS-1$
    						rm.setData(new IVMContext[0]);
    						rm.done();
    					}
    				}});
    		} catch (RejectedExecutionException e) {
    			// Session shut down, no delta to build.
    			rm.done();
    		}
    	}
    	else {
    		super.getContextsForEvent(parentDelta, event, rm);
    	}
    }
    
    
    
	@Override
	public int getDeltaFlags(Object e) {
		if (e instanceof IGroupAddedEvent || e instanceof IGroupDeletedEvent) {
			return IModelDelta.CONTENT;
		}
		
		if (e instanceof IGroupModifiedEvent) {
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
		IDMContext dmc = e instanceof IDMEvent<?> ? ((IDMEvent<?>)e).getDMContext() : null;
		
		if (buildDeltaForRecursiveVMNode(e, parentDelta, nodeOffset, requestMonitor))
			return;
		
		if (e instanceof IGroupModifiedEvent) {
			parentDelta.addNode(createVMContext(dmc), IModelDelta.CONTENT);
//			parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
			requestMonitor.done();
			return;
		}
		
		if (e instanceof IStartedDMEvent) {
			parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
			requestMonitor.done();
			return;
		}

		if (e instanceof IGroupAddedEvent || e instanceof IGroupDeletedEvent) {
			parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
			requestMonitor.done();
			return;
		}
		
		if (e instanceof ICommandControlShutdownDMEvent) {
	        parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
	    } else if (e instanceof IThreadRemovedDMEvent) {
//		    IDMContext dmc = e instanceof IDMEvent<?> ? ((IDMEvent<?>)e).getDMContext() : null;
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

    private final String MEMENTO_NAME = "GROUP_MEMENTO_NAME"; //$NON-NLS-1$
    
    @Override
    public void compareElements(IElementCompareRequest[] requests) {
    	for (final IElementCompareRequest request : requests) {

    		Object element = request.getElement();
    		final IMemento memento = request.getMemento();
    		final String mementoName = memento.getString(MEMENTO_NAME);

    		if (mementoName != null) {
    			if (element instanceof IDMVMContext) {

    				final IDMContext dmc = ((IDMVMContext)element).getDMContext();

    				if (dmc instanceof IGroupDMContext) {
    					try {
    						getSession().getExecutor().execute(new DsfRunnable() {
    							@Override
    							public void run() {
    								final IGDBGrouping execTranslator = getServicesTracker().getService(IGDBGrouping.class);
    								if (execTranslator != null) {
    									execTranslator.getExecutionData(
    											(IGroupDMContext)dmc,
    											new ViewerDataRequestMonitor<IGroupDMData>(execTranslator.getExecutor(), request) {
    												@Override
    												protected void handleCompleted() {
    													if (isSuccess()) {
    														memento.putString(MEMENTO_NAME, "Group." + getData().getName() + getData().getId()); //$NON-NLS-1$
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
    		request.done();
    	}
    }
    
    @Override
    public void encodeElements(IElementMementoRequest[] requests) {
    	for (final IElementMementoRequest request : requests) {

    		Object element = request.getElement();
    		final IMemento memento = request.getMemento();

    		if (element instanceof IDMVMContext) {

    			final IDMContext dmc = ((IDMVMContext)element).getDMContext();

    			if (dmc instanceof IGroupDMContext) {
    				try {
    					getSession().getExecutor().execute(new DsfRunnable() {
    						@Override
    						public void run() {
    							final IGDBGrouping execTranslator = getServicesTracker().getService(IGDBGrouping.class);
    							if (execTranslator != null) {
    								execTranslator.getExecutionData(
    										(IGroupDMContext)dmc,
    										new ViewerDataRequestMonitor<IGroupDMData>(execTranslator.getExecutor(), request) {
    											@Override
    											protected void handleCompleted() {
    												if (isSuccess()) {
    													memento.putString(MEMENTO_NAME, "Group." + getData().getName() + getData().getId()); //$NON-NLS-1$
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
    		request.done();
    	}
    }
}
