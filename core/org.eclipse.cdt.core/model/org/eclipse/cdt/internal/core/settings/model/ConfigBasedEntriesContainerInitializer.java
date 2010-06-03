/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.PathEntryContainerInitializer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class ConfigBasedEntriesContainerInitializer extends
		PathEntryContainerInitializer {

	@Override
	public void initialize(IPath containerPath, ICProject project)
			throws CoreException {
		ConfigBasedPathEntryContainer container = ConfigBasedPathEntryStore.createContainer(project.getProject());
		CoreModel.setPathEntryContainer(new ICProject[]{project}, container, null);
	}

}
