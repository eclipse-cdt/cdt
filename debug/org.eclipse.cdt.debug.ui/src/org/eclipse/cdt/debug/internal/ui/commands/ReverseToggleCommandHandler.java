/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.commands;

import java.net.URL;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler;
import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler.ReverseTraceMethod;
import org.eclipse.cdt.debug.core.model.IReverseToggleHandler;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.DebugCommandHandler;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.services.IEvaluationService;
import org.osgi.framework.Bundle;

/**
 * Command handler to toggle reverse debugging mode
 *
 * @since 7.0
 */
public class ReverseToggleCommandHandler extends DebugCommandHandler implements IDebugContextListener, IElementUpdater {

    ReverseTraceMethod traceMethod = null;
    ReverseTraceMethod lastTraceMethod = null;
    ImageDescriptor tracemethodOnImages[];
    ImageDescriptor tracemethodOffImages[];
    ImageDescriptor tracemethodDefaultImage = null;
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

    private ImageDescriptor getImageDescriptor (String path) {
        Bundle bundle = Platform.getBundle("org.eclipse.cdt.debug.ui"); //$NON-NLS-1$
        URL url = null;
        if (bundle != null){
                url = FileLocator.find(bundle, new Path(path), null);
                if(url != null) {
                        return ImageDescriptor.createFromURL(url);
                }
        }
        return null;
    }

    public ReverseToggleCommandHandler() {
       IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
       if (window != null) {
           fContextService = DebugUITools.getDebugContextManager().getContextService(window);
           if (fContextService != null) {
               fContextService.addPostDebugContextListener(this);

               // This constructor might be called after the launch, so we must refresh here too.
               // This can happen if we activate the action set after the launch.
               refresh(fContextService.getActiveContext());

               tracemethodOnImages = new ImageDescriptor[2];
               tracemethodOffImages = new ImageDescriptor[2];
               tracemethodDefaultImage = getImageDescriptor("icons/obj16/reverse_toggle.gif"); //$NON-NLS-1$
               tracemethodOnImages[0] = getImageDescriptor("icons/obj16/full_trace_on.gif"); //$NON-NLS-1$
               tracemethodOnImages[1] = getImageDescriptor("icons/obj16/branch_trace_on.gif"); //$NON-NLS-1$
               tracemethodOffImages[0] = getImageDescriptor("icons/obj16/full_trace_off.gif"); //$NON-NLS-1$
               tracemethodOffImages[1] = getImageDescriptor("icons/obj16/branch_trace_off.gif"); //$NON-NLS-1$

               traceMethod = ReverseTraceMethod.STOP_TRACE;
               lastTraceMethod = ReverseTraceMethod.STOP_TRACE;
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

        ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
        if (commandService != null) {
           commandService.refreshElements(REVERSE_TOGGLE_COMMAND_ID, null);
        }
    }

    private IChangeReverseMethodHandler getAdapter(IAdaptable adaptable) {
        IReverseToggleHandler adapter  = adaptable.getAdapter(IReverseToggleHandler.class);
        if (adapter == null) {
            IAdapterManager adapterManager = Platform.getAdapterManager();
            if (adapterManager.hasAdapter(adaptable, getCommandType().getName())) {
                adapter = (IReverseToggleHandler)adapterManager.loadAdapter(adaptable, IReverseToggleHandler.class.getName());
            }
        }
        if (adapter instanceof IChangeReverseMethodHandler)
            return (IChangeReverseMethodHandler)adapter;
        else
            return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.actions.DebugCommandHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
    	ReverseTraceMethod traceMethod;
        try {

        if(HandlerUtil.matchesRadioState(event)) {
            return null;
        }

        String radioState = event.getParameter(RadioState.PARAMETER_ID);

        if (radioState.equals("UseSoftTrace")) { //$NON-NLS-1$
            traceMethod = ReverseTraceMethod.FULL_TRACE;
        }
        else if (radioState.equals("UseHardTrace")) { //$NON-NLS-1$
                    traceMethod = ReverseTraceMethod.HARDWARE_TRACE;
            }
            else {
                // undefined trace method
                throw new ExecutionException("Undefined trace method for Reverse Debugging."); //$NON-NLS-1$
            }

            // store the parameter in the gdb command handler class
            if (fTargetAdapter != null && fTargetAdapter instanceof IChangeReverseMethodHandler) {
                ((IChangeReverseMethodHandler)fTargetAdapter).setTraceMethod(traceMethod);
            }

            // execute the event
            super.execute(event);

            // and finally update the radio current state
            HandlerUtil.updateRadioState(event.getCommand(), radioState);

            return null;
        }
        catch ( NullPointerException | ExecutionException e) {
            // Disable tracing
            if (fTargetAdapter != null && fTargetAdapter instanceof IChangeReverseMethodHandler) {
                if (fTargetAdapter.toggleNeedsUpdating()){
                    ReverseTraceMethod currMethod = ((IChangeReverseMethodHandler)fTargetAdapter).getTraceMethod(fActiveContext);
                    if(currMethod == ReverseTraceMethod.STOP_TRACE) {
                        if( lastTraceMethod != ReverseTraceMethod.STOP_TRACE && lastTraceMethod != ReverseTraceMethod.FULL_TRACE) {
                                traceMethod = ReverseTraceMethod.HARDWARE_TRACE;
                    }
                    else
                                traceMethod = ReverseTraceMethod.FULL_TRACE;

                    }
                    else
                        traceMethod = ReverseTraceMethod.STOP_TRACE;
                        ((IChangeReverseMethodHandler)fTargetAdapter).setTraceMethod(traceMethod);
                }
        }
            super.execute(event);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.ui.actions.DebugCommandHandler#postExecute(org.eclipse.debug.core.IRequest, java.lang.Object[])
     * 
     * We keep this logic for users that may not do the refresh themselves.
     */
    @Override
    protected void postExecute(final IRequest request, Object[] targets) {
        super.postExecute(request, targets);
        new WorkbenchJob("") { //$NON-NLS-1$
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                boolean prop = CDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(ICDebugPreferenceConstants.PREF_SHOW_ERROR_REVERSE_TRACE_METHOD_NOT_AVAILABLE);
                if(prop && request.getStatus() != null && request.getStatus().getCode() != 0 ) {
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    Shell activeShell = null;
                    if(window != null)
                        activeShell = window.getShell();
                    else
                        activeShell = new Shell(PlatformUI.getWorkbench().getDisplay());

                    MessageDialogWithToggle dialogbox = new MessageDialogWithToggle(activeShell, "Error", //$NON-NLS-1$
                            null, "Hardware Tracing Method not available, Reverse debugging is switched Off, please select another method", MessageDialog.QUESTION, //$NON-NLS-1$
                            new String[] {IDialogConstants.OK_LABEL}, 0,
                            "Don't show this message again", false); //$NON-NLS-1$
                    dialogbox.setPrefStore(CDebugUIPlugin.getDefault().getPreferenceStore());
                    dialogbox.setPrefKey(ICDebugPreferenceConstants.PREF_SHOW_ERROR_REVERSE_TRACE_METHOD_NOT_AVAILABLE);
                    if(dialogbox.open() == 0){
                        boolean toggled = dialogbox.getToggleState();
                        CDebugUIPlugin.getDefault().getPreferenceStore().setValue(ICDebugPreferenceConstants.PREF_SHOW_ERROR_REVERSE_TRACE_METHOD_NOT_AVAILABLE, !toggled);
                    }
                }
                // Request re-evaluation of property "org.eclipse.cdt.debug.ui.isReverseDebuggingEnabled" to update 
                // visibility of reverse stepping commands.
                IEvaluationService exprService = PlatformUI.getWorkbench().getService(IEvaluationService.class);
                if (exprService != null) {
                    exprService.requestEvaluation("org.eclipse.cdt.debug.ui.isReverseDebuggingEnabled"); //$NON-NLS-1$
                }
                // Refresh reverse toggle commands with the new state of reverse enabled. 
                // This is in order to keep multiple toggle actions in UI in sync.
                ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
                if (commandService != null) {
                   commandService.refreshElements(REVERSE_TOGGLE_COMMAND_ID, null);
                }
                
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    @Override
    public void updateElement(UIElement element,
                              @SuppressWarnings("rawtypes") Map parameters) {
       if(fTargetAdapter != null && fTargetAdapter instanceof IChangeReverseMethodHandler){
           ReverseTraceMethod reverseMethod = ((IChangeReverseMethodHandler)fTargetAdapter).getTraceMethod(fActiveContext);
           ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
           if(reverseMethod != traceMethod){
               lastTraceMethod = traceMethod;
               traceMethod = reverseMethod;
           }
           try{
               if (traceMethod != ReverseTraceMethod.STOP_TRACE && traceMethod != ReverseTraceMethod.FULL_TRACE) {
                   HandlerUtil.updateRadioState(commandService.getCommand(REVERSE_TOGGLE_COMMAND_ID), "UseHardTrace"); //$NON-NLS-1$
                   element.setTooltip("Toggle Hardware Trace"); //$NON-NLS-1$
                   element.setIcon(tracemethodOnImages[1]);
                   }
               else if (traceMethod == ReverseTraceMethod.FULL_TRACE) {
                   HandlerUtil.updateRadioState(commandService.getCommand(REVERSE_TOGGLE_COMMAND_ID), "UseSoftTrace"); //$NON-NLS-1$
                   element.setTooltip("Toggle Software Trace"); //$NON-NLS-1$
                   element.setIcon(tracemethodOnImages[0]);
                   }
               else {
                   element.setTooltip("Toggle Reverse Debugging"); //$NON-NLS-1$
                   if (lastTraceMethod != ReverseTraceMethod.STOP_TRACE && lastTraceMethod != ReverseTraceMethod.FULL_TRACE) {
                       HandlerUtil.updateRadioState(commandService.getCommand(REVERSE_TOGGLE_COMMAND_ID), "UseHardTrace"); //$NON-NLS-1$
                       element.setIcon(tracemethodOffImages[1]);
                       }
                   else if (lastTraceMethod == ReverseTraceMethod.FULL_TRACE) {
                       HandlerUtil.updateRadioState(commandService.getCommand(REVERSE_TOGGLE_COMMAND_ID), "UseSoftTrace"); //$NON-NLS-1$
                       element.setIcon(tracemethodOffImages[0]);
                       }
                   else {
                       element.setIcon(tracemethodDefaultImage);
                   }
               }
           }
           catch(ExecutionException e){
               // Do nothing
           }
       }
   }
}