/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represent a preprocessor function-style macro definition. e.g.
 * <pre>
 * #define ABC(def) GHI
 * </pre>
 * Note: macros that are expanded as parameters to function style macros are not captured in this
 * abstraction.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTPreprocessorFunctionStyleMacroDefinition extends IASTPreprocessorMacroDefinition {
	/**
	 * This property represents the relationship between a function style macro
	 * definition and one of its parameters.
	 */
	public static final ASTNodeProperty PARAMETER = new ASTNodeProperty(
			"IASTPreprocessorFunctionStyleMacroDefinition.PARAMETER - Function Macro Parameter"); //$NON-NLS-1$

	/**
	 * Returns the macro parameters.
	 *
	 * @return <code>IASTFunctionStyleMacroParameter[]</code> parameters
	 */
	public IASTFunctionStyleMacroParameter[] getParameters();

	/**
	 * Adds a function-style macro parameter.
	 *
	 * @param parm the parameter to add
	 */
	public void addParameter(IASTFunctionStyleMacroParameter parm);
}
