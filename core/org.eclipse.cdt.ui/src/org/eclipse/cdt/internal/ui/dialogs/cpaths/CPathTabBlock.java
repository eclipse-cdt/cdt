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
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CPathTabBlock extends AbstractPathOptionBlock {

	private CheckedListDialogField fCPathList;

	private CPathSourceEntryPage fSourcePage;
	private CPathProjectsEntryPage fProjectsPage;
	private CPathOutputEntryPage fOutputPage;
	//private LibrariesWorkbookPage fLibrariesPage;

	private CPathOrderExportPage fOrderExportPage;

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

		String[] buttonLabels = new String[] { /* 0 */CPathEntryMessages.getString("CPathsBlock.path.up.button"), //$NON-NLS-1$
				/* 1 */CPathEntryMessages.getString("CPathsBlock.path.down.button"), //$NON-NLS-1$
				/* 2 */null, /* 3 */CPathEntryMessages.getString("CPathsBlock.path.checkall.button"), //$NON-NLS-1$
				/* 4 */CPathEntryMessages.getString("CPathsBlock.path.uncheckall.button") //$NON-NLS-1$

		};
		BuildPathAdapter adapter = new BuildPathAdapter();

		fCPathList = new CheckedListDialogField(null, buttonLabels, new CPListLabelProvider());
		fCPathList.setDialogFieldListener(adapter);
		fCPathList.setLabelText(CPathEntryMessages.getString("CPathsBlock.path.label")); //$NON-NLS-1$
		fCPathList.setUpButtonIndex(0);
		fCPathList.setDownButtonIndex(1);
		fCPathList.setCheckAllButtonIndex(3);
		fCPathList.setUncheckAllButtonIndex(4);

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
		fOrderExportPage = new CPathOrderExportPage(fCPathList);
		addPage(fOrderExportPage);
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
			//fLibrariesPage.init(fCurrCProject);
		}
		Dialog.applyDialogFont(control);
		return control;
	}

	/**
	 * Initializes the classpath for the given project. Multiple calls to init
	 * are allowed, but all existing settings will be cleared and replace by the
	 * given or default paths.
	 * 
	 * @param jproject
	 *        The java project to configure. Does not have to exist.
	 * @param outputLocation
	 *        The output location to be set in the page. If <code>null</code>
	 *        is passed, jdt default settings are used, or - if the project is
	 *        an existing Java project- the output location of the existing
	 *        project
	 * @param classpathEntries
	 *        The classpath entries to be set in the page. If <code>null</code>
	 *        is passed, jdt default settings are used, or - if the project is
	 *        an existing Java project - the classpath entries of the existing
	 *        project
	 */
	public void init(ICProject cproject, IPathEntry[] cpathEntries) {
		setCProject(cproject);
		boolean projectExists = false;
		List newClassPath = null;

		IProject project = getProject();
		if (cpathEntries == null) {
			try {
				cpathEntries = getCProject().getRawPathEntries();
			} catch (CModelException e) {
			}
		}
		if (cpathEntries != null) {
			newClassPath = getExistingEntries(cpathEntries);
		}
		if (newClassPath == null) {
			newClassPath = getDefaultCPath(cproject);
		}

		List exportedEntries = new ArrayList();
		for (int i = 0; i < newClassPath.size(); i++) {
			CPListElement curr = (CPListElement) newClassPath.get(i);
			if (curr.isExported() && curr.getEntryKind() != IPathEntry.CDT_SOURCE) {
				exportedEntries.add(curr);
			}
		}

		fCPathList.setElements(newClassPath);
		fCPathList.setCheckedElements(exportedEntries);

		if (fProjectsPage != null) {
			fSourcePage.init(getCProject());
			fOutputPage.init(getCProject());
			fProjectsPage.init(getCProject());
			//			fLibrariesPage.init(fCurrCProject);
		}

		doStatusLineUpdate();
		initializeTimeStamps();
	}

	private List getDefaultCPath(ICProject cproj) {
		List list = new ArrayList();
		//		IResource srcFolder;
		//		IPreferenceStore store= PreferenceConstants.getPreferenceStore();
		//		String sourceFolderName=
		// store.getString(PreferenceConstants.SRCBIN_SRCNAME);
		//		if (store.getBoolean(PreferenceConstants.SRCBIN_FOLDERS_IN_NEWPROJ)
		// && sourceFolderName.length() > 0) {
		//			srcFolder= jproj.getProject().getFolder(sourceFolderName);
		//		} else {
		//			srcFolder= jproj.getProject();
		//		}
		//
		//		list.add(new CPListElement(jproj, IClasspathEntry.CPE_SOURCE,
		// srcFolder.getFullPath(), srcFolder));
		//
		//		IClasspathEntry[] jreEntries=
		// PreferenceConstants.getDefaultJRELibrary();
		//		list.addAll(getExistingEntries(jreEntries));
		return list;
	}

	// -------- evaluate default settings --------

	//	private List getDefaultClassPath(IJavaProject jproj) {
	//		List list= new ArrayList();
	//		IResource srcFolder;
	//		IPreferenceStore store= PreferenceConstants.getPreferenceStore();
	//		String sourceFolderName=
	// store.getString(PreferenceConstants.SRCBIN_SRCNAME);
	//		if (store.getBoolean(PreferenceConstants.SRCBIN_FOLDERS_IN_NEWPROJ) &&
	// sourceFolderName.length() > 0) {
	//			srcFolder= jproj.getProject().getFolder(sourceFolderName);
	//		} else {
	//			srcFolder= jproj.getProject();
	//		}
	//
	//		list.add(new CPListElement(jproj, IClasspathEntry.CPE_SOURCE,
	// srcFolder.getFullPath(), srcFolder));
	//
	//		IPathEntry[] jreEntries= PreferenceConstants.getDefaultJRELibrary();
	//		list.addAll(getExistingEntries(jreEntries));
	//		return list;
	//	}
	//	

	/**
	 * Validates the build path.
	 */
	public void updateCPathStatus() {
		getPathStatus().setOK();

		List elements = fCPathList.getElements();

		CPListElement entryMissing = null;
		int nEntriesMissing = 0;
		IPathEntry[] entries = new IPathEntry[elements.size()];

		for (int i = elements.size() - 1; i >= 0; i--) {
			CPListElement currElement = (CPListElement) elements.get(i);
			boolean isChecked = fCPathList.isChecked(currElement);
			if (currElement.getEntryKind() == IPathEntry.CDT_SOURCE) {
				if (isChecked) {
					fCPathList.setCheckedWithoutUpdate(currElement, false);
				}
			} else {
				currElement.setExported(isChecked);
			}

			entries[i] = currElement.getPathEntry();
			if (currElement.isMissing()) {
				nEntriesMissing++;
				if (entryMissing == null) {
					entryMissing = currElement;
				}
			}
		}

		if (nEntriesMissing > 0) {
			if (nEntriesMissing == 1) {
				getPathStatus().setWarning(CPathEntryMessages.getFormattedString("CPathsBlock.warning.EntryMissing", //$NON-NLS-1$
						entryMissing.getPath().toString()));
			} else {
				getPathStatus().setWarning(CPathEntryMessages.getFormattedString("CPathsBlock.warning.EntriesMissing", //$NON-NLS-1$
						String.valueOf(nEntriesMissing)));
			}
		}

		/*
		 * if (fCurrJProject.hasClasspathCycle(entries)) {
		 * fClassPathStatus.setWarning(NewWizardMessages.getString("BuildPathsBlock.warning.CycleInClassPath"));
		 * //$NON-NLS-1$ }
		 */
		updateBuildPathStatus();
	}


	/*
	 * Creates the Java project and sets the configured build path and output
	 * location. If the project already exists only build paths are updated.
	 */
	protected void internalConfigureCProject(List cPathEntries, IProgressMonitor monitor) throws CoreException, InterruptedException {
		// 10 monitor steps to go

		monitor.worked(2);

		int nEntries = cPathEntries.size();
		IPathEntry[] classpath = new IPathEntry[nEntries];

		// create and set the class path
		for (int i = 0; i < nEntries; i++) {
			CPListElement entry = ((CPListElement) cPathEntries.get(i));
			IResource res = entry.getResource();
			if ((res instanceof IFolder) && !res.exists()) {
				createFolder((IFolder) res, true, true, null);
			}
			classpath[i] = entry.getPathEntry();
		}

		monitor.worked(1);

		getCProject().setRawPathEntries(classpath, new SubProgressMonitor(monitor, 7));
	}

	/**
	 * Creates a folder and all parent folders if not existing. Project must
	 * exist. <code> org.eclipse.ui.dialogs.ContainerGenerator</code> is too
	 * heavy (creates a runnable)
	 */
	private void createFolder(IFolder folder, boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
		if (!folder.exists()) {
			IContainer parent = folder.getParent();
			if (parent instanceof IFolder) {
				createFolder((IFolder) parent, force, local, null);
			}
			folder.create(force, local, monitor);
		}
	}
}