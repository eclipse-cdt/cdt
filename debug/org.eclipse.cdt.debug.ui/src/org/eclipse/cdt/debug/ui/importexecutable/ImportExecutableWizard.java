/*******************************************************************************
 * Copyright (c) 2006, 2012 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.importexecutable;

import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.swt.widgets.FileDialog;

/**
 * Reference implementation of a wizard that imports executables.
 * Create your own version to import specific kinds of executables
 * with product specific messages and launch configuration types.
 *
 *
 */
public class ImportExecutableWizard extends AbstractImportExecutableWizard {

	@Override
	public String getPageOneTitle() {
		return Messages.ImportExecutableWizard_pageOneTitle;
	}

	@Override
	public String getPageOneDescription() {
		return Messages.ImportExecutableWizard_pageOneDescription;
	}

	@Override
	public String getExecutableListLabel() {
		return Messages.ImportExecutableWizard_executableListLabel;
	}

	@Override
	public void setupFileDialog(FileDialog dialog) {
		dialog.setText(Messages.ImportExecutableWizard_fileDialogTitle);
	}

	@Override
	public boolean supportsConfigurationType(ILaunchConfigurationType type) {
		return true;
	}

}
