/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
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
 *     Wind River Systems - adapted to work with platform Modules view (bug 210558)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.modules;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Modules view detail pane factory.
 */
public class ModuleDetailPaneFactory implements IDetailPaneFactory {

	public static final String MODULE_DETAIL_PANE_ID = ModuleDetailPane.ID;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#createDetailsArea(java.lang.String)
	 */
	@Override
	public IDetailPane createDetailPane(String id) {
		return new ModuleDetailPane();
	}

	@Override
	public Set<String> getDetailPaneTypes(IStructuredSelection selection) {
		Set<String> possibleIDs = new HashSet<>(1);
		possibleIDs.add(ModuleDetailPane.ID);
		return possibleIDs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPaneFactory#getDefaultDetailPane(java.util.Set, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public String getDefaultDetailPane(IStructuredSelection selection) {
		// Return null so that any contributed detail pane can override the default
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getName(java.lang.String)
	 */
	@Override
	public String getDetailPaneName(String id) {
		if (id.equals(ModuleDetailPane.ID)) {
			return ModuleDetailPane.NAME;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getDescription(java.lang.String)
	 */
	@Override
	public String getDetailPaneDescription(String id) {
		if (id.equals(ModuleDetailPane.ID)) {
			return ModuleDetailPane.DESCRIPTION;
		}
		return null;
	}

}
