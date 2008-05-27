/********************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * Anna Dushistova  (MontaVista) - Adopted from LaunchShellActionDelegate
 ********************************************************************************/
package org.eclipse.rse.internal.shells.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.internal.shells.ui.actions.SystemCommandAction;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.ui.handlers.HandlerUtil;

public class LaunchShellCommandHandler extends AbstractHandler {
	private SystemCommandAction _launchAction;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (_launchAction == null) {
			_launchAction = new SystemCommandAction(SystemBasePlugin
					.getActiveWorkbenchShell(), true);
		}
		_launchAction.updateSelection((IStructuredSelection) HandlerUtil
				.getCurrentSelection(event));
		_launchAction.run();
		return null;
	}

}
