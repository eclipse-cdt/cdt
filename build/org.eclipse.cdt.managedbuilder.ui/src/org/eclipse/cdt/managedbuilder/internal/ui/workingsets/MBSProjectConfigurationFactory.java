/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.ui.workingsets;

import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.ui.workingsets.IWorkingSetConfiguration;
import org.eclipse.cdt.internal.ui.workingsets.IWorkingSetProjectConfigurationFactory;
import org.eclipse.cdt.internal.ui.workingsets.WorkingSetProjectConfiguration;
import org.eclipse.cdt.internal.ui.workingsets.WorkspaceSnapshot.ProjectState;
import org.eclipse.core.resources.IProject;

/**
 * Working set project configuration factory for MBS projects.
 * 
 * @author Christian W. Damus (cdamus)
 * 
 * @since 6.0
 * 
 */
public class MBSProjectConfigurationFactory extends
		IWorkingSetProjectConfigurationFactory.Registry.Default {

	/**
	 * Initializes me.
	 */
	public MBSProjectConfigurationFactory() {
		super();
	}

	@Override
	protected WorkingSetProjectConfiguration createProjectConfiguration(
			IWorkingSetConfiguration parent) {

		return new MBSProjectConfiguration(parent);
	}

	@Override
	public ProjectState createProjectState(IProject project,
			ICProjectDescription desc) {
		
		return new MBSProjectState(project, desc);
	}
}
