/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerSuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMData;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController.SteppingTimedOutEvent;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.ModelProxyInstalledEvent;
import org.eclipse.cdt.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;

public class StackFramesVMNode extends AbstractDMVMNode 
    implements IElementLabelProvider, IElementMementoProvider, IElementPropertiesProvider
{
    /**
     * @since 2.0
     */    
    public static final String PROP_IS_INCOMPLETE_STACK_MARKER = "is_incomplete_stack_marker";  //$NON-NLS-1$
    
	/**
	 * View model context representing the end of an incomplete stack.
	 * 
	 * @since 1.1
	 */
	public class IncompleteStackVMContext extends AbstractVMContext {
		private final int fLevel;
		private final IExecutionDMContext fDmc;

		public IncompleteStackVMContext(IExecutionDMContext dmc, int level) {
			super(StackFramesVMNode.this);
			fDmc = dmc;
			fLevel = level;
		}
		public int getLevel() {
			return fLevel;
		}
		public IExecutionDMContext getExecutionDMContext() {
			return fDmc;
		}
		@Override
		public boolean equals(Object obj) {
			return obj instanceof IncompleteStackVMContext && 
			    ((IncompleteStackVMContext)obj).fDmc.equals(fDmc);
		}

		@Override
		public int hashCode() {
			return fDmc.hashCode();
		}
	}

	/**
	 * Temporary stack frame limit to allow incremental stack updates.
	 */
	private Map<IExecutionDMContext, Integer> fTemporaryLimits = new HashMap<IExecutionDMContext, Integer>();

    /**
     * The label provider delegate.  This VM node will delegate label updates to this provider
     * which can be created by sub-classes. 
     *  
     * @since 2.0
     */    
    private IElementLabelProvider fLabelProvider;

	public StackFramesVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, IStack.IFrameDMContext.class);
        fLabelProvider = createLabelProvider();
    }
    
    @Override
    public String toString() {
        return "StackFramesVMNode(" + getSession().getId() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
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
                new LabelText(
                    MessagesForLaunchVM.StackFramesVMNode_No_columns__Incomplete_stack_marker__text_format,
                    new String[] { PROP_NAME })
                {
                    @Override
                    public boolean isEnabled(IStatus status, Map<String, Object> properties) {
                        return Boolean.TRUE.equals(properties.get(PROP_IS_INCOMPLETE_STACK_MARKER));
                    }
                    
                },
                new LabelText(
                    MessagesForLaunchVM.StackFramesVMNode_No_columns__text_format, 
                    new String[] { 
                        ILaunchVMConstants.PROP_FRAME_ADDRESS, 
                        ILaunchVMConstants.PROP_FRAME_FUNCTION, 
                        ILaunchVMConstants.PROP_FRAME_FILE, 
                        ILaunchVMConstants.PROP_FRAME_LINE, 
                        ILaunchVMConstants.PROP_FRAME_COLUMN, 
                        ILaunchVMConstants.PROP_FRAME_MODULE})
                {
                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                        Integer line = (Integer)properties.get(ILaunchVMConstants.PROP_FRAME_LINE);
                        String file = (String)properties.get(ILaunchVMConstants.PROP_FRAME_FILE);
                        return line != null && line >= 0 && file != null && file.length() > 0;
                    };
                },
                new LabelText(
                    MessagesForLaunchVM.StackFramesVMNode_No_columns__No_line__text_format, 
                    new String[] { 
                        ILaunchVMConstants.PROP_FRAME_ADDRESS, 
                        ILaunchVMConstants.PROP_FRAME_FUNCTION, 
                        ILaunchVMConstants.PROP_FRAME_MODULE})
                {
                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                        String function = (String)properties.get(ILaunchVMConstants.PROP_FRAME_FUNCTION);
                        String module = (String)properties.get(ILaunchVMConstants.PROP_FRAME_MODULE);
                        return function != null && function.length() > 0 && module != null && module.length() > 0;
                    };
                },
                new LabelText(
                    MessagesForLaunchVM.StackFramesVMNode_No_columns__No_function__text_format, 
                    new String[] { 
                        ILaunchVMConstants.PROP_FRAME_ADDRESS, 
                        ILaunchVMConstants.PROP_FRAME_MODULE})
                {
                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                        String module = (String)properties.get(ILaunchVMConstants.PROP_FRAME_MODULE);
                        return module != null && module.length() > 0;
                    };
                },
                new LabelText(
                    MessagesForLaunchVM.StackFramesVMNode_No_columns__No_module__text_format, 
                    new String[] { 
                        ILaunchVMConstants.PROP_FRAME_ADDRESS, 
                        ILaunchVMConstants.PROP_FRAME_FUNCTION})
                {
                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                        String function = (String)properties.get(ILaunchVMConstants.PROP_FRAME_FUNCTION);
                        return function != null && function.length() > 0;
                    };
                },
                new LabelText(
                    MessagesForLaunchVM.StackFramesVMNode_No_columns__Address_only__text_format, 
                    new String[] { ILaunchVMConstants.PROP_FRAME_ADDRESS }),
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_STACKFRAME_RUNNING)) {
                    { setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED }); }
                    
                    @Override
                    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
                        return Boolean.FALSE.equals( properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED) );
                    };
                },
                new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_STACKFRAME))
            }));
        
        return provider;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode#updateHasElementsInSessionThread(org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate)
     */
    @Override
    protected void updateHasElementsInSessionThread(IHasChildrenUpdate update) {
        IRunControl runControl = getServicesTracker().getService(IRunControl.class);
        IExecutionDMContext execCtx = findDmcInPath(update.getViewerInput(), update.getElementPath(), IExecutionDMContext.class);
        if (runControl == null || execCtx == null) {
            handleFailedUpdate(update);
            return;
        }
        
        update.setHasChilren(runControl.isSuspended(execCtx) || runControl.isStepping(execCtx));
        update.done();
    }

    @Override
    protected void updateElementCountInSessionThread(final IChildrenCountUpdate update) {
        IStack stackService = getServicesTracker().getService(IStack.class);
        final IExecutionDMContext execDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IExecutionDMContext.class);
        if (stackService == null || execDmc == null) {
            handleFailedUpdate(update);
            return;
        }          
        
        final int stackFrameLimit= getStackFrameLimit(execDmc);
        stackService.getStackDepth(
            execDmc, stackFrameLimit == Integer.MAX_VALUE ? 0 : stackFrameLimit + 1,
            new ViewerDataRequestMonitor<Integer>(getSession().getExecutor(), update) { 
                @Override
                public void handleCompleted() {
                    if (!isSuccess()) {
                        handleFailedUpdate(update);
                        return;
                    }
                    int stackDepth= getData();
                    if (stackFrameLimit < stackDepth) {
                    	stackDepth = stackFrameLimit + 1;
                    }
					update.setChildCount(stackDepth);
                    update.done();
                }
            });
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode#updateElementsInSessionThread(org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate)
     */
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        IStack stackService = getServicesTracker().getService(IStack.class);
        final IExecutionDMContext execDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IExecutionDMContext.class);
        if (stackService == null || execDmc == null) {
            handleFailedUpdate(update);
            return;
        }          
        
        final int stackFrameLimit= getStackFrameLimit(execDmc);
        final int startIndex= update.getOffset();

		if (startIndex == 0 && update.getLength() == 1) {
            // Requesting top stack frame only
            stackService.getTopFrame(
                execDmc, 
                new ViewerDataRequestMonitor<IFrameDMContext>(getSession().getExecutor(), update) { 
                    @Override
                    public void handleCompleted() {
                        if (!isSuccess()) {
                            handleFailedUpdate(update);
                            return;
                        }
                        update.setChild(createVMContext(getData()), 0);
                        update.done();
                    }
                });
            
        } else {
        	if (startIndex >= 0 && update.getLength() > 0) {
            	// partial stack dump
                int endIndex= startIndex + update.getLength() - 1;
            	if (startIndex < stackFrameLimit && endIndex >= stackFrameLimit) {
            		endIndex = stackFrameLimit - 1;
            	}
            	stackService.getFrames(
                    execDmc, 
                    startIndex,
                    endIndex,
                    new ViewerDataRequestMonitor<IFrameDMContext[]>(getSession().getExecutor(), update) { 
                        @Override
                        public void handleCompleted() {
                            if (!isSuccess()) {
                                handleFailedUpdate(update);
                                return;
                            }
                            IFrameDMContext[] frames = getData();
							fillUpdateWithVMCs(update, frames, startIndex);
							if (startIndex + update.getLength() > stackFrameLimit) {
								update.setChild(new IncompleteStackVMContext(execDmc, stackFrameLimit), stackFrameLimit);
							}
                            update.done();
                        }
                    });
        	} else {
	        	// full stack dump
	            stackService.getFrames(
	                execDmc, 
	                new ViewerDataRequestMonitor<IFrameDMContext[]>(getSession().getExecutor(), update) { 
	                    @Override
	                    public void handleCompleted() {
	                        if (!isSuccess()) {
	                            handleFailedUpdate(update);
	                            return;
	                        }
	                        IFrameDMContext[] frames = getData();
	                        if (frames.length > stackFrameLimit) {
	                        	IFrameDMContext[] tmpFrames = new IFrameDMContext[stackFrameLimit];
	                        	System.arraycopy(frames, 0, tmpFrames, 0, stackFrameLimit);
	                        	frames = tmpFrames;
								update.setChild(new IncompleteStackVMContext(execDmc, stackFrameLimit), stackFrameLimit);
	                        }
							fillUpdateWithVMCs(update, frames);
	                        update.done();
	                    }
	                });
        	}
        }
    }
    
    public void update(final ILabelUpdate[] updates) {
        fLabelProvider.update(updates);
    }
    
    /**
     * @see IElementPropertiesProvider#update(IPropertiesUpdate[])
     * 
     * @since 2.0
     */    
    public void update(final IPropertiesUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
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
        IStack service = getServicesTracker().getService(IStack.class);
        
        for (final IPropertiesUpdate update : updates) {
            if (service == null) {
            	handleFailedUpdate(update);
            	continue;
            }

        	if (update.getElement() instanceof IncompleteStackVMContext) {
        	    update.setProperty(PROP_IS_INCOMPLETE_STACK_MARKER, Boolean.TRUE);
        		update.done();
        		continue;
        	}

            IFrameDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IFrameDMContext.class);
            if (dmc == null) {
            	handleFailedUpdate(update);
            	continue;
            }

            IRunControl runControlService = getServicesTracker().getService(IRunControl.class);
            IExecutionDMContext execDmc = DMContexts.getAncestorOfType(dmc, IExecutionDMContext.class);
            if (execDmc != null && runControlService != null) {
                update.setProperty(ILaunchVMConstants.PROP_IS_SUSPENDED, runControlService.isSuspended(execDmc));
                update.setProperty(ILaunchVMConstants.PROP_IS_STEPPING, runControlService.isStepping(execDmc));
            } else {
                update.setStatus(DsfUIPlugin.newErrorStatus(IDsfStatusConstants.INVALID_HANDLE, "Invalid context or service not available", null)); //$NON-NLS-1$                
            }

            service.getFrameData(
                dmc, 
                new ViewerDataRequestMonitor<IFrameDMData>(getSession().getExecutor(), update) { 
                    @Override
                    protected void handleSuccess() {
                        fillFrameDataProperties(update, getData());
                        update.done();
                    }
                });
        }
    }

    protected void fillFrameDataProperties(IPropertiesUpdate update, IFrameDMData data) {
        IAddress address = data.getAddress();
        if (address != null) {
            update.setProperty(ILaunchVMConstants.PROP_FRAME_ADDRESS, "0x" + address.toString(16)); //$NON-NLS-1$
        }

        String fileName = data.getFile();
        if (fileName != null) {
	        Object showFullPathPreference = getVMProvider().getPresentationContext().getProperty(IDsfDebugUIConstants.DEBUG_VIEW_SHOW_FULL_PATH_PROPERTY);
	        if (showFullPathPreference instanceof Boolean && (Boolean)showFullPathPreference == false) {
	        	fileName = new Path(fileName).lastSegment();
	        }
        }
        update.setProperty(ILaunchVMConstants.PROP_FRAME_FILE, fileName);
        
        update.setProperty(ILaunchVMConstants.PROP_FRAME_FUNCTION, data.getFunction());
        update.setProperty(ILaunchVMConstants.PROP_FRAME_LINE, data.getLine());
        update.setProperty(ILaunchVMConstants.PROP_FRAME_COLUMN, data.getColumn());
        update.setProperty(ILaunchVMConstants.PROP_FRAME_MODULE, data.getModule());        
    }
    
    protected void fillColumnLabel(IFrameDMContext dmContext, IFrameDMData dmData, String columnId, int idx, ILabelUpdate update) 
    {
        if (idx != 0) return;
        
        final IExecutionDMContext execDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IExecutionDMContext.class);
        if (execDmc == null) {
        	return;
        }
        IRunControl runControlService = getServicesTracker().getService(IRunControl.class); 
        SteppingController stepQueueMgr = (SteppingController) execDmc.getAdapter(SteppingController.class); 
        if (runControlService == null || stepQueueMgr == null) return;
        
        String imageKey = null;
        if (runControlService.isSuspended(execDmc) || 
            (runControlService.isStepping(execDmc) && !stepQueueMgr.isSteppingTimedOut(execDmc)))
        {
            imageKey = IDebugUIConstants.IMG_OBJS_STACKFRAME;
        } else {
            imageKey = IDebugUIConstants.IMG_OBJS_STACKFRAME_RUNNING;
        }            
        update.setImageDescriptor(DebugUITools.getImageDescriptor(imageKey), 0);
        
        //
        // Finally, if all goes well, set the label.
        //
        StringBuilder label = new StringBuilder();
        
        // Add the function name
        if (dmData.getFunction() != null && dmData.getFunction().length() != 0) { 
            label.append(" "); //$NON-NLS-1$
            label.append(dmData.getFunction());
            label.append("()"); //$NON-NLS-1$
        }
        
        boolean hasFileName = dmData.getFile() != null && dmData.getFile().length() != 0;
        
        // Add full file name
        if (hasFileName) {
            label.append(" at "); //$NON-NLS-1$
            label.append(dmData.getFile());
        }
        
        // Add line number 
        if (dmData.getLine() >= 0) {
            label.append(":"); //$NON-NLS-1$
            label.append(dmData.getLine());
            label.append(" "); //$NON-NLS-1$
        }
 
        // Add module 
        if (!hasFileName && (dmData.getModule() != null && dmData.getModule().length() != 0)) { 
            label.append(" "); //$NON-NLS-1$
            label.append(dmData.getModule());
            label.append(" "); //$NON-NLS-1$
        }
 
        // Add the address
        if (dmData.getAddress() != null) {
            label.append("- 0x" + dmData.getAddress().toString(16)); //$NON-NLS-1$
        }
            
        // Set the label to the result listener
        update.setLabel(label.toString(), 0);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode#getContextsForEvent(org.eclipse.cdt.dsf.ui.viewmodel.VMDelta, java.lang.Object, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
     */
    @Override
    public void getContextsForEvent(final VMDelta parentDelta, Object e, final DataRequestMonitor<IVMContext[]> rm) {
        if (e instanceof ModelProxyInstalledEvent || e instanceof DataModelInitializedEvent) {
            // Retrieve the list of stack frames, and mark the top frame to be selected.  
            getVMProvider().updateNode(
                this,
                new VMChildrenUpdate(
                    parentDelta, getVMProvider().getPresentationContext(), 0, 1,
                    new DataRequestMonitor<List<Object>>(getExecutor(), rm) { 
                        @Override
                        public void handleCompleted() {
                            if (isSuccess() && getData().size() != 0) {
                                rm.setData(new IVMContext[] { (IVMContext)getData().get(0) });
                            } else {
                                // In case of errors, return an empty set of frames.
                                rm.setData(new IVMContext[0]);
                            }
                            rm.done();
                        }
                    })
                );
            return;
        }
        super.getContextsForEvent(parentDelta, e, rm);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.IVMNode#getDeltaFlags(java.lang.Object)
     */
    public int getDeltaFlags(Object e) {
        // This node generates delta if the timers have changed, or if the 
        // label has changed.
        if (e instanceof ISuspendedDMEvent) {
            return IModelDelta.CONTENT | IModelDelta.EXPAND | IModelDelta.SELECT;
        } else if (e instanceof FullStackRefreshEvent) {
        	return IModelDelta.CONTENT;
        } else if (e instanceof SteppingTimedOutEvent) {
            return IModelDelta.CONTENT;
        } else if (e instanceof ModelProxyInstalledEvent || e instanceof DataModelInitializedEvent) {
            return IModelDelta.SELECT | IModelDelta.EXPAND;
        } else if (e instanceof ExpandStackEvent) {
        	return IModelDelta.CONTENT;
    	} else if (e instanceof IExitedDMEvent) {
    	    // Do not generate a delta for this event, but do clear the
    	    // internal stack frame limit to avoid a memory leak.
    	    resetStackFrameLimit( ((IExitedDMEvent)e).getDMContext() );
            return IModelDelta.NO_CHANGE;
        } else if (e instanceof PropertyChangeEvent) {
            String property = ((PropertyChangeEvent)e).getProperty();
            if (IDsfDebugUIConstants.PREF_STACK_FRAME_LIMIT_ENABLE.equals(property)
                || IDsfDebugUIConstants.PREF_STACK_FRAME_LIMIT.equals(property)) 
            {
                return IModelDelta.CONTENT;
            } else if (IDsfDebugUIConstants.DEBUG_VIEW_SHOW_FULL_PATH_PROPERTY.equals(property)) {
                return IModelDelta.STATE;
            }
        } else {
    	}

        return IModelDelta.NO_CHANGE;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.dsf.ui.viewmodel.IVMNode#buildDelta(java.lang.Object, org.eclipse.cdt.dsf.ui.viewmodel.VMDelta, int, org.eclipse.cdt.dsf.concurrent.RequestMonitor)
     */
    public void buildDelta(final Object e, final VMDelta parent, final int nodeOffset, final RequestMonitor rm) {
        if (e instanceof IContainerSuspendedDMEvent) {
            // Clear the limit on the stack frames for all stack frames under a given container.
            resetStackFrameLimit( ((IContainerSuspendedDMEvent)e).getDMContext() );

            IContainerSuspendedDMEvent csEvent = (IContainerSuspendedDMEvent)e;
            
            IExecutionDMContext triggeringCtx = csEvent.getTriggeringContexts().length != 0 
                ? csEvent.getTriggeringContexts()[0] : null;
                
            if (parent.getElement() instanceof IDMVMContext) {
                IExecutionDMContext threadDmc = null;
                threadDmc = DMContexts.getAncestorOfType( ((IDMVMContext)parent.getElement()).getDMContext(), IExecutionDMContext.class);
                buildDeltaForSuspendedEvent(threadDmc, triggeringCtx, parent, nodeOffset, rm);
            } else {
                rm.done();
            }
        } else if (e instanceof FullStackRefreshEvent) {
            IExecutionDMContext execDmc = ((FullStackRefreshEvent)e).getDMContext();
            buildDeltaForFullStackRefreshEvent(execDmc, execDmc, parent, nodeOffset, rm);
        } else if (e instanceof ISuspendedDMEvent) {
            resetStackFrameLimit( ((ISuspendedDMEvent)e).getDMContext() );
            IExecutionDMContext execDmc = ((ISuspendedDMEvent)e).getDMContext();
            buildDeltaForSuspendedEvent(execDmc, execDmc, parent, nodeOffset, rm);
        } else if (e instanceof SteppingTimedOutEvent) {
            buildDeltaForSteppingTimedOutEvent((SteppingTimedOutEvent)e, parent, nodeOffset, rm);
        } else if (e instanceof ModelProxyInstalledEvent || e instanceof DataModelInitializedEvent) {
            buildDeltaForModelProxyInstalledEvent(parent, nodeOffset, rm);
        } else if (e instanceof ExpandStackEvent) {
            IExecutionDMContext execDmc = ((ExpandStackEvent)e).getDMContext();
        	buildDeltaForExpandStackEvent(execDmc, parent, rm);
        } else if (e instanceof PropertyChangeEvent) {
            String property = ((PropertyChangeEvent)e).getProperty();
            if (IDsfDebugUIConstants.PREF_STACK_FRAME_LIMIT_ENABLE.equals(property)
                || IDsfDebugUIConstants.PREF_STACK_FRAME_LIMIT.equals(property)) 
            {
                buildDeltaForStackFrameLimitPreferenceChangedEvent(parent, rm);                
            } else if (IDsfDebugUIConstants.DEBUG_VIEW_SHOW_FULL_PATH_PROPERTY.equals(property)) {
                buildDeltaForShowFullPathPreferenceChangedEvent(parent, rm);                
            } else {
            	rm.done();
            }
        } else {
            rm.done();
        }
    }
    
    /**
     * Builds the delta in response to a suspended event.
     * <p>
     * Default behavior is to expand the thread, repaint <strong>only</strong> 
     * the top stack frame and select it.  The rest of the frames will be 
     * repainted after a short delay. 
     *   
     * @since 2.1
     */
	protected void buildDeltaForSuspendedEvent(final IExecutionDMContext executionCtx, final IExecutionDMContext triggeringCtx, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor rm) {
        // Check if we are building a delta for the thread that triggered the event.
        // Only then expand the stack frames and select the top one.
        if (executionCtx.equals(triggeringCtx)) {
            // Always expand the thread node to show the stack frames.
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.EXPAND);
    
            // Retrieve the list of stack frames, and mark the top frame to be selected.
            getVMProvider().updateNode(
                this,
                new VMChildrenUpdate(
                    parentDelta, getVMProvider().getPresentationContext(), 0, 1,
                    new DataRequestMonitor<List<Object>>(getExecutor(), rm) {
                        @Override
                        public void handleCompleted() {
                            final List<Object> data= getData();
							if (data != null && data.size() != 0) {
								parentDelta.addNode(data.get(0), 0, IModelDelta.SELECT | IModelDelta.STATE);
                            }
                            // Even in case of errors, complete the request monitor.
                            rm.done();
                        }
                    })
                );
        }
    }
    
	/**
	 * Builds a delta in response to automatic refresh event generated after 
	 * every suspend event.  
	 * <p>
	 * The default behavior is to check if the thread is
	 * still stepping or suspended and refresh the stack trace.
	 *   
	 * @since 2.1
	 */
	protected void buildDeltaForFullStackRefreshEvent(final IExecutionDMContext executionCtx, final IExecutionDMContext triggeringCtx, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor rm) {
	    try {
    	    getSession().getExecutor().execute(new DsfRunnable() {
    	        public void run() {
    	            IRunControl runControlService = getServicesTracker().getService(IRunControl.class); 
    	            IStack stackService = getServicesTracker().getService(IStack.class);
    	            if (stackService == null || runControlService == null) {
    	                // Required services have not initialized yet.  Ignore the event.
    	                rm.done();
    	                return;
    	            }         
    	            
    	            // Refresh the whole list of stack frames unless the target is already stepping the next command.  In 
    	            // which case, the refresh will occur when the stepping sequence slows down or stops.  Trying to
    	            // refresh the whole stack trace with every step would slow down stepping too much.
    	            if (triggeringCtx == null || !runControlService.isStepping(triggeringCtx)) {
    	                parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
    	            }
    	            
    	            rm.done();
    	        }
    	    });
	    } catch (RejectedExecutionException e) {
	        // Session shut down, no delta to build.
	        rm.done();
	    }
	}
	
    /**
     * Builds the delta in response to a time-out after stepping over a long 
     * executing function. 
     * <p>
     * The default behavior is to repainting the stack frame images to 
     * show the running symbol.
     * 
     * @since 2.1
     */
    protected void buildDeltaForSteppingTimedOutEvent(final SteppingTimedOutEvent e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor rm) {
        // Repaint the stack frame images to have the running symbol.
        //parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        rm.done();
    }
    
    /**
     * Builds the delta in response the debug view being opened.
     * <p>
     * The default behavior is to retrieve the list of stack frames,
     * and mark the top frame to be selected.
     * 
     * @since 2.1
     */
    protected void buildDeltaForModelProxyInstalledEvent(final VMDelta parentDelta, final int nodeOffset, final RequestMonitor rm) {
        getVMProvider().updateNode(
            this, 
            new VMChildrenUpdate(
                parentDelta, getVMProvider().getPresentationContext(), -1, -1,
                new DataRequestMonitor<List<Object>>(getExecutor(), rm) { 
                    @Override
                    public void handleCompleted() {
                        if (isSuccess() && getData().size() != 0) {
                            parentDelta.addNode( getData().get(0), 0, IModelDelta.SELECT | IModelDelta.EXPAND);
                        }                        
                        rm.done();
                    }
                })
            );
    }

    /**
     * Builds the delta in response to the user requesting to retrieve 
     * additional stack frames (changing the current stack frames limit).
     * <p>
     * The default behavior is to refresh the stack frames, and to select the
     * first frame of the new frames that are retrieved.
     * 
     * @since 2.1
     */
    protected void buildDeltaForExpandStackEvent(IExecutionDMContext execDmc, final VMDelta parentDelta, final RequestMonitor rm) {
    	parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        // Retrieve the list of new stack frames, and mark the first frame to be selected.
        final int offset = getStackFrameLimit(execDmc) / 2;
		getVMProvider().updateNode(
            this,
            new VMChildrenUpdate(
                parentDelta, getVMProvider().getPresentationContext(), offset, offset,
                new DataRequestMonitor<List<Object>>(getExecutor(), rm) {
                    @Override
                    public void handleCompleted() {
                        final List<Object> data= getData();
						if (data != null && data.size() != 0) {
							parentDelta.addNode(data.get(0), offset, IModelDelta.SELECT);
                        }
                        rm.done();
                    }
                })
            );
    }


    /**
     * Builds the delta in response to the stack frame limit preference changing.
     * <p>
     * Default behavior is to refresh the stack frames.
     * 
     * @since 2.1
     */
    protected void buildDeltaForStackFrameLimitPreferenceChangedEvent(final VMDelta parentDelta, final RequestMonitor rm) {
        parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        rm.done();
    }

    private void buildDeltaForShowFullPathPreferenceChangedEvent(final VMDelta parentDelta, final RequestMonitor rm) {
        parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.STATE);
        rm.done();
    }
    
    private String produceFrameElementName( String viewName , IFrameDMContext frame ) {
    	/*
    	 *  We are addressing Bugzilla 211490 which wants the Register View  to keep the same expanded
    	 *  state for registers for stack frames within the same thread. Different  threads could have
    	 *  different register sets ( e.g. one thread may have floating point & another may not ). But
    	 *  within a thread we are enforcing  the assumption that the register  sets will be the same.  
    	 *  So we make a more convenient work flow by keeping the same expansion when selecting amount
    	 *  stack frames within the same thread. We accomplish this by only differentiating by  adding
    	 *  the level for the Expression/Variables view. Otherwise we do not delineate based on  which
    	 *  view and this captures the Register View in its filter.
		 */
    	if ( viewName.startsWith(IDebugUIConstants.ID_VARIABLE_VIEW)   ||
    	     viewName.startsWith(IDebugUIConstants.ID_EXPRESSION_VIEW)    )
    	{
    		return "Frame." + frame.getLevel() + "." + frame.getSessionId(); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	else {
    		return "Frame" + frame.getSessionId(); //$NON-NLS-1$
    	}
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#compareElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest[])
     */
    public void compareElements(IElementCompareRequest[] requests) {
        
        for ( IElementCompareRequest request : requests ) {
        	
            Object element = request.getElement();
            IMemento memento = request.getMemento();
            String mementoName = memento.getString("STACK_FRAME_MEMENTO_NAME"); //$NON-NLS-1$
            
            if (mementoName != null) {
                if (element instanceof IDMVMContext) {
                	
                    IDMContext dmc = ((IDMVMContext)element).getDMContext();
                    
                    if ( dmc instanceof IFrameDMContext) {
                    	
                    	String elementName = produceFrameElementName( request.getPresentationContext().getId(), (IFrameDMContext) dmc );
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

            	if ( dmc instanceof IFrameDMContext) {

            		String elementName = produceFrameElementName( request.getPresentationContext().getId(), (IFrameDMContext) dmc );
            		memento.putString("STACK_FRAME_MEMENTO_NAME", elementName); //$NON-NLS-1$
            	} 
            }
            request.done();
        }
    }

	/**
	 * Get the current active stack frame limit. If no limit is applicable {@link Integer.MAX_VALUE} is returned.
	 * 
	 * @return the current stack frame limit
	 * 
	 * @since 1.1
	 */
	public int getStackFrameLimit(IExecutionDMContext execCtx) {
		if (fTemporaryLimits.containsKey(execCtx)) {
			return fTemporaryLimits.get(execCtx);
		}
        Object stackDepthLimit= getVMProvider().getPresentationContext().getProperty(IDsfDebugUIConstants.PREF_STACK_FRAME_LIMIT);
        if (stackDepthLimit instanceof Integer) {
        	return (Integer)stackDepthLimit;
        }
		return Integer.MAX_VALUE;
	}

	/**
	 * Resets the temporary stack frame limit for the given execution context.  
	 * The stack frame limit should be reset when the a thread is suspended.
	 * 
	 * @param execCtx
	 * 
	 * @since 2.1
	 */
    protected void resetStackFrameLimit(IExecutionDMContext execCtx) {
        if (execCtx instanceof IContainerDMContext) {
            for (Iterator<IExecutionDMContext> itr = fTemporaryLimits.keySet().iterator(); itr.hasNext();) {
                IExecutionDMContext limitCtx = itr.next();
                if (limitCtx.equals(execCtx) ||  DMContexts.isAncestorOf(limitCtx, execCtx)) {
                    itr.remove();
                }
            }
        } else {
            fTemporaryLimits.remove(execCtx);
        }
    }
    

	/**
	 * Increment the stack frame limit by the default increment.
	 * This implementation doubles the current limit.
	 * 
	 * @since 1.1
	 */
	public void incrementStackFrameLimit(IExecutionDMContext execCtx) {
		final int stackFrameLimit= getStackFrameLimit(execCtx);
		if (stackFrameLimit < Integer.MAX_VALUE / 2) {
			fTemporaryLimits.put(execCtx, stackFrameLimit * 2);
		} else {
            fTemporaryLimits.put(execCtx, Integer.MAX_VALUE);
		}
	}
}
