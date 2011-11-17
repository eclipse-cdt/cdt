/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import org.eclipse.cdt.core.model.ISourceRange;

/**
 * @see ISourceRange
 */
class SourceRange implements ISourceRange {

	protected int startPos, length;
	protected int idStartPos, idLength;
	protected int startLine, endLine;

	protected SourceRange(int startPos, int length) {
		this.startPos = startPos;
		this.length = length;
		idStartPos = 0;
		idLength = 0;
		startLine = 0;
		endLine = 0;
	}

	protected SourceRange(int startPos, int length, int idStartPos, int idLength) {
		this.startPos = startPos;
		this.length = length;
		this.idStartPos = idStartPos;
		this.idLength = idLength;
	}

	protected SourceRange(int startPos, int length, int idStartPos, int idLength,
		int startLine, int endLine) {
		this.startPos = startPos;
		this.length = length;
		this.idStartPos = idStartPos;
		this.idLength = idLength;
		this.startLine = startLine;
		this.endLine = endLine;
	}
	/**
	 * @see ISourceRange
	 */
	@Override
	public int getLength() {
		return length;
	}

	/**
	 * @see ISourceRange
	 */
	@Override
	public int getStartPos() {
		return startPos;
	}

	/**
	 */
	@Override
	public int getIdStartPos() {
		return idStartPos;
	}

	@Override
	public int getIdLength() {
		return idLength;
	}

	@Override
	public int getStartLine() {
		return startLine;
	}

	@Override
	public int getEndLine() {
		return endLine;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[offset="); //$NON-NLS-1$
		buffer.append(this.startPos);
		buffer.append(", length="); //$NON-NLS-1$
		buffer.append(this.length);
		buffer.append("]"); //$NON-NLS-1$

		buffer.append("[IdOffset="); //$NON-NLS-1$
		buffer.append(this.idStartPos);
		buffer.append(", idLength="); //$NON-NLS-1$
		buffer.append(this.idLength);
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}
}
