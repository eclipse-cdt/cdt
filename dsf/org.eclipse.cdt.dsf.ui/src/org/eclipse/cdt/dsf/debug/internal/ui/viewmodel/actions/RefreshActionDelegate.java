/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Texas Instruments - Bug 340478 
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.actions;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.AbstractVMProviderActionDelegate;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.VMHandlerUtils;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ICachingVMProvider;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;

/**
 * 
 */
public class RefreshActionDelegate extends AbstractVMProviderActionDelegate {

    @Override
	public void run(IAction action) {
        IVMProvider provider = VMHandlerUtils.getVMProviderForPart(getView());
        if (provider instanceof ICachingVMProvider) {
            ((ICachingVMProvider)provider).refresh();
        }
    }
    
    @Override
    public void init(IViewPart view) {
        super.init(view);
        IVMProvider vp = VMHandlerUtils.getVMProviderForPart(getView());
        getAction().setEnabled(vp instanceof ICachingVMProvider);
    }
    
    @Override
    public void debugContextChanged(DebugContextEvent event) {
        super.debugContextChanged(event);
        IVMProvider vp = VMHandlerUtils.getVMProviderForPart(getView());
        getAction().setEnabled(vp instanceof ICachingVMProvider);
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        IVMProvider vp = VMHandlerUtils.getVMProviderForPart(getView());
        getAction().setEnabled(vp instanceof ICachingVMProvider);
    }
}
