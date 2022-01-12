/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.navigator;

import org.eclipse.cdt.internal.ui.viewsupport.ProblemsLabelDecorator;
import org.eclipse.core.resources.IResource;

/**
 * A {@link ProblemsLabelDecorator} optimized for use with the Common Navigator.
 *
 * @since 4.0
 */
public class CNavigatorProblemsLabelDecorator extends ProblemsLabelDecorator {

	/**
	 * Create a problems label decorator for the Common Navigator.
	 */
	public CNavigatorProblemsLabelDecorator() {
		super(null);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.viewsupport.ProblemsLabelDecorator#fireProblemsChanged(org.eclipse.core.resources.IResource[], boolean)
	 */
	@Override
	protected void fireProblemsChanged(IResource[] changedResources, boolean isMarkerChange) {
		// performance: if the number of changed resources is large, it is faster
		// to trigger a viewer refresh by setting changedResources to null
		if (changedResources != null && changedResources.length > 500) {
			changedResources = null;
		}
		super.fireProblemsChanged(changedResources, isMarkerChange);
	}
}
