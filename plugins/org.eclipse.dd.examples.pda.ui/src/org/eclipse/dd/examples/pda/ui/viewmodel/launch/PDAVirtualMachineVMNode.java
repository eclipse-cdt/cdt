/*******************************************************************************
 * Copyright (c) 2006, 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial API and implementation
 *     Wind River Systems - Factored out AbstractContainerVMNode
 *******************************************************************************/

package org.eclipse.dd.examples.pda.ui.viewmodel.launch;


import org.eclipse.dd.dsf.concurrent.ImmediateExecutor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.AbstractContainerVMNode;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.dd.examples.pda.service.PDACommandControl;
import org.eclipse.dd.examples.pda.service.PDAVirtualMachineDMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IMemento;


/**
 * View Model node representing a PDA virtual machine.  It extends
 * the base container node and adds label and memento generation. 
 */
@SuppressWarnings("restriction")
public class PDAVirtualMachineVMNode extends AbstractContainerVMNode
    implements IElementMementoProvider
{
	public PDAVirtualMachineVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session);
	}

	@Override
    public String toString() {
        return "PDAContainerVMNode(" + getSession().getId() + ")"; 
    }


	@Override
	protected void updateElementsInSessionThread(IChildrenUpdate update) {
        // Get the instance of the service.  Note that there is no race condition
        // in getting the service since this method is called only in the 
        // service executor thread.
        final PDACommandControl commandControl = getServicesTracker().getService(PDACommandControl.class);

        // Check if the service is available.  If it is not, no elements are 
        // updated.
        if (commandControl == null) {
            handleFailedUpdate(update);
            return;
        }
        
        update.setChild(createVMContext(commandControl.getContext()), 0);
        update.done();
	}

	
    @Override
	protected void updateLabelInSessionThread(final ILabelUpdate update) {
        // Get a reference to the run control service.
        final IRunControl runControl = getServicesTracker().getService(IRunControl.class);
        if (runControl == null) {
            handleFailedUpdate(update);
            return;
        }
        
        // Find the PDA program context.
        final PDAVirtualMachineDMContext programCtx = 
            findDmcInPath(update.getViewerInput(), update.getElementPath(), PDAVirtualMachineDMContext.class);

        // Call service to get current program state
        final boolean isSuspended = runControl.isSuspended(programCtx);

        // Set the program icon based on the running state of the program.
        String imageKey = null;
        if (isSuspended) {
            imageKey = IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
        } else {
            imageKey = IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
        }
        update.setImageDescriptor(DebugUITools.getImageDescriptor(imageKey), 0);

        // Retrieve the last state change reason 
        getDMVMProvider().getModelData(
            this, update, runControl, programCtx, 
            new ViewerDataRequestMonitor<IExecutionDMData>(ImmediateExecutor.getInstance(), update) 
            { 
                @Override
                public void handleCompleted(){
                    // If the request failed, fail the udpate. 
                    if (!isSuccess()) {
                        handleFailedUpdate(update);
                        return;
                    }
    
                    // Compose the thread name string.
                    final StringBuilder builder = new StringBuilder(); 
    
                    builder.append("PDA [");
                    builder.append(programCtx.getProgram());
                    builder.append("]");
                    
                    if(isSuspended) {
                        builder.append(" (Suspended"); 
                    } else {
                        builder.append(" (Running"); 
                    }
                    // Reason will be null before ContainerSuspendEvent is fired
                    if(getData().getStateChangeReason() != null) {
                        builder.append(" : "); 
                        builder.append(getData().getStateChangeReason());
                    }
                    builder.append(")"); 
                    update.setLabel(builder.toString(), 0);
                    update.done();
                }
            },
            getExecutor());        
    }

    private String produceProgramElementName( String viewName , PDAVirtualMachineDMContext execCtx ) {
        return "PDA." + execCtx.getProgram(); //$NON-NLS-1$
    }
    
    private final String MEMENTO_NAME = "PDAPROGRAM_MEMENTO_NAME"; //$NON-NLS-1$
    
    public void compareElements(IElementCompareRequest[] requests) {
        
        for ( IElementCompareRequest request : requests ) {
            
            Object element = request.getElement();
            IMemento memento = request.getMemento();
            String mementoName = memento.getString(MEMENTO_NAME);
            
            if (mementoName != null) {
                if (element instanceof IDMVMContext) {
                    
                    IDMContext dmc = ((IDMVMContext)element).getDMContext();
                    
                    if ( dmc instanceof PDAVirtualMachineDMContext) {
                        
                        String elementName = produceProgramElementName( request.getPresentationContext().getId(), (PDAVirtualMachineDMContext) dmc );
                        request.setEqual( elementName.equals( mementoName ) );
                    } 
                }
            }
            request.done();
        }
    }
    
    public void encodeElements(IElementMementoRequest[] requests) {
        
        for ( IElementMementoRequest request : requests ) {
            
            Object element = request.getElement();
            IMemento memento = request.getMemento();
            
            if (element instanceof IDMVMContext) {

                IDMContext dmc = ((IDMVMContext)element).getDMContext();

                if ( dmc instanceof PDAVirtualMachineDMContext) {

                    String elementName = produceProgramElementName( request.getPresentationContext().getId(), (PDAVirtualMachineDMContext) dmc );
                    memento.putString(MEMENTO_NAME, elementName);
                } 
            }
            request.done();
        }
    }
}
