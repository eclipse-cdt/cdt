/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.cdt.debug.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.breakpoints.IToggleBreakpointsTargetCExtension;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyMessages;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Ruler action to add breakpoint with a dialog properties.
 */
public class AddBreakpointRulerAction extends AbstractDisassemblyBreakpointRulerAction {
    
    
	protected AddBreakpointRulerAction(IDisassemblyPart disassemblyPart, IVerticalRulerInfo rulerInfo) {
		super(disassemblyPart, rulerInfo);
		setText(DisassemblyMessages.Disassembly_action_AddBreakpoint_label);
	}
	
	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.AbstractDisassemblyAction#run()
	 */
	@Override
	public void run() {
	    IWorkbenchPart part = getDisassemblyPart();
	    ISelection selection = getSelection();
	    IToggleBreakpointsTargetCExtension toggleTarget = getToggleTarget(selection);
	    if (toggleTarget != null) {
	        try {
    	        if (toggleTarget.canCreateLineBreakpointsInteractive(part, selection)) {
    	            toggleTarget.createLineBreakpointsInteractive(part, selection);
    	        }
	        } catch (CoreException e) {
	            reportException(e);
	        }
	    }
	}

	@Override
	public void update() {
	    IDisassemblyPart part = getDisassemblyPart();
	    if (part != null && part.isConnected()) {
	        ISelection selection = getSelection();
	        IToggleBreakpointsTargetCExtension toggleTarget = getToggleTarget(selection);
	        if (toggleTarget != null) {
	            setEnabled( toggleTarget.canCreateLineBreakpointsInteractive(part, selection) );
	            return;
	        }
	    }
	    setEnabled(false);
	}
	
	private IToggleBreakpointsTargetCExtension getToggleTarget(ISelection selection) {
	    IToggleBreakpointsTargetManager toggleMgr = DebugUITools.getToggleBreakpointsTargetManager();
	    IToggleBreakpointsTarget toggleTarget = toggleMgr.getToggleBreakpointsTarget(getDisassemblyPart(), selection);
	    if (toggleTarget instanceof IToggleBreakpointsTargetCExtension) {
	        return (IToggleBreakpointsTargetCExtension)toggleTarget;
	    }
	    return null;
	}

	   /**
     * Report an error to the user.
     * 
     * @param e underlying exception
     */
    private void reportException(Exception e) {
        IStatus status= new Status(IStatus.ERROR, CDebugUIPlugin.PLUGIN_ID, "Error creating breakpoint: ", e); //$NON-NLS-1$
        ErrorDialog.openError(
            getDisassemblyPart().getSite().getShell(), 
            ActionMessages.getString("DisassemblyMessages.Disassembly_action_AddBreakpoint_errorTitle"),  //$NON-NLS-1$
            ActionMessages.getString("DisassemblyMessages.Disassembly_action_AddBreakpoint_errorMessage"), //$NON-NLS-1$
            status);
        CDebugUIPlugin.log(status); //
    }

	/**
     * Determines the text selection for the breakpoint action.  If clicking on the ruler inside
     * the highlighted text, return the text selection for the highlighted text.  Otherwise, 
     * return a text selection representing the start of the line.
     * 
     * @return  An ISelection as described.
     * @throws BadLocationException If underlying operations throw.
     */
    private ISelection getSelection() {
        IDocument document = getDocument();
        if (document != null) {
            int line = getRulerInfo().getLineOfLastMouseButtonActivity();
            
            try {
                IRegion region = getDocument().getLineInformation(line);
                ITextSelection textSelection = new TextSelection(document, region.getOffset(), 0);
                ISelectionProvider provider = getDisassemblyPart().getSite().getSelectionProvider();
                if (provider != null){
                    ISelection selection = provider.getSelection();
                    if (selection instanceof ITextSelection
                            && ((ITextSelection) selection).getStartLine() <= line
                            && ((ITextSelection) selection).getEndLine() >= line) {
                        textSelection = (ITextSelection) selection;
                    } 
                }
                return textSelection;
            } catch (BadLocationException e) {
            }
        }
        return StructuredSelection.EMPTY;
    }

}
