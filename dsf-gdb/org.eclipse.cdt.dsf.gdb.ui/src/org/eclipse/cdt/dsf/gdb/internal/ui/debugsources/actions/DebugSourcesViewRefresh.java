/*******************************************************************************
 * Copyright (c) 2018, 2019 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Jonah Graham- Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.actions;

import org.eclipse.cdt.dsf.gdb.internal.ui.debugsources.DebugSourcesView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class DebugSourcesViewRefresh implements IViewActionDelegate, IDebugContextListener, IActionDelegate2 {

	private IViewPart view;
	private IAction action;

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.action = action;
		updateEnablement();
	}

	@Override
	public void init(IViewPart view) {
		this.view = view;
		if (view != null) {
			IDebugContextService debugContextService = DebugUITools.getDebugContextManager()
					.getContextService(view.getSite().getWorkbenchWindow());
			debugContextService.addPostDebugContextListener(this);
		}
		updateEnablement();
	}

	private void updateEnablement() {
		if (view instanceof DebugSourcesView) {
			DebugSourcesView debugSourcesView = (DebugSourcesView) view;
			action.setEnabled(debugSourcesView.canRefresh());
		} else {
			action.setEnabled(false);
		}
	}

	@Override
	public void init(IAction action) {
		this.action = action;
		updateEnablement();
	}

	@Override
	public void dispose() {
		if (view != null) {
			DebugUITools.getDebugContextManager().getContextService(view.getSite().getWorkbenchWindow())
					.removePostDebugContextListener(this);
			view = null;
		}
		updateEnablement();
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		updateEnablement();
	}

	@Override
	public void run(IAction action) {
		throw new UnsupportedOperationException("call runWithEvent instead"); //$NON-NLS-1$
	}

	@Override
	public void runWithEvent(IAction action, Event event) {
		if (view instanceof DebugSourcesView) {
			DebugSourcesView debugSourcesView = (DebugSourcesView) view;
			debugSourcesView.refresh();
		}
	}

}
