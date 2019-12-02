/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
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
import org.eclipse.cdt.utils.coff.Coff;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.core.runtime.IPath;

/**
 * @deprecated. Deprecated as of CDT 6.9. Use 64 bit version {@link PEBinaryObject64}.
 * This class is planned for removal in next major release.
 */
@Deprecated
public class PEBinaryObject extends BinaryObjectAdapter {

	BinaryObjectInfo info;
	IAddressFactory addressFactory;
	ISymbol[] symbols;
	AR.ARHeader header;

	public PEBinaryObject(IBinaryParser parser, IPath path, AR.ARHeader header) {
		super(parser, path, IBinaryFile.OBJECT);
	}

	public PEBinaryObject(IBinaryParser parser, IPath p, int type) {
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
		if (adapter.equals(PE.class)) {
			try {
				if (header != null) {
					return (T) new PE(getPath().toOSString(), header.getObjectDataOffset());
				}
				return (T) new PE(getPath().toOSString());
			} catch (IOException e) {
			}
		}
		if (adapter.equals(ISymbolReader.class)) {
			PE pe = getAdapter(PE.class);
			if (pe != null) {
				return (T) pe.getSymbolReader();
			}
		}
		return super.getAdapter(adapter);
	}

	protected PE getPE() throws IOException {
		if (header != null) {
			return new PE(getPath().toOSString(), header.getObjectDataOffset());
		}
		return new PE(getPath().toOSString());
	}

	protected void loadAll() throws IOException {
		try (PE pe = getPE()) {
			loadInfo(pe);
			loadSymbols(pe);
		}
	}

	protected void loadInfo() throws IOException {
		try (PE pe = getPE()) {
			loadInfo(pe);
		}
	}

	protected void loadInfo(PE pe) throws IOException {
		info = new BinaryObjectInfo();
		PE.Attribute attribute = pe.getAttribute();
		info.isLittleEndian = attribute.isLittleEndian();
		info.hasDebug = attribute.hasDebug();
		info.cpu = attribute.getCPU();
	}

	protected void loadSymbols(PE pe) throws IOException {
		ArrayList<Symbol> list = new ArrayList<>();
		loadSymbols(pe, list);
		symbols = list.toArray(NO_SYMBOLS);
		Arrays.sort(symbols);
		list.clear();
	}

	protected void loadSymbols(PE pe, List<Symbol> list) throws IOException {
		Coff.Symbol[] peSyms = pe.getSymbols();
		byte[] table = pe.getStringTable();
		addSymbols(peSyms, table, list);
	}

	protected void addSymbols(Coff.Symbol[] peSyms, byte[] table, List<Symbol> list) {
		for (org.eclipse.cdt.utils.coff.Coff.Symbol peSym : peSyms) {
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
