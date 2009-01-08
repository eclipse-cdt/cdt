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
package org.eclipse.cdt.examples.dsf.pda.ui.viewmodel.launch;

import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractThreadVMNode;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.examples.dsf.pda.service.PDARunControl;
import org.eclipse.cdt.examples.dsf.pda.service.PDAThreadDMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IMemento;


/**
 * View model node supplying the PDA thread elements.  It extends 
 * the base threads node and adds label and memento generation.
 */
@SuppressWarnings("restriction")
public class PDAThreadsVMNode extends AbstractThreadVMNode 
    implements IElementLabelProvider, IElementMementoProvider
{
    public PDAThreadsVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session);
    }

    @Override
    public String toString() {
        return "PDAThreadVMNode(" + getSession().getId() + ")"; 
    }
    
    @Override
    protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
        	final PDARunControl runControl = getServicesTracker().getService(PDARunControl.class);
            if ( runControl == null ) {
                    handleFailedUpdate(update);
                    continue;
            }
            
            final PDAThreadDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), PDAThreadDMContext.class);

            String imageKey = null;
            if (getServicesTracker().getService(IRunControl.class).isSuspended(dmc)) {
                imageKey = IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
            } else {
                imageKey = IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
            }
            update.setImageDescriptor(DebugUITools.getImageDescriptor(imageKey), 0);

            // Find the Reason for the State
            getDMVMProvider().getModelData(
                this, update, runControl, dmc,
                new ViewerDataRequestMonitor<IExecutionDMData>(getSession().getExecutor(), update) {
                    @Override
                    public void handleCompleted(){
                        if (!isSuccess()) {
                            update.setLabel("<unavailable>", 0);
                            update.done();
                            return;
                        }

                        // We're in a new dispatch cycle, and we have to check whether the
                        // service reference is still valid.
                        final PDARunControl runControl = getServicesTracker().getService(PDARunControl.class);
                        if ( runControl == null ) {
                            handleFailedUpdate(update);
                            return;
                        }
    
                        final StateChangeReason reason = getData().getStateChangeReason();
    
                        // Create Labels of type Thread[GDBthreadId]RealThreadID/Name (State: Reason)
                        // Thread[1] 3457 (Suspended:BREAKPOINT)
                        final StringBuilder builder = new StringBuilder();
                        builder.append("Thread ");
                        builder.append(dmc.getID());
                        if(getServicesTracker().getService(IRunControl.class).isSuspended(dmc))
                            builder.append(" (Suspended"); 
                        else
                            builder.append(" (Running"); 
                        // Reason will be null before ContainerSuspendEvent is fired
                        if(reason != null) {
                            builder.append(" : "); 
                            builder.append(reason);
                        }
                        builder.append(")"); 
                        update.setLabel(builder.toString(), 0);
                        update.done();
                	}
                }, 
                getExecutor());
            
        }
    }

	private String produceThreadElementName(String viewName, PDAThreadDMContext execCtx) {
		return "Thread." + execCtx.getID(); 
    }

    private static final String MEMENTO_NAME = "THREAD_MEMENTO_NAME"; 
    
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
                    if ( dmc instanceof PDAThreadDMContext) {
                    	String elementName = produceThreadElementName( 
                    	    request.getPresentationContext().getId(), (PDAThreadDMContext) dmc );
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
            	if ( dmc instanceof PDAThreadDMContext) {
            		String elementName = produceThreadElementName( request.getPresentationContext().getId(), (PDAThreadDMContext) dmc );
                	memento.putString(MEMENTO_NAME, elementName);
                }
            }
            request.done();
        }
    }

}
