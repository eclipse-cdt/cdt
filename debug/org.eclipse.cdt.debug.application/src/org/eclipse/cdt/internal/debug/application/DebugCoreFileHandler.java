/*******************************************************************************
 * Copyright (c) 2012, 2014 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 * Red Hat Inc. - modified for use in Standalone Debugger
 *******************************************************************************/

package org.eclipse.cdt.internal.debug.application;

import org.eclipse.cdt.debug.application.CoreFileDialog;
import org.eclipse.cdt.debug.application.CoreFileInfo;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DebugCoreFileHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		CoreFileDialog dialog = new CoreFileDialog(new Shell());

		if (dialog.open() == IDialogConstants.OK_ID) {
			CoreFileInfo info = dialog.getCoreFileInfo();
			try {
				final ILaunchConfiguration config = DebugCoreFile.createLaunchConfig(new NullProgressMonitor(), null,
						info.getHostPath(), info.getCoreFilePath());
				if (config != null) {
					Display.getDefault().syncExec(() -> DebugUITools.launch(config, ILaunchManager.DEBUG_MODE));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}

		return null;
	}

}
