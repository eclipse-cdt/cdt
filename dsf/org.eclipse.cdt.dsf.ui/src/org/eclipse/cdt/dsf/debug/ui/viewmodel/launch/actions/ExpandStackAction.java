/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.actions;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.AbstractVMProviderActionDelegate;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.AbstractLaunchVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ExpandStackEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.StackFramesVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.StackFramesVMNode.IncompleteStackVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Increment the (temporary) stack limit for the selected stack.
 */
public class ExpandStackAction extends AbstractVMProviderActionDelegate implements IObjectActionDelegate {

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		Object element = getViewerInput();
		if (element instanceof IncompleteStackVMContext) {
		    IncompleteStackVMContext incStackVmc = ((IncompleteStackVMContext) element); 
			IVMNode node = incStackVmc.getVMNode();
			if (incStackVmc.getVMNode() instanceof StackFramesVMNode) {
				final IExecutionDMContext exeCtx= incStackVmc.getExecutionDMContext();
				((StackFramesVMNode) node).incrementStackFrameLimit(exeCtx);
				final ExpandStackEvent event = new ExpandStackEvent(exeCtx);
				final AbstractLaunchVMProvider vmProvider = (AbstractLaunchVMProvider) getVMProvider();
				vmProvider.getExecutor().execute(new DsfRunnable() {
				    @Override
					public void run() {
				        vmProvider.handleEvent(event);
				    }
				});
			}
		}
	}

    @Override
    public void init(IViewPart view) {
        super.init(view);
		updateEnablement();
    }
    
    @Override
    public void debugContextChanged(DebugContextEvent event) {
        super.debugContextChanged(event);
		updateEnablement();
    }

    @Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		updateEnablement();
	}

	private void updateEnablement() {
		boolean enabled = false;
		if (getVMProvider() instanceof AbstractLaunchVMProvider) {
			Object element = getViewerInput();
			enabled = element instanceof IncompleteStackVMContext;
		}
		getAction().setEnabled(enabled);
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if (targetPart instanceof IViewPart) {
			init((IViewPart) targetPart);
		}
	}
}
