/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.CygPath;
import org.eclipse.cdt.utils.ICygwinToolsProvider;
import org.eclipse.cdt.utils.coff.Coff;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.cdt.utils.coff.PE.Attribute;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 */
public class BinaryObject extends BinaryFile implements IBinaryObject {

	PE.Attribute attribute;
	ISymbol[] symbols;
	int type = IBinaryFile.OBJECT;
	private ISymbol[] NO_SYMBOLS = new ISymbol[0];
	
	public BinaryObject(IPath p) throws IOException {
		super(p);
	}

	public BinaryObject(IPath p, PE pe, ICygwinToolsProvider provider) throws IOException {
		super(p);
		setToolsProvider(provider);
		loadInformation(pe);
		pe.dispose();
		hasChanged();
	}

	public void setType(int t) {
		type = t;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getSymbol(long)
	 */
	public ISymbol getSymbol(long addr) {
		ISymbol[] syms = getSymbols();
		int i = Arrays.binarySearch(syms, new Long(addr));
		if (i < 0 || i >= syms.length) {
			return null;
		}
		return syms[i];
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getType()
	 */
	public int getType() {
		return type;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getBSS()
	 */
	public long getBSS() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getCPU()
	 */
	public String getCPU() {
		Attribute attr = getAttribute();
		if (attr != null) {
			return attribute.getCPU();
		}
		return "";
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getData()
	 */
	public long getData() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getName()
	 */
	public String getName() {
		return getPath().lastSegment().toString();
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getSymbols()
	 */
	public ISymbol[] getSymbols() {
		if (hasChanged() || symbols == null) {
			try {
				loadInformation();
			} catch (IOException e) {
			}
			if (symbols == null) {
				symbols = NO_SYMBOLS;
			}
		}
		return symbols;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getText()
	 */
	public long getText() {
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#hasDebug()
	 */
	public boolean hasDebug() {
		Attribute attr = getAttribute();
		if (attr != null) {
			return attr.hasDebug();
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#isLittleEndian()
	 */
	public boolean isLittleEndian() {
		Attribute attr = getAttribute();
		if (attr != null) {
			return attr.isLittleEndian();
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryExecutable#getNeededSharedLibs()
	 */
	public String[] getNeededSharedLibs() {
		return new String[0];
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryShared#getSoName()
	 */
	public String getSoName() {
		return "";
	}

	protected PE getPE() throws IOException {
		return new PE(getPath().toOSString());
	}

	protected PE.Attribute getAttribute() {
		if (hasChanged()) {
			PE pe = null;
			try {
				pe = getPE();
				loadAttributes(pe); 
			} catch (IOException e) {
			} finally {
				if (pe != null) {
					try {
						pe.dispose();
					} catch (IOException e1) {
					}
				}
			}
		}
		return attribute;
	}

	protected void loadInformation() throws IOException {
		PE pe = getPE();
		loadInformation(pe);
		pe.dispose();
	}

	private void loadInformation(PE pe) throws IOException {
		loadAttributes(pe);
		loadSymbols(pe);
	}

	private void loadAttributes(PE pe) throws IOException {
		attribute = pe.getAttribute();
	}

	private void loadSymbols(PE pe) throws IOException {
		ArrayList list = new ArrayList();
		Addr2line addr2line = getAddr2Line();
		CPPFilt cppfilt = getCPPFilt();
		CygPath cygpath = getCygPath();

		Coff.Symbol[] peSyms = pe.getSymbols();
		byte[] table = pe.getStringTable();
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

	protected void addSymbols(Coff.Symbol[] peSyms, byte[] table, Addr2line addr2line, CPPFilt cppfilt, CygPath cygpath, List list) {
		for (int i = 0; i < peSyms.length; i++) {
			if (peSyms[i].isFunction() || peSyms[i].isPointer() ||peSyms[i].isArray()) {
				String name = peSyms[i].getName(table);
				if (name == null || name.trim().length() == 0 ||
				    !Character.isJavaIdentifierStart(name.charAt(0))) {
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
					}
				}

				sym.filename = null;
				sym.startLine = 0;
				sym.endLine = 0;
				if (addr2line != null) {
					try {
						String filename =  addr2line.getFileName(sym.addr);
						if (filename != null) {
							if (cygpath != null) {
								sym.filename =  new Path(cygpath.getFileName(filename));
							} else {
								sym.filename = new Path(filename);
							}
						}
						sym.startLine = addr2line.getLineNumber(sym.addr);
					} catch (IOException e) {
					}
				}
				list.add(sym);
			}
		}
	}

}
