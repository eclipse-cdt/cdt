/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
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

	private IContainerDescriptor fPageDesc;
	private IPathEntry fEntryToEdit;

	private IPathEntry[] fNewEntries;
	private IPathEntry[] fContainerEntries;
	private ICPathContainerPage fContainerPage;
	private ICElement fCurrElement;
	private IPathEntry[] fCurrCPath;
	private CPathFilterPage fFilterPage;

	private CPathContainerSelectionPage fSelectionWizardPage;
	private int[] fFilterType;

	/**
	 * Constructor for ClasspathContainerWizard.
	 */
	public CPathContainerWizard(IPathEntry entryToEdit, ICElement currElement, IPathEntry[] currEntries) {
		this(entryToEdit, null, currElement, currEntries, null);
	}

	/**
	 * Constructor for ClasspathContainerWizard.
	 */
	public CPathContainerWizard(IContainerDescriptor pageDesc, ICElement currElement, IPathEntry[] currEntries) {
		this(null, pageDesc, currElement, currEntries, null);
	}

	public CPathContainerWizard(IPathEntry entryToEdit, IContainerDescriptor pageDesc, ICElement currElement,
			IPathEntry[] currEntries, int[] filterType) {
		fEntryToEdit = entryToEdit;
		fPageDesc = pageDesc;
		fNewEntries = null;

		fFilterType = filterType;
		fCurrElement = currElement;
		fCurrCPath = currEntries;
	}

	public IPathEntry getEntriesParent() {
		return fContainerEntries[0];
	}

	public IPathEntry[] getEntries() {
		return fNewEntries;
	}

	public IPathEntry[] getContainers() {
		return fContainerEntries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IWizard#performFinish()
	 */
	public boolean performFinish() {
		if (fContainerPage != null) {
			if (fContainerPage.finish()) {
				fContainerEntries = fContainerPage.getContainerEntries();
				if (fFilterPage != null && fFilterPage.isPageComplete()) {
					fNewEntries = fFilterPage.getSelectedEntries();
				}
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
		} else if (fEntryToEdit == null) { // new entry: show selection page as
			// first page
			IContainerDescriptor[] containers = CPathContainerDescriptor.getDescriptors();
			List allContainers = new ArrayList(Arrays.asList(containers));
			if (fFilterType != null) {
				allContainers.add(0, new ProjectContainerDescriptor(fFilterType));
			}
			fSelectionWizardPage = new CPathContainerSelectionPage(
					(IContainerDescriptor[])allContainers.toArray(new IContainerDescriptor[0]));
			addPage(fSelectionWizardPage);

			// add as dummy, will not be shown
			fContainerPage = new CPathContainerDefaultPage();
			addPage(fContainerPage);
		} else { // fPageDesc == null && fEntryToEdit != null
			IContainerDescriptor[] containers = CPathContainerDescriptor.getDescriptors();
			IContainerDescriptor descriptor = findDescriptorPage(containers, fEntryToEdit);
			fContainerPage = getContainerPage(descriptor);
			addPage(fContainerPage);
		}
		if (fFilterType != null) {
			fFilterPage = new CPathFilterPage(fCurrElement, fFilterType);
			addPage(fFilterPage);
		}
		super.addPages();
	}

	private ICPathContainerPage getContainerPage(IContainerDescriptor pageDesc) {
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
		containerPage.initialize(fCurrElement.getCProject(), fCurrCPath);
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

			IContainerDescriptor selected = fSelectionWizardPage.getSelected();
			fContainerPage = getContainerPage(selected);
			return fContainerPage;
		} else if (page == fContainerPage && fFilterPage != null) {
			if (fContainerPage.finish() && fContainerPage.getContainerEntries().length > 0
					&& fContainerPage.getContainerEntries()[0] != null) {
				fFilterPage.setParentEntry(fContainerPage.getContainerEntries()[0]);
			}
			return fFilterPage;
		}
		return super.getNextPage(page);
	}

	private void handlePageCreationFailed(CoreException e) {
		String title = CPathEntryMessages.getString("CPathContainerWizard.pagecreationerror.title"); //$NON-NLS-1$
		String message = CPathEntryMessages.getString("CPathContainerWizard.pagecreationerror.message"); //$NON-NLS-1$
		ExceptionHandler.handle(e, getShell(), title, message);
	}

	private IContainerDescriptor findDescriptorPage(IContainerDescriptor[] containers, IPathEntry entry) {
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
		boolean canFinish = false;
		if (fContainerPage != null) {
			canFinish = fContainerPage.isPageComplete();
		}
		if (canFinish == true && fFilterPage != null) {
			canFinish = fFilterPage.isPageComplete();
		}
		return canFinish;
	}

	public static int openWizard(Shell shell, CPathContainerWizard wizard) {
		WizardDialog dialog = new WizardDialog(shell, wizard);
		PixelConverter converter = new PixelConverter(shell);
		dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(70), converter.convertHeightInCharsToPixels(20));
		dialog.create();
		return dialog.open();
	}

}