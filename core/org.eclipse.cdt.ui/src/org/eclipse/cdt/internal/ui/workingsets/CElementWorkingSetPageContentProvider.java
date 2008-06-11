/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.workingsets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.BaseCElementContentProvider;

class CElementWorkingSetPageContentProvider extends BaseCElementContentProvider {

	/*
	 * @see org.eclipse.cdt.internal.ui.BaseCElementContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object element) {
		if (element instanceof IWorkspaceRoot) {
			IWorkspaceRoot root = (IWorkspaceRoot)element;
			IProject[] projects = root.getProjects();
			List<ICProject> list = new ArrayList<ICProject>(projects.length);
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
