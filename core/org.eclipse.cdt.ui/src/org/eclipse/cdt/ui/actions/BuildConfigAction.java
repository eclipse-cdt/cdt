/*******************************************************************************
 * Copyright (c) 2007, 2008 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.BuildAction;

/**
 * Action which builds the active configurations of the selected projects.
 */
public class BuildConfigAction extends ChangeConfigAction {

	private BuildAction buildAction;

	/**
	 * Constructs the action.
	 * @param projects List of selected managed-built projects
	 * @param configName Build configuration name
	 * @param accel Number to be used as accelerator
	 */
	public BuildConfigAction(HashSet<IProject> projects, String configName, String displayName, int accel,
			BuildAction buildAction) {
		super(projects, configName, displayName, accel);
		this.buildAction = buildAction;
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		super.run();
		buildAction.selectionChanged(new StructuredSelection(fProjects.toArray()));
		buildAction.run();
	}
}
