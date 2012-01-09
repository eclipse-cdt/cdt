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
package org.eclipse.cdt.utils;

import java.util.Arrays;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.core.runtime.IPath;

/**
 */
public abstract class BinaryObjectAdapter extends BinaryFile implements IBinaryObject {

	protected ISymbol[] NO_SYMBOLS = new ISymbol[0];

	public class BinaryObjectInfo {
		public long bss;
		public long data;
		public long text;
		public boolean hasDebug;
		public boolean isLittleEndian;
		public String soname;
		public String[] needed;
		public String cpu;

		public BinaryObjectInfo() {
			cpu = soname = ""; //$NON-NLS-1$
			needed = new String[0];
		}
	}

	public BinaryObjectAdapter(IBinaryParser parser, IPath path, int type) {
		super(parser, path, type);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getSymbol(long)
	 */
	@Override
	public ISymbol getSymbol(IAddress addr) {
		ISymbol[] syms = getSymbols();
		int insertion = Arrays.binarySearch(syms, addr);
		if (insertion >= 0) {
			return syms[insertion];
		}
		if (insertion == -1) {
			return null;
		}
		insertion = -insertion - 1;
		ISymbol symbol =  syms[insertion - 1];
		if (addr.compareTo(symbol.getAddress().add(symbol.getSize())) < 0) {
			return syms[insertion - 1];
		}
		return null;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getBSS()
	 */
	@Override
	public long getBSS() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.bss;
		}
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getCPU()
	 */
	@Override
	public String getCPU() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.cpu;
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getData()
	 */
	@Override
	public long getData() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.data;
		}
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getText()
	 */
	@Override
	public long getText() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.text;
		}
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#hasDebug()
	 */
	@Override
	public boolean hasDebug() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.hasDebug;
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#isLittleEndian()
	 */
	@Override
	public boolean isLittleEndian() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.isLittleEndian;
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable#getNeededSharedLibs()
	 */
	public String[] getNeededSharedLibs() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.needed;
		}
		return new String[0];
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryShared#getSoName()
	 */
	public String getSoName() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.soname;
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getName()
	 */
	@Override
	public String getName() {
		return getPath().lastSegment().toString();
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getSymbols()
	 */
	@Override
	public abstract ISymbol[] getSymbols();
	@Override
	public abstract IAddressFactory getAddressFactory();

	protected abstract BinaryObjectInfo getBinaryObjectInfo();

}
