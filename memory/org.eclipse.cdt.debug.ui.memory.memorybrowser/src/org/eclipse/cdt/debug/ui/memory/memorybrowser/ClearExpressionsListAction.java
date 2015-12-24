/*******************************************************************************
 * Copyright (c) 2007-2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	
	public void init(IViewPart view) {
		fView = view;
	}

	public void run(IAction action) {
		if ( fView instanceof MemoryBrowser ) {
			MemoryBrowser browser = (MemoryBrowser) fView;
			browser.clearExpressionHistoryForActiveTab();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {}
}
