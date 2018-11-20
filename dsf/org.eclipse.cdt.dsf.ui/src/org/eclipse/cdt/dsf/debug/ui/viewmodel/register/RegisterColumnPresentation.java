/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.register;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 *
 */
public class RegisterColumnPresentation implements IColumnPresentation {

	public static final String ID = DsfUIPlugin.PLUGIN_ID + ".REGISTERS_COLUMN_PRESENTATION_ID"; //$NON-NLS-1$

	@Override
	public void init(IPresentationContext context) {
	}

	@Override
	public void dispose() {
	}

	// @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getAvailableColumns()
	@Override
	public String[] getAvailableColumns() {
		return new String[] { IDebugVMConstants.COLUMN_ID__NAME, IDebugVMConstants.COLUMN_ID__TYPE,
				IDebugVMConstants.COLUMN_ID__VALUE, IDebugVMConstants.COLUMN_ID__DESCRIPTION, };
	}

	// @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getHeader(java.lang.String)
	@Override
	public String getHeader(String id) {
		if (IDebugVMConstants.COLUMN_ID__NAME.equals(id)) {
			return MessagesForRegisterVM.RegisterColumnPresentation_name;
		} else if (IDebugVMConstants.COLUMN_ID__TYPE.equals(id)) {
			return MessagesForRegisterVM.RegisterColumnPresentation_type;
		} else if (IDebugVMConstants.COLUMN_ID__VALUE.equals(id)) {
			return MessagesForRegisterVM.RegisterColumnPresentation_value;
		} else if (IDebugVMConstants.COLUMN_ID__DESCRIPTION.equals(id)) {
			return MessagesForRegisterVM.RegisterColumnPresentation_description;
		}
		return null;
	}

	// @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getId()
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ImageDescriptor getImageDescriptor(String id) {
		return null;
	}

	// @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#getInitialColumns()
	@Override
	public String[] getInitialColumns() {
		return new String[] { IDebugVMConstants.COLUMN_ID__NAME, IDebugVMConstants.COLUMN_ID__VALUE,
				IDebugVMConstants.COLUMN_ID__DESCRIPTION };
	}

	// @see org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation#isOptional()
	@Override
	public boolean isOptional() {
		return true;
	}

}
