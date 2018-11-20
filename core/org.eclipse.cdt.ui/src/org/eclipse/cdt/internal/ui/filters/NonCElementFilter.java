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
package org.eclipse.cdt.internal.ui.filters;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filters out all non-C elements.
 */
public class NonCElementFilter extends ViewerFilter {

	/**
	 * Returns the result of this filter, when applied to the
	 * given inputs.
	 *
	 * @return Returns true if element should be included in filtered set
	 */
	@Override
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof ICElement)
			return true;

		if (element instanceof IProject) {
			IProject project = (IProject) element;
			if (!project.isOpen() || CoreModel.hasCNature(project)) {
				return true;
			}
			return false;
		} else if (element instanceof IResource) {
			IProject project = ((IResource) element).getProject();
			return project == null || !project.isOpen();
		}

		// Exclude all IStorage elements which are neither C elements nor resources
		if (element instanceof IStorage)
			return false;

		return true;
	}
}
