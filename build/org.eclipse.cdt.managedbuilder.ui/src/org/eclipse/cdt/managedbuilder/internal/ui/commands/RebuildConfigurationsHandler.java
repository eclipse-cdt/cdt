/*******************************************************************************
 * Copyright (c) 2010, 2014 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.util.AbstractResourceActionHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Implementation of the command that cleans and rebuilds selected configurations.
 * User selects which configurations to rebuild via {@link RebuildConfigurationsDialog}.
 */
public class RebuildConfigurationsHandler extends AbstractResourceActionHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<IProject> projects = getSelectedCdtProjects(getSelection(event));
		if (!projects.isEmpty()) {
			RebuildConfigurationsDialog dialog = new RebuildConfigurationsDialog(
					projects.toArray(new IProject[projects.size()]));
			dialog.open();
		}
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		super.setEnabled(evaluationContext);
		setBaseEnabled(!RebuildConfigurationsHandler.getSelectedCdtProjects(getSelection()).isEmpty());
	}

	/**
	 * Returns a list of CDT projects from the selection.
	 */
	public static List<IProject> getSelectedCdtProjects(IStructuredSelection selection) {
		if (selection.isEmpty())
			return Collections.emptyList();

		List<IProject> projects = new ArrayList<>();

		for (Object element : selection.toArray()) {
			IProject project = null;
			if (element instanceof IProject) {
				project = (IProject) element;
			} else if (element instanceof ICProject) {
				project = ((ICProject) element).getProject();
			}

			if (project != null && CoreModel.getDefault().isNewStyleProject(project)) {
				projects.add(project);
			}
		}
		return projects;
	}
}
