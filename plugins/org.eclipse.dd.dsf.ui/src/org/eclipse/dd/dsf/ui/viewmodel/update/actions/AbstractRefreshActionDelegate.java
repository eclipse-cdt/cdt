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

package org.eclipse.dd.dsf.ui.viewmodel.update.actions;

import org.eclipse.dd.dsf.ui.viewmodel.update.VMCache;
import org.eclipse.dd.dsf.ui.viewmodel.update.VMCacheManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public abstract class AbstractRefreshActionDelegate implements IViewActionDelegate {

	protected IViewPart fView;
	
	public void init(IViewPart view) {
		fView = view;
	}

	public void run(IAction action) {
		VMCacheManager.getVMCacheManager().registerCache(getContext(), createCache());
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	protected abstract Object getContext();
	
	protected abstract VMCache createCache();

}
