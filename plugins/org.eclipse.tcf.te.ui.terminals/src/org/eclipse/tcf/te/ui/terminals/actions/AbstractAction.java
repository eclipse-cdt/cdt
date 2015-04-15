/*******************************************************************************
 * Copyright (c) 2011, 2013 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.tcf.te.ui.terminals.activator.UIPlugin;
import org.eclipse.tcf.te.ui.terminals.nls.Messages;
import org.eclipse.tcf.te.ui.terminals.tabs.TabFolderManager;
import org.eclipse.tcf.te.ui.terminals.tabs.TabFolderToolbarHandler;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.actions.AbstractTerminalAction;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Abstract terminal action wrapper implementation.
 */
@SuppressWarnings("restriction")
public abstract class AbstractAction extends AbstractTerminalAction {
	// Reference to the parent toolbar handler
	private final TabFolderToolbarHandler parent;

	/**
	 * Constructor.
	 *
	 * @param parent
	 *            The parent toolbar handler instance. Must not be
	 *            <code>null</code>.
	 * @param id
	 *            The terminal action id. Must not be <code>null</code>.
	 */
	public AbstractAction(TabFolderToolbarHandler parent, String id) {
		super(id);

		Assert.isNotNull(parent);
		this.parent = parent;
	}

	/**
	 * Returns the parent toolbar handler.
	 *
	 * @return The parent toolbar handler.
	 */
	protected final TabFolderToolbarHandler getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.terminal.control.actions.AbstractTerminalAction#getTarget()
	 */
	@Override
	protected ITerminalViewControl getTarget() {
		return getParent().getActiveTerminalViewControl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.terminal.control.actions.AbstractTerminalAction#run()
	 */
	@Override
	public void run() {
		// Get the active tab item from the tab folder manager
		TabFolderManager manager = (TabFolderManager)getParent().getAdapter(TabFolderManager.class);
		if (manager != null) {
			// If we have the active tab item, we can get the active terminal control
			CTabItem activeTabItem = manager.getActiveTabItem();
			if (activeTabItem != null) {
				// And execute the command
				executeCommand(activeTabItem.getData("customData")); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Executes the command for the given data node as current and active menu selection.
	 * <p>
	 * <b>Node:</b> If the provided data node is <code>null</code>, the method will trigger
	 *              the command with an empty selection.
	 *
	 * @param data The terminal custom data node or <code>null</code>.
	 */
	protected void executeCommand(Object data) {
		// Get the command service from the workbench
		ICommandService service = (ICommandService)PlatformUI.getWorkbench().getAdapter(ICommandService.class);
		if (service != null && getCommandId() != null) {
			// Get the command
			final Command command = service.getCommand(getCommandId());
			if (command != null && command.isDefined()) {
				IHandlerService handlerSvc = (IHandlerService)PlatformUI.getWorkbench().getService(IHandlerService.class);
				Assert.isNotNull(handlerSvc);

				// Construct a selection element
				IStructuredSelection selection = data != null ? new StructuredSelection(data) : new StructuredSelection();
				// Construct the application context
				EvaluationContext context = new EvaluationContext(handlerSvc.getCurrentState(), selection);
				// Apply the selection to the "activeMenuSelection" and "selection" variable too
				context.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
				context.addVariable(ISources.ACTIVE_MENU_SELECTION_NAME, selection);
				// Allow plugin activation
				context.setAllowPluginActivation(true);
				// And execute the event
				try {
					ParameterizedCommand pCmd = ParameterizedCommand.generateCommand(command, null);
					Assert.isNotNull(pCmd);

					handlerSvc.executeCommandInContext(pCmd, null, context);
				} catch (Exception e) {
					IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
												NLS.bind(Messages.AbstractAction_error_commandExecutionFailed, getCommandId(), e.getLocalizedMessage()),
												e);
					UIPlugin.getDefault().getLog().log(status);
				}
			}
		}
	}

	/**
	 * Returns the command id of the command to execute.
	 *
	 * @return The command id. Must be never <code>null</code>.
	 */
	protected abstract String getCommandId();

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.internal.terminal.control.actions.AbstractTerminalAction#updateAction(boolean)
	 */
	@Override
	public void updateAction(boolean aboutToShow) {
		// Ignore the flag given from outside. We have to decide ourself
		// what the enabled state of the action is
		boolean enabled = getTarget() != null;

		// If a target terminal control is available, we need to find the corresponding
		// VLM target object which we need to trigger the handler
		if (enabled) {
			// The action will be enabled if we can determine the VLM target object
			enabled = false;
			// Get the active tab item from the tab folder manager
			TabFolderManager manager = (TabFolderManager)getParent().getAdapter(TabFolderManager.class);
			if (manager != null) {
				// If we have the active tab item, we can get the active terminal control
				CTabItem activeTabItem = manager.getActiveTabItem();
				if (activeTabItem != null) {
					enabled = checkEnableAction(activeTabItem.getData("customData")); //$NON-NLS-1$
				}
			}
		}

		setEnabled(enabled);
	}

	/**
	 * Checks if the action should be enabled based on the given terminal data object.
	 *
	 * @param data The terminal data node or <code>null</code>.
	 * @return <code>True</code> to enable the action, <code>false</code> otherwise.
	 */
	protected boolean checkEnableAction(Object data) {
		return data != null;
	}

	/**
	 * Returns if the action is a separator. Returning <code>true</code> here
	 * means that an additional separator toolbar element is added right or
	 * above of the action.
	 *
	 * @return <code>True</code> if the action is separating the parent contribution manager, <code>false</code> otherwise.
	 */
	public boolean isSeparator() {
		return false;
	}
}
