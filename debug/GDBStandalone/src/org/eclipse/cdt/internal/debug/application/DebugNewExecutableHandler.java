/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.debug.application;

import org.eclipse.cdt.debug.application.NewExecutableDialog;
import org.eclipse.cdt.debug.application.NewExecutableInfo;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;

public class DebugNewExecutableHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		NewExecutableDialog dialog = new NewExecutableDialog(new Shell());
		
		if (dialog.open() == IDialogConstants.OK_ID) {
			NewExecutableInfo info = dialog.getExecutableInfo();
			String executable = info.getHostPath();
			String arguments = info.getArguments();
			
//			ILaunchConfiguration config = DebugExecutable.importAndCreateLaunchConfig(monitor, executable, null, arguments);
		}


		return null;
	}

}
