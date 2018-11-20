/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This represents the definition of a macro.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTPreprocessorMacroDefinition extends IASTPreprocessorStatement, IASTNameOwner {
	/**
	 * <code>MACRO_NAME</code> describes the relationship between a macro
	 * definition and it's name.
	 */
	public static final ASTNodeProperty MACRO_NAME = new ASTNodeProperty(
			"IASTPreprocessorMacroDefinition.MACRO_NAME - Macro Name"); //$NON-NLS-1$

	/**
	 * Get the macro name.
	 */
	public IASTName getName();

	/**
	 * Returns the macro expansion, or an empty string for dynamic style macros.
	 */
	public String getExpansion();

	/**
	 * Returns the location of the macro expansion, or <code>null</code> if not supported.
	 * For built-in macros the location will always be null.
	 * @since 5.0
	 */
	public IASTFileLocation getExpansionLocation();

	/**
	 * Returns whether this macro definition occurs in active code.
	 * @since 5.1
	 */
	@Override
	public boolean isActive();
}
