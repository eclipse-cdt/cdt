/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
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

import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractThreadVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ILaunchVMConstants;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.examples.dsf.pda.service.PDAThreadDMContext;
import org.eclipse.cdt.examples.dsf.pda.ui.PDAUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
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
    protected void updatePropertiesInSessionThread(IPropertiesUpdate[] updates) {
        for (int i = 0; i < updates.length; i++) {
            final PDAThreadDMContext dmc = findDmcInPath(updates[i].getViewerInput(), updates[i].getElementPath(), PDAThreadDMContext.class);
            if (dmc != null) {
                updates[i].setProperty(ILaunchVMConstants.PROP_ID, Integer.toString(dmc.getID()));
            } else {
                updates[i].setStatus(new Status(IStatus.ERROR, PDAUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE, "Invalid context", null)); //$NON-NLS-1$
            }
        }
        super.updatePropertiesInSessionThread(updates);
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
