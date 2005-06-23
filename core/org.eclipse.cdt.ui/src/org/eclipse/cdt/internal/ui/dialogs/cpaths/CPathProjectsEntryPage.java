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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class CPathProjectsEntryPage extends CPathBasePage {

	private CheckedListDialogField fProjectsList;
	ICProject fCurrCProject;
	private ListDialogField fCPathList;

	public CPathProjectsEntryPage(ListDialogField cPathList) {
		super(CPathEntryMessages.getString("ProjectsEntryPage.title")); //$NON-NLS-1$
		setDescription(CPathEntryMessages.getString("ProjectsEntryPage.description")); //$NON-NLS-1$
		ProjectsListListener listener = new ProjectsListListener();

		String[] buttonLabels = new String[] { /* 0 */CPathEntryMessages.getString("ProjectsEntryPage.projects.checkall.button"), //$NON-NLS-1$
		/* 1 */CPathEntryMessages.getString("ProjectsEntryWorkbookPage.projects.uncheckall.button")}; //$NON-NLS-1$

		fProjectsList = new CheckedListDialogField(null, buttonLabels, new CPElementLabelProvider());
		fProjectsList.setDialogFieldListener(listener);
		fProjectsList.setLabelText(CPathEntryMessages.getString("ProjectsEntryPage.projects.label")); //$NON-NLS-1$
		fProjectsList.setCheckAllButtonIndex(0);
		fProjectsList.setUncheckAllButtonIndex(1);

		fProjectsList.setViewerSorter(new CPElementSorter());
		fCPathList = cPathList;
	}

	public Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);

		Composite composite = new Composite(parent, SWT.NONE);

		setControl(composite);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { fProjectsList}, true);
		LayoutUtil.setHorizontalGrabbing(fProjectsList.getListControl(null));

		int buttonBarWidth = converter.convertWidthInCharsToPixels(24);
		fProjectsList.setButtonsMinWidth(buttonBarWidth);
		
		CUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(composite, ICHelpContextIds.PROJECT_PATHS_PROJECTS);
	}

	private class ProjectsListListener implements IDialogFieldListener {

		// ---------- IDialogFieldListener --------

		public void dialogFieldChanged(DialogField field) {
			if (fCurrCProject != null) {
				// already initialized
				updateCPathList();
			}
		}
	}

	public void init(ICProject cproject) {
		updateProjectsList(cproject);
	}

	void updateProjectsList(ICProject currCProject) {
		ICModel cmodel = currCProject.getCModel();

		List projects = new ArrayList();
		final List checkedProjects = new ArrayList();
		try {
			ICProject[] cprojects = cmodel.getCProjects();
			
			// a vector remembering all projects that dont have to be added anymore
			List existingProjects = new ArrayList(cprojects.length);
			existingProjects.add(currCProject.getProject());
			
			// add the projects-cpentries that are already on the C Path
			List cpelements = fCPathList.getElements();
			for (int i = cpelements.size() - 1; i >= 0; i--) {
				CPElement cpelem = (CPElement) cpelements.get(i);
				if (isEntryKind(cpelem.getEntryKind())) {
					existingProjects.add(cpelem.getResource());
					projects.add(cpelem);
					checkedProjects.add(cpelem);
				}
			}
			
			for (int i = 0; i < cprojects.length; i++) {
				IProject proj = cprojects[i].getProject();
				if (!existingProjects.contains(proj)) {
					projects.add(new CPElement(fCurrCProject, IPathEntry.CDT_PROJECT, proj.getFullPath(), proj));
				}
			}
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e);
		}
		fProjectsList.setElements(projects);
		fProjectsList.setCheckedElements(checkedProjects);
		fCurrCProject = currCProject;
	}

	void updateCPathList() {
		List projelements = fProjectsList.getCheckedElements();

		boolean remove = false;
		List pelements = fCPathList.getElements();
		// backwards, as entries will be deleted
		for (int i = pelements.size() - 1; i >= 0; i--) {
			CPElement pe = (CPElement) pelements.get(i);
			if (isEntryKind(pe.getEntryKind())) {
				if (!projelements.remove(pe)) {
					pelements.remove(i);
					remove = true;
				}
			}
		}
		for (int i = 0; i < projelements.size(); i++) {
			pelements.add(projelements.get(i));
		}
		if (remove || (projelements.size() > 0)) {
			fCPathList.setElements(pelements);
		}
	}

	/*
	 * @see BuildPathBasePage#getSelection
	 */
	public List getSelection() {
		return fProjectsList.getSelectedElements();
	}

	/*
	 * @see BuildPathBasePage#setSelection
	 */
	public void setSelection(List selElements) {
		fProjectsList.selectElements(new StructuredSelection(selElements));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage#isEntryKind(int)
	 */
	public boolean isEntryKind(int kind) {
		return kind == IPathEntry.CDT_PROJECT;
	}
}
