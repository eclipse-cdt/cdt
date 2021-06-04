/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.core.model;

/**
 * Represents a pragma statement.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 7.3
 */
public interface IPragma extends ICElement, ISourceManipulation, ISourceReference {
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
}
