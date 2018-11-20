/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents the name of a function style macro parameter. This
 * is not an IASTName, as there are not any bindings for
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTFunctionStyleMacroParameter extends IASTNode {

	/**
	 * Constant <code>EMPTY_PARAMETER_ARRAY</code> is used to return anempty
	 * array.
	 */
	public static final IASTFunctionStyleMacroParameter[] EMPTY_PARAMETER_ARRAY = new IASTFunctionStyleMacroParameter[0];

	/**
	 * Get the parameter name.
	 *
	 * @return String name
	 */
	public String getParameter();

	/**
	 * Set the parameter name.
	 *
	 * @param value
	 *            String
	 */
	public void setParameter(String value);

}
