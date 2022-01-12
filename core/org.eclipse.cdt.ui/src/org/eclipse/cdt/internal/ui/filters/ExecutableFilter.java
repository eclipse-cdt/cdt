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
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IBinary) {
			IBinary bin = (IBinary) element;
			if (!(parentElement instanceof IBinaryContainer)) {
				if (bin.isExecutable()) {
					return false;
				}
			}
		}
		return true;
	}
}
