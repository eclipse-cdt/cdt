/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

/**
 * Opens the remote file system export wizard.
 */
public class RemoteFileOpenImportWizardActionDelegate extends RemoteFileImportExportActionDelegate {
	/**
	 * Opens the remote file system export wizard.
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		Shell parent = getShell();
		RemoteFileImportData importData = null;
		IFile file = getDescriptionFile(getSelection());
		if (file == null) {
			SystemBasePlugin.logError("No description file found"); //$NON-NLS-1$
			return;
		}
		try {
			importData = readRemoteFileImportData(file);
		} catch (CoreException e) {
			SystemBasePlugin.logError("Error occured trying to read description file" + file.getFullPath(), e); //$NON-NLS-1$
			return;
		} catch (IOException e) {
			SystemBasePlugin.logError("Error occured trying to read description file" + file.getFullPath(), e); //$NON-NLS-1$
			return;
		} catch (SAXException e) {
			SystemBasePlugin.logError("Error occured trying to read description file" + file.getFullPath(), e); //$NON-NLS-1$
			return;
		}
		if (importData == null) {
			SystemBasePlugin.logError("No export data"); //$NON-NLS-1$
			return;
		}
		RemoteImportWizard wizard = new RemoteImportWizard();
		wizard.init(getWorkbench(), importData);
		WizardDialog dialog = new WizardDialog(parent, wizard);
		dialog.create();
		dialog.open();
	}

	/**
	 * Reads the remote file export data from a file.
	 */
	private RemoteFileImportData readRemoteFileImportData(IFile description) throws CoreException, IOException, SAXException {
		Assert.isLegal(description.isAccessible());
		Assert.isNotNull(description.getFileExtension());
		Assert.isLegal(description.getFileExtension().equals(Utilities.IMPORT_DESCRIPTION_EXTENSION));
		RemoteFileImportData importData = new RemoteFileImportData();
		IRemoteFileImportDescriptionReader reader = null;
		try {
			reader = importData.createImportDescriptionReader(description.getContents());
			reader.read(importData);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return importData;
	}
}
