/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.model.ISourceRange;

public class SourceRange implements ISourceRange {

	private int fOffset;
	private int fLength;

	public SourceRange(int offset, int length) {
		fOffset= offset;
		fLength= length;
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
