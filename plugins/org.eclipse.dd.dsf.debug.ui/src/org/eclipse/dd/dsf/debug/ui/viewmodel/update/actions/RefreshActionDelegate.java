/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.dd.dsf.debug.ui.viewmodel.update.actions;

import org.eclipse.dd.dsf.ui.viewmodel.update.VMCacheManager;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

@SuppressWarnings("restriction")
public class RefreshActionDelegate implements IViewActionDelegate {

protected IViewPart fView;
	
	public void init(IViewPart view) {
		fView = view;
	}

	public void run(IAction action) {
		VMCacheManager.getVMCacheManager().flush(getContext());
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
	
    private Object getContext()
	{
		return ((TreeModelViewer) ((AbstractDebugView) fView).getViewer()).getPresentationContext();
	}
}
