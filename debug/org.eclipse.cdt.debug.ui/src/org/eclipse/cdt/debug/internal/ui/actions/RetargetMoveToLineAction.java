/*******************************************************************************
 * Copyright (c) 2008, 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Freescale Semiconductor - Initial API and implementation
 * Ericsson                - Updated with the latest platform changes of RetargetAction (302273)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Global retargettable move to line action.
 */
public class RetargetMoveToLineAction extends RetargetAction {
	
	private DebugContextListener fContextListener = new DebugContextListener();
	private ISuspendResume fTargetElement = null;
	
	class DebugContextListener implements IDebugContextListener {

		protected void contextActivated(ISelection selection) {
			fTargetElement = null;
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (ss.size() == 1) {
                    fTargetElement = (ISuspendResume)
                        DebugPlugin.getAdapter(ss.getFirstElement(), ISuspendResume.class);
				}
			}
			IAction action = getAction();
			if (action != null) {
				action.setEnabled(fTargetElement != null && hasTargetAdapter());
			}
		}

		@Override
		public void debugContextChanged(DebugContextEvent event) {
			contextActivated(event.getContext());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
		DebugUITools.getDebugContextManager().getContextService(fWindow).removeDebugContextListener(fContextListener);
		super.dispose();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {
		super.init(window);
		IDebugContextService service = DebugUITools.getDebugContextManager().getContextService(window);
		service.addDebugContextListener(fContextListener);
		ISelection activeContext = service.getActiveContext();
		fContextListener.contextActivated(activeContext);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetAction#canPerformAction(java.lang.Object, org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected boolean canPerformAction(Object target, ISelection selection,	IWorkbenchPart part) {
		return fTargetElement != null &&
			((IMoveToLineTarget)target).canMoveToLine(part, selection, fTargetElement);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetAction#getAdapterClass()
	 */
	@Override
	protected Class getAdapterClass() {
		return IMoveToLineTarget.class;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetAction#performAction(java.lang.Object, org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	protected void performAction(Object target, ISelection selection, IWorkbenchPart part) throws CoreException {
		((IMoveToLineTarget)target).moveToLine(part, selection, fTargetElement);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetAction#getOperationUnavailableMessage()
	 */
	@Override
	protected String getOperationUnavailableMessage() {
		return ActionMessages.getString("RetargetMoveToLineAction.0"); //$NON-NLS-1$
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (fTargetElement == null) {
			action.setEnabled(false);
		} else {
			super.selectionChanged(action, selection);
		}
	}
}
