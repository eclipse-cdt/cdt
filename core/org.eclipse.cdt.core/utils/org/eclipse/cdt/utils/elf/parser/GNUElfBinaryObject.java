/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.utils.elf.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.cdt.utils.elf.Elf;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/*
 * GNUBinaryObject
 */
public class GNUElfBinaryObject extends ElfBinaryObject {

	private Addr2line addr2line;

	/**
	 * @param parser
	 * @param path
	 */
	public GNUElfBinaryObject(GNUElfParser parser, IPath path) {
		super(parser, path);
	}

	public Addr2line getAddr2line(boolean autodisposing) {
		if (!autodisposing) {
			GNUElfParser parser = (GNUElfParser) getBinaryParser();
			return parser.getAddr2line(getPath());
		}
		if (addr2line == null) {
			GNUElfParser parser = (GNUElfParser) getBinaryParser();
			addr2line = parser.getAddr2line(getPath());
			if (addr2line != null) {
				timestamp = System.currentTimeMillis();
				Runnable worker = new Runnable() {

					public void run() {
						long diff = System.currentTimeMillis() - timestamp;
						while (diff < 10000) {
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								break;
							}
							diff = System.currentTimeMillis() - timestamp;
						}
						stopAddr2Line();
					}
				};
				new Thread(worker, "Addr2line Reaper").start(); //$NON-NLS-1$
			}
		} else {
			timestamp = System.currentTimeMillis();
		}
		return addr2line;
	}

	synchronized void stopAddr2Line() {
		if (addr2line != null) {
			addr2line.dispose();
		}
		addr2line = null;
	}

	protected CPPFilt getCPPFilt() {
		GNUElfParser parser = (GNUElfParser) getBinaryParser();
		return parser.getCPPFilt();
	}

	protected Objdump getObjdump() {
		GNUElfParser parser = (GNUElfParser) getBinaryParser();
		return parser.getObjdump(getPath());
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.IBinaryFile#getContents()
	 */
	public InputStream getContents() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.elf.parser.ElfBinaryObject#addSymbols(org.eclipse.cdt.utils.elf.Elf.Symbol[],
	 *      int, java.util.List)
	 */
	protected void addSymbols(Elf.Symbol[] array, int type, List list) {
		CPPFilt cppfilt = getCPPFilt();
		Addr2line addr2line = getAddr2line(false);
		for (int i = 0; i < array.length; i++) {
			String name = array[i].toString();
			if (cppfilt != null) {
				try {
					name = cppfilt.getFunction(name);
				} catch (IOException e1) {
					cppfilt = null;
				}
			}
			IAddress addr = array[i].st_value;
			long size = array[i].st_size;
			if (addr2line != null) {
				try {
					String filename = addr2line.getFileName(addr);
					// Addr2line returns the funny "??" when it can not find
					// the file.
					IPath file = (filename != null && !filename.equals("??")) ? new Path(filename) : Path.EMPTY; //$NON-NLS-1$
					int startLine = addr2line.getLineNumber(addr);
					int endLine = addr2line.getLineNumber(addr.add(BigInteger.valueOf(size - 1)));
					list.add(new GNUSymbol(this, name, type, addr, size, file, startLine, endLine));
				} catch (IOException e) {
					addr2line = null;
					// the symbol still needs to be added
					list.add(new GNUSymbol(this, name, type, addr, size));
				}
			} else {
				list.add(new GNUSymbol(this, name, type, addr, size));
			}
		}
		if (cppfilt != null) {
			cppfilt.dispose();
		}
		if (addr2line != null) {
			addr2line.dispose();
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