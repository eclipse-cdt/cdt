/*******************************************************************************
 * Copyright (c) 2007, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.ui.viewmodel.actions;

import org.eclipse.cdt.dsf.ui.viewmodel.IVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @since 1.1
 */
abstract public class AbstractVMProviderActionDelegate
		implements IViewActionDelegate, IDebugContextListener, IActionDelegate2 {

	private IViewPart fView = null;
	private IAction fAction = null;
	private ISelection fDebugContext;

	@Override
	public void init(IViewPart view) {
		fView = view;

		// Get the current selection from the DebugView so we can determine if we want this menu action to be live or not.
		IDebugContextService debugContextService = DebugUITools.getDebugContextManager()
				.getContextService(view.getSite().getWorkbenchWindow());
		debugContextService.addPostDebugContextListener(this);
		fDebugContext = debugContextService.getActiveContext();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (fAction != action) {
			fAction = action;
		}
	}

	@Override
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	@Override
	public void init(IAction action) {
		fAction = action;
	}

	@Override
	public void dispose() {
		DebugUITools.getDebugContextManager().getContextService(getView().getSite().getWorkbenchWindow())
				.removePostDebugContextListener(this);
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		fDebugContext = event.getContext();
	}

	protected IViewPart getView() {
		return fView;
	}

	protected IAction getAction() {
		return fAction;
	}

	protected Object getViewerInput() {
		if (fDebugContext instanceof IStructuredSelection) {
			return ((IStructuredSelection) fDebugContext).getFirstElement();
		}
		return null;
	}

	protected IVMProvider getVMProvider() {
		Object viewerInput = getViewerInput();
		IPresentationContext presentationContext = getPresentationContext();

		if (viewerInput instanceof IAdaptable && presentationContext != null) {
			IVMAdapter adapter = ((IAdaptable) viewerInput).getAdapter(IVMAdapter.class);

			if (adapter != null) {
				return adapter.getVMProvider(presentationContext);
			}
		}

		return null;
	}

	protected IPresentationContext getPresentationContext() {
		if (fView instanceof AbstractDebugView && ((AbstractDebugView) fView).getViewer() instanceof TreeModelViewer) {
			return ((TreeModelViewer) ((AbstractDebugView) fView).getViewer()).getPresentationContext();
		}
		return null;
	}
}
