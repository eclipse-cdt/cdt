/*******************************************************************************
 * Copyright (c) 2006, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial API and implementation
 *     Wind River Systems - Factored out AbstractContainerVMNode
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;


import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractContainerVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ExecutionContextLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ILaunchVMConstants;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IMemento;


@SuppressWarnings("restriction")
public class ContainerVMNode extends AbstractContainerVMNode
    implements IElementLabelProvider, IElementMementoProvider 
{
	public ContainerVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session);
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
                new GdbExecutionContextLabelText(
                MessagesForGdbLaunchVM.ContainerVMNode_No_columns__text_format,
                    new String[] { 
                        ExecutionContextLabelText.PROP_NAME_KNOWN, 
                        PROP_NAME,  
                        ExecutionContextLabelText.PROP_ID_KNOWN, 
                        ILaunchVMConstants.PROP_ID, 
                        IGdbLaunchVMConstants.PROP_CORES_ID_KNOWN, 
                        IGdbLaunchVMConstants.PROP_CORES_ID }), 
                new LabelText(MessagesForGdbLaunchVM.ContainerVMNode_No_columns__Error__label, new String[0]),
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
    protected void updatePropertiesInSessionThread(IPropertiesUpdate[] updates) {
        IPropertiesUpdate[] parentUpdates = new IPropertiesUpdate[updates.length]; 
        
        for (int i = 0; i < updates.length; i++) {
            final IPropertiesUpdate update = updates[i];
            
            final ViewerCountingRequestMonitor countringRm = 
                new ViewerCountingRequestMonitor(ImmediateExecutor.getInstance(), updates[i]);
            int count = 0;
            
            // Create a delegating update which will let the super-class fill in the 
            // standard container properties.
            parentUpdates[i] = new VMDelegatingPropertiesUpdate(updates[i], countringRm);
            count++;
            
            if (update.getProperties().contains(PROP_NAME) || 
                update.getProperties().contains(ILaunchVMConstants.PROP_ID) ||
                update.getProperties().contains(IGdbLaunchVMConstants.PROP_CORES_ID)) 
            {
            	
            IProcesses processService = getServicesTracker().getService(IProcesses.class);
            final IProcessDMContext procDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IProcessDMContext.class);
            
            if (processService == null || procDmc == null) {
                update.setStatus(DsfUIPlugin.newErrorStatus(IDsfStatusConstants.INVALID_HANDLE, "Service or handle invalid", null)); //$NON-NLS-1$
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
                            countringRm.done();
                        }
                    });
                count++;
            }
            }
            
            countringRm.setDoneCount(count);
        }
        
        super.updatePropertiesInSessionThread(parentUpdates);
    }
    
    protected void fillThreadDataProperties(IPropertiesUpdate update, IThreadDMData data) {
        update.setProperty(PROP_NAME, data.getName());
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
    }

    
	@Override
	public int getDeltaFlags(Object e) {
		if (e instanceof ICommandControlShutdownDMEvent) {
	        return IModelDelta.CONTENT;
	    }
	    return super.getDeltaFlags(e);
	}

	@Override
	public void buildDelta(Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
		if (e instanceof ICommandControlShutdownDMEvent) {
	        parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
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
