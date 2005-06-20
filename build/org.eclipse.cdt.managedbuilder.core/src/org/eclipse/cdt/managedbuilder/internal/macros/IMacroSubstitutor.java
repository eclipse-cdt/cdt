/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;


/**
 * This interface represents the logic of how macro references should be resolved
 * The implementer of this interface is passed to the MacroResolver and
 * when the the resolve* methods of this interface are called by the MacroResolver 
 * each time the macro reference is encountered in the string being resolved
 * to resolve the macro encountered macro reference
 * 
 * @since 3.0
 */
public interface IMacroSubstitutor {
	
	/**
	 * called to resolve to String the macro reference of the specified name
	 * 
	 * @param macroName the macro name
	 * @return String
	 * @throws BuildMacroException
	 */
	public String resolveToString(String macroName) throws BuildMacroException;

	/**
	 * called to resolve to String-List the macro reference of the specified name
	 * 
	 * @param macroName the macro name
	 * @return String[]
	 * @throws BuildMacroException
	 */
	public String[] resolveToStringList(String macroName) throws BuildMacroException;
	
	/**
	 * called to set the context type and context info to be used
	 * 
	 * @param contextType the context type
	 * @param contextData the context data
	 * @throws BuildMacroException
	 */
	public void setMacroContextInfo(int contextType, Object contextData) throws BuildMacroException;

	/**
	 * returns the macro conttext info used 
	 * @return IMacroContextInfo
	 */
	public IMacroContextInfo getMacroContextInfo();

}
