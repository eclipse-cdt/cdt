/*******************************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.macho.parser;

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
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr32Factory;
import org.eclipse.cdt.utils.BinaryObjectAdapter;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.cdt.utils.macho.AR;
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
	private AR.ARHeader header;
	private IAddressFactory addressFactory;

	/**
	 * @param parser
	 * @param path
	 * @param header
	 */
	public MachOBinaryObject(IBinaryParser parser, IPath path, AR.ARHeader header) {
		super(parser, path, IBinaryFile.OBJECT);
	}

	/**
	 * @param parser
	 * @param path
	 * @param type
	 */
	public MachOBinaryObject(IBinaryParser parser, IPath path, int type) {
		super(parser, path, type);
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

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() throws IOException {
		if (getPath() != null && header != null) {
			return new ByteArrayInputStream(header.getObjectData());
		}
		return super.getContents();
	}
	
	protected MachOHelper getMachOHelper() throws IOException {
		if (header != null) {
			return new MachOHelper(getPath().toOSString(), header.getObjectDataOffset());
		}
		return new MachOHelper(getPath().toOSString());
	}
	
	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryObject#getName()
	 */
	public String getName() {
		if (header != null) {
			return header.getObjectName();
		}
		return super.getName();
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

	protected CPPFilt getCPPFilt() {
		MachOParser parser = (MachOParser) getBinaryParser();
		return parser.getCPPFilt();
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
			String name = array[i].toString();
			if (cppfilt != null) {
				try {
					name = cppfilt.getFunction(name);
				} catch (IOException e1) {
					cppfilt = null;
				}
			}
			long addr = array[i].n_value;
			int size = 0;
			String filename = array[i].getFilename();
			IPath filePath = (filename != null) ? new Path(filename) : null; //$NON-NLS-1$
			list.add(new Symbol(this, name, type, new Addr32(array[i].n_value), size, filePath, array[i].getLineNumber(addr), array[i].getLineNumber(addr + size - 1)));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getAddressFactory()
	 */
	public IAddressFactory getAddressFactory() {
		if (addressFactory == null) {
			addressFactory = new Addr32Factory();
		}
		return addressFactory;
	}
}
