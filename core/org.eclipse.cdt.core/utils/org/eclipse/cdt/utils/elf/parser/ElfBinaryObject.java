/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.elf.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.AR;
import org.eclipse.cdt.utils.Addr32Factory;
import org.eclipse.cdt.utils.BinaryObjectAdapter;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.ElfHelper;
import org.eclipse.core.runtime.IPath;

/*
 * ElfBinaryObject
 */
public class ElfBinaryObject extends BinaryObjectAdapter {

	private BinaryObjectInfo info;
	private ISymbol[] symbols;
	private final AR.ARHeader header;
	private IAddressFactory addressFactory;
	private volatile Elf.Attribute fElfAttributes;

	public ElfBinaryObject(IBinaryParser parser, IPath p, AR.ARHeader h){
		super(parser, p, IBinaryFile.OBJECT);
		header = h;
	}

	public ElfBinaryObject(IBinaryParser parser, IPath p, int type){
		super(parser, p, type);
		header = null;
	}

	/**
	 * @param elfAttributes the elfAttributes to set
	 */
	void setElfAttributes(Elf.Attribute elfAttributes) {
		fElfAttributes= elfAttributes;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryObject#getSymbols()
	 */
	@Override
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
	@Override
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
		if (header != null) {
			return new ElfHelper(header.getArchiveName(), header.getObjectDataOffset());
		}
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
		addressFactory = attribute.getAddressFactory();
		fElfAttributes= null;
	}

	protected void loadSymbols(ElfHelper helper) throws IOException {
		ArrayList<Symbol> list = new ArrayList<Symbol>();

//		addSymbols(helper.getExternalFunctions(), ISymbol.FUNCTION, list);
		addSymbols(helper.getLocalFunctions(), ISymbol.FUNCTION, list);
//		addSymbols(helper.getExternalObjects(), ISymbol.VARIABLE, list);
		addSymbols(helper.getLocalObjects(), ISymbol.VARIABLE, list);
		list.trimToSize();

		symbols = list.toArray(NO_SYMBOLS);
		Arrays.sort(symbols);
		list.clear();
	}

	protected void addSymbols(Elf.Symbol[] array, int type, List<Symbol> list) {
		for (org.eclipse.cdt.utils.elf.Elf.Symbol element : array) {
			// Multiple function symbol entries for the same address are generated
			// do not add duplicate symbols with 0 size to the list
			boolean duplicateAddressFound = false;
			if (type == ISymbol.FUNCTION && element.st_size == 0){
				for (Symbol s : list) {
					if (s.getAddress().equals(element.st_value)){
						duplicateAddressFound = true;
						break;
					}
				}
			}
			if (!duplicateAddressFound)	
				list.add(new Symbol(this, element.toString(), type, element.st_value, element.st_size));
		}
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(Elf.class)) {
			try {
				return new Elf(getPath().toOSString());
			} catch (IOException e) {
			}
		}
		if (adapter.equals(ISymbolReader.class)) {
			Elf elf = (Elf)getAdapter(Elf.class);
			if (elf != null) {
				return elf.getSymbolReader();
			}
		}
		return super.getAdapter(adapter);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getAddressFactory()
	 */
	@Override
	public IAddressFactory getAddressFactory() {
		if (addressFactory == null) {
			if (fElfAttributes != null) {
				addressFactory= fElfAttributes.getAddressFactory();
			}
			if (addressFactory == null) {
				try {
					loadInfo();
				} catch (IOException e) {
					return new Addr32Factory();
				}
			}
		}
		return addressFactory;
	}
	
	/*
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#isLittleEndian()
	 */
	@Override
	public boolean isLittleEndian() {
		if (fElfAttributes != null) {
			return fElfAttributes.isLittleEndian();
		}
		return super.isLittleEndian();
	}
	
	/*
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getCPU()
	 */
	@Override
	public String getCPU() {
		if (fElfAttributes != null) {
			return fElfAttributes.getCPU();
		}
		return super.getCPU();
	}
}
