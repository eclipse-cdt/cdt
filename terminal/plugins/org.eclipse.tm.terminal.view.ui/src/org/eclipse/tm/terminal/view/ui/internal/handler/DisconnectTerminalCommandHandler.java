/*******************************************************************************
 * Copyright (c) 2014, 2018 Wind River Systems, Inc. and others. All rights reserved.
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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView;
import org.eclipse.tm.terminal.view.ui.tabs.TabFolderManager;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Disconnect terminal connection command handler implementation.
 */
public class DisconnectTerminalCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		CTabItem item = null;

		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof CTabItem && ((CTabItem) element).getData() instanceof ITerminalViewControl) {
				item = (CTabItem) element;
			}
		}

		if (item == null && HandlerUtil.getActivePart(event) instanceof ITerminalsView) {
			ITerminalsView view = (ITerminalsView) HandlerUtil.getActivePart(event);
			TabFolderManager mgr = view.getAdapter(TabFolderManager.class);
			if (mgr != null && mgr.getActiveTabItem() != null) {
				item = mgr.getActiveTabItem();
			}
		}

		if (item != null && item.getData() instanceof ITerminalViewControl) {
			ITerminalViewControl terminal = (ITerminalViewControl) item.getData();
			if (terminal != null && !terminal.isDisposed()) {
				terminal.disconnectTerminal();
			}
		}

		return null;
	}

}
