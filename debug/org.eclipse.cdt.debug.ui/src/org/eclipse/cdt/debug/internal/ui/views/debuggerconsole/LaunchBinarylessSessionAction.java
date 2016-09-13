/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.views.debuggerconsole;

import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.launch.CBinarylessDebugLaunchShortcut;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;

public class LaunchBinarylessSessionAction extends Action {
	DebuggerConsoleView fView;
	public LaunchBinarylessSessionAction(DebuggerConsoleView view) {
		fView = view;
		setEnabled(fView != null);

		setText(ConsoleMessages.LaunchBinarylessSessionAction_name);
		setToolTipText(ConsoleMessages.LaunchBinarylessSessionAction_description);
		setImageDescriptor(CDebugImages.DESC_OBJS_EXECUTABLE_WITH_SYMBOLS);
	}

	@Override
	public void run() {
		if (fView != null) {
			CBinarylessDebugLaunchShortcut launchShortcut = new CBinarylessDebugLaunchShortcut();
			launchShortcut.launch((IEditorPart)null, "debug"); //$NON-NLS-1$
		}
	}
	
	public void dispose() {
		fView = null;
	}
}
