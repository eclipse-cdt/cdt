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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.ElfHelper;
import org.eclipse.cdt.utils.elf.Elf.Attribute;
import org.eclipse.cdt.utils.elf.ElfHelper.Sizes;
import org.eclipse.core.runtime.IPath;

/**
 */
public class BinaryObject extends BinaryFile implements IBinaryObject {
	protected String soname;
	protected String[] needed;
	protected int type = IBinaryFile.OBJECT;

	private long timestamp;
	private Sizes sizes;
	private Attribute attribute;
	private ArrayList symbols;

	public BinaryObject(IPath path) throws IOException {
		super(path);
		loadInformation();
		hasChanged();
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
			if (symbols == null) {
				symbols = new ArrayList(5);
			}
			try {
				loadInformation();
			} catch (IOException e) {
			}
		}
		return (ISymbol[]) symbols.toArray(new ISymbol[0]);
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() {
		InputStream stream = null;
		if (path != null) {
			try {
				stream = new FileInputStream(path.toFile());
			} catch (IOException e) {
			}
		}
		if (stream == null) {
			stream = super.getContents();
		}
		return stream;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getName()
	 */
	public String getName() {
		if (path != null) {
			return path.lastSegment().toString();
		}
		return "";
	}

	public String toString() {
		return getName();
	}

	protected Attribute getAttribute() {
		if (hasChanged()) {
			try {
				loadInformation();
			} catch (IOException e) {
			}
		}
		return attribute;
	}

	protected Sizes getSizes() {
		if (hasChanged()) {
			try {
				loadInformation();
			} catch (IOException e) {
			}
		}
		return sizes;
	}

	boolean hasChanged() {
		long modification = path.toFile().lastModified();
		boolean changed = modification != timestamp;
		timestamp = modification;
		return changed;
	}

	protected ElfHelper getElfHelper() throws IOException {
		if (path != null) {
			return new ElfHelper(path.toOSString());
		}
		throw new IOException("No file assiocated with Binary");
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
		if (symbols != null) {
			symbols.clear();
			loadSymbols(helper);
			symbols.trimToSize();
		}
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
		Elf.Dynamic[] sharedlibs = helper.getNeeded();
		needed = new String[sharedlibs.length];
		for (int i = 0; i < sharedlibs.length; i++) {
			needed[i] = sharedlibs[i].toString();
		}
		sizes = helper.getSizes();
		soname = helper.getSoname();
		attribute = helper.getElf().getAttributes();
		// Hack should be remove when Elf is clean
		helper.getElf().setCppFilter(false);

		Addr2line addr2line = getAddr2Line();
		CPPFilt cppfilt = getCPPFilt();

		addSymbols(helper.getExternalFunctions(), ISymbol.FUNCTION, addr2line, cppfilt);
		addSymbols(helper.getLocalFunctions(), ISymbol.FUNCTION, addr2line, cppfilt);
		addSymbols(helper.getExternalObjects(), ISymbol.VARIABLE, addr2line, cppfilt);
		addSymbols(helper.getLocalObjects(), ISymbol.VARIABLE, addr2line, cppfilt);
		symbols.trimToSize();

		if (addr2line != null) {
			addr2line.dispose();
		}
		if (cppfilt != null) {
			cppfilt.dispose();
		}
	}

	protected void addSymbols(Elf.Symbol[] array, int type, Addr2line addr2line, CPPFilt cppfilt) {
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
			sym.startLine =  -1;
			sym.endLine = sym.startLine;
			if (addr2line != null) {
				try {
					sym.filename =  addr2line.getFileName(sym.addr);
					sym.startLine = addr2line.getLineNumber(sym.addr);
					sym.endLine = addr2line.getLineNumber(sym.addr + array[i].st_size - 1);
				} catch (IOException e) {
				}
			}
			addSymbol(sym);
		}
	}

	protected void addSymbol(Symbol sym) {
		symbols.add(sym);
	}

	protected Addr2line getAddr2Line() {
		IPath addr2LinePath = getAddr2LinePath();
		Addr2line addr2line = null;
		try {
			addr2line = new Addr2line(addr2LinePath.toOSString(), getPath().toOSString());
		} catch (IOException e1) {
		}
		return addr2line;
	}

	protected CPPFilt getCPPFilt() {
		IPath cppFiltPath = getCPPFiltPath();
		CPPFilt cppfilt = null;
		try {
			cppfilt = new CPPFilt(cppFiltPath.toOSString());
		} catch (IOException e2) {
		}
		return cppfilt;
	}

}
