/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.commands;

import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.actions.MessagesForVMActions;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Base class for actions which delegate functionality to an adapter retrieved
 * from the current debug context.
 * 
 * @since 2.0
 */
abstract public class RetargetDebugContextCommand extends AbstractHandler implements IDebugContextListener  {

    private ISelection fDebugContext;
    private Object fTargetAdapter = null;
    private IDebugContextService fContextService = null;
    private String fCommandId = null;
    
    protected Object getTargetAdapter() { return fTargetAdapter; }
    protected ISelection getDebugContext() { return fDebugContext; }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public RetargetDebugContextCommand() {
    	IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    	fContextService = DebugUITools.getDebugContextManager().getContextService(window);
    	fContextService.addPostDebugContextListener(this);
        fDebugContext = fContextService.getActiveContext(); 
        update();
    }

    @Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		fCommandId = event.getCommand().getId();
		
        if (fTargetAdapter != null) {
            try {
            	performCommand(fTargetAdapter, fDebugContext);
            } catch (ExecutionException e) {
            	Shell shell = HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell();
                ErrorDialog.openError(shell, MessagesForVMActions.RetargetDebugContextAction_ErrorDialog_title, MessagesForVMActions.RetargetDebugContextAction_ErrorDialog_message, null); 
            }
        }

        update();
        
		return null;
	}
    
    /**
     * Returns whether the specific operation is supported.
     * 
     * @param target the target adapter 
     * @param debugContext the selection to verify the operation on
     * @return whether the operation can be performed
     */
    protected abstract boolean canPerformCommand(Object target, ISelection debugContext); 

    /**
     * Performs the specific operation.
     * 
     * @param target the target adapter 
     * @param debugContext the selection to verify the operation on
     * @throws CoreException if an exception occurs
     */
    protected abstract void performCommand(Object target, ISelection debugContext) throws ExecutionException;

    /**
     * Returns the type of adapter (target) this command works on.
     * 
     * @return the type of adapter this command works on
     */
    protected abstract Class<?> getAdapterClass();

    public void update() {
    	boolean enabled = false;
    	
        fTargetAdapter = null;
        if (fDebugContext instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) fDebugContext;
            if (!ss.isEmpty()) {
                Object object = ss.getFirstElement();
                if (object instanceof IAdaptable) {
                    fTargetAdapter = getAdapter((IAdaptable) object);
                    if (fTargetAdapter != null) {
                    	enabled = canPerformCommand(fTargetAdapter, fDebugContext);
                    } 
                }
            }
        }
        
       	setBaseEnabled(enabled);
        
        if (fCommandId != null) {
        	ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        	if (commandService != null) {
        		commandService.refreshElements(fCommandId, null);
        	}
        }
    }

    @Override
	public void dispose() {
    	// Must use the stored service.  If we try to fetch the service
    	// again with the workbenchWindow, it may fail if the window is
    	// already closed.
    	fContextService.removePostDebugContextListener(this);
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
