/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.update.actions;

import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.actions.AbstractVMProviderActionDelegate;
import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

public class AtomicUpdatePolicyAction extends AbstractVMProviderActionDelegate {

	private final static String ATOMIC_UPDATE = "ATOMIC_UPDATE";
	
	public void run(IAction action) { 
		DsfDebugUIPlugin.getDefault().getPreferenceStore().setValue(ATOMIC_UPDATE, "true");
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		
		this.getAction().setChecked(DsfDebugUIPlugin.getDefault().getPreferenceStore().getBoolean(ATOMIC_UPDATE));
            
	}

}
