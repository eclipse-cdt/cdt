/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.utils.xcoff.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.BinaryObjectAdapter;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.CygPath;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.cdt.utils.xcoff.XCoff32;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Binary file in AIX XCOFF32 format
 * 
 * @author vhirsl
 */
public class XCOFFBinaryObject extends BinaryObjectAdapter {
	BinaryObjectInfo info;
	ISymbol[] symbols;

	/**
	 * @param parser
	 * @param path
	 */
	public XCOFFBinaryObject(IBinaryParser parser, IPath path) {
		super(parser, path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getSymbols()
	 */
	public ISymbol[] getSymbols() {
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
		if (hasChanged() || info == null) {
			try {
				loadInfo();
			} catch (IOException e) {
				info = new BinaryObjectInfo();
			}
		}
		return info;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getType()
	 */
	public int getType() {
		return IBinaryFile.OBJECT;
	}

	protected XCoff32 getXCoff32() throws IOException {
		return new XCoff32(getPath().toOSString());
	}

	protected void loadAll() throws IOException {
		XCoff32 xcoff = null;
		try {
			xcoff = getXCoff32();
			loadInfo(xcoff);
			loadSymbols(xcoff);
		} finally {
			if (xcoff != null) {
				xcoff.dispose();
			}
		}
	}

	protected void loadInfo() throws IOException {
		XCoff32 xcoff = null;
		try {
			xcoff = getXCoff32();
			loadInfo(xcoff);
		} finally {
			if (xcoff != null) {
				xcoff.dispose();
			}
		}
	}

	protected void loadInfo(XCoff32 xcoff) throws IOException {
		info = new BinaryObjectInfo();
		XCoff32.Attribute attribute = xcoff.getAttributes();
		info.isLittleEndian = attribute.isLittleEndian();
		info.hasDebug = attribute.hasDebug();
		info.cpu = attribute.getCPU();
	}

	protected void loadSymbols(XCoff32 xcoff) throws IOException {
		ArrayList list = new ArrayList();
		Addr2line addr2line = getAddr2line();
		CPPFilt cppfilt = getCPPFilt();
		CygPath cygpath = getCygPath();

		XCoff32.Symbol[] peSyms = xcoff.getSymbols();
		byte[] table = xcoff.getStringTable();
		addSymbols(peSyms, table, addr2line, cppfilt, cygpath, list);

		if (addr2line != null) {
			addr2line.dispose();
		}
		if (cppfilt != null) {
			cppfilt.dispose();
		}
		if (cygpath != null) {
			cygpath.dispose();
		}

		symbols = (ISymbol[])list.toArray(NO_SYMBOLS);
		Arrays.sort(symbols);
		list.clear();
	}

	protected void addSymbols(XCoff32.Symbol[] peSyms, byte[] table, Addr2line addr2line, CPPFilt cppfilt, CygPath cygpath, List list) {
		for (int i = 0; i < peSyms.length; i++) {
			if (peSyms[i].isFunction() || peSyms[i].isVariable()) {
				String name = peSyms[i].getName(table);
				if (name == null || name.trim().length() == 0 /*|| 
				    !Character.isJavaIdentifierStart(name.charAt(0))*/) {
					continue;
				}
				Symbol sym = new Symbol(this);
				sym.type = peSyms[i].isFunction() ? ISymbol.FUNCTION : ISymbol.VARIABLE;
				sym.addr = peSyms[i].n_value;

				sym.name = name;
				if (cppfilt != null) {
					try {
						sym.name = cppfilt.getFunction(sym.name);
					} catch (IOException e1) {
						cppfilt = null;
					}
				}

				sym.filename = null;
				sym.startLine = 0;
				sym.endLine = 0;
				if (addr2line != null) {
					try {
						String filename =  addr2line.getFileName(sym.addr);
						// Addr2line returns the funny "??" when it can not find the file.
						if (filename != null && filename.equals("??")) { //$NON-NLS-1$
							filename = null;
						}

						if (filename != null) {
							if (cygpath != null) {
								sym.filename =  new Path(cygpath.getFileName(filename));
							} else {
								sym.filename = new Path(filename);
							}
						}
						sym.startLine = addr2line.getLineNumber(sym.addr);
					} catch (IOException e) {
						addr2line = null;
					}
				}
				list.add(sym);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getAddr2line()
	 */
	public Addr2line getAddr2line() {
		XCOFF32Parser parser = (XCOFF32Parser)getBinaryParser();
		return parser.getAddr2line(getPath());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getCPPFilt()
	 */
	public CPPFilt getCPPFilt() {
		XCOFF32Parser parser = (XCOFF32Parser)getBinaryParser();
		return parser.getCPPFilt();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getObjdump()
	 */
	public Objdump getObjdump() {
		XCOFF32Parser parser = (XCOFF32Parser)getBinaryParser();
		return parser.getObjdump(getPath());
	}

	private CygPath getCygPath() {
		return null;
	}
}
