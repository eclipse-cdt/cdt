/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
		public IAddressFactory addressFactory;

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
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getBSS()
	 */
	public long getBSS() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.bss;
		}
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getCPU()
	 */
	public String getCPU() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.cpu;
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getData()
	 */
	public long getData() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.data;
		}
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getText()
	 */
	public long getText() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.text;
		}
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#hasDebug()
	 */
	public boolean hasDebug() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.hasDebug;
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#isLittleEndian()
	 */
	public boolean isLittleEndian() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.isLittleEndian;
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryExecutable#getNeededSharedLibs()
	 */
	public String[] getNeededSharedLibs() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.needed;
		}
		return new String[0];
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryShared#getSoName()
	 */
	public String getSoName() {
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.soname;
		}
		return ""; //$NON-NLS-1$
	}
	public IAddressFactory getAddressFactory()
	{
		BinaryObjectInfo info = getBinaryObjectInfo();
		if (info != null) {
			return info.addressFactory;
		}
		return null; //$NON-NLS-1$
	}
	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getName()
	 */
	public String getName() {
		return getPath().lastSegment().toString();
	}

	public String toString() {
		return getName();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getSymbols()
	 */
	public abstract ISymbol[] getSymbols();

	protected abstract BinaryObjectInfo getBinaryObjectInfo();

}
