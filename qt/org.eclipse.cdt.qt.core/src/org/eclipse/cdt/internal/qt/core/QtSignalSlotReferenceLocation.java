/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;

/**
 * The location of the signal/slot reference is stored as the location of the parent
 * macro expansion + an offset, which is the number of characters between the start
 * of the expansion and the start of the argument (including whitespace).  E.g. in,
 *
 * <pre>
 * SIGNAL( signal1( int ) )
 * ^       ^            ^ c: end of reference name
 * |       +------------- b: start of reference name
 * +--------------------- a: start of macro expansion
 * </pre>
 *
 * The offset is b - a and length is c - a.  This means that the result of 'Find
 * References' will highlight just "signal( int )".
 *
 * @see QtSignalSlotReferenceName
 */
public class QtSignalSlotReferenceLocation implements IASTImageLocation {

	private final IASTFileLocation referenceLocation;
	private final int offset;
	private final int length;

	public QtSignalSlotReferenceLocation(IASTFileLocation referenceLocation, int offset, int length) {
		this.referenceLocation = referenceLocation;
		this.offset = offset;
		this.length = length;
	}

	@Override
	public int getLocationKind() {
		return IASTImageLocation.ARGUMENT_TO_MACRO_EXPANSION;
	}

	@Override
	public int getNodeOffset() {
		return referenceLocation.getNodeOffset() + offset;
	}

	@Override
	public int getNodeLength() {
		return length;
	}

	@Override
	public String getFileName() {
		return referenceLocation.getFileName();
	}

	@Override
	public IASTFileLocation asFileLocation() {
		return referenceLocation;
	}

	@Override
	public int getEndingLineNumber() {
		return referenceLocation.getEndingLineNumber();
	}

	@Override
	public int getStartingLineNumber() {
		return referenceLocation.getStartingLineNumber();
	}

	@Override
	public IASTPreprocessorIncludeStatement getContextInclusionStatement() {
		return referenceLocation.getContextInclusionStatement();
	}
}
