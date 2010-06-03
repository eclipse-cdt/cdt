/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.actions;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.IRefreshAllTarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;

/**
 * 
 */
public class RefreshAllRetargetAction extends RetargetDebugContextAction {

    @Override
    protected boolean canPerformAction(Object target, ISelection selection) {
        return true;
    }

    @Override
    protected Class<?> getAdapterClass() {
        return IRefreshAllTarget.class;
    }

    @Override
    protected void performAction(Object target, ISelection debugContext) throws CoreException {
        ((IRefreshAllTarget)target).refresh(debugContext);
    }

}
