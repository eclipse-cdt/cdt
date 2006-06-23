/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CPathTabBlock extends AbstractPathOptionBlock {
	
	private final int[] pathTypes = {IPathEntry.CDT_SOURCE, IPathEntry.CDT_PROJECT, IPathEntry.CDT_OUTPUT, IPathEntry.CDT_LIBRARY,IPathEntry.CDT_CONTAINER};

	private ListDialogField fCPathList;

	private CPathSourceEntryPage fSourcePage;
	private CPathProjectsEntryPage fProjectsPage;
	private CPathOutputEntryPage fOutputPage;
	private CPathContainerEntryPage fContainerPage;
	private CPathLibraryEntryPage fLibrariesPage;

	private class BuildPathAdapter implements IDialogFieldListener {

		// ---------- IDialogFieldListener --------
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

		String[] buttonLabels = new String[]{ /* 0 */CPathEntryMessages.getString("CPathsBlock.path.up.button"), //$NON-NLS-1$
				/* 1 */CPathEntryMessages.getString("CPathsBlock.path.down.button"), //$NON-NLS-1$
				/* 2 */null, /* 3 */CPathEntryMessages.getString("CPathsBlock.path.checkall.button"), //$NON-NLS-1$
				/* 4 */CPathEntryMessages.getString("CPathsBlock.path.uncheckall.button") //$NON-NLS-1$

		};
		BuildPathAdapter adapter = new BuildPathAdapter();

		fCPathList = new ListDialogField(null, buttonLabels, null);
		fCPathList.setDialogFieldListener(adapter);
	}

	protected List getCPaths() {
		return fCPathList.getElements();
	}

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

	protected void initialize(ICElement element, List cPaths) {

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

	protected int[] getFilteredTypes() {
		return pathTypes;
	}

	protected int[] getAppliedFilteredTypes() {
		return pathTypes;
	}
	/**
	 * Validates the build path.
	 */
	public void updateCPathStatus() {
		getPathStatus().setOK();

		List elements = fCPathList.getElements();

		CPElement entryError = null;
		int nErrorEntries = 0;
		IPathEntry[] entries = new IPathEntry[elements.size()];

		for (int i = elements.size() - 1; i >= 0; i--) {
			CPElement currElement = (CPElement)elements.get(i);

			entries[i] = currElement.getPathEntry();
			if (currElement.getStatus().getSeverity() != IStatus.OK) {
				nErrorEntries++;
				if (entryError == null) {
					entryError = currElement;
				}
			}
		}

		if (nErrorEntries > 0) {
			if (nErrorEntries == 1) {
				getPathStatus().setWarning(entryError.getStatus().getMessage());
			} else {
				getPathStatus().setWarning(CPathEntryMessages.getFormattedString("CPElement.status.multiplePathErrors", //$NON-NLS-1$
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
