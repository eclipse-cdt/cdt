/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.internal;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.eclipse.tm.terminal.view.ui.tabs.TabFolderManager;


/**
 * Terminal property tester implementation.
 */
@SuppressWarnings("restriction")
public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@SuppressWarnings("cast")
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		// This property is supposed to return always false
		if ("oldViewActivityEnabled".equals(property)) { //$NON-NLS-1$
			return false;
		}

		if ("hasApplicableLauncherDelegates".equals(property)) { //$NON-NLS-1$
			ISelection selection = receiver instanceof ISelection ? (ISelection)receiver : new StructuredSelection(receiver);
			return expectedValue.equals(Boolean.valueOf(LauncherDelegateManager.getInstance().getApplicableLauncherDelegates(selection).length > 0));
		}

		if ("canDisconnect".equals(property) && receiver instanceof ITerminalsView) { //$NON-NLS-1$
			CTabItem tabItem = null;

			TabFolderManager manager = (TabFolderManager) ((ITerminalsView)receiver).getAdapter(TabFolderManager.class);
			if (manager != null) {
				tabItem = manager.getActiveTabItem();
			}

			if (tabItem != null && !tabItem.isDisposed() && tabItem.getData() instanceof ITerminalViewControl) {
	            ITerminalViewControl terminal = (ITerminalViewControl)tabItem.getData();
	            TerminalState state = terminal.getState();
	            return expectedValue.equals(Boolean.valueOf(state != TerminalState.CLOSED));
			}
			return false;
		}

		return false;
	}

}
