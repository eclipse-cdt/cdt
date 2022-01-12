/*******************************************************************************
 * Copyright (c) 2011, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.internal.handler;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.terminal.view.core.TerminalContextPropertiesProviderFactory;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalContextPropertiesProvider;
import org.eclipse.tm.terminal.view.core.interfaces.constants.IContextPropertiesConstants;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.interfaces.tracing.ITraceIds;
import org.eclipse.tm.terminal.view.ui.internal.dialogs.LaunchTerminalSettingsDialog;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Launch terminal command handler implementation.
 */
public class LaunchTerminalCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String commandId = event.getCommand().getId();
		// "org.eclipse.tm.terminal.view.ui.command.launchToolbar"
		// "org.eclipse.tm.terminal.view.ui.command.launch"

		long start = System.currentTimeMillis();

		if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER)) {
			DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			String date = format.format(new Date(start));

			UIPlugin.getTraceHandler().trace("Started at " + date + " (" + start + ")", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER, LaunchTerminalCommandHandler.this);
		}

		// Get the active shell
		Shell shell = HandlerUtil.getActiveShell(event);
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (commandId.equals("org.eclipse.tm.terminal.view.ui.command.launchToolbar")) { //$NON-NLS-1$
			if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER)) {
				UIPlugin.getTraceHandler().trace("(a) Attempt to open launch terminal settings dialog after " //$NON-NLS-1$
						+ (System.currentTimeMillis() - start) + " ms.", //$NON-NLS-1$
						ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER, LaunchTerminalCommandHandler.this);
			}

			LaunchTerminalSettingsDialog dialog = new LaunchTerminalSettingsDialog(shell, start);

			if (isValidSelection(selection)) {
				dialog.setSelection(selection);
			}
			if (dialog.open() == Window.OK) {
				// Get the terminal settings from the dialog
				Map<String, Object> properties = dialog.getSettings();
				if (properties != null) {
					String delegateId = (String) properties.get(ITerminalsConnectorConstants.PROP_DELEGATE_ID);
					Assert.isNotNull(delegateId);
					ILauncherDelegate delegate = LauncherDelegateManager.getInstance().getLauncherDelegate(delegateId,
							false);
					Assert.isNotNull(delegateId);
					delegate.execute(properties, null);
				}
			}
		} else {
			if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER)) {
				UIPlugin.getTraceHandler().trace(
						"Getting applicable launcher delegates after " + (System.currentTimeMillis() - start) + " ms.", //$NON-NLS-1$ //$NON-NLS-2$
						ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER, LaunchTerminalCommandHandler.this);
			}

			// Check if the dialog needs to be shown at all
			ILauncherDelegate[] delegates = LauncherDelegateManager.getInstance()
					.getApplicableLauncherDelegates(selection);

			if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER)) {
				UIPlugin.getTraceHandler().trace(
						"Got applicable launcher delegates after " + (System.currentTimeMillis() - start) + " ms.", //$NON-NLS-1$ //$NON-NLS-2$
						ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER, LaunchTerminalCommandHandler.this);
			}

			if (delegates.length > 1 || (delegates.length == 1 && delegates[0].needsUserConfiguration())) {
				if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER)) {
					UIPlugin.getTraceHandler().trace("(b) Attempt to open launch terminal settings dialog after " //$NON-NLS-1$
							+ (System.currentTimeMillis() - start) + " ms.", //$NON-NLS-1$
							ITraceIds.TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER, LaunchTerminalCommandHandler.this);
				}

				// Create the launch terminal settings dialog
				LaunchTerminalSettingsDialog dialog = new LaunchTerminalSettingsDialog(shell, start);
				if (isValidSelection(selection)) {
					dialog.setSelection(selection);
				}
				if (dialog.open() == Window.OK) {
					// Get the terminal settings from the dialog
					Map<String, Object> properties = dialog.getSettings();
					if (properties != null) {
						String delegateId = (String) properties.get(ITerminalsConnectorConstants.PROP_DELEGATE_ID);
						Assert.isNotNull(delegateId);
						ILauncherDelegate delegate = LauncherDelegateManager.getInstance()
								.getLauncherDelegate(delegateId, false);
						Assert.isNotNull(delegateId);
						delegate.execute(properties, null);
					}
				}
			} else if (delegates.length == 1) {
				ILauncherDelegate delegate = delegates[0];
				Map<String, Object> properties = new HashMap<>();

				// Store the id of the selected delegate
				properties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID, delegate.getId());
				// Store the selection
				properties.put(ITerminalsConnectorConstants.PROP_SELECTION, selection);

				// Execute
				delegate.execute(properties, null);
			}
		}

		return null;
	}

	private boolean isValidSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			ITerminalContextPropertiesProvider provider = TerminalContextPropertiesProviderFactory.getProvider(element);
			if (provider != null) {
				Map<String, String> props = provider.getTargetAddress(element);
				if (props != null && props.containsKey(IContextPropertiesConstants.PROP_ADDRESS)) {
					return true;
				}
			}
		}

		return false;
	}
}
