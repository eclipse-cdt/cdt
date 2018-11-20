/*******************************************************************************
 * Copyright (c) 2008, 2009 Intel Corporation and others.
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
package org.eclipse.cdt.core.cdtvariables;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.runtime.CoreException;

/**
 * Public interface to access to UserVarSupplier class methods
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IUserVarSupplier {
	ICdtVariable createMacro(ICdtVariable copy, ICConfigurationDescription contextData);

	ICdtVariable createMacro(String macroName, int type, String value, ICConfigurationDescription contextData);

	ICdtVariable createMacro(String macroName, int type, String value[], ICConfigurationDescription contextData);

	void deleteAll(ICConfigurationDescription contextData);

	ICdtVariable deleteMacro(String name, ICConfigurationDescription contextData);

	ICdtVariable[] getMacros(ICConfigurationDescription contextData);

	IStorableCdtVariables getWorkspaceVariablesCopy();

	boolean isDynamic(ICdtVariable v);

	void setMacros(ICdtVariable m[], ICConfigurationDescription contextData);

	boolean setWorkspaceVariables(IStorableCdtVariables vars) throws CoreException;

	void storeWorkspaceVariables(boolean force);
}
