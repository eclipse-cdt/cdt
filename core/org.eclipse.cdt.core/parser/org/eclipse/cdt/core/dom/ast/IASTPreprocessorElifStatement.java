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
 *    Emanuel Graf (IFS)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Represents a #elif preprocessor statement.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTPreprocessorElifStatement extends IASTPreprocessorStatement {

	/**
	 * Was this #elif branch taken?
	 * 
	 * @return boolean
	 */
	public boolean taken();

	/**
	 * The condition of the elif.
	 * 
	 * @return the Condition
	 */
	public char[] getCondition();

}
