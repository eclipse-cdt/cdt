/*******************************************************************************
 * Copyright (c) 2005, 2008 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.cdtvariables;

import java.util.Map;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.SupplierBasedCdtVariableSubstitutor;

/**
 * This substituter resolves all macro references
 *
 * @see org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor
 * @since 3.0
 */
public class CoreVariableSubstitutor extends SupplierBasedCdtVariableSubstitutor {

	public CoreVariableSubstitutor(IVariableContextInfo contextInfo, String inexistentMacroValue, String listDelimiter,
			Map<?, ?> delimiterMap, String incorrectlyReferencedMacroValue) {
		super(contextInfo, inexistentMacroValue, listDelimiter, delimiterMap, incorrectlyReferencedMacroValue);
		// TODO Auto-generated constructor stub
	}

	public CoreVariableSubstitutor(IVariableContextInfo contextInfo, String inexistentMacroValue,
			String listDelimiter) {
		super(contextInfo, inexistentMacroValue, listDelimiter);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.IMacroSubstitutor#setMacroContextInfo(int, java.lang.Object)
	 */
	public void setMacroContextInfo(int contextType, Object contextData) throws CdtVariableException {
		setMacroContextInfo(getMacroContextInfo(contextType, contextData));
	}

	protected IVariableContextInfo getMacroContextInfo(int contextType, Object contextData) {
		return new DefaultVariableContextInfo(contextType, contextData);
	}

}
