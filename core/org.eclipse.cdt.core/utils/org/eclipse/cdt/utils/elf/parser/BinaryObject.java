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
package org.eclipse.cdt.utils.elf.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.IToolsProvider;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.ElfHelper;
import org.eclipse.cdt.utils.elf.Elf.Attribute;
import org.eclipse.cdt.utils.elf.ElfHelper.Sizes;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 */
public class BinaryObject extends BinaryFile implements IBinaryObject {
	protected String soname;
	protected String[] needed;
	protected int type = IBinaryFile.OBJECT;
	private Sizes sizes;
	private Attribute attribute;
	private ISymbol[] symbols;
	private ISymbol[] NO_SYMBOLS = new ISymbol[0];

	public BinaryObject(IPath path) throws IOException {
		super(path);
	}

	public BinaryObject(IPath path, ElfHelper helper, IToolsProvider provider) throws IOException {
		super(path);
		setToolsProvider(provider);
		loadInformation(helper);
		helper.dispose();
		hasChanged();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getSymbol(long)
	 */
	public ISymbol getSymbol(long addr) {
		ISymbol[] syms = getSymbols();
		int insertion = Arrays.binarySearch(syms, new Long(addr));
		if (insertion > 0) {
			return syms[insertion];
		}
		if (insertion == -1) {
			return null;
		}
		insertion = -insertion - 1;
		return syms[insertion - 1];
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getBSS()
	 */
	public long getBSS() {
		Sizes sz = getSizes();
		if (sz != null) {
			return sizes.bss;
		}
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
		Sizes sz = getSizes();
		if (sz != null) {
			return sizes.data;
		}
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getText()
	 */
	public long getText() {
		Sizes sz = getSizes();
		if (sz != null) {
			return sizes.text;
		}
		return 0;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#hasDebug()
	 */
	public boolean hasDebug() {
		Attribute attr = getAttribute();
		if (attr != null) {
			return attribute.hasDebug();
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#isLittleEndian()
	 */
	public boolean isLittleEndian() {
		Attribute attr = getAttribute();
		if (attr != null) {
			return attribute.isLittleEndian();
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getType()
	 */
	public int getType() {
		return type;
	}

	public void setType(int t) {
		type = t;
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
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getName()
	 */
	public String getName() {
		return getPath().lastSegment().toString();
	}

	public String toString() {
		return getName();
	}

	protected Attribute getAttribute() {
		if (hasChanged()) {
			ElfHelper helper = null;
			try {
				helper = getElfHelper();
				loadAttributes(helper);
			} catch (IOException e) {
			} finally {
				if (helper != null) {
					helper.dispose();
				}
			}
		}
		return attribute;
	}

	protected Sizes getSizes() {
		if (hasChanged()) {
			ElfHelper helper = null;
			try {
				helper = getElfHelper();
				loadAttributes(helper);
			} catch (IOException e) {
			} finally {
				if (helper != null) {
					helper.dispose();
				}
			}
		}
		return sizes;
	}

	protected ElfHelper getElfHelper() throws IOException {
		return new ElfHelper(getPath().toOSString());
	}

	protected void loadInformation() throws IOException {
		ElfHelper helper = null;
		try {
			helper = getElfHelper();
			loadInformation(helper);
		} finally {
			if (helper != null) {
				helper.dispose();
			}
		}
	}

	private void loadInformation(ElfHelper helper) throws IOException {
		loadAttributes(helper);
		loadSymbols(helper);
	}

	private void loadAttributes(ElfHelper helper) throws IOException {
		Elf.Dynamic[] sharedlibs = helper.getNeeded();
		needed = new String[sharedlibs.length];
		for (int i = 0; i < sharedlibs.length; i++) {
			needed[i] = sharedlibs[i].toString();
		}
		sizes = helper.getSizes();
		soname = helper.getSoname();
		attribute = helper.getElf().getAttributes();
	}

	private void loadSymbols(ElfHelper helper) throws IOException {
		ArrayList list = new ArrayList();
		// Hack should be remove when Elf is clean
		helper.getElf().setCppFilter(false);

		Addr2line addr2line = getAddr2Line();
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
				}
			}
			sym.addr = array[i].st_value;
			sym.filename = null;
			sym.startLine =  0;
			sym.endLine = sym.startLine;
			if (addr2line != null) {
				try {
					String filename =  addr2line.getFileName(sym.addr);
					// Addr2line returns the funny "??" when it can not find the file.
					sym.filename = (filename != null && !filename.equals("??")) ? new Path(filename) : null;
					sym.startLine = addr2line.getLineNumber(sym.addr);
					sym.endLine = addr2line.getLineNumber(sym.addr + array[i].st_size - 1);
				} catch (IOException e) {
				}
			}
			list.add(sym);
		}
	}

}
