/*******************************************************************************
 * Copyright (c) 2023 ArSysOp.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;

public final class ResolveProjectScope implements Function<Object, Optional<ProjectScope>> {

	private final ResolveProject project;

	public ResolveProjectScope(IWorkspace workspace) {
		this.project = new ResolveProject(workspace);
	}

	@Override
	public Optional<ProjectScope> apply(Object context) {
		return project.apply(context).map(ProjectScope::new);
	}

}
