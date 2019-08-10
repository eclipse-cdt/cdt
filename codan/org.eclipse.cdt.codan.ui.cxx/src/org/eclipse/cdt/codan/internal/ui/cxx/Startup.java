/*******************************************************************************
 * Copyright (c) 2009, 2013 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Alex Ruiz (Google)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.cxx;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author Alena Laskavaia
 */
public class Startup implements IStartup {
	@Override
	public void earlyStartup() {
		registerListeners();
	}

	/**
	 * Register part listener for editor to install c ast reconcile listener
	 */
	private void registerListeners() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(() -> {
			// Install a part listener on currenly open workbench windows.
			for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
				CodanPartListener.installOnWindow(window);
			}

			// Install a window listener which will be notified of
			// new windows opening, and install a part listener on them.
			workbench.addWindowListener(new CodanWindowListener());
		});
	}
}
