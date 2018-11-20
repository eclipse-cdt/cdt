/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
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

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;

/**
 * This interface represents the logic of how macro references should be resolved
 * The implementer of this interface is passed to the MacroResolver and
 * when the the resolve* methods of this interface are called by the MacroResolver
 * each time the macro reference is encountered in the string being resolved
 * to resolve the macro encountered macro reference
 *
 * @since 3.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IVariableSubstitutor {

	/**
	 * called to resolve to String the macro reference of the specified name
	 *
	 * @param macroName the macro name
	 * @return String
	 * @throws CdtVariableException
	 */
	public String resolveToString(String macroName) throws CdtVariableException;

	/**
	 * called to resolve to String-List the macro reference of the specified name
	 *
	 * @param macroName the macro name
	 * @return String[]
	 * @throws CdtVariableException
	 */
	public String[] resolveToStringList(String macroName) throws CdtVariableException;

	/**
	 * called to set the context type and context info to be used
	 *
	 * @param contextType the context type
	 * @param contextData the context data
	 * @throws BuildMacroException
	 */
	//	public void setMacroContextInfo(int contextType, Object contextData) throws BuildMacroException;

	/**
	 * returns the macro conttext info used
	 * @return IMacroContextInfo
	 */
	//	public IMacroContextInfo getMacroContextInfo();

}
