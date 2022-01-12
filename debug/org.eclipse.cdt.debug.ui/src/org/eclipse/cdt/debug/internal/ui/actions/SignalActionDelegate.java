/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * The delegate of the "Resume With Signal" action.
 */
public class SignalActionDelegate extends ActionDelegate implements IObjectActionDelegate {

	private ICSignal fSignal = null;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	@Override
	public void run(IAction action) {
		if (getSignal() != null) {
			final MultiStatus ms = new MultiStatus(CDebugUIPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED,
					ActionMessages.getString("SignalActionDelegate.0"), null); //$NON-NLS-1$
			BusyIndicator.showWhile(Display.getCurrent(), () -> {
				try {
					doAction(getSignal());
				} catch (DebugException e) {
					ms.merge(e.getStatus());
				}
			});
			if (!ms.isOK()) {
				IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
				if (window != null) {
					CDebugUIPlugin.errorDialog(ActionMessages.getString("SignalActionDelegate.1"), ms); //$NON-NLS-1$
				} else {
					CDebugUIPlugin.log(ms);
				}
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof ICSignal) {
				boolean enabled = enablesFor((ICSignal) element);
				action.setEnabled(enabled);
				if (enabled) {
					setSignal((ICSignal) element);
					return;
				}
			}
		}
		action.setEnabled(false);
		setSignal(null);
	}

	protected void doAction(ICSignal signal) throws DebugException {
		signal.signal();
	}

	private boolean enablesFor(ICSignal signal) {
		return (signal != null && signal.getDebugTarget().isSuspended());
	}

	private void setSignal(ICSignal signal) {
		fSignal = signal;
	}

	protected ICSignal getSignal() {
		return fSignal;
	}
}
