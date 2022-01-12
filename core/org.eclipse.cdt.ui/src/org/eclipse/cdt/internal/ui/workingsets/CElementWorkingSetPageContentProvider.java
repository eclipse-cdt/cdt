/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.workingsets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.BaseCElementContentProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;

class CElementWorkingSetPageContentProvider extends BaseCElementContentProvider {

	/*
	 * @see org.eclipse.cdt.internal.ui.BaseCElementContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object element) {
		if (element instanceof IWorkspaceRoot) {
			IWorkspaceRoot root = (IWorkspaceRoot) element;
			IProject[] projects = root.getProjects();
			List<ICProject> list = new ArrayList<>(projects.length);
			for (int i = 0; i < projects.length; i++) {
				if (CoreModel.hasCNature(projects[i])) {
					list.add(CoreModel.getDefault().create(projects[i]));
				}
			}
			return list.toArray();
		}
		return super.getChildren(element);
	}
}
