/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ISymbolReader;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr32Factory;
import org.eclipse.cdt.utils.BinaryObjectAdapter;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.macho.AR;
import org.eclipse.cdt.utils.macho.MachO;
import org.eclipse.cdt.utils.macho.MachOHelper;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/*
 * MachOBinaryObject 
 */
public class MachOBinaryObject extends BinaryObjectAdapter {

	protected AR.ARHeader header;
	protected IAddressFactory addressFactory;
	protected MachO.Attribute attributes;
	protected MachOHelper.Sizes sizes;
	protected ISymbol[] symbols;
	protected String soname;
	protected String[] needed;
	protected long timeStamp;
	private static final String[] NO_NEEDED = new String[0];

	
	/**
	 * @param parser
	 * @param path
	 * @param header
	 */
	public MachOBinaryObject(IBinaryParser parser, IPath path, AR.ARHeader header) {
		super(parser, path, IBinaryFile.OBJECT);
		this.header = header;
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
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getBinaryObjectInfo()
	 */
	protected BinaryObjectInfo getBinaryObjectInfo() {
		// we don't use this method
		// overload to do nothing
		return new BinaryObjectInfo();
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
		IPath path = getPath();
		if (path != null) {
			if (header != null) {
				return new MachOHelper(path.toOSString(), header.getObjectDataOffset());
			} else {
				return new MachOHelper(path.toOSString());
			}
		}
		return null;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getAddressFactory()
	 */
	public IAddressFactory getAddressFactory() {
		if (addressFactory == null) {
			addressFactory = new Addr32Factory();
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
	
	protected MachO.Attribute internalGetAttributes() {
		if (hasChanged()) {
			clearCachedValues();
		}
		if (attributes == null) {
			MachOHelper helper = null;
			try {
				helper = getMachOHelper();
				if (helper != null) {
					attributes = helper.getMachO().getAttributes();
				}
			} catch (IOException e) {
			} finally {
				if (helper != null) {
					helper.dispose();
				}
			}
		}
		return attributes;
	}
	
	protected MachOHelper.Sizes internalGetSizes() {
		if (hasChanged()) {
			clearCachedValues();
		}
		if (sizes == null) {
			MachOHelper helper = null;
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
		MachOHelper helper = null;
		try {
			helper = getMachOHelper();
			if (helper != null) {
				//TODO we can probably optimize this further in MachOHelper

				symbols = loadSymbols(helper);
				//TODO is the sort necessary?
				Arrays.sort(symbols);
				
				soname = helper.getSoname();
				needed = helper.getNeeded();
				
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
			symbols = NO_SYMBOLS;
		} finally {
			if (helper != null) {
				helper.dispose();
			}
		}
	}

	protected ISymbol[] loadSymbols(MachOHelper helper) throws IOException {
		CPPFilt cppfilt =  null;
		try {
			ArrayList list = new ArrayList();
			// Hack should be remove when Elf is clean
			helper.getMachO().setCppFilter(false);
			cppfilt = getCPPFilt();
			//TODO we can probably optimize this further in MachOHelper
			addSymbols(helper.getExternalFunctions(), ISymbol.FUNCTION, cppfilt, list);
			addSymbols(helper.getLocalFunctions(), ISymbol.FUNCTION, cppfilt, list);
			addSymbols(helper.getExternalObjects(), ISymbol.VARIABLE, cppfilt, list);
			addSymbols(helper.getLocalObjects(), ISymbol.VARIABLE, cppfilt, list);
			return (ISymbol[]) list.toArray(new ISymbol[list.size()]);
		} finally {
			if (cppfilt != null) {
				cppfilt.dispose();
			}
		}
	}

	protected CPPFilt getCPPFilt() {
		MachOParser parser = (MachOParser) getBinaryParser();
		return parser.getCPPFilt();
	}

	private void addSymbols(MachO.Symbol[] array, int type, CPPFilt cppfilt, List list) {
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
	
	public String getCPU() {
		MachO.Attribute attribute = internalGetAttributes();
		if (attribute != null) {
			return attribute.getCPU();
		}
		return ""; //$NON-NLS-1$
	}

	public boolean hasDebug() {
		MachO.Attribute attribute = internalGetAttributes();
		if (attribute != null) {
			return attribute.hasDebug();
		}
		return false;
	}

	public boolean isLittleEndian() {
		MachO.Attribute attribute = internalGetAttributes();
		if (attribute != null) {
			return attribute.isLittleEndian();
		}
		return false;
	}

	public long getBSS() {
		MachOHelper.Sizes size = internalGetSizes();
		if (size != null) {
			return size.bss;
		}
		return 0;
	}

	public long getData() {
		MachOHelper.Sizes size = internalGetSizes();
		if (size != null) {
			return size.data;
		}
		return 0;
	}

	public long getText() {
		MachOHelper.Sizes size = internalGetSizes();
		if (size != null) {
			return size.text;
		}
		return 0;
	}

	public ISymbol[] getSymbols() {
		ISymbol[] syms = internalGetSymbols();
		if (syms != null) {
			return syms;
		}
		return NO_SYMBOLS;
	}

	public ISymbol getSymbol(IAddress addr) {
		//TODO should this be cached?
		// fall back to super implementation for now
		return super.getSymbol(addr);
	}
	
	public String[] getNeededSharedLibs() {
		String[] libs = internalGetNeeded();
		if (libs != null) {
			return libs;
		}
		return NO_NEEDED;
	}

	public String getSoName() {
		String name = internalGetSoName();
		if (name != null) {
			return name;
		}
		return ""; //$NON-NLS-1$
	}

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
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(MachO.class)) {
			try {
				return new MachO(getPath().toOSString());
			} catch (IOException e) {
			}
		}
		if (adapter.equals(ISymbolReader.class)) {
			MachO macho = (MachO)getAdapter(MachO.class);
			if (macho != null) {
				return macho.getSymbolReader();
			}
		}
		return super.getAdapter(adapter);
	}

}
