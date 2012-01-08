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

import java.util.ArrayList;
import java.util.List;

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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.dialogs.TabFolderOptionBlock;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.util.CoreUtility;

/**
 * Abstract block for C/C++ Project Paths page for 3.X projects.
 * 
 * @deprecated as of CDT 4.0. This option block was used to set preferences/properties
 * for 3.X style projects.
 */
@Deprecated
abstract public class AbstractPathOptionBlock extends TabFolderOptionBlock implements ICOptionContainer {

	private StatusInfo fCPathStatus;
	private StatusInfo fBuildPathStatus;

	private ICElement fCurrCElement;
	
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
	public IPathEntry[] getRawCPath() throws CModelException{
		List<CPElement> elements = getCPaths();

		IPathEntry[] entries = getCProject().getRawPathEntries();
		List<IPathEntry> cpath = new ArrayList<IPathEntry>(elements.size() + entries.length);

		int[] applyTypes = getAppliedFilteredTypes(); 
		// create and set the paths
		for (int i = 0; i < elements.size(); i++) {
			CPElement entry = (elements.get(i));			
			for (int applyType : applyTypes) {
				if (entry.getEntryKind() == applyType) {
					cpath.add(entry.getPathEntry());
					break;
				}
			}
		}

		// add entries which do not match type being applyed by the ui block
		for (IPathEntry entrie : entries) {
			int pathType = entrie.getEntryKind();
			boolean found = false;
			for (int applyType : applyTypes) {
				if (pathType == applyType) {
					found = true;
					break;
				}
			}
			if (!found) {
				cpath.add(entrie);
			}
		}
		
		return cpath.toArray(new IPathEntry[cpath.size()]);
	}

	/**
	 * Initializes the paths for the given project. Multiple calls to init are
	 * allowed, but all existing settings will be cleared and replace by the
	 * given or default paths.
	 * 
	 * @param element
	 *        The C/C++ project to configure. Does not have to exist.
	 * @param cpathEntries
	 *        The path entries to be set in the page. If <code>null</code> is
	 *        passed, jdt default settings are used, or - if the project is an
	 *        existing Java project - the path entries of the existing project
	 */
	public void init(ICElement element, IPathEntry[] cpathEntries) {
		setCElement(element);
		List<CPElement> newCPath = null;

		if (cpathEntries == null) {
			try {
				cpathEntries = getCProject().getRawPathEntries();
			} catch (CModelException e) {
			}
			
		}
		if (cpathEntries != null) {
			newCPath = getFilteredElements(cpathEntries, getFilteredTypes());
		} else {
			newCPath = new ArrayList<CPElement>();
		}
		initialize(element, newCPath);
	}

	abstract protected int[] getFilteredTypes(); // path type which block would like access to
	abstract protected int[] getAppliedFilteredTypes(); // path type which block modifies 

	abstract protected void initialize(ICElement element, List<CPElement> cPaths);

	protected ArrayList<CPElement> getFilteredElements(IPathEntry[] cPathEntries, int[] types) {
		ArrayList<CPElement> newCPath = new ArrayList<CPElement>();
		for (IPathEntry curr : cPathEntries) {
			if (contains(types, curr.getEntryKind())) {
				newCPath.add(CPElement.createFromExisting(curr, getCElement()));
			}
		}
		return newCPath;
	}

	// returns true if set contains elem
	private boolean contains(int[] set, int elem) {
		if (set == null)
			return false;
		for (int i = 0; i < set.length; ++i) {
			if (set[i] == elem)
				return true;
		}
		return false;
	}

	abstract protected List<CPElement> getCPaths();

	private String getEncodedSettings() {
		StringBuffer buf = new StringBuffer();
		
		List<CPElement> elements = getCPaths();
		int nElements = elements.size();
		buf.append('[').append(nElements).append(']');
		for (int i = 0; i < nElements; i++) {
			CPElement elem = elements.get(i);
			elem.appendEncodedSettings(buf);
		}
		return buf.toString();
	}

	public boolean hasChangesInDialog() {
		String currSettings = getEncodedSettings();
		return !currSettings.equals(fUserSettingsTimeStamp);
	}

	public boolean hasChangesInCPathFile() {
		IFile file = getProject().getFile(".cdtproject"); //$NON-NLS-1$
		return fFileTimeStamp != file.getModificationStamp();
	}

	public void initializeTimeStamps() {
		IFile file = getProject().getFile(".cdtproject"); //$NON-NLS-1$
		fFileTimeStamp = file.getModificationStamp();
		fUserSettingsTimeStamp = getEncodedSettings();
	}

	@Override
	abstract protected void addTabs();

	protected void setCElement(ICElement element) {
		fCurrCElement = element;
	}

	protected ICElement getCElement() {
		return fCurrCElement;
	}

	protected ICProject getCProject() {
		return fCurrCElement.getCProject();
	}

	@Override
	public IProject getProject() {
		return getCProject().getProject();
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

	@Override
	public void setCurrentPage(ICOptionPage page) {
		super.setCurrentPage(page);
		CPathBasePage newPage = (CPathBasePage) page;
		if (fCurrPage != null) {
			List<?> selection = fCurrPage.getSelection();
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
	@Override
	public void updateContainer() {
		update();
	}

	protected void updateBuildPathStatus() {
		List<CPElement> elements = getCPaths();
		IPathEntry[] entries = new IPathEntry[elements.size()];

		for (int i = elements.size() - 1; i >= 0; i--) {
			CPElement currElement = elements.get(i);
			entries[i] = currElement.getPathEntry();
		}

		ICModelStatus status = CoreModel.validatePathEntries(getCProject(), entries);
		if (!status.isOK()) {
			fBuildPathStatus.setError(status.getMessage());
			return;
		}
		fBuildPathStatus.setOK();
	}

	@Override
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

	@Override
	protected ICOptionPage getStartPage() {
		if (fCurrPage == null) {
			return super.getStartPage();
		}
		return fCurrPage;
	}

	protected void internalConfigureCProject(List<CPElement> cPathEntries, IProgressMonitor monitor) throws CoreException,
			InterruptedException {
		// 10 monitor steps to go

		monitor.worked(2);
		
		IPathEntry[] entries = getCProject().getRawPathEntries();
		
		List<IPathEntry> cpath = new ArrayList<IPathEntry>(cPathEntries.size() + entries.length);

		int[] applyTypes = getAppliedFilteredTypes(); 
		// create and set the paths
		for (int i = 0; i < cPathEntries.size(); i++) {
			CPElement entry = (cPathEntries.get(i));			
			for (int applyType : applyTypes) {
				if (entry.getEntryKind() == applyType) {
					IResource res = entry.getResource();
					if ((res instanceof IFolder) && !res.exists()) {
						CoreUtility.createFolder((IFolder) res, true, true, null);
					}
					cpath.add(entry.getPathEntry());
					break;
				}
			}
		}

		// add entries which do not match type being applyed by the ui block
		for (IPathEntry entrie : entries) {
			int pathType = entrie.getEntryKind();
			boolean found = false;
			for (int applyType : applyTypes) {
				if (pathType == applyType) {
					found = true;
					break;
				}
			}
			if (!found) {
				cpath.add(entrie);
			}
		}
		monitor.worked(1);

		getCProject().setRawPathEntries(cpath.toArray(new IPathEntry[cpath.size()]),
				new SubProgressMonitor(monitor, 7));
	}

	// -------- creation -------------------------------

	public void configureCProject(IProgressMonitor monitor) throws CoreException, InterruptedException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.setTaskName(CPathEntryMessages.CPathsBlock_operationdesc_c); 
		monitor.beginTask("", 10); //$NON-NLS-1$

		try {
			internalConfigureCProject(getCPaths(), monitor);
			initializeTimeStamps();
		} finally {
			monitor.done();
		}
	}
}
