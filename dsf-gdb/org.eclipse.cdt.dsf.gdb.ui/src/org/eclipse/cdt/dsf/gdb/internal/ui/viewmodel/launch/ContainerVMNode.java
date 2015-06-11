/*******************************************************************************
 * Copyright (c) 2006, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial API and implementation
 *     Wind River Systems - Factored out AbstractContainerVMNode
 *     Patrick Chuong (Texas Instruments) - Add support for icon overlay in the debug view (Bug 334566)     
 *     Marc Khouzam (Ericsson) - Respect the "Show Full Path" option for the process name (Bug 378418)
 *     Marc Khouzam (Ericsson) - Support for exited processes in the debug view (bug 407340)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;


import java.util.Map;
import java.util.Vector;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.internal.ui.pinclone.PinCloneUtils;
import org.eclipse.cdt.debug.ui.IPinProvider.IPinElementColorDescriptor;
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
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractContainerVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ExecutionContextLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ILaunchVMConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.provisional.service.IMIExecutionContextTranslator.IGroupDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbPinProvider;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadExitedDMData;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IThreadRemovedDMEvent;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerCountingRequestMonitor;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
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
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
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

/**
 * This VMNode represents a GDB process (inferior).
 */
public class ContainerVMNode extends AbstractContainerVMNode
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
	
	public ContainerVMNode(AbstractDMVMProvider provider, DsfSession session) {
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
	    return "ContainerVMNode(" + getSession().getId() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
    protected IElementLabelProvider createLabelProvider() {
        PropertiesBasedLabelProvider provider = new PropertiesBasedLabelProvider();
        
        provider.setColumnInfo(
            PropertiesBasedLabelProvider.ID_COLUMN_NO_COLUMNS, 
            new LabelColumnInfo(new LabelAttribute[] { 
            	
            	/* EXITED CONTAINER LABEL */
            	new GdbExecutionContextLabelText(
            	MessagesForGdbLaunchVM.ContainerVMNode_No_columns__exited_format,
            		new String[] { 
            			ExecutionContextLabelText.PROP_NAME_KNOWN, 
            			PROP_NAME,  
            			ExecutionContextLabelText.PROP_ID_KNOWN, 
            			ILaunchVMConstants.PROP_ID,
            			IGdbLaunchVMConstants.PROP_EXIT_CODE_KNOWN,
            			IGdbLaunchVMConstants.PROP_EXIT_CODE }) {
					@Override
					public boolean isEnabled(IStatus status, Map<String, Object> properties) {
						Boolean exited = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_THREAD_EXITED);
						return Boolean.TRUE.equals(exited);
					}
            	},
                /* EXITED CONTAINER IMAGE */
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_OS_PROCESS_TERMINATED)) {
					{ setPropertyNames(new String[] { 
							IGdbLaunchVMConstants.PROP_THREAD_EXITED }); }
				},                

            	/* ACTIVE CONTAINER LABEL */
                new GdbExecutionContextLabelText(
                MessagesForGdbLaunchVM.ContainerVMNode_No_columns__text_format,
                    new String[] { 
                        ExecutionContextLabelText.PROP_NAME_KNOWN, 
                        PROP_NAME,  
                        ExecutionContextLabelText.PROP_ID_KNOWN, 
                        ILaunchVMConstants.PROP_ID, 
                        IGdbLaunchVMConstants.PROP_CORES_ID_KNOWN, 
                        IGdbLaunchVMConstants.PROP_CORES_ID,
                        IGdbLaunchVMConstants.PROP_THREAD_SUMMARY_KNOWN, 
                        IGdbLaunchVMConstants.PROP_THREAD_SUMMARY }), 
                        
                new LabelText(MessagesForGdbLaunchVM.ContainerVMNode_No_columns__Error__label, new String[0]),
                
                /* RUNNING CONTAINER - RED PIN */
                new LabelImage(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_CONTAINER_RUNNING_R_PINNED)) {
					{ setPropertyNames(new String[] {
							ILaunchVMConstants.PROP_IS_SUSPENDED, 
							IGdbLaunchVMConstants.PROP_PINNED_CONTEXT, 
							IGdbLaunchVMConstants.PROP_PIN_COLOR }); }

					@Override
					public boolean isEnabled(IStatus status, Map<String, Object> properties) {
						Boolean prop = (Boolean) properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED);
						Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
						Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR); 
						return (prop != null && pin_prop != null && pin_color_prop != null) ? 
								!prop.booleanValue() && pin_prop.booleanValue() && pin_color_prop.equals(IPinElementColorDescriptor.RED) : false;
					};
				},                
				/* RUNNING CONTAINER - GREEN PIN */
                new LabelImage(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_CONTAINER_RUNNING_G_PINNED)) {
					{ setPropertyNames(new String[] {
							ILaunchVMConstants.PROP_IS_SUSPENDED, 
							IGdbLaunchVMConstants.PROP_PINNED_CONTEXT, 
							IGdbLaunchVMConstants.PROP_PIN_COLOR }); }

					@Override
					public boolean isEnabled(IStatus status, Map<String, Object> properties) {
						Boolean prop = (Boolean) properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED);
						Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
						Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR); 
						return (prop != null && pin_prop != null && pin_color_prop != null) ? 
								!prop.booleanValue() && pin_prop.booleanValue() && pin_color_prop.equals(IPinElementColorDescriptor.GREEN) : false;
					};
				},				
				/* RUNNING CONTAINER - BLUE PIN */
                new LabelImage(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_CONTAINER_RUNNING_B_PINNED)) {
					{ setPropertyNames(new String[] {
							ILaunchVMConstants.PROP_IS_SUSPENDED, 
							IGdbLaunchVMConstants.PROP_PINNED_CONTEXT, 
							IGdbLaunchVMConstants.PROP_PIN_COLOR }); }

					@Override
					public boolean isEnabled(IStatus status, Map<String, Object> properties) {
						Boolean prop = (Boolean) properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED);
						Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
						Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR); 
						return (prop != null && pin_prop != null && pin_color_prop != null) ? 
								!prop.booleanValue() && pin_prop.booleanValue() && pin_color_prop.equals(IPinElementColorDescriptor.BLUE) : false;
					};
				},				
				/* RUNNING CONTAINER - NO PIN */
				new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_DEBUG_TARGET)) {
                    { setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED }); }
                    
                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                        return Boolean.FALSE.equals(properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED));
                    };
                },
                
                /* SUSPENDED CONTAINER - RED PIN */
                new LabelImage(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_CONTAINER_SUSPENDED_R_PINNED)) {
                	{ setPropertyNames(new String[] { 
                			IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
                			IGdbLaunchVMConstants.PROP_PIN_COLOR }); }
                	
                	@Override 
                	public boolean isEnabled(IStatus status, Map<String, Object> properties) {
                		Boolean pin_prop = (Boolean)properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT); 
                		Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR); 
                		return (pin_prop != null && pin_color_prop != null) ? 
                				pin_prop.booleanValue() && pin_color_prop.equals(IPinElementColorDescriptor.RED) : false; 
                	};
                },                
                /* SUSPENDED CONTAINER - GREEN PIN */
                new LabelImage(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_CONTAINER_SUSPENDED_G_PINNED)) {
                	{ setPropertyNames(new String[] { 
                			IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
                			IGdbLaunchVMConstants.PROP_PIN_COLOR }); }
                	
                	@Override 
                	public boolean isEnabled(IStatus status, Map<String, Object> properties) { 
                		Boolean pin_prop = (Boolean)properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT); 
                		Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR); 
                		return (pin_prop != null && pin_color_prop != null) ? 
                				pin_prop.booleanValue() && pin_color_prop.equals(IPinElementColorDescriptor.GREEN) : false; 
                	};
                },                 
                /* SUSPENDED CONTAINER - BLUE PIN */
                new LabelImage(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_CONTAINER_SUSPENDED_B_PINNED)) {
                	{ setPropertyNames(new String[] { 
                			IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
                			IGdbLaunchVMConstants.PROP_PIN_COLOR }); }
                	
                	@Override 
                	public boolean isEnabled(IStatus status, Map<String, Object> properties) { 
                		Boolean pin_prop = (Boolean)properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT); 
                		Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR); 
                		return (pin_prop != null && pin_color_prop != null) ? 
                				pin_prop.booleanValue() && pin_color_prop.equals(IPinElementColorDescriptor.BLUE) : false; 
                	};
                },                 
                /* SUSPENDED CONTAINER - NO PIN */
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

		// If there is a group in the path we must ask for its children.
		// If there is not, we will get a null and then the service will give us the top processes.
		IContainerDMContext group = findDmcInPath(update.getViewerInput(), update.getElementPath(), IGroupDMContext.class);
		runControl.getExecutionContexts(
			group,
			new ViewerDataRequestMonitor<IExecutionDMContext[]>(getExecutor(), update) {
				@Override
				public void handleCompleted() {
					if (!isSuccess()) {
						handleFailedUpdate(update);
						return;
					}
					
					IExecutionDMContext[] execDmcs = getData();
					if (execDmcs != null) {
						// Extract the processes as there could be threads or groups in the answer
						Vector<IMIContainerDMContext> processes = new Vector<>();
						for (IExecutionDMContext exec : execDmcs) {
							if (exec instanceof IMIContainerDMContext) {
								processes.add((IMIContainerDMContext)exec);
							}
						}
						fillUpdateWithVMCs(
								update,
								processes.toArray(new IMIContainerDMContext[processes.size()]));
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

        	// set pin properties
            IDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IDMContext.class);
            IPinElementColorDescriptor colorDesc = PinCloneUtils.getPinElementColorDescriptor(GdbPinProvider.getPinnedHandles(), dmc);
            update.setProperty(IGdbLaunchVMConstants.PROP_PIN_COLOR, 
            		colorDesc != null ? colorDesc.getOverlayColor() : null);
        	update.setProperty(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT, 
        			PinCloneUtils.isPinnedTo(GdbPinProvider.getPinnedHandles(), dmc));
            
            if (update.getProperties().contains(PROP_NAME) || 
                update.getProperties().contains(ILaunchVMConstants.PROP_ID) ||
                update.getProperties().contains(IGdbLaunchVMConstants.PROP_CORES_ID) ||
                update.getProperties().contains(IGdbLaunchVMConstants.PROP_THREAD_EXITED) ||
                update.getProperties().contains((IGdbLaunchVMConstants.PROP_EXIT_CODE))) 
            {
            	IProcesses processService = getServicesTracker().getService(IProcesses.class);
            	final IProcessDMContext procDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IProcessDMContext.class);

            	if (processService == null || procDmc == null) {
            		update.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Service or handle invalid", null)); //$NON-NLS-1$
            	} else {
            		processService.getExecutionData(
            			procDmc,
            			new ViewerDataRequestMonitor<IThreadDMData>(getExecutor(), update) {
            				@Override
            				public void handleCompleted() {
            					if (isSuccess()) {
            						fillThreadDataProperties(update, getData());
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
    
    protected void fillThreadDataProperties(IPropertiesUpdate update, IThreadDMData data) {
        String fileName = data.getName();
        if (fileName != null) {
	        Object showFullPathPreference = getVMProvider().getPresentationContext().getProperty(IDsfDebugUIConstants.DEBUG_VIEW_SHOW_FULL_PATH_PROPERTY);
	        if (showFullPathPreference instanceof Boolean && (Boolean)showFullPathPreference == false) {
	        	fileName = new Path(fileName).lastSegment();
	        }
        }
        update.setProperty(PROP_NAME, fileName);
        update.setProperty(ILaunchVMConstants.PROP_ID, data.getId());
        
		String coresStr = null;
        if (data instanceof IGdbThreadDMData) {
        	String[] cores = ((IGdbThreadDMData)data).getCores();
        	if (cores != null) {
        		StringBuffer str = new StringBuffer();
        		for (String core : cores) {
        			str.append(core + ","); //$NON-NLS-1$
        		}
        		if (str.length() > 0) {
        			coresStr = str.substring(0, str.length() - 1);
        		}
        	}
        }
        update.setProperty(IGdbLaunchVMConstants.PROP_CORES_ID, coresStr);	

        if (data instanceof IGdbThreadExitedDMData) {
        	update.setProperty(IGdbLaunchVMConstants.PROP_THREAD_EXITED, true);

        	Integer exitCode = ((IGdbThreadExitedDMData)data).getExitCode();
        	if (exitCode != null) {
        		update.setProperty(IGdbLaunchVMConstants.PROP_EXIT_CODE, exitCode);
        	}
        }
    }

    protected void fillThreadSummary(final IPropertiesUpdate update, final RequestMonitor rm) {
    	if (!fHideRunningThreadsProperty) {
    		// Disable the thread summary when we are not hiding threads
            update.setProperty(IGdbLaunchVMConstants.PROP_THREAD_SUMMARY, null);  
            rm.done();
            return;
    	}
    	
        IProcesses processService = getServicesTracker().getService(IProcesses.class);
        final IContainerDMContext procDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IContainerDMContext.class);
        
        if (processService == null || procDmc == null) {
            update.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Service or handle invalid", null)); //$NON-NLS-1$
        } else {
        	// Fetch all the threads
            processService.getProcessesBeingDebugged(
                procDmc,
                new ViewerDataRequestMonitor<IDMContext[]>(getSession().getExecutor(), update) {
                    @Override
                    public void handleCompleted() {
                    	IRunControl runControl = getServicesTracker().getService(IRunControl.class);
						if (!isSuccess() ||
								!(getData() instanceof IExecutionDMContext[]) ||
								runControl == null) {
				            update.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Unable to get threads summary", null)); //$NON-NLS-1$
                            rm.done();
                            return;
                        }
                        
                        // For each thread, count how many are running and therefore hidden
						// Remove running threads from the list
						int runningCount = 0;
						for (IExecutionDMContext execDmc : (IExecutionDMContext[])getData()) {
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
    }
    
	@Override
	public int getDeltaFlags(Object e) {
		//TODO
//		if (e instanceof IContainerLayoutChangedEvent) {
//			return IModelDelta.CONTENT;
//		}
//		
//		if (e instanceof ISuspendedDMEvent) {
//			return IModelDelta.STATE;
//		}

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
//		if (e instanceof IStartedDMEvent) {
//			parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
//			requestMonitor.done();
//			return;
//		}
//
//		if (e instanceof IContainerLayoutChangedEvent) {
//			parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
//			requestMonitor.done();
//			return;
//		}
//		
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

// Not sure this is needed
//    /*
//     * (non-Javadoc)
//     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractContainerVMNode#getContextsForEvent(org.eclipse.cdt.dsf.ui.viewmodel.VMDelta, java.lang.Object, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
//     */
//    @Override
//    public void getContextsForEvent(VMDelta parentDelta, Object event, final DataRequestMonitor<IVMContext[]> rm) {
//    	if (event instanceof FullStackRefreshEvent &&
//    			((FullStackRefreshEvent)event).getDMContext() instanceof IContainerDMContext)
//    	{
//    		// The step sequence end event occurred on a container and not on a thread.  Do not
//    		// return a context for this event, which will force the view model to generate
//    		// a delta for all the threads.
//    		rm.done(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "", null)); //$NON-NLS-1$
//    		return;
//    	}
//
//    	if( getContextsForRecursiveVMNode( parentDelta, event, rm))
//    		return;
//
//    	super.getContextsForEvent(parentDelta, event, rm);
//    }
}
