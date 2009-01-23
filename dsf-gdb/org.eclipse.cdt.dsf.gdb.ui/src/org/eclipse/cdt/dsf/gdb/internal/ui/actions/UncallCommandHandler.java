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

import org.eclipse.cdt.dsf.gdb.actions.IUncallHandler;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.commands.RetargetDebugContextCommand;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;

/**
 * Command handler to trigger an uncall operation
 */
public class UncallCommandHandler extends RetargetDebugContextCommand {

    @Override
    protected boolean canPerformCommand(Object target, ISelection debugContext) {
		return ((IUncallHandler)target).canUncall(debugContext);
    }

    @Override
    protected Class<?> getAdapterClass() {
        return IUncallHandler.class;
    }

    @Override
    protected void performCommand(Object target, ISelection debugContext) throws ExecutionException {
        ((IUncallHandler)target).uncall(debugContext);
    }
}
