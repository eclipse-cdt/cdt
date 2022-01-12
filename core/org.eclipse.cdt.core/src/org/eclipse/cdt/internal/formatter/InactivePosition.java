/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.cdt.internal.formatter;

import org.eclipse.jface.text.Position;

public class InactivePosition extends Position {

	private boolean preprocessorRegion;

	public boolean isPreprocessorRegion() {
		return preprocessorRegion;
	}

	/**
	 * Creates a new position with the given offset and length 0.
	 *
	 * @param offset the position offset, must be &gt;= 0
	 * @param preprocessor True if the region is a preprocessor region, false otherwise
	 */
	public InactivePosition(int offset, boolean preprocessor) {
		super(offset, 0);
		preprocessorRegion = preprocessor;
	}

	/**
	 * Creates a new position with the given offset and length.
	 *
	 * @param offset the position offset, must be &gt;= 0
	 * @param length the position length, must be &gt;= 0
	 * @param preprocessor True if the region is a preprocessor region, false otherwise
	 */
	public InactivePosition(int offset, int length, boolean preprocessor) {
		super(offset, length);
		preprocessorRegion = preprocessor;
	}

}
