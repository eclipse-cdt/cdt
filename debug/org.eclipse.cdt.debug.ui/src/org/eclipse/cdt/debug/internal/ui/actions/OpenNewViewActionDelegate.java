/*****************************************************************
 * Copyright (c) 2010, 2014 Texas Instruments and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Pin and Clone Supports (331781)
 *     Marc Dumais (Ericsson) - Bug 437692
 *****************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Opens a new view of the same type.
 */
public class OpenNewViewActionDelegate implements IViewActionDelegate {
	private OpenNewViewAction fOpenNewViewAction = new OpenNewViewAction();

	@Override
	public void run(IAction action) {
		fOpenNewViewAction.run();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void init(IViewPart view) {
		fOpenNewViewAction.init(view);
	}
}
