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

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * org.eclipse.cdt.debug.internal.ui.disassembly.commands.OpenDisassemblyHandler: 
 * //TODO Add description.
 */
public class OpenDisassemblyHandler extends AbstractHandler {

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
	public Object execute( ExecutionEvent event ) throws ExecutionException {
        ISelection s = HandlerUtil.getCurrentSelection( event );
        if ( s instanceof IStructuredSelection ) {
            Object element = ((IStructuredSelection)s).getFirstElement();
            IWorkbenchSite site = HandlerUtil.getActiveSite( event );
            if ( element != null && site != null ) {
                try {
                    CDebugUIPlugin.getDefault().getDisassemblyEditorManager().openEditor( site.getPage(), element );
                }
                catch( DebugException e ) {
                    throw new ExecutionException( "Error openning disassembly.", e );
                }
            }
        }
        return null;
    }
}
