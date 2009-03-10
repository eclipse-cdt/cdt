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
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractThreadVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ILaunchVMConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerCountingRequestMonitor;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.VMDelegatingPropertiesUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementCompareRequest;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;
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
                                final IMIExecutionDMContext execDmc = findDmcInPath(
                                    update.getViewerInput(), update.getElementPath(), IMIExecutionDMContext.class);
                                if (execDmc != null) {
                                    update.setProperty(ILaunchVMConstants.PROP_ID, Integer.toString(execDmc.getThreadId()));
                                } else {
                                    update.setStatus(getStatus());
                                }
                            }
                            countringRm.done();
                        }
                    });
                count++;
            }
            
            countringRm.setDoneCount(count);
        }
        super.updatePropertiesInSessionThread(parentUpdates);
    }
    
    protected void fillThreadDataProperties(IPropertiesUpdate update, IThreadDMData data) {
        update.setProperty(PROP_NAME, data.getName());
        update.setProperty(ILaunchVMConstants.PROP_ID, data.getId());
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
