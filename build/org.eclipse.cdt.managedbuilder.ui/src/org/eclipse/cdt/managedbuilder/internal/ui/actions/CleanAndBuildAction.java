/*******************************************************************************
 * Copyright (c) 2010, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.actions;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action which cleans and rebuilds selected configurations. User selects
 * which configurations to rebuild via {@link CleanAndBuildDialog}.
 */
public class CleanAndBuildAction implements IObjectActionDelegate {
	private ArrayList<IProject> projects = null;

	@Override
	public void run(IAction action) {
		if (projects!=null) {
			CleanAndBuildDialog dialog = new CleanAndBuildDialog(projects.toArray(new IProject[projects.size()]));
			dialog.open();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		projects = getSelectedCdtProjects(selection);
		action.setEnabled(projects.size() > 0);
	}


	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @return list of CDT projects from the selection.
	 *
	 * @param selection - selected items.
	 */
	public static ArrayList<IProject> getSelectedCdtProjects(ISelection selection) {
		ArrayList<IProject> projects = new ArrayList<IProject>();

		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			Object[] selected = ((IStructuredSelection)selection).toArray();
			if (selected.length > 0) {
				for (Object sel : selected) {
					IProject prj = null;
					if (sel instanceof IProject)
						prj = (IProject)sel;
					else if (sel instanceof ICProject)
						prj = ((ICProject)sel).getProject();

					if (prj != null && CoreModel.getDefault().isNewStyleProject(prj)) {
						projects.add(prj);
					}
				}
			}
		}
		return projects;
	}


}
