/*******************************************************************************
 * Copyright (c) 2002, 2015 QNX Software Systems and others.
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
 *     Apple Computer - work on performance optimizations
 *******************************************************************************/
package org.eclipse.cdt.utils.macho.parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr32Factory;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.cdt.utils.Addr64Factory;
import org.eclipse.cdt.utils.BinaryObjectAdapter;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.cdt.utils.macho.AR;
import org.eclipse.cdt.utils.macho.MachO64;
import org.eclipse.cdt.utils.macho.MachOHelper64;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * MachOBinaryObject64
 * @since 5.2
 */
public class MachOBinaryObject64 extends BinaryObjectAdapter {

	protected AR.ARHeader header;
	protected IAddressFactory addressFactory;
	protected MachO64.Attribute attributes;
	protected MachOHelper64.Sizes sizes;
	protected ISymbol[] symbols;
	protected String soname;
	protected String[] needed;
	protected long timeStamp;
	protected boolean is64 = false;
	private static final String[] NO_NEEDED = new String[0];

	/**
	 * @param parser
	 * @param path
	 * @param header
	 */
	public MachOBinaryObject64(IBinaryParser parser, IPath path, AR.ARHeader header) {
		super(parser, path, IBinaryFile.OBJECT);
		this.header = header;
	}

	/**
	 * @param parser
	 * @param path
	 * @param type
	 */
	public MachOBinaryObject64(IBinaryParser parser, IPath path, int type) {
		super(parser, path, type);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getBinaryObjectInfo()
	 */
	@Override
	protected BinaryObjectInfo getBinaryObjectInfo() {
		// we don't use this method
		// overload to do nothing
		return new BinaryObjectInfo();
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

	protected MachOHelper64 getMachOHelper() throws IOException {
		IPath path = getPath();
		if (path != null) {
			if (header != null) {
				return new MachOHelper64(path.toOSString(), header.getObjectDataOffset());
			} else {
				return new MachOHelper64(path.toOSString());
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getName()
	 */
	@Override
	public String getName() {
		if (header != null) {
			return header.getObjectName();
		}
		return super.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getAddressFactory()
	 */
	@Override
	public IAddressFactory getAddressFactory() {
		if (addressFactory == null) {
			if (soname == null)
				loadBinaryInfo();
			if (is64) {
				addressFactory = new Addr64Factory();
			} else {
				addressFactory = new Addr32Factory();
			}
		}
		return addressFactory;
	}

	protected void clearCachedValues() {
		attributes = null;
		sizes = null;
		symbols = null;
		soname = null;
		needed = null;
	}

	protected MachO64.Attribute internalGetAttributes() {
		if (hasChanged()) {
			clearCachedValues();
		}
		if (attributes == null) {
			MachOHelper64 helper = null;
			try {
				helper = getMachOHelper();
				if (helper != null) {
					attributes = helper.getMachO().getAttributes();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (helper != null) {
					helper.dispose();
				}
			}
		}
		return attributes;
	}

	protected MachOHelper64.Sizes internalGetSizes() {
		if (hasChanged()) {
			clearCachedValues();
		}
		if (sizes == null) {
			MachOHelper64 helper = null;
			try {
				helper = getMachOHelper();
				if (helper != null) {
					sizes = helper.getSizes();
					// since we're invoking the helper we might as well update
					// the attributes since it's a pretty lightweight operation
					if (attributes == null) {
						attributes = helper.getMachO().getAttributes();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (helper != null) {
					helper.dispose();
				}
			}
		}
		return sizes;
	}

	protected ISymbol[] internalGetSymbols() {
		if (hasChanged()) {
			clearCachedValues();
		}
		if (symbols == null) {
			loadBinaryInfo();
		}
		return symbols;
	}

	protected String internalGetSoName() {
		if (hasChanged()) {
			clearCachedValues();
		}
		if (soname == null) {
			loadBinaryInfo();
		}
		return soname;
	}

	protected String[] internalGetNeeded() {
		if (hasChanged()) {
			clearCachedValues();
		}
		if (needed == null) {
			loadBinaryInfo();
		}
		return needed;
	}

	protected void loadBinaryInfo() {
		MachOHelper64 helper = null;
		try {
			helper = getMachOHelper();
			if (helper != null) {
				//TODO we can probably optimize this further in MachOHelper
				symbols = loadSymbols(helper);
				//TODO is the sort necessary?
				Arrays.sort(symbols);
				soname = helper.getSoname();
				needed = helper.getNeeded();
				is64 = helper.is64();
				// since we're invoking the helper we might as well update the
				// sizes since it's a pretty lightweight operation by comparison
				if (sizes == null) {
					sizes = helper.getSizes();
				}
				// since we're invoking the helper we might as well update the
				// attributes since it's a pretty lightweight operation by comparison
				if (attributes == null) {
					attributes = helper.getMachO().getAttributes();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			symbols = NO_SYMBOLS;
		} finally {
			if (helper != null) {
				helper.dispose();
			}
		}
	}

	protected ISymbol[] loadSymbols(MachOHelper64 helper) throws IOException {
		CPPFilt cppfilt = null;
		try {
			ArrayList<Symbol> list = new ArrayList<>();
			// Hack should be remove when Elf is clean
			helper.getMachO().setCppFilter(false);
			cppfilt = getCPPFilt();
			//TODO we can probably optimize this further in MachOHelper64
			addSymbols(helper.getExternalFunctions(), ISymbol.FUNCTION, cppfilt, list);
			addSymbols(helper.getLocalFunctions(), ISymbol.FUNCTION, cppfilt, list);
			addSymbols(helper.getExternalObjects(), ISymbol.VARIABLE, cppfilt, list);
			addSymbols(helper.getLocalObjects(), ISymbol.VARIABLE, cppfilt, list);
			return list.toArray(new ISymbol[list.size()]);
		} finally {
			if (cppfilt != null) {
				cppfilt.dispose();
			}
		}
	}

	protected CPPFilt getCPPFilt() {
		MachOParser64 parser = (MachOParser64) getBinaryParser();
		return parser.getCPPFilt();
	}

	private void addSymbols(MachO64.Symbol[] array, int type, CPPFilt cppfilt, List<Symbol> list) {
		for (MachO64.Symbol element : array) {
			String name = element.toString();
			if (cppfilt != null) {
				try {
					name = cppfilt.getFunction(name);
				} catch (IOException e1) {
					cppfilt = null;
				}
			}
			long addr = element.n_value;
			int size = 0;
			String filename = element.getFilename();
			IPath filePath = (filename != null) ? new Path(filename) : null;
			IAddress symbolAddr;
			if (element.is64)
				symbolAddr = new Addr32(element.n_value);
			else
				symbolAddr = new Addr64(BigInteger.valueOf(element.n_value));
			list.add(new Symbol(this, name, type, symbolAddr, size, filePath, element.getLineNumber(addr),
					element.getLineNumber(addr + size - 1)));
		}
	}

	@Override
	public String getCPU() {
		MachO64.Attribute attribute = internalGetAttributes();
		if (attribute != null) {
			return attribute.getCPU();
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public boolean hasDebug() {
		MachO64.Attribute attribute = internalGetAttributes();
		if (attribute != null) {
			return attribute.hasDebug();
		}
		return false;
	}

	@Override
	public boolean isLittleEndian() {
		MachO64.Attribute attribute = internalGetAttributes();
		if (attribute != null) {
			return attribute.isLittleEndian();
		}
		return false;
	}

	@Override
	public long getBSS() {
		MachOHelper64.Sizes size = internalGetSizes();
		if (size != null) {
			return size.bss;
		}
		return 0;
	}

	@Override
	public long getData() {
		MachOHelper64.Sizes size = internalGetSizes();
		if (size != null) {
			return size.data;
		}
		return 0;
	}

	@Override
	public long getText() {
		MachOHelper64.Sizes size = internalGetSizes();
		if (size != null) {
			return size.text;
		}
		return 0;
	}

	@Override
	public ISymbol[] getSymbols() {
		ISymbol[] syms = internalGetSymbols();
		if (syms != null) {
			return syms;
		}
		return NO_SYMBOLS;
	}

	@Override
	public ISymbol getSymbol(IAddress addr) {
		//TODO should this be cached?
		// fall back to super implementation for now
		return super.getSymbol(addr);
	}

	@Override
	public String[] getNeededSharedLibs() {
		String[] libs = internalGetNeeded();
		if (libs != null) {
			return libs;
		}
		return NO_NEEDED;
	}

	@Override
	public String getSoName() {
		String name = internalGetSoName();
		if (name != null) {
			return name;
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	protected boolean hasChanged() {
		IPath path = getPath();
		if (path != null) {
			File file = path.toFile();
			if (file != null) {
				long modification = file.lastModified();
				if (modification != timeStamp) {
					timeStamp = modification;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.equals(MachO64.class)) {
			try {
				return (T) new MachO64(getPath().toOSString());
			} catch (IOException e) {
			}
		}
		if (adapter.equals(ISymbolReader.class)) {
			MachO64 macho = getAdapter(MachO64.class);
			if (macho != null) {
				return (T) macho.getSymbolReader();
			}
		}
		return super.getAdapter(adapter);
	}

}
