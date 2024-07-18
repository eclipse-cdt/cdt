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

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.debug.core.model.ITargetProperties;
import org.eclipse.cdt.debug.internal.ui.actions.CDTDebugPropertyTester;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RegistryToggleState;

/**
 * Handles the command org.eclipse.cdt.debug.internal.ui.actions.ToggleInstructionStepModeCommand
 * Turns instruction step mode on/off for selected target.
 * @author Raghunandana Murthappa
 */
public class ToggleInstructionStepModeHandler extends AbstractHandler implements IPreferenceChangeListener {

	private IEclipsePreferences preference = InstanceScope.INSTANCE.getNode("org.eclipse.cdt.dsf.ui"); //$NON-NLS-1$
	private Command command = null;

	public ToggleInstructionStepModeHandler() {
		preference.addPreferenceChangeListener(this);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		command = event.getCommand();
		State state = event.getCommand().getState(RegistryToggleState.STATE_ID);
		if (state == null) {
			throw new ExecutionException(NLS.bind(Messages.ToogleCommand_State_Not_found, command.getId()));
		}

		boolean currentState = (Boolean) state.getValue();
		HandlerUtil.toggleCommandState(command);

		ISteppingModeTarget target = CDTDebugPropertyTester.getSteppingModeTarget();
		if (target != null) {
			target.enableInstructionStepping(!currentState);
		}

		return IStatus.OK;
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		String key = event.getKey();
		if (!key.equals(ITargetProperties.PREF_INSTRUCTION_STEPPING_MODE)) {
			return;
		}
		try {
			if (command != null) {
				State state = command.getState(RegistryToggleState.STATE_ID);
				if (state == null) {
					throw new ExecutionException(NLS.bind(Messages.ToogleCommand_State_Not_found, command.getId()));
				}
				boolean prefValue = Boolean.valueOf((String) event.getNewValue());
				boolean cmdCurState = (Boolean) state.getValue();
				if (cmdCurState != prefValue) {
					HandlerUtil.toggleCommandState(command);
				}

			}
		} catch (ExecutionException e) {
			CDebugCorePlugin.log(e);
		}
	}

	@Override
	public void dispose() {
		preference.removePreferenceChangeListener(this);
		super.dispose();
	}

}
