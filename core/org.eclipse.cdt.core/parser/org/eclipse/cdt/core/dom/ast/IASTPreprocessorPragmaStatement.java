/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
 *    Emanuel Graf (IFS)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Represents a #pragma directive or a pragma operator.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTPreprocessorPragmaStatement extends IASTPreprocessorStatement {
	/**
	 * Returns the pragma message.
	 */
	public char[] getMessage();

	/**
	 * Returns whether this uses the pragma operator syntax, e.g: <code>_Pragma("once")</code>
	 * @since 5.2
	 */
	public boolean isPragmaOperator();
}
