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

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;



/**
 * The ExecutableFilter is a filter used to determine whether
 * an executable is shown
 */
public class ExecutableFilter extends ViewerFilter {
	
	/* (non-Javadoc)
	 * Method declared on ViewerFilter.
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IBinary) {
			IBinary bin = (IBinary)element;
			if (! (parentElement instanceof IBinaryContainer)) {
				if (bin.isExecutable()) {
					return false;
				}
			}
		}
		return true;
	}
}
