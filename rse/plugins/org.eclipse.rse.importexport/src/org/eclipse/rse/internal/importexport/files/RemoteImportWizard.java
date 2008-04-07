/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [174945] split importexport icons from rse.ui
 * David McKnight   (IBM)        - [219792][importexport][ftp] RSE hangs on FTP import
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.internal.importexport.RemoteImportExportPlugin;
import org.eclipse.rse.internal.importexport.SystemImportExportResources;
import org.eclipse.rse.ui.wizards.AbstractSystemWizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

// Similar to org.eclipse.ui.wizards.datatransfer.FileSystemImportWizard
// Changes marked with "IFS:" comments.  Also see use of RemoteImportExportPlugin
/**
 * Standard workbench wizard for importing resources from the local file system
 * into the workspace.
 * <p>
 * This class may be instantiated and used without further configuration.
 * </p>
 * <p>
 * Example:
 * 
 * <pre>
 * IWizard wizard = new RemoteImportWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * 
 * During the call to <code>open</code>, the wizard dialog is presented to
 * the user. When the user hits Finish, the user-selected files are imported
 * into the workspace, the dialog closes, and the call to <code>open</code>
 * returns.
 * </p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RemoteImportWizard extends AbstractSystemWizard implements IImportWizard {
	private IWorkbench workbench;
	private IStructuredSelection selection;
	private RemoteImportWizardPage1 mainPage;
	private RemoteFileImportData importData;
	private boolean initializeFromExportData;

	/**
	 * Creates a wizard for importing resources into the workspace from
	 * the file system.
	 */
	public RemoteImportWizard() {
		IDialogSettings workbenchSettings = RemoteImportExportPlugin.getDefault().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("RemoteImportWizard"); //$NON-NLS-1$
		if (section == null) section = workbenchSettings.addNewSection("RemoteImportWizard"); //$NON-NLS-1$
		setDialogSettings(section);
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		mainPage = new RemoteImportWizardPage1(workbench, selection);
		addPage(mainPage);
	}

	/**
	 * Returns the image descriptor with the given key.
	 */
	private ImageDescriptor getImageDescriptor(String key) {
		return RemoteImportExportPlugin.getDefault().getImageDescriptor(key);
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.workbench = workbench;
		selection = currentSelection;
		setWindowTitle(SystemImportExportResources.RESID_FILEIMPORT_TITLE);
		setDefaultPageImageDescriptor(getImageDescriptor(RemoteImportExportPlugin.ICON_IMPORTWIZARD_ID));
		setNeedsProgressMonitor(true);
	}

	public void init(IWorkbench workbench, RemoteFileImportData importData) {
		this.workbench = workbench;
		this.selection = new StructuredSelection(importData.getElements().toArray());
		this.importData = importData;
		setInitializeFromImportData(true);
		setWindowTitle(SystemImportExportResources.RESID_FILEIMPORT_TITLE);
		setDefaultPageImageDescriptor(getImageDescriptor(RemoteImportExportPlugin.ICON_IMPORTWIZARD_ID));
		setNeedsProgressMonitor(true);
	}

	protected void setInitializeFromImportData(boolean init) {
		this.initializeFromExportData = init;
	}

	public boolean getInitializeFromImportData() {
		return initializeFromExportData;
	}

	public RemoteFileImportData getImportData() {
		return importData;
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		return mainPage.finish();
	}

	public boolean performCancel() {
		mainPage.cancel();
		return super.performCancel();
	}
}
