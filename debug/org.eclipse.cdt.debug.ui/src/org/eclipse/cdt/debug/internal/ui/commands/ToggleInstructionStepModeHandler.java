/*******************************************************************************
 * Copyright (c) 2024 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.commands;

import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.debug.core.model.ITargetProperties;
import org.eclipse.cdt.debug.internal.ui.actions.CDTDebugPropertyTester;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RegistryToggleState;

/**
 * Handles the command org.eclipse.cdt.debug.internal.ui.actions.ToggleInstructionStepModeCommand
 * Turns instruction step mode on/off for selected target.
 * @author Raghunandana Murthappa
 */
public class ToggleInstructionStepModeHandler extends AbstractHandler
		implements IPropertyChangeListener, IDebugContextListener {

	private static final String TISM_COMMAND_ID = "org.eclipse.cdt.debug.internal.ui.actions.ToggleInstructionStepModeCommand"; //$NON-NLS-1$

	private Command fCommand = null;

	private ISteppingModeTarget fTarget = null;

	private IWorkbenchWindow fWorkbenchWindow = null;

	public ToggleInstructionStepModeHandler() {
		fWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		DebugUITools.getDebugContextManager().getContextService(fWorkbenchWindow).addDebugContextListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (fCommand != null && event.getNewValue() instanceof Boolean) {
			boolean prefState = ((Boolean) event.getNewValue()).booleanValue();
			try {
				State state = fCommand.getState(RegistryToggleState.STATE_ID);
				boolean currentState = (Boolean) state.getValue();
				if (currentState != prefState) {
					HandlerUtil.toggleCommandState(fCommand);
				}
			} catch (ExecutionException e) {
				CDebugUIPlugin.log(e);
			}
		}
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		if (commandService != null) {
			fCommand = commandService.getCommand(TISM_COMMAND_ID);

		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		State state = fCommand.getState(RegistryToggleState.STATE_ID);
		if (state == null) {
			throw new ExecutionException(NLS.bind(Messages.ToogleCommand_State_Not_found, fCommand.getId()));
		}

		boolean currentState = (Boolean) state.getValue();
		HandlerUtil.toggleCommandState(fCommand);

		ISteppingModeTarget target = getTarget();
		if (target != null) {
			target.enableInstructionStepping(!currentState);
		}

		return IStatus.OK;
	}

	private boolean isTerminated(ISteppingModeTarget target) {
		return ((target instanceof ITerminate && ((ITerminate) target).isTerminated())
				|| (target instanceof IDisconnect && ((IDisconnect) target).isDisconnected()));
	}

	@Override
	public void dispose() {
		if (fWorkbenchWindow != null) {
			DebugUITools.getDebugContextManager().getContextService(fWorkbenchWindow).removeDebugContextListener(this);
		}
		ISteppingModeTarget target = getTarget();
		if (target != null && target instanceof ITargetProperties) {
			((ITargetProperties) target).removePropertyChangeListener(this);
		}
		setTarget(null);
		super.dispose();
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		ISelection selection = event.getContext();
		ISteppingModeTarget newTarget = null;
		if (selection instanceof IStructuredSelection) {
			newTarget = CDTDebugPropertyTester
					.getTargetFromSelection(((IStructuredSelection) selection).getFirstElement());
		}

		if (newTarget == null) {
			return;
		}

		ISteppingModeTarget oldTarget = getTarget();
		if (newTarget.equals(oldTarget)) {
			return;
		}

		if (oldTarget != null) {
			if (oldTarget instanceof ITargetProperties) {
				((ITargetProperties) oldTarget).removePropertyChangeListener(this);
			}
		}

		setTarget(newTarget);
		if (newTarget instanceof ITargetProperties) {
			((ITargetProperties) newTarget).addPropertyChangeListener(this);
		}

		try {
			boolean prefState = !isTerminated(newTarget) && newTarget.isInstructionSteppingEnabled();
			if (fCommand != null) {
				State state = fCommand.getState(RegistryToggleState.STATE_ID);
				boolean currentState = (Boolean) state.getValue();
				if (currentState != prefState) {
					HandlerUtil.toggleCommandState(fCommand);
				}
			}
		} catch (ExecutionException e) {
			CDebugUIPlugin.log(e);
		}
	}

	private ISteppingModeTarget getTarget() {
		return fTarget;
	}

	private void setTarget(ISteppingModeTarget target) {
		fTarget = target;
	}
}
