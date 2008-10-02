/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems and others.
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

import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.AbstractThreadVMNode;
import org.eclipse.dd.dsf.debug.service.IProcesses;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.dd.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.dd.mi.service.IMIExecutionDMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
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
    protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
        	final IRunControl runControl = getServicesTracker().getService(IRunControl.class);
            if (runControl == null) {
                    handleFailedUpdate(update);
                    continue;
            }
            
            final IMIExecutionDMContext execDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IMIExecutionDMContext.class);

            String imageKey = null;
            final boolean threadSuspended;
            if (runControl.isSuspended(execDmc)) {
            	threadSuspended = true;
                imageKey = IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
            } else {
            	threadSuspended = false;
                imageKey = IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
            }
            update.setImageDescriptor(DebugUITools.getImageDescriptor(imageKey), 0);

            // Find the Reason for the State
            runControl.getExecutionData(execDmc,
            		new ViewerDataRequestMonitor<IExecutionDMData>(getSession().getExecutor(), update) {
            	@Override
				public void handleCompleted(){
                    if (!isSuccess()) {
                    	update.setLabel("<unavailable>", 0); //$NON-NLS-1$
                    	update.done();
                        return;
                    }

                    final IProcesses procService = getServicesTracker().getService(IProcesses.class);
                    if ( procService == null ) {
                        handleFailedUpdate(update);
                        return;
                    }

                    final StateChangeReason reason = getData().getStateChangeReason();

                    // Retrieve the rest of the thread information
                    final IThreadDMContext threadDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IThreadDMContext.class);

                    procService.getExecutionData(
                    	threadDmc,
                        new ViewerDataRequestMonitor<IThreadDMData>(getSession().getExecutor(), update) {
                            @Override
                            public void handleCompleted() {
                            	// We can still generate a good enough label even if this call fails
                            	// so continue and check if we should use getData() or not.

                                // Create Labels of type Thread[GDBthreadId]RealThreadID/Name (State: Reason)
                                // Thread[1] 3457 (Suspended:BREAKPOINT)
                                final StringBuilder builder = new StringBuilder("Thread["); //$NON-NLS-1$
                                builder.append(execDmc.getThreadId());
                                builder.append("] "); //$NON-NLS-1$
                                if (isSuccess()) {
                                	builder.append(getData().getId());
                                	builder.append(getData().getName());
                                }
                                if(threadSuspended)
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

	private String produceThreadElementName(String viewName, IMIExecutionDMContext execCtx) {
		return "Thread." + execCtx.getThreadId(); //$NON-NLS-1$
    }

    private static final String MEMENTO_NAME = "THREAD_MEMENTO_NAME"; //$NON-NLS-1$
    
    /*
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider#compareElements(org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest[])
     */
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
