/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Pawel Piech (WindRiver) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=228063
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.debug.core.model.ITargetProperties;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Turns instruction step mode on/off for selected target.
 */
public class ToggleInstructionStepModeActionDelegate extends ActionDelegate
		implements IViewActionDelegate, IWorkbenchWindowActionDelegate, IPropertyChangeListener, IDebugContextListener {

	private ISteppingModeTarget fTarget = null;

	private IAction fAction = null;

	private IWorkbenchWindow fWindow = null;

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		IAction action = getAction();
		if (action != null) {
			if (event.getNewValue() instanceof Boolean) {
				boolean value = ((Boolean) event.getNewValue()).booleanValue();
				if (value != action.isChecked())
					action.setChecked(value);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		fWindow = view.getSite().getWorkbenchWindow();
		DebugUITools.getDebugContextManager().getContextService(fWindow).addDebugContextListener(this);
	}

	@Override
	public void init(IWorkbenchWindow window) {
		fWindow = window;
		DebugUITools.getDebugContextManager().getContextService(fWindow).addDebugContextListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	@Override
	public void dispose() {
		DebugUITools.getDebugContextManager().getContextService(fWindow).removeDebugContextListener(this);
		ISteppingModeTarget target = getTarget();
		if (target != null && target instanceof ITargetProperties) {
			((ITargetProperties) target).removePropertyChangeListener(this);
		}
		setTarget(null);
		setAction(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init(IAction action) {
		setAction(action);
		action.setChecked(false);
		action.setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		boolean enabled = getAction().isChecked();
		ISteppingModeTarget target = getTarget();
		if (target != null) {
			target.enableInstructionStepping(enabled);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		if (fAction == null)
			return;

		ISelection selection = event.getContext();
		ISteppingModeTarget newTarget = null;
		if (selection instanceof IStructuredSelection) {
			newTarget = getTargetFromSelection(((IStructuredSelection) selection).getFirstElement());
		}
		ISteppingModeTarget oldTarget = getTarget();
		if (oldTarget != null && !oldTarget.equals(newTarget)) {
			if (oldTarget instanceof ITargetProperties) {
				((ITargetProperties) oldTarget).removePropertyChangeListener(this);
			}
			setTarget(null);
			fAction.setChecked(false);
		}
		if (newTarget != null && !isTerminated(newTarget)) {
			setTarget(newTarget);
			if (newTarget instanceof ITargetProperties) {
				((ITargetProperties) newTarget).addPropertyChangeListener(this);
			}
			fAction.setChecked(newTarget.isInstructionSteppingEnabled());
		}
		fAction.setEnabled(newTarget != null && newTarget.supportsInstructionStepping() && !isTerminated(newTarget));
	}

	private boolean isTerminated(ISteppingModeTarget target) {
		return ((target instanceof ITerminate && ((ITerminate) target).isTerminated())
				|| (target instanceof IDisconnect && ((IDisconnect) target).isDisconnected()));
	}

	private ISteppingModeTarget getTarget() {
		return this.fTarget;
	}

	private void setTarget(ISteppingModeTarget target) {
		this.fTarget = target;
	}

	private IAction getAction() {
		return this.fAction;
	}

	private void setAction(IAction action) {
		this.fAction = action;
	}

	private ISteppingModeTarget getTargetFromSelection(Object element) {
		ISteppingModeTarget target = null;
		if (element instanceof IDebugElement) {
			IDebugTarget debugTarget = ((IDebugElement) element).getDebugTarget();
			if (debugTarget instanceof ISteppingModeTarget) {
				target = (ISteppingModeTarget) debugTarget;
			}
		}
		if (target == null) {
			if (element instanceof IAdaptable) {
				target = ((IAdaptable) element).getAdapter(ISteppingModeTarget.class);
			}
		}
		return target;
	}

}
