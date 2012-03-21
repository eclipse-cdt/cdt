/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpoints;

import java.util.Iterator;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.internal.ui.breakpoints.CBreakpointContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Action for opening a Property Pages Dialog on the C breakpoint object 
 * in the currently selected element.
 * <p>
 * Generally speaking, this action is useful in pop-up menus because it allows
 * the user to browse and change properties of selected elements. When
 * performed, the action will bring up a Property Pages Dialog containing
 * property pages registered with the workbench for elements of the selected
 * type.
 * </p>
 * <p>
 * Although the action is capable of calculating if there are any applicable
 * pages for the current selection, this calculation is costly because it
 * require searching the workbench registry. Where performance is critical, the
 * action can simply be added to the pop-up menu. In the event of no applicable
 * pages, the action will just open an appropriate message dialog.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 * @since 7.2
 */
public class CBreakpointPropertyDialogAction extends SelectionProviderAction {

    /**
     * Provides the shell in which to open the property dialog.
     */
    private IShellProvider fShellProvider;
    
    private IDebugContextProvider fDebugContextProvider;
    
    /**
     * The id of the page to open up on.
     */
    private String fInitialPageId = "org.eclipse.cdt.debug.ui.propertypages.breakpoint.common"; //$NON-NLS-1$
    
    public CBreakpointPropertyDialogAction(IShellProvider shell, ISelectionProvider selectionProvider, IDebugContextProvider debugContextProvider) {
        super(selectionProvider, WorkbenchMessages.PropertyDialog_text); 
        Assert.isNotNull(shell);
        fDebugContextProvider = debugContextProvider;
        fShellProvider = shell;
        setToolTipText(WorkbenchMessages.PropertyDialog_toolTip); 
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
                IWorkbenchHelpContextIds.PROPERTY_DIALOG_ACTION);
    }
    
    protected ISelection getDebugContext() {
        return fDebugContextProvider.getActiveContext();
    }
    
    /**
     * Returns whether this action is actually applicable to the current
     * selection. If this action is disabled, it will return <code>false</code>
     * without further calculation. If it is enabled, it will check with the
     * workbench's property page manager to see if there are any property pages
     * registered for the selected element's type.
     * <p>
     * This method is generally too expensive to use when updating the enabled
     * state of the action on each selection change.
     * </p>
     * 
     * @return <code>true</code> if the selection is not empty and there are
     *         property pages for the selected element, and <code>false</code>
     *         otherwise
     */
    public boolean isCBreakpointSelection() {
        if (!isEnabled()) {
            return false;
        }
        return isApplicableForSelection(getStructuredSelection(), getDebugContext());
    }

    /**
     * Returns whether this action is applicable to the current selection. This
     * checks that the selection is not empty, and checks with the workbench's
     * property page manager to see if there are any property pages registered
     * for the selected element's type.
     * <p>
     * This method is generally too expensive to use when updating the enabled
     * state of the action on each selection change.
     * </p>
     * 
     * @param selection
     *            The selection to test
     * @return <code>true</code> if the selection is of not empty and there are
     *         property pages for the selected element, and <code>false</code>
     *         otherwise
     */
    public boolean isApplicableForSelection(IStructuredSelection selection, ISelection debugContext) {
        return isCBreakpointSelection(selection);
    }

    /**
     * Returns whether the given selection contains only elements of type ICBreakpoint
     * @param selection
     * @return
     */
    private boolean isCBreakpointSelection(IStructuredSelection selection) {
        if (selection.isEmpty()) return false;
        
        for (Iterator<?> itr = selection.iterator(); itr.hasNext();) { 
            if ( !(itr.next() instanceof ICBreakpoint) ) {
                return false;
            }
        }
        return true;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        CBreakpointContext bpContext = getCBreakpointContext();
        if (bpContext != null) {
            PreferenceDialog dialog = createDialog(bpContext);
            
            if (dialog != null) {
                dialog.open();
            }
        }
    }
    
    private CBreakpointContext getCBreakpointContext() {
        IStructuredSelection ss = getStructuredSelection();
        if (ss.size() >= 1 && ss.getFirstElement() instanceof ICBreakpoint) {
            return new CBreakpointContext((ICBreakpoint)ss.getFirstElement(), fDebugContextProvider.getActiveContext());
        }
        return null;
    }
    
    /**
     * Create the dialog for the receiver. If no pages are found, an informative
     * message dialog is presented instead.
     * 
     * @return PreferenceDialog or <code>null</code> if no applicable pages
     *         are found.
     */
    protected PreferenceDialog createDialog(CBreakpointContext bpContext) {
        IStructuredSelection ss = getStructuredSelection();
        if (ss.isEmpty())
            return null;
        
        return PreferencesUtil.createPropertyDialogOn(fShellProvider.getShell(), bpContext, fInitialPageId, null, null);
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(!selection.isEmpty());
    }
    
}
