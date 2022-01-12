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
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class EndOfFileException extends Exception {
	private static final long serialVersionUID = 1607883323361197919L;

	private final boolean fEndsInactiveCode;
	private final int fOffset;

	/**
	 * @since 5.2
	 */
	public EndOfFileException(int offset) {
		this(offset, false);
	}

	/**
	 * @since 5.2
	 */
	public EndOfFileException(int offset, boolean endsInactiveCode) {
		fOffset = offset;
		fEndsInactiveCode = endsInactiveCode;
	}

	/**
	 * @since 5.1
	 */
	public boolean endsInactiveCode() {
		return fEndsInactiveCode;
	}

	/**
	 * Returns the offset at which the translation unit ends, or -1 if not known.
	 * @since 5.2
	 */
	public int getEndOffset() {
		return fOffset;
	}
}
