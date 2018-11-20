/*******************************************************************************
 * Copyright (c) 2007-2011 Wind River Systems, Inc. and others.
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

package org.eclipse.cdt.debug.ui.memory.memorybrowser;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class ClearExpressionsListAction implements IViewActionDelegate {

	private IViewPart fView;

	@Override
	public void init(IViewPart view) {
		fView = view;
	}

	@Override
	public void run(IAction action) {
		if (fView instanceof MemoryBrowser) {
			MemoryBrowser browser = (MemoryBrowser) fView;
			browser.clearExpressionHistoryForActiveTab();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
