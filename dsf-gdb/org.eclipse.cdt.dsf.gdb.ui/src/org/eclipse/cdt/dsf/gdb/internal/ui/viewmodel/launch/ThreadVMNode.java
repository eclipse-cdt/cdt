/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for multi threaded functionality	
 *     Patrick Chuong (Texas Instruments) - Add support for icon overlay in the debug view (Bug 334566)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import java.util.Map;

import org.eclipse.cdt.debug.internal.ui.pinclone.PinCloneUtils;
import org.eclipse.cdt.debug.ui.IPinProvider.IPinElementColorDescriptor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractThreadVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ExecutionContextLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ILaunchVMConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbPinProvider;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerCountingRequestMonitor;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IMemento;


@SuppressWarnings("restriction")
public class ThreadVMNode extends AbstractThreadVMNode 
    implements IElementLabelProvider, IElementMementoProvider
{
    public ThreadVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session);
    }

    @Override
    public String toString() {
        return "ThreadVMNode(" + getSession().getId() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    @Override
    protected IElementLabelProvider createLabelProvider() {
        PropertiesBasedLabelProvider provider = new PropertiesBasedLabelProvider();
        
        provider.setColumnInfo(
            PropertiesBasedLabelProvider.ID_COLUMN_NO_COLUMNS, 
            new LabelColumnInfo(new LabelAttribute[] { 
                // Text is made of the thread name followed by its state and state change reason. 
                new GdbExecutionContextLabelText(
                    MessagesForGdbLaunchVM.ThreadVMNode_No_columns__text_format,
                    new String[] { 
                        ExecutionContextLabelText.PROP_NAME_KNOWN, 
                        PROP_NAME, 
                        ExecutionContextLabelText.PROP_ID_KNOWN, 
                        ILaunchVMConstants.PROP_ID, 
                        IGdbLaunchVMConstants.PROP_OS_ID_KNOWN, 
                        IGdbLaunchVMConstants.PROP_OS_ID, 
                        IGdbLaunchVMConstants.PROP_CORES_ID_KNOWN, 
                        IGdbLaunchVMConstants.PROP_CORES_ID,
                        ILaunchVMConstants.PROP_IS_SUSPENDED,
                        ExecutionContextLabelText.PROP_STATE_CHANGE_REASON_KNOWN, 
                        ILaunchVMConstants.PROP_STATE_CHANGE_REASON,
                        ExecutionContextLabelText.PROP_STATE_CHANGE_DETAILS_KNOWN,
                        ILaunchVMConstants.PROP_STATE_CHANGE_DETAILS}),
                new LabelText(MessagesForGdbLaunchVM.ThreadVMNode_No_columns__Error__label, new String[0]),
                /* RUNNING THREAD - RED PIN */
                new LabelImage(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_RUNNING_R_PINNED)) {
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
				/* RUNNING THREAD - GREEN PIN */
				new LabelImage(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_RUNNING_G_PINNED)) {
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
				/* RUNNING THREAD - BLUE PIN */
				new LabelImage(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_RUNNING_B_PINNED)) {
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
				/* RUNNING THREAD - NO PIN */
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING)) {
                    { setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED }); }
                    
                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                    	// prop has been seen to be null during session shutdown [313823]
                    	Boolean prop = (Boolean)properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED);
                    	return (prop != null) ? !prop.booleanValue() : false;
                    };
                },
                /* SUSPENDED THREAD - RED PIN */
                new LabelImage(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_SUSPENDED_R_PINNED)) {
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
                /* SUSPENDED THREAD - GREEN PIN */
                new LabelImage(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_SUSPENDED_G_PINNED)) {
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
                /* SUSPENDED THREAD - BLUE PIN */
                new LabelImage(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_SUSPENDED_B_PINNED)) {
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
                /* SUSPENDED THREAD - NO PIN */
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED)),
            }));
        return provider;
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

            IMIExecutionDMContext execDmc = findDmcInPath(
                update.getViewerInput(), update.getElementPath(), IMIExecutionDMContext.class);
            if (execDmc != null) {
                update.setProperty(ILaunchVMConstants.PROP_ID, Integer.toString(execDmc.getThreadId()));

                // set pin properties
                IPinElementColorDescriptor colorDesc = PinCloneUtils.getPinElementColorDescriptor(GdbPinProvider.getPinnedHandles(), execDmc);
        		updates[i].setProperty(IGdbLaunchVMConstants.PROP_PIN_COLOR, 
        				colorDesc != null ? colorDesc.getOverlayColor() : null);
        		updates[i].setProperty(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT, 
        				PinCloneUtils.isPinnedTo(GdbPinProvider.getPinnedHandles(), execDmc));
            }

            if (update.getProperties().contains(PROP_NAME) || 
                update.getProperties().contains(IGdbLaunchVMConstants.PROP_OS_ID) ||
                update.getProperties().contains(IGdbLaunchVMConstants.PROP_CORES_ID)) 
            {
            	IProcesses processService = getServicesTracker().getService(IProcesses.class);
            	final IThreadDMContext threadDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IThreadDMContext.class);

            	if (processService == null || threadDmc == null) {
                    update.setStatus(DsfUIPlugin.newErrorStatus(IDsfStatusConstants.INVALID_HANDLE, "Service or handle invalid", null)); //$NON-NLS-1$
                } else {
                    processService.getExecutionData(
                    	threadDmc,
                        new ViewerDataRequestMonitor<IThreadDMData>(getExecutor(), update) {
                            @Override
                            public void handleCompleted() {
                                if (isSuccess()) {
                                    fillThreadDataProperties(update, getData());
                                } 
                                update.setStatus(getStatus());
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
    	if (data.getName() != null && data.getName().length() > 0) {
    		update.setProperty(PROP_NAME, data.getName());
    	}
        update.setProperty(IGdbLaunchVMConstants.PROP_OS_ID, data.getId());
        
        if (data instanceof IGdbThreadDMData) {
        	String[] cores = ((IGdbThreadDMData)data).getCores();
        	if (cores != null) {
        		StringBuffer str = new StringBuffer();
        		for (String core : cores) {
        			str.append(core + ","); //$NON-NLS-1$
        		}
        		if (str.length() > 0) {
        			String coresStr = str.substring(0, str.length() - 1);
        			update.setProperty(IGdbLaunchVMConstants.PROP_CORES_ID, coresStr);        	
        		}
        	}
        }
    }

	private String produceThreadElementName(String viewName, IMIExecutionDMContext execCtx) {
		return "Thread." + execCtx.getThreadId(); //$NON-NLS-1$
    }

    private static final String MEMENTO_NAME = "THREAD_MEMENTO_NAME"; //$NON-NLS-1$
    
    /*
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#compareElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest[])
     */
    @Override
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
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#encodeElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest[])
     */
    @Override
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
