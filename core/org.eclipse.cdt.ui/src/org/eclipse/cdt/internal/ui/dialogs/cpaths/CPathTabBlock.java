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
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.core.model.CModelStatus;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CPathTabBlock extends TabFolderOptionBlock implements ICOptionContainer {

	private CheckedListDialogField fCPathList;

	private StatusInfo fCPathStatus;
	private StatusInfo fBuildPathStatus;

	private ICProject fCurrCProject;

	private String fUserSettingsTimeStamp;
	private long fFileTimeStamp;

	private int fPageIndex, fPageCount;

	private CPathSourceEntryPage fSourcePage;
	private CPathProjectsEntryPage fProjectsPage;
	private CPathOutputEntryPage fOutputPage;
	//private LibrariesWorkbookPage fLibrariesPage;

	private CPathBasePage fCurrPage;

	private IStatusChangeListener fContext;

	private CPathOrderExportPage fOrderExportPage;

	private class BuildPathAdapter implements IDialogFieldListener {

		// ---------- IDialogFieldListener --------
		public void dialogFieldChanged(DialogField field) {
			buildPathDialogFieldChanged(field);
		}
	}

	void buildPathDialogFieldChanged(DialogField field) {
		if (field == fCPathList) {
			updateClassPathStatus();
		}
		doStatusLineUpdate();
	}

	public CPathTabBlock(IStatusChangeListener context, int pageToShow) {
		super(true);
		fContext = context;
		fPageIndex = pageToShow;

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

		fCPathStatus = new StatusInfo();
		fBuildPathStatus = new StatusInfo();

		fCurrCProject = null;
		setOptionContainer(this);
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

	private void addPage(CPathBasePage page) {
		addTab(page);
		if (fPageIndex == fPageCount) {
			fCurrPage = page;
		}
		fPageCount++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock#getStartPage()
	 */
	protected ICOptionPage getStartPage() {
		if (fCurrPage == null) {
			return super.getStartPage();
		}
		return fCurrPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		if (fCurrCProject != null) {
			fSourcePage.init(fCurrCProject);
			fOutputPage.init(fCurrCProject);
			fProjectsPage.init(fCurrCProject);
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
		fCurrCProject = cproject;
		boolean projectExists = false;
		List newClassPath = null;

		IProject project = fCurrCProject.getProject();
		projectExists = (project.exists() && project.getFile(".cdtproject").exists()); //$NON-NLS-1$
		if (projectExists) {
			if (cpathEntries == null) {
				try {
					cpathEntries = fCurrCProject.getRawPathEntries();
				} catch (CModelException e) {
				}
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
			fSourcePage.init(fCurrCProject);
			fOutputPage.init(fCurrCProject);
			fProjectsPage.init(fCurrCProject);
			//			fLibrariesPage.init(fCurrCProject);
		}

		doStatusLineUpdate();
		initializeTimeStamps();
	}

	private ArrayList getExistingEntries(IPathEntry[] cPathEntries) {
		ArrayList newCPath = new ArrayList();
		for (int i = 0; i < cPathEntries.length; i++) {
			IPathEntry curr = cPathEntries[i];
			newCPath.add(CPListElement.createFromExisting(curr, fCurrCProject));
		}
		return newCPath;
	}

	private String getEncodedSettings() {
		StringBuffer buf = new StringBuffer();

		int nElements = fCPathList.getSize();
		buf.append('[').append(nElements).append(']');
		for (int i = 0; i < nElements; i++) {
			CPListElement elem = (CPListElement) fCPathList.getElement(i);
			elem.appendEncodedSettings(buf);
		}
		return buf.toString();
	}

	public boolean hasChangesInDialog() {
		String currSettings = getEncodedSettings();
		return !currSettings.equals(fUserSettingsTimeStamp);
	}

	public boolean hasChangesInCPathFile() {
		IFile file = fCurrCProject.getProject().getFile(".cdtproject"); //$NON-NLS-1$
		return fFileTimeStamp != file.getModificationStamp();
	}

	public void initializeTimeStamps() {
		IFile file = fCurrCProject.getProject().getFile(".cdtproject"); //$NON-NLS-1$
		fFileTimeStamp = file.getModificationStamp();
		fUserSettingsTimeStamp = getEncodedSettings();
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

	// -------- public api --------

	/**
	 * @return Returns the Java project. Can return
	 *         <code>null<code> if the page has not
	 * been initialized.
	 */
	public ICProject getCProject() {
		return fCurrCProject;
	}

	/**
	 * @return Returns the current class path (raw). Note that the entries
	 *         returned must not be valid.
	 */
	public IPathEntry[] getRawCPath() {
		List elements = fCPathList.getElements();
		int nElements = elements.size();
		IPathEntry[] entries = new IPathEntry[elements.size()];

		for (int i = 0; i < nElements; i++) {
			CPListElement currElement = (CPListElement) elements.get(i);
			entries[i] = currElement.getPathEntry();
		}
		return entries;
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

	private void doStatusLineUpdate() {
		IStatus res = findMostSevereStatus();
		fContext.statusChanged(res);
	}

	private IStatus findMostSevereStatus() {
		return StatusUtil.getMostSevere(new IStatus[] { fCPathStatus, fBuildPathStatus});
	}

	/**
	 * Validates the build path.
	 */
	public void updateClassPathStatus() {
		fCPathStatus.setOK();

		List elements = fCPathList.getElements();

		CPListElement entryMissing = null;
		int nEntriesMissing = 0;
		IPathEntry[] entries = new IPathEntry[elements.size()];

		for (int i = elements.size() - 1; i >= 0; i--) {
			CPListElement currElement = (CPListElement) elements.get(i);
			boolean isChecked = fCPathList.isChecked(currElement);
			if ( currElement.getEntryKind() == IPathEntry.CDT_SOURCE) {
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
				fCPathStatus.setWarning(CPathEntryMessages.getFormattedString("BuildPathsBlock.warning.EntryMissing", //$NON-NLS-1$
						entryMissing.getPath().toString()));
			} else {
				fCPathStatus.setWarning(CPathEntryMessages.getFormattedString("BuildPathsBlock.warning.EntriesMissing", //$NON-NLS-1$
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

	private void updateBuildPathStatus() {
		List elements = fCPathList.getElements();
		IPathEntry[] entries = new IPathEntry[elements.size()];

		for (int i = elements.size() - 1; i >= 0; i--) {
			CPListElement currElement = (CPListElement) elements.get(i);
			entries[i] = currElement.getPathEntry();
		}

		ICModelStatus status = CModelStatus.VERIFIED_OK; // CoreModelUtil.validateCPathEntries(fCurrCProject, entries);
		if (!status.isOK()) {
			fBuildPathStatus.setError(status.getMessage());
			return;
		}
		fBuildPathStatus.setOK();
	}

	// -------- creation -------------------------------

	public void configureCProject(IProgressMonitor monitor) throws CoreException, InterruptedException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.setTaskName(CPathEntryMessages.getString("CPathsBlock.operationdesc_c")); //$NON-NLS-1$
		monitor.beginTask("", 10); //$NON-NLS-1$

		try {
			internalConfigureCProject(fCPathList.getElements(), monitor);
		} finally {
			monitor.done();
		}
	}

	/*
	 * Creates the Java project and sets the configured build path and output
	 * location. If the project already exists only build paths are updated.
	 */
	private void internalConfigureCProject(List cPathEntries, IProgressMonitor monitor) throws CoreException, InterruptedException {
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

		fCurrCProject.setRawPathEntries(classpath, new SubProgressMonitor(monitor, 7));
		initializeTimeStamps();
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


	// -------- tab switching ----------

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock#setCurrentPage(org.eclipse.cdt.ui.dialogs.ICOptionPage)
	 */
	public void setCurrentPage(ICOptionPage page) {
		super.setCurrentPage(page);
		CPathBasePage newPage = (CPathBasePage) page;
		if (fCurrPage != null) {
			List selection = fCurrPage.getSelection();
			if (!selection.isEmpty()) {
				newPage.setSelection(selection);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#updateContainer()
	 */
	public void updateContainer() {
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getProject()
	 */
	public IProject getProject() {
		return fCurrCProject.getProject();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getPreferences()
	 */
	public Preferences getPreferences() {
		return null;
	}
}
