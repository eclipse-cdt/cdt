/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.DMContextVMLayoutNode.DMContextVMContext;
import org.eclipse.debug.internal.ui.contexts.DebugContextManager;
import org.eclipse.debug.internal.ui.contexts.provisional.IDebugContextListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This is is a standard root node which listens to the selection in Debug View.
 * Views such as variables and registers base their content based on the 
 * selection in Debug view, and this node provides tracking of that selection.
 */
@SuppressWarnings("restriction")
public class DebugViewSelectionRootLayoutNode extends AbstractVMRootLayoutNode 
    implements IVMRootLayoutNode, IDebugContextListener 
{
    private RootVMC<Object> fRootVMC;
    
    public DebugViewSelectionRootLayoutNode(DsfExecutor executor, IWorkbenchWindow window) {
        super(executor);
        ISelection selection = DebugContextManager.getDefault().getActiveContext(window);
        if (selection instanceof IStructuredSelection) {
            fRootVMC = new RootVMC<Object>( this, ((IStructuredSelection)selection).getFirstElement() );
        } else {
            fRootVMC = new RootVMC<Object>( this, null );
        }
        DebugContextManager.getDefault().addDebugContextListener(this, window);
    }

    @Override
    public void sessionDispose() {
        DebugContextManager.getDefault().removeDebugContextListener(this);
        super.sessionDispose();
    }
    
    /**
     * If the input object is a DMC-VMC, and the event is a DMC event.
     * Then we can filter the event to make sure that the view does not
     * react to events that relate to objects outside this view.
     * 
     * The logic is such: 
     * - iterate through the full hierarchy of the DMC in the event,
     * - for each DMC in event, search for a DMC of the same type in the input 
     * event,
     * - if an ancestor of that type is found, it indicates that the event 
     * and the input object share the same hierarchy
     * - finally compare the DMContexts from the event to the DMC from the input 
     * object, 
     * - if there is a match then we know that the event relates
     * to the hierarchy in view,  
     * - if there is no match, then we know that the event related to a
     * some sibling of the input object, and no delta should be generated,
     * - if none of the ancestor types matched, then the event is completely
     * unrelated to the input object, and the layout nodes in the view must
     * determine whether a delta is needed. 
     */
    @Override
    public boolean hasDeltaFlags(Object event) {
        /*
         * TODO: This logic needs to be tested and probably enhanced.
         */
        if (event instanceof IDMEvent && fRootVMC.getInputObject() instanceof DMContextVMContext) {
            boolean potentialMatchFound = false;
            boolean matchFound = false;
            
            IDMContext eventDmc = ((IDMEvent)event).getDMContext();
            IDMContext inputDmc = ((DMContextVMContext)fRootVMC.getInputObject()).getDMC();
            for (IDMContext eventDmcAncestor : DMContexts.toList(eventDmc)) {
                IDMContext inputDmcAncestor = DMContexts.getAncestorOfType(inputDmc, eventDmcAncestor.getClass()); 
                if (inputDmcAncestor != null) {
                    potentialMatchFound = true;
                    if (inputDmcAncestor.equals(eventDmcAncestor)) {
                        matchFound = true;
                    }
                }
            }
            if (potentialMatchFound && !matchFound) {
                return false;
            }
        }

        return super.hasDeltaFlags(event);
    }

    public IRootVMC getRootVMC() {
        return fRootVMC;
    }
    
    public void contextActivated(ISelection selection, IWorkbenchPart part) {
        if (selection instanceof IStructuredSelection) {
            fRootVMC = new RootVMC<Object>( this, ((IStructuredSelection)selection).getFirstElement() );
        } else {
            fRootVMC = new RootVMC<Object>( this, null );
        }
    }
    
    public void contextChanged(ISelection selection, IWorkbenchPart part) {
        // No need to do anything. Element changes should be handled 
        // through standard event handlers.
    }
}
