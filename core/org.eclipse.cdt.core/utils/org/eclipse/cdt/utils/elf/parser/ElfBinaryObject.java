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
package org.eclipse.cdt.utils.elf.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.*;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.BinaryObjectAdapter;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.ElfHelper;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/*
 * ElfBinaryObject 
 */
public class ElfBinaryObject extends BinaryObjectAdapter {

	private BinaryObjectInfo info;
	private ISymbol[] symbols;

	public ElfBinaryObject(IBinaryParser parser, IPath path) {
		super(parser, path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getType()
	 */
	public int getType() {
		return IBinaryFile.OBJECT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getSymbols()
	 */
	public ISymbol[] getSymbols() {
		// Call the hasChanged first, to initialize the timestamp
		if (hasChanged() || symbols == null) {
			try {
				loadAll();
			} catch (IOException e) {
				symbols = NO_SYMBOLS;
			}
		}
		return symbols;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getBinaryObjectInfo()
	 */
	protected BinaryObjectInfo getBinaryObjectInfo() {
		// Call the hasChanged first, to initialize the timestamp
		if (hasChanged() || info == null) {
			try {
				loadInfo();
			} catch (IOException e) {
				info = new BinaryObjectInfo();
			}
		}
		return info;
	}

	protected ElfHelper getElfHelper() throws IOException {
		return new ElfHelper(getPath().toOSString());
	}

	protected void loadAll() throws IOException {
		ElfHelper helper = null;
		try {
			helper = getElfHelper();
			loadInfo(helper);
			loadSymbols(helper);
		} finally {
			if (helper != null) {
				helper.dispose();
			}
		}
	}

	protected void loadInfo() throws IOException {
		ElfHelper helper = null;
		try {
			helper = getElfHelper();
			loadInfo(helper);
		} finally {
			if (helper != null) {
				helper.dispose();
			}
		}		
	}
	
	protected void loadInfo(ElfHelper helper) throws IOException {
		info = new BinaryObjectInfo();
		Elf.Dynamic[] sharedlibs = helper.getNeeded();
		info.needed = new String[sharedlibs.length];
		for (int i = 0; i < sharedlibs.length; i++) {
			info.needed[i] = sharedlibs[i].toString();
		}
		ElfHelper.Sizes sizes = helper.getSizes();
		info.bss = sizes.bss;
		info.data = sizes.data;
		info.text = sizes.text;
	
		info.soname = helper.getSoname();
		
		Elf.Attribute attribute = helper.getElf().getAttributes();
		info.isLittleEndian = attribute.isLittleEndian();
		info.hasDebug = attribute.hasDebug();
		info.cpu = attribute.getCPU();
	}

	protected void loadSymbols(ElfHelper helper) throws IOException {
		ArrayList list = new ArrayList();

		Addr2line addr2line = getAddr2line();
		CPPFilt cppfilt = getCPPFilt();

		addSymbols(helper.getExternalFunctions(), ISymbol.FUNCTION, addr2line, cppfilt, list);
		addSymbols(helper.getLocalFunctions(), ISymbol.FUNCTION, addr2line, cppfilt, list);
		addSymbols(helper.getExternalObjects(), ISymbol.VARIABLE, addr2line, cppfilt, list);
		addSymbols(helper.getLocalObjects(), ISymbol.VARIABLE, addr2line, cppfilt, list);
		list.trimToSize();

		if (addr2line != null) {
			addr2line.dispose();
		}
		if (cppfilt != null) {
			cppfilt.dispose();
		}

		symbols = (ISymbol[])list.toArray(NO_SYMBOLS);
		Arrays.sort(symbols);
		list.clear();
	}

	protected void addSymbols(Elf.Symbol[] array, int type, Addr2line addr2line, CPPFilt cppfilt, List list) {
		for (int i = 0; i < array.length; i++) {
			Symbol sym = new Symbol(this);
			sym.type = type;
			sym.name = array[i].toString();
			if (cppfilt != null) {
				try {
					sym.name = cppfilt.getFunction(sym.name);
				} catch (IOException e1) {
					cppfilt = null;
				}
			}
			sym.addr = array[i].st_value;
			sym.size = array[i].st_size;
			sym.filename = null;
			sym.startLine =  0;
			sym.endLine = sym.startLine;
			if (addr2line != null) {
				try {
					String filename =  addr2line.getFileName(sym.addr);
					// Addr2line returns the funny "??" when it can not find the file.
					sym.filename = (filename != null && !filename.equals("??")) ? new Path(filename) : null; //$NON-NLS-1$
					sym.startLine = addr2line.getLineNumber(sym.addr);
					sym.endLine = addr2line.getLineNumber(sym.addr + sym.size - 1);
				} catch (IOException e) {
					addr2line = null;
				}
			}
			list.add(sym);
		}
	}

}
