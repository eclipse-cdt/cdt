/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;

public abstract class BuildCdtVariablesSupplierBase implements IBuildMacroSupplier {

	@Override
	public abstract IBuildMacro getMacro(String macroName, int contextType,
			Object contextData);

	@Override
	public abstract IBuildMacro[] getMacros(int contextType, Object contextData);

	@Override
	public ICdtVariable getVariable(String macroName,
			IMacroContextInfo context) {
		return getMacro(macroName, context.getContextType(), context.getContextData());
	}

	@Override
	public ICdtVariable[] getVariables(IMacroContextInfo context) {
		return getMacros(context.getContextType(), context.getContextData());
	}

	@Override
	public ICdtVariable getVariable(String macroName,
			IVariableContextInfo context) {
		if(context instanceof IMacroContextInfo){
			IMacroContextInfo info = (IMacroContextInfo)context;
			return getMacro(macroName, info.getContextType(), info.getContextData());
		}
		return null;
	}

	@Override
	public ICdtVariable[] getVariables(IVariableContextInfo context) {
		if(context instanceof IMacroContextInfo){
			IMacroContextInfo info = (IMacroContextInfo)context;
			return getMacros(info.getContextType(), info.getContextData());
		}
		return null;
	}

}
