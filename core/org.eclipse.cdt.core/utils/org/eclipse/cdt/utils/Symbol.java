/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.core.runtime.IPath;

public class Symbol implements ISymbol {

	protected final BinaryObjectAdapter binary;
	private final String name;

	private final IAddress addr;
	private final int type;
	private final long size;
	private final int startLine;
	private final int endLine;
	private final IPath sourceFile;

	public Symbol(BinaryObjectAdapter binary, String name, int type, IAddress addr, long size, IPath sourceFile,
			int startLine, int endLine) {
		this.binary = binary;
		this.name = name;
		this.type = type;
		this.addr = addr;
		this.size = size;
		this.startLine = startLine;
		this.endLine = endLine;
		this.sourceFile = sourceFile;
	}

	public Symbol(BinaryObjectAdapter binary, String name, int type, IAddress addr, long size) {
		this.binary = binary;
		this.name = name;
		this.type = type;
		this.addr = addr;
		this.size = size;
		this.startLine = -1;
		this.endLine = -1;
		this.sourceFile = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getBinarObject()
	 */
	@Override
	public IBinaryObject getBinaryObject() {
		return binary;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getFilename()
	 */
	@Override
	public IPath getFilename() {
		return sourceFile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getType()
	 */
	@Override
	public int getType() {
		return type;
	}

	@Override
	public IAddress getAddress() {
		return addr;
	}

	@Override
	public int getEndLine() {
		return endLine;
	}

	@Override
	public int getStartLine() {
		return startLine;
	}

	@Override
	public int getLineNumber(long offset) {
		return -1;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public int compareTo(Object obj) {
		IAddress thisVal = this.addr;
		IAddress anotherVal = null;
		if (obj instanceof Symbol) {
			anotherVal = ((Symbol) obj).addr;
		} else if (obj instanceof IAddress) {
			anotherVal = (IAddress) obj;
		}
		return thisVal.compareTo(anotherVal);
	}
}
