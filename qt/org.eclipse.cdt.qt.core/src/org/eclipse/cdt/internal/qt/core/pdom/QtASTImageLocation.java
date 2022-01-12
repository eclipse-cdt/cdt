/*
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;

/**
 * The location of the signal/slot reference is stored as the location of the parent
 * macro expansion + an offset, which is the number of characters between the start
 * of the expansion and the start of the argument (including whitespace).  E.g. in,
 *
 * <pre>
 * MACRO( expansionParameter )
 * ^      ^                ^ c: end of reference name
 * |      +----------------- b: start of reference name
 * +------------------------ a: start of macro expansion
 * </pre>
 *
 * The offset is b - a and length is c - b.  This means that the result of 'Find
 * References' will highlight just "parameter".
 */
public class QtASTImageLocation implements IASTImageLocation {

	private final IASTFileLocation refLocation;
	private final int offset;
	private final int length;

	public QtASTImageLocation(IASTFileLocation refLocation, int offset, int length) {
		this.refLocation = refLocation;
		this.offset = offset;
		this.length = length;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	@Override
	public IASTFileLocation asFileLocation() {
		return this;
	}

	@Override
	public String getFileName() {
		return refLocation.getFileName();
	}

	@Override
	public int getNodeOffset() {
		return refLocation.getNodeOffset() + offset;
	}

	@Override
	public int getNodeLength() {
		return length;
	}

	@Override
	public int getStartingLineNumber() {
		return refLocation.getStartingLineNumber();
	}

	@Override
	public int getEndingLineNumber() {
		return refLocation.getEndingLineNumber();
	}

	@Override
	public IASTPreprocessorIncludeStatement getContextInclusionStatement() {
		return refLocation.getContextInclusionStatement();
	}

	@Override
	public int getLocationKind() {
		return REGULAR_CODE;
	}
}
