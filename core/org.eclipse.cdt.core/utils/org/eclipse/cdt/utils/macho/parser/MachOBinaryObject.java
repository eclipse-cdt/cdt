/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.utils.macho.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.*;
import org.eclipse.cdt.utils.BinaryObjectAdapter;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.macho.MachO;
import org.eclipse.cdt.utils.macho.MachOHelper;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/*
 * MachOBinaryObject 
 */
public class MachOBinaryObject extends BinaryObjectAdapter {

	private BinaryObjectInfo info;
	private ISymbol[] symbols;

	public MachOBinaryObject(IBinaryParser parser, IPath path) {
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

	protected MachOHelper getMachOHelper() throws IOException {
		return new MachOHelper(getPath().toOSString());
	}

	protected void loadAll() throws IOException {
		MachOHelper helper = null;
		try {
			helper = getMachOHelper();
			loadInfo(helper);
			loadSymbols(helper);
		} finally {
			if (helper != null) {
				helper.dispose();
			}
		}
	}

	protected void loadInfo() throws IOException {
		MachOHelper helper = null;
		try {
			helper = getMachOHelper();
			loadInfo(helper);
		} finally {
			if (helper != null) {
				helper.dispose();
			}
		}		
	}
	
	protected void loadInfo(MachOHelper helper) throws IOException {
		info = new BinaryObjectInfo();
		info.needed = helper.getNeeded();
		MachOHelper.Sizes sizes = helper.getSizes();
		info.bss = sizes.bss;
		info.data = sizes.data;
		info.text = sizes.text;
	
		info.soname = helper.getSoname();
		
		MachO.Attribute attribute = helper.getMachO().getAttributes();
		info.isLittleEndian = attribute.isLittleEndian();
		info.hasDebug = attribute.hasDebug();
		info.cpu = attribute.getCPU();
	}

	protected void loadSymbols(MachOHelper helper) throws IOException {
		ArrayList list = new ArrayList();
		// Hack should be remove when Elf is clean
		helper.getMachO().setCppFilter(false);

		CPPFilt cppfilt = getCPPFilt();

		addSymbols(helper.getExternalFunctions(), ISymbol.FUNCTION, cppfilt, list);
		addSymbols(helper.getLocalFunctions(), ISymbol.FUNCTION, cppfilt, list);
		addSymbols(helper.getExternalObjects(), ISymbol.VARIABLE, cppfilt, list);
		addSymbols(helper.getLocalObjects(), ISymbol.VARIABLE, cppfilt, list);
		list.trimToSize();

		if (cppfilt != null) {
			cppfilt.dispose();
		}

		symbols = (ISymbol[])list.toArray(NO_SYMBOLS);
		Arrays.sort(symbols);
		list.clear();
	}

	protected void addSymbols(MachO.Symbol[] array, int type, CPPFilt cppfilt, List list) {
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
			sym.addr = array[i].n_value;
			sym.size = 0;
			sym.filename = null;
			sym.startLine =  0;
			sym.endLine = sym.startLine;
			String filename = array[i].getFilename();
			sym.filename = (filename != null) ? new Path(filename) : null; //$NON-NLS-1$
			sym.startLine = array[i].getLineNumber(sym.addr);
			sym.endLine = array[i].getLineNumber(sym.addr + sym.size - 1);
			list.add(sym);
		}
	}

}
