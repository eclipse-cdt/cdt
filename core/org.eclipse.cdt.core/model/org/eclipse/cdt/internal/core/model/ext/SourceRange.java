/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.model.ISourceRange;

public class SourceRange implements ISourceRange {
	private int fOffset;
	private int fLength;

	public SourceRange(int offset, int length) {
		fOffset = offset;
		fLength = length;
	}

	@Override
	public int getEndLine() {
		return -1;
	}

	@Override
	public int getIdLength() {
		return fLength;
	}

	@Override
	public int getIdStartPos() {
		return fOffset;
	}

	@Override
	public int getLength() {
		return fLength;
	}

	@Override
	public int getStartLine() {
		return -1;
	}

	@Override
	public int getStartPos() {
		return fOffset;
	}
}
