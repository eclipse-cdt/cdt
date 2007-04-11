package org.eclipse.rse.internal.importexport.files;

/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.internal.importexport.SystemImportExportResources;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.wizards.AbstractSystemWizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

// Similar to org.eclipse.ui.wizards.datatransfer.FileSystemImportWizard
// Changes marked with "IFS:" comments.  Also see use of RemoteImportExportPlugin
/**
 * Standard workbench wizard for importing resources from the local file system
 * into the workspace.
 * <p>
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * IWizard wizard = new RemoteImportWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, the user-selected files are imported
 * into the workspace, the dialog closes, and the call to <code>open</code>
 * returns.
 * </p>
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
		IDialogSettings workbenchSettings = RSEUIPlugin.getDefault().getDialogSettings();
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
	 * Returns the image descriptor with the given relative path.
	 */
	private ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "icons/full/"; //$NON-NLS-1$
		return RSEUIPlugin.getDefault().getPluginImage(iconPath + relativePath);
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.workbench = workbench;
		selection = currentSelection;
		setWindowTitle(SystemImportExportResources.RESID_FILEIMPORT_TITLE);
		setDefaultPageImageDescriptor(getImageDescriptor("wizban/import_wiz.gif")); //$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}

	public void init(IWorkbench workbench, RemoteFileImportData importData) {
		this.workbench = workbench;
		this.selection = new StructuredSelection(importData.getElements().toArray());
		this.importData = importData;
		setInitializeFromImportData(true);
		setWindowTitle(SystemImportExportResources.RESID_FILEIMPORT_TITLE);
		setDefaultPageImageDescriptor(getImageDescriptor("wizban/import_wiz.gif")); //$NON-NLS-1$
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
}
