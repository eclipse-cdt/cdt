/*******************************************************************************
 * Copyright (c) 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.cdtvariables;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.IStorableCdtVariables;
import org.eclipse.cdt.core.cdtvariables.IUserVarSupplier;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;

/**
 * Wrapper for UserDefinedMacroSupplier methods
 * Note that only config-level macros are supported.
 */
public class UserVarSupplier implements IUserVarSupplier {
	private static final int CTX = ICoreVariableContextInfo.CONTEXT_CONFIGURATION;
	private static UserVarSupplier sup = null;

	/**
	 * @return an instance of this class.
	 */
	public static UserVarSupplier getInstance() {
		if (sup == null)
			sup = new UserVarSupplier();
		return sup;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.cdtvariables.IUserVarSupplier#createMacro(org.eclipse.cdt.core.cdtvariables.ICdtVariable, int, java.lang.Object)
	 */
	@Override
	public ICdtVariable createMacro(ICdtVariable copy, ICConfigurationDescription contextData) {
		return CdtVariableManager.fUserDefinedMacroSupplier.createMacro(copy, CTX, contextData);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.cdtvariables.IUserVarSupplier#createMacro(java.lang.String, int, java.lang.String, java.lang.Object)
	 */
	@Override
	public ICdtVariable createMacro(String macroName, int type, String value,
			ICConfigurationDescription contextData) {
		return CdtVariableManager.fUserDefinedMacroSupplier.createMacro(macroName, type, value, CTX, contextData);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.cdtvariables.IUserVarSupplier#createMacro(java.lang.String, int, java.lang.String[], java.lang.Object)
	 */
	@Override
	public ICdtVariable createMacro(String macroName, int type, String[] value,
			ICConfigurationDescription contextData) {
		return CdtVariableManager.fUserDefinedMacroSupplier.createMacro(macroName, type, value, CTX, contextData);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.cdtvariables.IUserVarSupplier#deleteAll(java.lang.Object)
	 */
	@Override
	public void deleteAll(ICConfigurationDescription contextData) {
		CdtVariableManager.fUserDefinedMacroSupplier.deleteAll(CTX, contextData);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.cdtvariables.IUserVarSupplier#deleteMacro(java.lang.String, java.lang.Object)
	 */
	@Override
	public ICdtVariable deleteMacro(String name, ICConfigurationDescription contextData) {
		return CdtVariableManager.fUserDefinedMacroSupplier.deleteMacro(name, CTX, contextData);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.cdtvariables.IUserVarSupplier#getMacros(java.lang.Object)
	 */
	@Override
	public ICdtVariable[] getMacros(ICConfigurationDescription contextData) {
		return CdtVariableManager.fUserDefinedMacroSupplier.getMacros(CTX, contextData);
	}

	@Override
	public void setMacros(ICdtVariable[] m, ICConfigurationDescription contextData) {
		CdtVariableManager.fUserDefinedMacroSupplier.setMacros(m, CTX, contextData);
	}

	/* check whether variable is dynamic */
	@Override
	public boolean isDynamic(ICdtVariable v) {
		if (v instanceof EclipseVariablesVariableSupplier.EclipseVarMacro) {
			EclipseVariablesVariableSupplier.EclipseVarMacro evar =
				(EclipseVariablesVariableSupplier.EclipseVarMacro)v;
			if (evar.getVariable() instanceof IDynamicVariable)
				return true;
		}
		return false;
	}

	@Override
	public void storeWorkspaceVariables(boolean force) {
		CdtVariableManager.fUserDefinedMacroSupplier.storeWorkspaceVariables(force);
	}

	@Override
	public IStorableCdtVariables getWorkspaceVariablesCopy() {
		return CdtVariableManager.fUserDefinedMacroSupplier.getWorkspaceVariablesCopy();
	}

	@Override
	public boolean setWorkspaceVariables(IStorableCdtVariables vars)
			throws CoreException {
		if (vars instanceof StorableCdtVariables)
			return CdtVariableManager.fUserDefinedMacroSupplier.setWorkspaceVariables((StorableCdtVariables)vars);
		else
			return false;
	}
}