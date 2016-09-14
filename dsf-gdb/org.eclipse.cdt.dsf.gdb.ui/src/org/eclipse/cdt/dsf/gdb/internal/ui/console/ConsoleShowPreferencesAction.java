/*******************************************************************************
 * Copyright (c) 2016 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.action.Action;

@SuppressWarnings("restriction")
public class ConsoleShowPreferencesAction extends Action {

	public ConsoleShowPreferencesAction() {
		setText("&Preferences"); //$NON-NLS-1$
	}

	@Override
	public void run() {
    	SWTFactory.showPreferencePage("org.eclipse.cdt.dsf.gdb.ui.console.GdbConsolePreferencePage"); //$NON-NLS-1$
    }
}
