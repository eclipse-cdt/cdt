/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.som.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr32Factory;
import org.eclipse.cdt.utils.BinaryObjectAdapter;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.IGnuToolFactory;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.cdt.utils.som.AR;
import org.eclipse.cdt.utils.som.AR.ARHeader;
import org.eclipse.cdt.utils.som.SOM;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Binary file in HP-UX SOM format
 *
 * @author vhirsl
 */
public class SOMBinaryObject extends BinaryObjectAdapter {

	Addr2line addr2line;
	BinaryObjectInfo info;
	ISymbol[] symbols;
	long starttime;
	private ARHeader header;
	private IAddressFactory addressFactory;

	/**
	 * @param parser
	 * @param path
	 * @param header
	 */
	public SOMBinaryObject(IBinaryParser parser, IPath path, AR.ARHeader header) {
		super(parser, path, IBinaryFile.OBJECT);
		this.header = header;
	}

	/**
	 * @param parser
	 * @param path
	 * @param type
	 */
	public SOMBinaryObject(IBinaryParser parser, IPath path, int type) {
		super(parser, path, type);
	}

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
	public String getName() {
		if (header != null) {
			return header.getObjectName();
		}
		return super.getName();
	}

	@Override
	public InputStream getContents() throws IOException {
		InputStream stream = null;
		if (getPath() != null && header != null) {
			return new ByteArrayInputStream(header.getObjectData());
		}
		Objdump objdump = getObjdump();
		if (objdump != null) {
			try {
				byte[] contents = objdump.getOutput();
				stream = new ByteArrayInputStream(contents);
			} catch (IOException e) {
				// Nothing
			}
		}
		if (stream == null) {
			stream = super.getContents();
		}
		return stream;
	}

	protected SOM getSOM() throws IOException {
		if (header != null) {
			return new SOM(getPath().toOSString(), header.getObjectDataOffset());
		}
		return new SOM(getPath().toOSString());
	}

	protected void loadAll() throws IOException {
		SOM som = null;
		try {
			som = getSOM();
			loadInfo(som);
			loadSymbols(som);
		} finally {
			if (som != null) {
				som.dispose();
			}
		}
	}

	protected void loadInfo() throws IOException {
		SOM som = null;
		try {
			som = getSOM();
			loadInfo(som);
		} finally {
			if (som != null) {
				som.dispose();
			}
		}
	}

	protected void loadInfo(SOM som) throws IOException {
		info = new BinaryObjectInfo();
		SOM.Attribute attribute = som.getAttributes();
		info.isLittleEndian = attribute.isLittleEndian();
		info.hasDebug = attribute.hasDebug();
		info.cpu = attribute.getCPU();
	}

	protected void loadSymbols(SOM som) throws IOException {
		ArrayList<Symbol> list = new ArrayList<>();

		SOM.Symbol[] peSyms = som.getSymbols();
		byte[] table = som.getStringTable();
		addSymbols(peSyms, table, list);

		symbols = list.toArray(NO_SYMBOLS);
		Arrays.sort(symbols);
		list.clear();
	}

	protected void addSymbols(SOM.Symbol[] peSyms, byte[] table, List<Symbol> list) {
		CPPFilt cppfilt = getCPPFilt();
		Addr2line addr2line = getAddr2line(false);
		for (org.eclipse.cdt.utils.som.SOM.Symbol peSym : peSyms) {
			if (peSym.isFunction() || peSym.isVariable()) {
				String name = peSym.getName(table);
				if (name == null || name.trim().length() == 0 || !Character.isJavaIdentifierStart(name.charAt(0))) {
					continue;
				}
				int type = peSym.isFunction() ? ISymbol.FUNCTION : ISymbol.VARIABLE;
				IAddress addr = new Addr32(peSym.symbol_value);
				int size = 4;
				if (cppfilt != null) {
					try {
						name = cppfilt.getFunction(name);
					} catch (IOException e1) {
						cppfilt = null;
					}
				}
				if (addr2line != null) {
					try {
						String filename = addr2line.getFileName(addr);
						// Addr2line returns the funny "??" when it can not find
						// the file.
						if (filename != null && filename.equals("??")) { //$NON-NLS-1$
							filename = null;
						}

						IPath file = filename != null ? new Path(filename) : Path.EMPTY;
						int startLine = addr2line.getLineNumber(addr);
						int endLine = addr2line.getLineNumber(addr.add(size - 1));
						list.add(new SomSymbol(this, name, type, addr, size, file, startLine, endLine));
					} catch (IOException e) {
						addr2line = null;
						// the symbol still needs to be added
						list.add(new SomSymbol(this, name, type, addr, size));
					}
				} else {
					list.add(new SomSymbol(this, name, type, addr, size));
				}
			}
		}
		if (cppfilt != null) {
			cppfilt.dispose();
		}
		if (addr2line != null) {
			addr2line.dispose();
		}
	}

	public Addr2line getAddr2line(boolean autodisposing) {
		if (!autodisposing) {
			return getAddr2line();
		}
		if (addr2line == null) {
			addr2line = getAddr2line();
			if (addr2line != null) {
				starttime = System.currentTimeMillis();
				Runnable worker = () -> {
					long diff = System.currentTimeMillis() - starttime;
					while (diff < 10000) {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							break;
						}
						diff = System.currentTimeMillis() - starttime;
					}
					stopAddr2Line();
				};
				new Thread(worker, "Addr2line Reaper").start(); //$NON-NLS-1$
			}
		} else {
			starttime = System.currentTimeMillis();
		}
		return addr2line;
	}

	synchronized void stopAddr2Line() {
		if (addr2line != null) {
			addr2line.dispose();
		}
		addr2line = null;
	}

	/**
	 * @return
	 */
	private Addr2line getAddr2line() {
		IGnuToolFactory factory = getBinaryParser().getAdapter(IGnuToolFactory.class);
		if (factory != null) {
			return factory.getAddr2line(getPath());
		}
		return null;
	}

	protected CPPFilt getCPPFilt() {
		IGnuToolFactory factory = getBinaryParser().getAdapter(IGnuToolFactory.class);
		if (factory != null) {
			return factory.getCPPFilt();
		}
		return null;
	}

	protected Objdump getObjdump() {
		IGnuToolFactory factory = getBinaryParser().getAdapter(IGnuToolFactory.class);
		if (factory != null) {
			return factory.getObjdump(getPath());
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == Addr2line.class) {
			return (T) getAddr2line(false);
		} else if (adapter == CPPFilt.class) {
			return (T) getCPPFilt();
		}
		return super.getAdapter(adapter);
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
