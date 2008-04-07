/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.disassembly.commands;

import java.util.Map;

import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.internal.ui.disassembly.editor.DisassemblyEditorInput;
import org.eclipse.cdt.debug.internal.ui.disassembly.editor.DisassemblyEditorPresentation;
import org.eclipse.cdt.debug.internal.ui.disassembly.viewer.DisassemblyDocumentProvider;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class DisassemblyDisplayModeHandler extends AbstractHandler implements IElementUpdater {

    private static final String ID_PARAMETER_MODE = "org.eclipse.cdt.debug.command.disassemblyDisplayMode.parameterMode"; //$NON-NLS-1$

    private boolean fShowInstructions = false;
    private boolean fShowSource = false;

    public DisassemblyDisplayModeHandler() {
        super();
        fShowInstructions = CDebugUIPlugin.getDefault().getPreferenceStore().getBoolean( ICDebugPreferenceConstants.PREF_DISASM_SHOW_INSTRUCTIONS );
        fShowSource = CDebugUIPlugin.getDefault().getPreferenceStore().getBoolean( ICDebugPreferenceConstants.PREF_DISASM_SHOW_SOURCE );
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute( ExecutionEvent event ) throws ExecutionException {
        DisassemblyEditorPresentation presentation = getEditorPresentation( event );
        if ( presentation != null ) {
            String param = event.getParameter( ID_PARAMETER_MODE );
            if ( IInternalCDebugUIConstants.DISASM_DISPLAY_MODE_INSTRUCTIONS.equals( param ) ) {
                fShowInstructions = !fShowInstructions;
                presentation.setShowIntstructions( fShowInstructions );
            }
            else if ( IInternalCDebugUIConstants.DISASM_DISPLAY_MODE_SOURCE.equals( param ) ) {
                fShowSource = !fShowSource;
                presentation.setShowSource( fShowSource );
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public void updateElement( UIElement element, Map parameters ) {
        String param = (String)parameters.get( ID_PARAMETER_MODE );
        if ( IInternalCDebugUIConstants.DISASM_DISPLAY_MODE_INSTRUCTIONS.equals( param ) ) {
            element.setChecked( fShowInstructions );
        }
        else if ( IInternalCDebugUIConstants.DISASM_DISPLAY_MODE_SOURCE.equals( param ) ) {
            element.setChecked( fShowSource );
        }
    }

    private DisassemblyEditorPresentation getEditorPresentation( ExecutionEvent event ) throws ExecutionException {
        ISelection s = HandlerUtil.getActiveMenuEditorInputChecked( event );
        if ( s instanceof IStructuredSelection ) {
            Object o = ((IStructuredSelection)s).getFirstElement();
            if ( o instanceof DisassemblyEditorInput ) {
                IDocumentProvider dp = CDebugUIPlugin.getDefault().getDisassemblyEditorManager().getDocumentProvider();
                if ( dp instanceof DisassemblyDocumentProvider ) {
                    return (DisassemblyEditorPresentation)((DisassemblyDocumentProvider)dp).getDocumentPresentation( o );
                }
            }
        }
        return null;
    }
}
