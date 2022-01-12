/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.model;

/**
 * Represents a field declared in a type.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMacro extends ICElement, ISourceManipulation, ISourceReference {
	/**
	 * Returns the Identifier List.
	 * @return String
	 */
	String getIdentifierList();

	/**
	 * Returns the Token Sequence.
	 * @return String
	 */
	String getTokenSequence();

	/**
	 * Returns true if this macro is of function style.
	 * @since 5.3
	 */
	boolean isFunctionStyle();
}
