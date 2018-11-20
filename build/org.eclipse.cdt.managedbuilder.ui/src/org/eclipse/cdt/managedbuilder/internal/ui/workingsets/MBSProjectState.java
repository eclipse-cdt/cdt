/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.ui.workingsets;

import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.ui.workingsets.WorkspaceSnapshot.ProjectState;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * A managed-build implementation of the workspace snapshot project state. It
 * knows how to build the selected configuration without activating it.
 *
 * @author Christian W. Damus (cdamus)
 *
 * @since 6.0
 */
class MBSProjectState extends ProjectState {

	/**
	 * Initializes me with my project and its description.
	 *
	 * @param project
	 *            my project
	 * @param desc
	 *            its description
	 */
	public MBSProjectState(IProject project, ICProjectDescription desc) {
		super(project, desc);
	}

	@Override
	protected IStatus build(String configID, IProgressMonitor monitor) {
		IStatus result = MBSProjectConfiguration.build(getProject(), getConfiguration(configID), monitor);

		if (result.isOK()) {
			built(configID);
		}

		return result;
	}
}
