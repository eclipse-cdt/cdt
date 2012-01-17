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
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class DisassemblyDisplayModeHandler extends AbstractHandler implements IElementUpdater {

    private static final String ID_PARAMETER_MODE = "org.eclipse.cdt.debug.command.disassemblyDisplayMode.parameterMode"; //$NON-NLS-1$

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
	public Object execute( ExecutionEvent event ) throws ExecutionException {
        DisassemblyEditorPresentation presentation = getEditorPresentation( event );
        if ( presentation != null ) {
            String param = event.getParameter( ID_PARAMETER_MODE );
            if ( IInternalCDebugUIConstants.DISASM_DISPLAY_MODE_INSTRUCTIONS.equals( param ) ) {
                presentation.setShowIntstructions( !presentation.showIntstructions() );
            }
            else if ( IInternalCDebugUIConstants.DISASM_DISPLAY_MODE_SOURCE.equals( param ) ) {
                presentation.setShowSource( !presentation.showSource() );
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
     */
    @Override
	@SuppressWarnings("unchecked")
    public void updateElement( UIElement element, Map parameters ) {
        IWorkbenchPartSite site = (IWorkbenchPartSite)element.getServiceLocator().getService( IWorkbenchPartSite.class );
        if ( site != null ) {
            IWorkbenchPart part = site.getPart();
            if ( part instanceof ITextEditor ) {
                IEditorInput input = ((ITextEditor)part).getEditorInput();
                if ( input instanceof DisassemblyEditorInput ) {
                    IDocumentProvider dp = ((ITextEditor)part).getDocumentProvider();
                    if ( dp instanceof DisassemblyDocumentProvider ) {
                        IDocumentPresentation p = ((DisassemblyDocumentProvider)dp).getDocumentPresentation( input );
                        if ( p instanceof DisassemblyEditorPresentation ) {
                            DisassemblyEditorPresentation presentation = (DisassemblyEditorPresentation)p;
                            String param = (String)parameters.get( ID_PARAMETER_MODE );
                            if ( IInternalCDebugUIConstants.DISASM_DISPLAY_MODE_INSTRUCTIONS.equals( param ) ) {
                                element.setChecked( presentation.showIntstructions() );
                            }
                            else if ( IInternalCDebugUIConstants.DISASM_DISPLAY_MODE_SOURCE.equals( param ) ) {
                                element.setChecked( presentation.showSource() );
                            }
                        }
                    }
                }
            }
        }
    }

    private DisassemblyEditorPresentation getEditorPresentation( ExecutionEvent event ) throws ExecutionException {
        ISelection s = HandlerUtil.getActiveMenuEditorInputChecked( event );
        if ( s instanceof IStructuredSelection ) {
            Object o = ((IStructuredSelection)s).getFirstElement();
            if ( o instanceof DisassemblyEditorInput ) {
            	DisassemblyDocumentProvider dp = CDebugUIPlugin.getDefault().getDisassemblyEditorManager().getDocumentProvider();
                if ( dp != null ) {
                    return (DisassemblyEditorPresentation)dp.getDocumentPresentation( o );
                }
            }
        }
        return null;
    }
}
