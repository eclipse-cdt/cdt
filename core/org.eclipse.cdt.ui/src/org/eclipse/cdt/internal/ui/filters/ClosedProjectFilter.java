/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.filters;


import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;


/**
 * Filters closed projects
 */
public class ClosedProjectFilter extends ViewerFilter {

	/*
	 * @see ViewerFilter
	 */
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof ICElement) 
			return ((ICElement)element).getCProject().getProject().isOpen();
		if (element instanceof IResource)
			return ((IResource)element).getProject().isOpen();
		return true;
	}
}
