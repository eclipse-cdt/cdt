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
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents a preprocessor #undef statement.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTPreprocessorUndefStatement extends IASTPreprocessorStatement {

	/**
	 * Returns the reference to the macro, or <code>null</code>.
	 */
	public IASTName getMacroName();

	/**
	 * Returns whether this macro definition occurs in active code.
	 * @since 5.1
	 */
	@Override
	public boolean isActive();
}
