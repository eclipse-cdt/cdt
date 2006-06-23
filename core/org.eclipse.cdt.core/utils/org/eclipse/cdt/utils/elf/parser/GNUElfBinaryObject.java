/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.elf.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.IGnuToolFactory;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.cdt.utils.AR.ARHeader;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.cdt.utils.elf.ElfHelper;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/*
 * GNUBinaryObject
 */
public class GNUElfBinaryObject extends ElfBinaryObject {

	private Addr2line autoDisposeAddr2line;
	private Addr2line symbolLoadingAddr2line;
	private CPPFilt symbolLoadingCPPFilt;
	long starttime;
	
	/**
	 * @param parser
	 * @param path
	 * @param header
	 */
	public GNUElfBinaryObject(IBinaryParser parser, IPath path, ARHeader header) {
		super(parser, path, header);
	}

	/**
	 * @param parser
	 * @param path
	 * @param type
	 */
	public GNUElfBinaryObject(IBinaryParser parser, IPath path, int type) {
		super(parser, path, type);
	}

	public Addr2line getAddr2line(boolean autodisposing) {
		if (!autodisposing) {
			return getAddr2line();
		}
		if (autoDisposeAddr2line == null) {
			autoDisposeAddr2line = getAddr2line();
			if (autoDisposeAddr2line != null) {
				starttime = System.currentTimeMillis();
				Runnable worker = new Runnable() {

					public void run() {

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
					}
				};
				new Thread(worker, "Addr2line Reaper").start(); //$NON-NLS-1$
			}
		} else {
			starttime = System.currentTimeMillis(); // reset autodispose timeout
		}
		return autoDisposeAddr2line;
	}

	synchronized void stopAddr2Line() {
		if (autoDisposeAddr2line != null) {
			autoDisposeAddr2line.dispose();
		}
		autoDisposeAddr2line = null;
	}

	private Addr2line getAddr2line() {
		IGnuToolFactory factory = (IGnuToolFactory)getBinaryParser().getAdapter(IGnuToolFactory.class);
		if (factory != null) {
			return factory.getAddr2line(getPath());
		}
		return null;
	}
 
	protected CPPFilt getCPPFilt() {
		IGnuToolFactory factory = (IGnuToolFactory)getBinaryParser().getAdapter(IGnuToolFactory.class);
		if (factory != null) {
			return factory.getCPPFilt();
		}
		return null;
	}

	protected Objdump getObjdump() {
		IGnuToolFactory factory = (IGnuToolFactory)getBinaryParser().getAdapter(IGnuToolFactory.class);
		if (factory != null) {
			return factory.getObjdump(getPath());
		}
		return null;
	}

	/**
	 * @throws IOException
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() throws IOException {
		InputStream stream = null;
		Objdump objdump = getObjdump();
		if (objdump != null) {
			try {
				byte[] contents = objdump.getOutput();
				stream = new ByteArrayInputStream(contents);
			} catch (IOException e) {
				// Nothing
			}
			objdump.dispose();
		}
		if (stream == null) {
			stream = super.getContents();
		}
		return stream;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.elf.parser.ElfBinaryObject#loadSymbols(org.eclipse.cdt.utils.elf.ElfHelper)
	 */
	protected void loadSymbols(ElfHelper helper) throws IOException {
		symbolLoadingAddr2line = getAddr2line(false);
		symbolLoadingCPPFilt = getCPPFilt();
		super.loadSymbols(helper);
		if (symbolLoadingAddr2line != null) {
			symbolLoadingAddr2line.dispose();
			symbolLoadingAddr2line = null;
		}
		if (symbolLoadingCPPFilt != null) {
			symbolLoadingCPPFilt.dispose();
			symbolLoadingCPPFilt =  null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.elf.parser.ElfBinaryObject#addSymbols(org.eclipse.cdt.utils.elf.Elf.Symbol[],
	 *      int, java.util.List)
	 */
	protected void addSymbols(Elf.Symbol[] array, int type, List list) {
		for (int i = 0; i < array.length; i++) {
			String name = array[i].toString();
			if (symbolLoadingCPPFilt != null) {
				try {
					name = symbolLoadingCPPFilt.getFunction(name);
				} catch (IOException e1) {
					symbolLoadingCPPFilt.dispose();
					symbolLoadingCPPFilt = null;
				}
			}
			IAddress addr = array[i].st_value;
			long size = array[i].st_size;
			if (symbolLoadingAddr2line != null) {
				try {
					String filename = symbolLoadingAddr2line.getFileName(addr);
					// Addr2line returns the funny "??" when it can not find
					// the file.
					IPath file = (filename != null && !filename.equals("??")) ? new Path(filename) : Path.EMPTY; //$NON-NLS-1$
					int startLine = symbolLoadingAddr2line.getLineNumber(addr);
					int endLine = symbolLoadingAddr2line.getLineNumber(addr.add(size - 1));
					list.add(new GNUSymbol(this, name, type, addr, size, file, startLine, endLine));
				} catch (IOException e) {
					symbolLoadingAddr2line.dispose();
					symbolLoadingAddr2line = null;
					// the symbol still needs to be added
					list.add(new GNUSymbol(this, name, type, addr, size));
				}
			} else {
				list.add(new GNUSymbol(this, name, type, addr, size));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == Addr2line.class) {
			return getAddr2line(false);
		} else if (adapter == CPPFilt.class) {
			return getCPPFilt();
		}
		return super.getAdapter(adapter);
	}
}
