/*******************************************************************************
 * Copyright (c) 2010 Verigy and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.AbstractVMProviderActionDelegate;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.FetchMoreChildrenEvent;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.GdbExpressionVMProvider;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.GdbVariableVMNode;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.GdbVariableVMNode.IncompleteChildrenVMC;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.GdbVariableVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @since 3.0
 */
public class FetchMoreChildrenAction extends AbstractVMProviderActionDelegate
		implements IObjectActionDelegate {

	private ISelection selection;
	
    @Override
	public void run(IAction action) {
		IncompleteChildrenVMC incompleteChildrenVmc = getIncompleteChildrenVMC();

		if (incompleteChildrenVmc != null) {
			if (selection instanceof ITreeSelection) {
				ITreeSelection treeSelection = (ITreeSelection) selection;
				TreePath path = treeSelection.getPaths()[0];

				IVMNode node = incompleteChildrenVmc.getVMNode();

				IExpressionDMContext exprCtx = incompleteChildrenVmc.getParentDMContext();
				((GdbVariableVMNode) node).incrementChildCountLimit(exprCtx);
				final FetchMoreChildrenEvent fetchMoreChildrenEvent = new FetchMoreChildrenEvent(exprCtx, path);
				final AbstractVMProvider vmProvider = (AbstractVMProvider) getVMProvider();
				vmProvider.getExecutor().execute(new DsfRunnable() {
	                @Override
					public void run() {
						vmProvider.handleEvent(fetchMoreChildrenEvent);
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
		this.selection = selection;
		updateEnablement();
	}

	private void updateEnablement() {
		boolean enabled = false;
		if ((getVMProvider() instanceof GdbExpressionVMProvider)
				|| (getVMProvider() instanceof GdbVariableVMProvider)) {
			enabled = (getIncompleteChildrenVMC() != null);
		}
		getAction().setEnabled(enabled);
	}

    @Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if (targetPart instanceof IViewPart) {
			init((IViewPart) targetPart);
		}
	}
	
	private IncompleteChildrenVMC getIncompleteChildrenVMC() {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			
			if (ss.size() == 1) {
				// Only single selection is supported.
				Object selectedObject = ss.getFirstElement();
				if (selectedObject instanceof IncompleteChildrenVMC) {
					return (IncompleteChildrenVMC) selectedObject;
				}
			}
		}
		
		return null;
	}
}
