/*******************************************************************************
 * Copyright (c) 2015 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Alvaro Sanchez-Leon (Ericsson) - Initial API and implementation (Bug 473536)
*******************************************************************************/
package org.eclipse.cdt.debug.ui.memory.search; 

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;
 
/**
 * A delegate for the "Find Replace" action.
 */
public class FindReplaceActionDelegate extends ActionDelegate implements IViewActionDelegate, IObjectActionDelegate {

	private IWorkbenchPart fPart;

	protected IWorkbenchPart getPart() {
		return fPart;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	    fPart = targetPart;
	}

	@Override
	public void init(IViewPart view) {
		// No need to keep track of the view part at this time
	}

	@Override
	public void run( IAction action ) {
		FindReplaceHandler handler = new FindReplaceHandler();
		try {
			handler.execute(new ExecutionEvent());
		} catch (ExecutionException e) {
			String message = "Unable to execute the FindReplace action";
	        Status status = new Status(IStatus.ERROR, MemorySearchPlugin.getUniqueIdentifier(),
	                DebugException.INTERNAL_ERROR, message, e);

	        MemorySearchPlugin.getDefault().getLog().log(status);
		}
	}
}
