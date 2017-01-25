/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.debuggerconsole;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.launch.UseExistingGDBLaunchShortcut;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;

public class LaunchExistingGDBSessionAction extends Action {
	DebuggerConsoleView fView;
	public LaunchExistingGDBSessionAction(DebuggerConsoleView view) {
		fView = view;
		setEnabled(fView != null);

		setText(ConsoleMessages.LaunchExistingGDBSessionAction_name);
		setToolTipText(ConsoleMessages.LaunchExistingGDBSessionAction_description);
		setImageDescriptor(CDebugImages.DESC_OBJS_EXECUTABLE_WITH_SYMBOLS);
	}

	@Override
	public void run() {
		if (fView != null) {
			UseExistingGDBLaunchShortcut launchShortcut = new UseExistingGDBLaunchShortcut();
			launchShortcut.launch((IEditorPart)null, "debug"); //$NON-NLS-1$
		}
	}
	
	public void dispose() {
		fView = null;
	}
}
