/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Base class for actions which delegate functionality to an adapter retrieved
 * from the current debug context.
 * 
 * @since 1.1
 */
abstract public class RetargetDebugContextAction implements IWorkbenchWindowActionDelegate, IDebugContextListener, IActionDelegate2  {

    private IWorkbenchWindow fWindow = null;
    private IAction fAction = null;
    private ISelection fDebugContext;
    private Object fTargetAdapter = null;
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
	public void init(IWorkbenchWindow window) {
        fWindow = window;
        IDebugContextService debugContextService = DebugUITools.getDebugContextManager().getContextService(fWindow);
        debugContextService.addPostDebugContextListener(this);
        fDebugContext = debugContextService.getActiveContext(); 
        update();
    }


    @Override
	public void selectionChanged(IAction action, ISelection selection) {
        if (fAction != action) {
            fAction = action;
        }
        // Update on debug context changed events
    }
    
    @Override
	public void runWithEvent(IAction action, Event event) {
        run(action);
    }
    
    @Override
	public void run(IAction action) {
        if (fTargetAdapter != null) {
            try {
                performAction(fTargetAdapter, fDebugContext);
            } catch (CoreException e) {
                ErrorDialog.openError(fWindow.getShell(), MessagesForVMActions.RetargetDebugContextAction_ErrorDialog_title, MessagesForVMActions.RetargetDebugContextAction_ErrorDialog_message, e.getStatus()); 
            }
        }
    }

    /**
     * Returns whether the specific operation is supported.
     * 
     * @param target the target adapter 
     * @param selection the selection to verify the operation on
     * @param part the part the operation has been requested on
     * @return whether the operation can be performed
     */
    protected abstract boolean canPerformAction(Object target, ISelection debugContext); 

    /**
     * Performs the specific breakpoint toggling.
     * 
     * @param selection selection in the active part 
     * @param part active part
     * @throws CoreException if an exception occurrs
     */
    protected abstract void performAction(Object target, ISelection debugContext) throws CoreException;

    /**
     * Returns the type of adapter (target) this action works on.
     * 
     * @return the type of adapter this action works on
     */
    protected abstract Class<?> getAdapterClass();

    @Override
	public void init(IAction action) {
        fAction = action;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IUpdate#update()
     */
    public void update() {
        if (fAction == null) {
            return;
        }
        fTargetAdapter = null;
        if (fDebugContext instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) fDebugContext;
            if (!ss.isEmpty()) {
                Object object = ss.getFirstElement();
                if (object instanceof IAdaptable) {
                    fTargetAdapter = getAdapter((IAdaptable) object);
                    if (fTargetAdapter != null) {
                        fAction.setEnabled(canPerformAction(fTargetAdapter, fDebugContext));
                        return;
                    }
                }
            }
        }
        fAction.setEnabled(false);
    }

    @Override
	public void dispose() {
        DebugUITools.getDebugContextManager().getContextService(fWindow).removePostDebugContextListener(this);
        fTargetAdapter = null;
    }
    
    @Override
	public void debugContextChanged(DebugContextEvent event) {
        fDebugContext = event.getContext();
        update();
    }
    
    protected Object getAdapter(IAdaptable adaptable) {
        Object adapter  = adaptable.getAdapter(getAdapterClass());
        if (adapter == null) {
            IAdapterManager adapterManager = Platform.getAdapterManager();
            if (adapterManager.hasAdapter(adaptable, getAdapterClass().getName())) { 
                adapter = adapterManager.loadAdapter(adaptable, getAdapterClass().getName()); 
            }
        }
        return adapter;
    }
}
