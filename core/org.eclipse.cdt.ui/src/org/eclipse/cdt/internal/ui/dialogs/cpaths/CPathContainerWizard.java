/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.ui.wizards.ICPathContainerPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class CPathContainerWizard extends Wizard {

	private CPathContainerDescriptor fPageDesc;
	private IPathEntry fEntryToEdit;

	private IPathEntry[] fNewEntries;
	private ICPathContainerPage fContainerPage;
	private ICProject fCurrProject;
	private IPathEntry[] fCurrClasspath;

	private CPathContainerSelectionPage fSelectionWizardPage;

	/**
	 * Constructor for ClasspathContainerWizard.
	 */
	public CPathContainerWizard(IPathEntry entryToEdit, ICProject currProject, IPathEntry[] currEntries) {
		this(entryToEdit, null, currProject, currEntries);
	}

	/**
	 * Constructor for ClasspathContainerWizard.
	 */
	public CPathContainerWizard(CPathContainerDescriptor pageDesc, ICProject currProject, IPathEntry[] currEntries) {
		this(null, pageDesc, currProject, currEntries);
	}

	/**
	 * @deprecated
	 */
	public CPathContainerWizard(CPathContainerDescriptor pageDesc) {
		this(null, pageDesc, null, null);
	}

	private CPathContainerWizard(IPathEntry entryToEdit, CPathContainerDescriptor pageDesc, ICProject currProject,
			IPathEntry[] currEntries) {
		fEntryToEdit = entryToEdit;
		fPageDesc = pageDesc;
		fNewEntries = null;

		fCurrProject = currProject;
		fCurrClasspath = currEntries;
	}

	/**
	 * @deprecated use getNewEntries()
	 */
	public IPathEntry getNewEntry() {
		IPathEntry[] entries = getNewEntries();
		if (entries != null) {
			return entries[0];
		}
		return null;
	}

	public IPathEntry[] getNewEntries() {
		return fNewEntries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IWizard#performFinish()
	 */
	public boolean performFinish() {
		if (fContainerPage != null) {
			if (fContainerPage.finish()) {
				fNewEntries = fContainerPage.getContainerEntries();
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IWizard#addPages()
	 */
	public void addPages() {
		if (fPageDesc != null) {
			fContainerPage = getContainerPage(fPageDesc);
			addPage(fContainerPage);
		} else if (fEntryToEdit == null) { // new entry: show selection page as first page
			CPathContainerDescriptor[] containers = CPathContainerDescriptor.getDescriptors();

			fSelectionWizardPage = new CPathContainerSelectionPage(containers);
			addPage(fSelectionWizardPage);

			// add as dummy, will not be shown
			fContainerPage = new CPathContainerDefaultPage();
			addPage(fContainerPage);
		} else { // fPageDesc == null && fEntryToEdit != null
			CPathContainerDescriptor[] containers = CPathContainerDescriptor.getDescriptors();
			CPathContainerDescriptor descriptor = findDescriptorPage(containers, fEntryToEdit);
			fContainerPage = getContainerPage(descriptor);
			addPage(fContainerPage);
		}
		super.addPages();
	}

	private ICPathContainerPage getContainerPage(CPathContainerDescriptor pageDesc) {
		ICPathContainerPage containerPage = null;
		if (pageDesc != null) {
			try {
				containerPage = pageDesc.createPage();
			} catch (CoreException e) {
				handlePageCreationFailed(e);
				containerPage = null;
			}
		}

		if (containerPage == null) {
			containerPage = new CPathContainerDefaultPage();
		}

		containerPage.initialize(fCurrProject, fCurrClasspath);
		containerPage.setSelection(fEntryToEdit);
		containerPage.setWizard(this);
		return containerPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IWizard#getNextPage(IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == fSelectionWizardPage) {

			CPathContainerDescriptor selected = fSelectionWizardPage.getSelected();
			fContainerPage = getContainerPage(selected);
			return fContainerPage;
		}
		return super.getNextPage(page);
	}

	private void handlePageCreationFailed(CoreException e) {
		String title = CPathEntryMessages.getString("CPathContainerWizard.pagecreationerror.title"); //$NON-NLS-1$
		String message = CPathEntryMessages.getString("CPathContainerWizard.pagecreationerror.message"); //$NON-NLS-1$
		ExceptionHandler.handle(e, getShell(), title, message);
	}

	private CPathContainerDescriptor findDescriptorPage(CPathContainerDescriptor[] containers, IPathEntry entry) {
		for (int i = 0; i < containers.length; i++) {
			if (containers[i].canEdit(entry)) {
				return containers[i];
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IWizard#canFinish()
	 */
	public boolean canFinish() {
		if (fSelectionWizardPage != null) {
			if (!fContainerPage.isPageComplete()) {
				return false;
			}
		}
		if (fContainerPage != null) {
			return fContainerPage.isPageComplete();
		}
		return false;
	}

	public static int openWizard(Shell shell, CPathContainerWizard wizard) {
		WizardDialog dialog = new WizardDialog(shell, wizard);
		PixelConverter converter = new PixelConverter(shell);
		dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(70), converter.convertHeightInCharsToPixels(20));
		dialog.create();
		return dialog.open();
	}

}