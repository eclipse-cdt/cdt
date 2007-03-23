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

import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode.DMVMContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This is is a standard root node which listens to the selection in Debug View.
 * Views such as variables and registers base their content on the 
 * selection in Debug view, and this node provides tracking of that selection.
 * <p>
 * Note: The variables/registers views track the selection using the same 
 * IDebugContextListener interface, but they only use the first element of the 
 * selection, as in IStructuredSelection.getFirstElement().  Therefore the root 
 * node also has to use the first element as the root object instead of the 
 * whole selection. 
 */
@SuppressWarnings("restriction")
public class DebugViewSelectionRootLayoutNode extends AbstractVMRootLayoutNode 
    implements IVMRootLayoutNode, IDebugContextListener 
{
    private ISelection fSelection;
    
    public DebugViewSelectionRootLayoutNode(AbstractVMProvider provider) {
        super(provider);
        IWorkbenchWindow activeWindow = DsfDebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        if (activeWindow != null) {
            fSelection = DebugUITools.getDebugContextManager().getContextService(activeWindow).getActiveContext();
        }
        DebugUITools.getDebugContextManager().addDebugContextListener(this);
    }

    @Override
    public void dispose() {
        DebugUITools.getDebugContextManager().removeDebugContextListener(this);
        super.dispose();
    }
    
    /**
     * If the input object is a Data Model context, and the event is a DMC event.
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
    public int getDeltaFlags(Object event) {
        IDMContext<?> inputDmc = getSelectedDMC();
        if (event instanceof IDMEvent && inputDmc != null) {
            boolean potentialMatchFound = false;
            boolean matchFound = false;
            
            IDMContext<?> eventDmc = ((IDMEvent<?>)event).getDMContext();
            for (IDMContext<?> eventDmcAncestor : DMContexts.toList(eventDmc)) {
                IDMContext<?> inputDmcAncestor = DMContexts.getAncestorOfType(inputDmc, eventDmcAncestor.getClass()); 
                if (inputDmcAncestor != null) {
                    potentialMatchFound = true;
                    if (inputDmcAncestor.equals(eventDmcAncestor)) {
                        return super.getDeltaFlags(event);
                    }
                }
            }
            if (potentialMatchFound && !matchFound) {
                return IModelDelta.NO_CHANGE;
            }
        }

        return super.getDeltaFlags(event);
    }

    /**
     * Returns the full selection as it came from the DebugContextManager.  
     * @return
     */
    public ISelection getSelection() {
        return fSelection;
    }
    
    public Object getRootObject() {
        if (fSelection instanceof IStructuredSelection) {
            return ((IStructuredSelection)fSelection).getFirstElement();
        }
        return null;
    }
    
    public void debugContextChanged(DebugContextEvent event) {
        fSelection = event.getContext();
    }
    
    private IDMContext<?> getSelectedDMC() {
        Object selection = fSelection;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structSelection = (IStructuredSelection)selection; 
            if (structSelection.getFirstElement() instanceof DMVMContext) 
            {
                // Correct cast: (AbstractDMVMLayoutNode<?>.DMVMContext) breaks the javac compiler
                @SuppressWarnings("unchecked")
                DMVMContext vmc = (DMVMContext)structSelection.getFirstElement();
                return vmc.getDMC();
            }
        }
        return null;
    }
}
