/*******************************************************************************
 * Copyright (c) 2006, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial API and implementation
 *     Wind River Systems - Factored out AbstractContainerVMNode
 *******************************************************************************/

package org.eclipse.cdt.examples.dsf.pda.ui.viewmodel.launch;


import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractContainerVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ILaunchVMConstants;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.examples.dsf.pda.service.PDACommandControl;
import org.eclipse.cdt.examples.dsf.pda.service.PDAVirtualMachineDMContext;
import org.eclipse.cdt.examples.dsf.pda.ui.PDAUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
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
    protected void updatePropertiesInSessionThread(IPropertiesUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            // Find the PDA program context.
            final PDAVirtualMachineDMContext dmc = 
                findDmcInPath(updates[i].getViewerInput(), updates[i].getElementPath(), PDAVirtualMachineDMContext.class);
            if (dmc != null) {
                updates[i].setProperty(PROP_NAME, "PDA");
                updates[i].setProperty(ILaunchVMConstants.PROP_ID, dmc.getProgram());
            } else {
                updates[i].setStatus(new Status(IStatus.ERROR, PDAUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            }
        }
        super.updatePropertiesInSessionThread(updates);
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
