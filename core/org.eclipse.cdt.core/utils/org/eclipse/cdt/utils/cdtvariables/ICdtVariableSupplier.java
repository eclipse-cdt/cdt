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
package org.eclipse.cdt.utils.cdtvariables;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;

/**
 *
 * @since 3.0
 */
public interface ICdtVariableSupplier {

	/**
	 * @param macroName macro name
	 * @return IBuildMacro
	 */
	public ICdtVariable getVariable(String macroName, IVariableContextInfo context);

	/**
	 * @return IBuildMacro[]
	 */
	public ICdtVariable[] getVariables(IVariableContextInfo context);
}
