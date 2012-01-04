/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
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
		if (paneID.equals(ModuleDetailPane.ID)){
			return ModuleDetailPane.DESCRIPTION;
		}
		return null;
	}

	@Override
	public String getDetailPaneName(String paneID) {
		if (paneID.equals(ModuleDetailPane.ID)){
			return ModuleDetailPane.NAME;
		}
		return null;
	}

	@Override
	public Set<?> getDetailPaneTypes(IStructuredSelection selection) {
		Set<String> possibleIDs = new HashSet<String>(1);
		possibleIDs.add(ModuleDetailPane.ID);
		return possibleIDs;
	}

}
