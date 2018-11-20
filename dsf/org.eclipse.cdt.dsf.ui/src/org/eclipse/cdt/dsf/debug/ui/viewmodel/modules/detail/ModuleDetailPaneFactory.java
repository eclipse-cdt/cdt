/*******************************************************************************
 *  Copyright (c) 2006, 2013 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     Ericsson AB		  - Modules view for DSF implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.modules.detail;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ModuleDetailPaneFactory implements IDetailPaneFactory {
	public static final String MODULE_DETAIL_PANE_ID = ModuleDetailPane.ID;

	@Override
	public IDetailPane createDetailPane(String paneID) {
		return new ModuleDetailPane();
	}

	@Override
	public String getDefaultDetailPane(IStructuredSelection selection) {
		return null;
	}

	@Override
	public String getDetailPaneDescription(String paneID) {
		if (paneID.equals(ModuleDetailPane.ID)) {
			return ModuleDetailPane.DESCRIPTION;
		}
		return null;
	}

	@Override
	public String getDetailPaneName(String paneID) {
		if (paneID.equals(ModuleDetailPane.ID)) {
			return ModuleDetailPane.NAME;
		}
		return null;
	}

	@Override
	public Set getDetailPaneTypes(IStructuredSelection selection) {
		Set<String> possibleIDs = new HashSet<>(1);
		possibleIDs.add(ModuleDetailPane.ID);
		return possibleIDs;
	}

}
