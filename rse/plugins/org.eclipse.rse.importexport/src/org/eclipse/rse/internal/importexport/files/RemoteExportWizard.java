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
 *******************************************************************************/
package org.eclipse.rse.internal.importexport.files;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.internal.importexport.RemoteImportExportPlugin;
import org.eclipse.rse.internal.importexport.SystemImportExportResources;
import org.eclipse.rse.ui.wizards.AbstractSystemWizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Standard workbench wizard for exporting resources from the workspace to the
 * local file system.
 * <p>
 * This class may be instantiated and used without further configuration.
 * </p>
 * <p>
 * Example:
 * 
 * <pre>
 * IWizard wizard = new RemoteExportWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * 
 * During the call to <code>open</code>, the wizard dialog is presented to
 * the user. When the user hits Finish, the user-selected workspace resources
 * are exported to the user-specified location in the local file system, the
 * dialog closes, and the call to <code>open</code> returns.
 * </p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RemoteExportWizard extends AbstractSystemWizard implements IExportWizard {
	private IStructuredSelection selection;
	private RemoteExportWizardPage1 mainPage;
	private RemoteFileExportData exportData;
	private boolean initializeFromExportData;

	/**
	 * Creates a wizard for exporting workspace resources to the local file system.
	 */
	public RemoteExportWizard() {
		IDialogSettings workbenchSettings = RemoteImportExportPlugin.getDefault().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("RemoteExportWizard"); //$NON-NLS-1$
		if (section == null) section = workbenchSettings.addNewSection("RemoteExportWizard"); //$NON-NLS-1$
		setDialogSettings(section);
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		mainPage = new RemoteExportWizardPage1(selection);
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
		// make it the current selection by default but look it up otherwise
		this.selection = currentSelection;
		if (currentSelection.isEmpty() && workbench.getActiveWorkbenchWindow() != null) {
			IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
			if (page != null) {
				IEditorPart currentEditor = page.getActiveEditor();
				if (currentEditor != null) {
					Object selectedResource = currentEditor.getEditorInput().getAdapter(IResource.class);
					if (selectedResource != null) selection = new StructuredSelection(selectedResource);
				}
			}
		}
		setInitializeFromExportData(false);
		setWindowTitle(SystemImportExportResources.RESID_FILEEXPORT_TITLE);
		setDefaultPageImageDescriptor(getImageDescriptor(RemoteImportExportPlugin.ICON_EXPORTWIZARD_ID));
		setNeedsProgressMonitor(true);
	}

	public void init(IWorkbench workbench, RemoteFileExportData exportData) {
		this.selection = new StructuredSelection(exportData.getElements().toArray());
		this.exportData = exportData;
		setInitializeFromExportData(true);
		setWindowTitle(SystemImportExportResources.RESID_FILEEXPORT_TITLE);
		setDefaultPageImageDescriptor(getImageDescriptor(RemoteImportExportPlugin.ICON_EXPORTWIZARD_ID));
		setNeedsProgressMonitor(true);
	}

	protected void setInitializeFromExportData(boolean init) {
		this.initializeFromExportData = init;
	}

	public boolean getInitializeFromExportData() {
		return initializeFromExportData;
	}

	public RemoteFileExportData getExportData() {
		return exportData;
	}

	/**
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		return mainPage.finish();
	}
}
