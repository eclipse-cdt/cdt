/*******************************************************************************
 * Copyright (c) 2000, 2019 Space Codesign Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Space Codesign Systems - Initial API and implementation
 *     QNX Software Systems - Initial PEBinaryObject class
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.utils.AR;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr32Factory;
import org.eclipse.cdt.utils.BinaryObjectAdapter;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.cdt.utils.coff.Coff64;
import org.eclipse.cdt.utils.coff.PE64;
import org.eclipse.core.runtime.IPath;

/**
 * @since 6.9
 */
public class PEBinaryObject64 extends BinaryObjectAdapter {

	BinaryObjectInfo info;
	IAddressFactory addressFactory;
	ISymbol[] symbols;
	AR.ARHeader header;

	public PEBinaryObject64(IBinaryParser parser, IPath path, AR.ARHeader header) {
		super(parser, path, IBinaryFile.OBJECT);
	}

	public PEBinaryObject64(IBinaryParser parser, IPath p, int type) {
		super(parser, p, type);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getName()
	 */
	@Override
	public String getName() {
		if (header != null) {
			return header.getObjectName();
		}
		return super.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getContents()
	 */
	@Override
	public InputStream getContents() throws IOException {
		if (getPath() != null && header != null) {
			return new ByteArrayInputStream(header.getObjectData());
		}
		return super.getContents();
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getSymbols()
	 */
	@Override
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

	@Override
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

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(PE64.class)) {
			try {
				if (header != null) {
					return (T) new PE64(getPath().toOSString(), header.getObjectDataOffset());
				}
				return (T) new PE64(getPath().toOSString());
			} catch (IOException e) {
			}
		}
		if (adapter.equals(ISymbolReader.class)) {
			PE64 pe = getAdapter(PE64.class);
			if (pe != null) {
				return (T) pe.getSymbolReader();
			}
		}
		return super.getAdapter(adapter);
	}

	protected PE64 getPE() throws IOException {
		if (header != null) {
			return new PE64(getPath().toOSString(), header.getObjectDataOffset());
		}
		return new PE64(getPath().toOSString());
	}

	protected void loadAll() throws IOException {
		PE64 pe = null;
		try {
			pe = getPE();
			loadInfo(pe);
			loadSymbols(pe);
		} finally {
			if (pe != null) {
				pe.dispose();
			}
		}
	}

	protected void loadInfo() throws IOException {
		PE64 pe = null;
		try {
			pe = getPE();
			loadInfo(pe);
		} finally {
			if (pe != null) {
				pe.dispose();
			}
		}
	}

	protected void loadInfo(PE64 pe) throws IOException {
		info = new BinaryObjectInfo();
		PE64.Attribute attribute = getPE().getAttribute();
		info.isLittleEndian = attribute.isLittleEndian();
		info.hasDebug = attribute.hasDebug();
		info.cpu = attribute.getCPU();
	}

	protected void loadSymbols(PE64 pe) throws IOException {
		ArrayList<Symbol> list = new ArrayList<>();
		loadSymbols(pe, list);
		symbols = list.toArray(NO_SYMBOLS);
		Arrays.sort(symbols);
		list.clear();
	}

	protected void loadSymbols(PE64 pe, List<Symbol> list) throws IOException {
		Coff64.Symbol[] peSyms = pe.getSymbols();
		byte[] table = pe.getStringTable();
		addSymbols(peSyms, table, list);
	}

	protected void addSymbols(Coff64.Symbol[] peSyms, byte[] table, List<Symbol> list) {
		for (org.eclipse.cdt.utils.coff.Coff64.Symbol peSym : peSyms) {
			if (peSym.isFunction() || peSym.isPointer() || peSym.isArray()) {
				String name = peSym.getName(table);
				if (name == null || name.trim().length() == 0 || !Character.isJavaIdentifierStart(name.charAt(0))) {
					continue;
				}
				int type = peSym.isFunction() ? ISymbol.FUNCTION : ISymbol.VARIABLE;
				list.add(new Symbol(this, name, type, new Addr32(peSym.n_value), peSym.getSize()));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getAddressFactory()
	 */
	@Override
	public IAddressFactory getAddressFactory() {
		if (addressFactory == null) {
			addressFactory = new Addr32Factory();
		}
		return addressFactory;
	}
}
