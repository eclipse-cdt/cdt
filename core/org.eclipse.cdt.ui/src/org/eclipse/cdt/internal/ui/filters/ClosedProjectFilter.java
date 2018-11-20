/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.filters;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filters closed projects
 */
public class ClosedProjectFilter extends ViewerFilter {
	@Override
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof ICElement)
			return ((ICElement) element).getCProject().getProject().isOpen();
		if (element instanceof IResource)
			return ((IResource) element).getProject().isOpen();
		return true;
	}
}
