/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import org.eclipse.cdt.dsf.gdb.actions.IReverseToggleHandler;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.commands.RetargetDebugContextCommand;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.viewers.ISelection;

/**
 * Command handler to toggle reverse debugging mode
 * 
 * @since 2.0
 */
public class ReverseToggleCommandHandler extends RetargetDebugContextCommand {

    @Override
    protected boolean canPerformCommand(Object target, ISelection debugContext) {
		return ((IReverseToggleHandler)target).canToggleReverse(debugContext);
    }

    @Override
    protected Class<?> getAdapterClass() {
        return IReverseToggleHandler.class;
    }

    @Override
    protected void performCommand(Object target, ISelection debugContext) throws ExecutionException {
        ((IReverseToggleHandler)target).toggleReverse(debugContext);
    }
    
    @Override
    public void debugContextChanged(DebugContextEvent event) {
        super.debugContextChanged(event);
        
        // Make sure the toggle state reflects the actual state
        // We must check this, in case we have multiple launches
        // or if we re-launch
        if (fTargetAdapter != null && fToolItem != null) {
        	boolean toggled = ((IReverseToggleHandler)fTargetAdapter).isReverseToggled(event.getContext());
        	fToolItem.setSelection(toggled);
        }
    }
}
