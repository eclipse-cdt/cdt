/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.commands;

import java.util.Map;

import org.eclipse.cdt.debug.core.model.IReverseToggleHandler;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.DebugCommandHandler;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.services.IEvaluationService;

/**
 * Command handler to toggle reverse debugging mode
 *
 * @since 7.0
 */
public class ReverseToggleCommandHandler extends DebugCommandHandler implements IDebugContextListener, IElementUpdater {
	@Override
	protected Class<?> getCommandType() {
		return IReverseToggleHandler.class;
	}

    //
    // The below logic allows us to keep the checked state of the toggle button
    // properly set.  This is because in some case, the checked state may change
    // without the user actually pressing the button.  For instance, if we restart
    // the inferior, the toggle may automatically turn off.
    // To figure this out, whenever a debug context changes, we make sure we are
    // showing the proper checked state.
    //

    // We must hard-code the command id so as to know it from the very start (bug 290699)
    private static final String REVERSE_TOGGLE_COMMAND_ID = "org.eclipse.cdt.debug.ui.command.reverseToggle"; //$NON-NLS-1$

    private Object fActiveContext = null;
    private IReverseToggleHandler fTargetAdapter = null;
    private IDebugContextService fContextService = null;

    public ReverseToggleCommandHandler() {
       IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
       if (window != null) {
    	   fContextService = DebugUITools.getDebugContextManager().getContextService(window);
    	   if (fContextService != null) {
    		   fContextService.addPostDebugContextListener(this);

    		   // This constructor might be called after the launch, so we must refresh here too.
    		   // This can happen if we activate the action set after the launch.
    		   refresh(fContextService.getActiveContext());
    	   }
       }
    }

    @Override
    public void dispose() {
        // Must use the stored context service.  If we try to fetch the service
        // again with the workbenchWindow, it may fail if the window is
        // already closed.
        if (fContextService != null) {
            fContextService.removePostDebugContextListener(this);
        }
        fTargetAdapter = null;
        fActiveContext = null;
        super.dispose();
    }

    @Override
	public void debugContextChanged(DebugContextEvent event) {
        refresh(event.getContext());
    }

    private void refresh(ISelection selection) {
       fTargetAdapter = null;
       fActiveContext = null;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            if (!ss.isEmpty()) {
                fActiveContext = ss.getFirstElement();
                if (fActiveContext instanceof IAdaptable) {
                   fTargetAdapter = getAdapter((IAdaptable) fActiveContext);
                }
            }
        }

        ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        if (commandService != null) {
           commandService.refreshElements(REVERSE_TOGGLE_COMMAND_ID, null);
        }
    }

    private IReverseToggleHandler getAdapter(IAdaptable adaptable) {
       IReverseToggleHandler adapter  = (IReverseToggleHandler)adaptable.getAdapter(IReverseToggleHandler.class);
        if (adapter == null) {
            IAdapterManager adapterManager = Platform.getAdapterManager();
            if (adapterManager.hasAdapter(adaptable, getCommandType().getName())) {
                adapter = (IReverseToggleHandler)adapterManager.loadAdapter(adaptable, IReverseToggleHandler.class.getName());
            }
        }
        return adapter;
    }

    @Override
    protected void postExecute(IRequest request, Object[] targets) {
    	super.postExecute(request, targets);
    	// request re-evaluation of property "org.eclipse.cdt.debug.ui.isReverseDebuggingEnabled"
    	new WorkbenchJob("") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
		        IEvaluationService exprService = (IEvaluationService) PlatformUI.getWorkbench().getService(IEvaluationService.class);
		        if (exprService != null) { 
		        	exprService.requestEvaluation("org.eclipse.cdt.debug.ui.isReverseDebuggingEnabled"); //$NON-NLS-1$
		        }
				return Status.OK_STATUS;
			}
		}.schedule();
    }

    @Override
	public void updateElement(UIElement element,
                              @SuppressWarnings("rawtypes") Map parameters) {
       // Make sure the toggle state reflects the actual state
       // We must check this, in case we have multiple launches
       // or if we re-launch (restart)
       if (fTargetAdapter != null && fTargetAdapter.toggleNeedsUpdating()){
           boolean toggled = fTargetAdapter.isReverseToggled(fActiveContext);
           element.setChecked(toggled);
       }
   }
}