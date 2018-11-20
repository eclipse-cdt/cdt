/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.OffsetLimitReachedException;

/**
 * A token sequence serves as input to the macro expansion.
 */
interface ITokenSequence {
	/**
	 * Returns the current token
	 */
	Token currentToken();

	/**
	 * Consumes the current token and returns the next one.
	 */
	Token nextToken() throws OffsetLimitReachedException;

	/**
	 * Returns the offset of the last token consumed.
	 */
	int getLastEndOffset();
}