/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.numberformat.detail;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.detailsupport.MessagesForDetailPane;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 *  This provides a simple Detail Pane Factory for the core debug views for DSF.
 */

public class NumberFormatDetailPaneFactory implements IDetailPaneFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#createDetailsArea(java.lang.String)
	 */
	@Override
	public IDetailPane createDetailPane(String id) {
		return new NumberFormatDetailPane();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getDetailsTypes(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Set getDetailPaneTypes(IStructuredSelection selection) {
		Set<String> possibleIDs = new HashSet<>(1);
		possibleIDs.add(NumberFormatDetailPane.ID);
		return possibleIDs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPaneFactory#getDefaultDetailPane(java.util.Set, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public String getDefaultDetailPane(IStructuredSelection selection) {
		return null; // Allow competing detail pane factories to override this one
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getName(java.lang.String)
	 */
	@Override
	public String getDetailPaneName(String id) {
		if (id.equals(NumberFormatDetailPane.ID)) {
			return MessagesForDetailPane.NumberFormatDetailPane_Name;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getDescription(java.lang.String)
	 */
	@Override
	public String getDetailPaneDescription(String id) {
		if (id.equals(NumberFormatDetailPane.ID)) {
			return MessagesForDetailPane.NumberFormatDetailPane_Description;
		}
		return null;
	}

}
