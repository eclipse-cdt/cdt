/*******************************************************************************
 * Copyright (c) 2002, 2003, 2004 QNX Software Systems Ltd. and others. All
 * rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.core.model.CModelStatus;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;


abstract public class AbstractPathOptionBlock extends TabFolderOptionBlock implements ICOptionContainer {

	private StatusInfo fCPathStatus;
	private StatusInfo fBuildPathStatus;

	private ICProject fCurrCProject;

	private String fUserSettingsTimeStamp;
	private long fFileTimeStamp;

	private int fPageIndex, fPageCount;
	private CPathBasePage fCurrPage;

	private IStatusChangeListener fContext;

	public AbstractPathOptionBlock(IStatusChangeListener context, int pageToShow) {
		super(false);
		
		fContext = context;
		fPageIndex = pageToShow;
		
		fCPathStatus = new StatusInfo();
		fBuildPathStatus = new StatusInfo();

		setOptionContainer(this);

	}

	// -------- public api --------

	/**
	 * @return Returns the current class path (raw). Note that the entries
	 *         returned must not be valid.
	 */
	public IPathEntry[] getRawCPath() {
		List elements = getCPaths();
		int nElements = elements.size();
		IPathEntry[] entries = new IPathEntry[elements.size()];

		for (int i = 0; i < nElements; i++) {
			CPListElement currElement = (CPListElement) elements.get(i);
			entries[i] = currElement.getPathEntry();
		}
		return entries;
	}

	protected ArrayList getExistingEntries(IPathEntry[] cPathEntries) {
		ArrayList newCPath = new ArrayList();
		for (int i = 0; i < cPathEntries.length; i++) {
			IPathEntry curr = cPathEntries[i];
			newCPath.add(CPListElement.createFromExisting(curr, fCurrCProject));
		}
		return newCPath;
	}
	
	abstract protected List getCPaths();

	private String getEncodedSettings() {
		StringBuffer buf = new StringBuffer();

		int nElements = getCPaths().size();
		buf.append('[').append(nElements).append(']');
		for (int i = 0; i < nElements; i++) {
			CPListElement elem = (CPListElement) getCPaths().get(i);
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

	abstract protected void addTabs();

	protected void setCProject(ICProject project) {
		fCurrCProject = project;
	}
	
	protected ICProject getCProject() {
		return fCurrCProject;
	}
	
	public IProject getProject() {
		return fCurrCProject.getProject();
	}

	protected void doStatusLineUpdate() {
		IStatus res = findMostSevereStatus();
		fContext.statusChanged(res);
	}

	private IStatus findMostSevereStatus() {
		return StatusUtil.getMostSevere(new IStatus[] { fCPathStatus, fBuildPathStatus});
	}

	protected StatusInfo getPathStatus() {
		return fCPathStatus;
	}
	
	// -------- tab switching ----------

	public void setCurrentPage(ICOptionPage page) {
		super.setCurrentPage(page);
		CPathBasePage newPage = (CPathBasePage) page;
		if (fCurrPage != null) {
			List selection = fCurrPage.getSelection();
			if (!selection.isEmpty()) {
				newPage.setSelection(selection);
			}
		}
		fCurrPage = (CPathBasePage) page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#updateContainer()
	 */
	public void updateContainer() {
		update();
	}

	protected void updateBuildPathStatus() {
		List elements = getCPaths();
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

	public Preferences getPreferences() {
		return null;
	}

	protected void addPage(CPathBasePage page) {
		addTab(page);
		if (fPageIndex == fPageCount) {
			fCurrPage = page;
		}
		fPageCount++;
	}

	protected ICOptionPage getStartPage() {
		if (fCurrPage == null) {
			return super.getStartPage();
		}
		return fCurrPage;
	}
	
	abstract protected void internalConfigureCProject(List cPathEntries, IProgressMonitor monitor) throws CoreException, InterruptedException;
	
	// -------- creation -------------------------------

	public void configureCProject(IProgressMonitor monitor) throws CoreException, InterruptedException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.setTaskName(CPathEntryMessages.getString("CPathsBlock.operationdesc_c")); //$NON-NLS-1$
		monitor.beginTask("", 10); //$NON-NLS-1$

		try {
			internalConfigureCProject(getCPaths(), monitor);
			initializeTimeStamps();
		} finally {
			monitor.done();
		}
	}

}
