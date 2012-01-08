/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IPathEntry;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;

/**
 * Block for C/C++ Project Paths page for 3.X projects.
 * 
 * @deprecated as of CDT 4.0. This option block was for property pages
 * for 3.X style projects.
 */
@Deprecated
public class CPathTabBlock extends AbstractPathOptionBlock {
	
	private final int[] pathTypes = {IPathEntry.CDT_SOURCE, IPathEntry.CDT_PROJECT, IPathEntry.CDT_OUTPUT, IPathEntry.CDT_LIBRARY,IPathEntry.CDT_CONTAINER};

	private ListDialogField<CPElement> fCPathList;

	private CPathSourceEntryPage fSourcePage;
	private CPathProjectsEntryPage fProjectsPage;
	private CPathOutputEntryPage fOutputPage;
	private CPathContainerEntryPage fContainerPage;
	private CPathLibraryEntryPage fLibrariesPage;

	private class BuildPathAdapter implements IDialogFieldListener {

		// ---------- IDialogFieldListener --------
		@Override
		public void dialogFieldChanged(DialogField field) {
			buildPathDialogFieldChanged(field);
		}
	}

	void buildPathDialogFieldChanged(DialogField field) {
		if (field == fCPathList) {
			updateCPathStatus();
		}
		doStatusLineUpdate();
	}

	public CPathTabBlock(IStatusChangeListener context, int pageToShow) {
		super(context, pageToShow);

		String[] buttonLabels = new String[]{ CPathEntryMessages.CPathsBlock_path_up_button, 
				CPathEntryMessages.CPathsBlock_path_down_button, 
				/* 2 */null, CPathEntryMessages.CPathsBlock_path_checkall_button, 
				CPathEntryMessages.CPathsBlock_path_uncheckall_button

		};
		BuildPathAdapter adapter = new BuildPathAdapter();

		fCPathList = new ListDialogField<CPElement>(null, buttonLabels, null);
		fCPathList.setDialogFieldListener(adapter);
	}

	@Override
	protected List<CPElement> getCPaths() {
		return fCPathList.getElements();
	}

	@Override
	protected void addTabs() {
		fSourcePage = new CPathSourceEntryPage(fCPathList);
		addPage(fSourcePage);
		fOutputPage = new CPathOutputEntryPage(fCPathList);
		addPage(fOutputPage);
		fProjectsPage = new CPathProjectsEntryPage(fCPathList);
		addPage(fProjectsPage);
		fLibrariesPage = new CPathLibraryEntryPage(fCPathList);
		addPage(fLibrariesPage);
		fContainerPage = new CPathContainerEntryPage(fCPathList);
		addPage(fContainerPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		if (getCProject() != null) {
			fSourcePage.init(getCProject());
			fOutputPage.init(getCProject());
			fProjectsPage.init(getCProject());
			fContainerPage.init(getCProject());
			fLibrariesPage.init(getCProject());
		}
		Dialog.applyDialogFont(control);
		return control;
	}

	@Override
	protected void initialize(ICElement element, List<CPElement> cPaths) {

		fCPathList.setElements(cPaths);

		if (fProjectsPage != null) {
			fSourcePage.init(getCProject());
			fOutputPage.init(getCProject());
			fProjectsPage.init(getCProject());
			fContainerPage.init(getCProject());
			fLibrariesPage.init(getCProject());
		}

		doStatusLineUpdate();
		initializeTimeStamps();
	}

	@Override
	protected int[] getFilteredTypes() {
		return pathTypes;
	}

	@Override
	protected int[] getAppliedFilteredTypes() {
		return pathTypes;
	}
	/**
	 * Validates the build path.
	 */
	public void updateCPathStatus() {
		getPathStatus().setOK();

		List<CPElement> elements = fCPathList.getElements();

		CPElement entryError = null;
		int nErrorEntries = 0;
		IPathEntry[] entries = new IPathEntry[elements.size()];

		for (int i = elements.size() - 1; i >= 0; i--) {
			CPElement currElement = elements.get(i);

			entries[i] = currElement.getPathEntry();
			if (currElement.getStatus().getSeverity() != IStatus.OK) {
				nErrorEntries++;
				if (entryError == null) {
					entryError = currElement;
				}
			}
		}

		if (nErrorEntries > 0) {
			if (nErrorEntries == 1 && entryError != null) {
				getPathStatus().setWarning(entryError.getStatus().getMessage());
			} else {
				getPathStatus().setWarning(NLS.bind(CPathEntryMessages.CPElement_status_multiplePathErrors, 
						String.valueOf(nErrorEntries)));
			}
		}

		/*
		 * if (fCurrJProject.hasClasspathCycle(entries)) {
		 * fClassPathStatus.setWarning(NewWizardMessages.getString("BuildPathsBlock.warning.CycleInClassPath"));
		 * //$NON-NLS-1$ }
		 */
		updateBuildPathStatus();
	}
}
