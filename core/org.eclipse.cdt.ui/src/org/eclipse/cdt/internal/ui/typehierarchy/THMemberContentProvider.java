/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.typehierarchy;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class THMemberContentProvider implements IStructuredContentProvider {
	private static final Object[] NO_CHILDREN = new Object[0];
	private THHierarchyModel fModel;

	public THMemberContentProvider() {
	}

	@Override
	final public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fModel = (THHierarchyModel) newInput;
	}

	@Override
	public void dispose() {
		fModel = null;
	}

	@Override
	final public Object[] getElements(Object inputElement) {
		if (fModel == null) {
			return NO_CHILDREN;
		}
		return fModel.getMembers();
	}
}
