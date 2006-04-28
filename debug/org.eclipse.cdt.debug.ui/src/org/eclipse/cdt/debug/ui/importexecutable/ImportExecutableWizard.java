/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.importexecutable;

import java.io.File;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICDescriptorOperation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
	
	public String getPageOneTitle() {
		return Messages.ImportExecutableWizard_pageOneTitle;
	}

	public String getPageOneDescription() {
		return Messages.ImportExecutableWizard_pageOneDescription;
	}

	public String getExecutableListLabel() {
		return Messages.ImportExecutableWizard_executableListLabel;
	}

	public void setupFileDialog(FileDialog dialog) {
		dialog.setText(Messages.ImportExecutableWizard_fileDialogTitle);
		dialog.setFilterExtensions(new String[] { "*.*", "*.exe", "*.dll" });
		dialog.setFilterNames(new String[] { Messages.ImportExecutableWizard_AllFiles, Messages.ImportExecutableWizard_Applications, Messages.ImportExecutableWizard_LIbaries });
	}

	public void addBinaryParsers(IProject newProject) throws CoreException {
		ICDescriptorOperation op = new ICDescriptorOperation() {

			public void execute(ICDescriptor descriptor, IProgressMonitor monitor) throws CoreException {
				descriptor.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
				descriptor.create(CCorePlugin.BINARY_PARSER_UNIQ_ID, "org.eclipse.cdt.core.PE");
			}
		};
		CCorePlugin.getDefault().getCDescriptorManager().runDescriptorOperation(newProject.getProject(), op, null);
	}

	public boolean supportsConfigurationType(ILaunchConfigurationType type) {
		return type.getIdentifier().startsWith("org.eclipse.cdt.launch");
	}

	public boolean isExecutableFile(File file) {
		String filename = file.getName().toLowerCase();
		if (filename.endsWith(".exe") || filename.endsWith(".dll")
				|| filename.endsWith(".elf"))
			return true;
		return false;
	}

}
