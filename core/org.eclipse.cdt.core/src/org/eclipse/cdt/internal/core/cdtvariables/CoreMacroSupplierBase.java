/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
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

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.utils.cdtvariables.ICdtVariableSupplier;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;

public abstract class CoreMacroSupplierBase implements ICdtVariableSupplier {

	@Override
	public ICdtVariable getVariable(String macroName, IVariableContextInfo context) {
		if (context instanceof ICoreVariableContextInfo) {
			ICoreVariableContextInfo info = (ICoreVariableContextInfo) context;
			return getMacro(macroName, info.getContextType(), info.getContextData());
		}
		return null;
	}

	protected abstract ICdtVariable getMacro(String name, int type, Object data);

	@Override
	public ICdtVariable[] getVariables(IVariableContextInfo context) {
		if (context instanceof ICoreVariableContextInfo) {
			ICoreVariableContextInfo info = (ICoreVariableContextInfo) context;
			return getMacros(info.getContextType(), info.getContextData());
		}
		return null;
	}

	protected abstract ICdtVariable[] getMacros(int type, Object data);

}
