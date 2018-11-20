/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation;

/**
 * Represents a range within a source file.
 */

public interface ISourceRange extends Comparable<ISourceRange> {
	/**
	 * Returns 0-based absolute number for the inclusive start of the range.
	 */
	int getBeginOffset();

	/**
	 * Returns 0-based absolute number for the inclusive end of the range.
	 */
	int getEndOffset();

	/**
	 * Checks whether the range contains the given offset.
	 */
	boolean contains(int offset);
}
