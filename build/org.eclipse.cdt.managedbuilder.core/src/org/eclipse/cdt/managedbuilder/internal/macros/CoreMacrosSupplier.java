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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;

public class CoreMacrosSupplier extends BuildCdtVariablesSupplierBase {
	private ICConfigurationDescription fCfgDes;
	private ICdtVariableManager fMngr; 
	CoreMacrosSupplier(ICConfigurationDescription cfgDes){
		fCfgDes = cfgDes;
		fMngr = CCorePlugin.getDefault().getCdtVariableManager();
	}

	@Override
	public IBuildMacro getMacro(String macroName, int contextType,
			Object contextData) {
		return BuildMacroProvider.wrap(getVariable(macroName, null));
	}

	@Override
	public IBuildMacro[] getMacros(int contextType, Object contextData) {
		return BuildMacroProvider.wrap(getVariables(null));
	}

	@Override
	public ICdtVariable getVariable(String macroName, IMacroContextInfo context) {
		return fMngr.getVariable(macroName, fCfgDes);
	}

	@Override
	public ICdtVariable[] getVariables(IMacroContextInfo context) {
		return fMngr.getVariables(fCfgDes);
	}
}
