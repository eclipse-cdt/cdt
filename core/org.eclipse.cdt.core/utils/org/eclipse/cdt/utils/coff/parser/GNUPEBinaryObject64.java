/*******************************************************************************
 * Copyright (c) 2000, 2024 Space Codesign Systems and others.
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
 *     QNX Software Systems - Initial CygwinPEBinaryObject class
 *     John Dallaway - Initial GNUPEBinaryObject64 class (#361)
 *     John Dallaway - Update for parity with GNU ELF implementation (#652)
 *******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.AR.ARHeader;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.IGnuToolFactory;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.cdt.utils.Symbol;
import org.eclipse.cdt.utils.coff.Coff64;
import org.eclipse.cdt.utils.coff.PE64;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/** @since 8.2 */
public class GNUPEBinaryObject64 extends PEBinaryObject64 {

	private Addr2line autoDisposeAddr2line;
	private Addr2line symbolLoadingAddr2line;
	private CPPFilt symbolLoadingCPPFilt;
	long starttime;

	public GNUPEBinaryObject64(IBinaryParser parser, IPath path, ARHeader header) {
		super(parser, path, header);
	}

	public GNUPEBinaryObject64(IBinaryParser parser, IPath path, int type) {
		super(parser, path, type);
	}

	@Override
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
		}
		if (stream == null) {
			stream = super.getContents();
		}
		return stream;
	}

	/** @since 8.4 */
	public Addr2line getAddr2line(boolean autodisposing) {
		if (!autodisposing) {
			return getAddr2line();
		}
		if (autoDisposeAddr2line == null) {
			autoDisposeAddr2line = getAddr2line();
			if (autoDisposeAddr2line != null) {
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
			starttime = System.currentTimeMillis(); // reset autodispose timeout
		}
		return autoDisposeAddr2line;
	}

	private synchronized void stopAddr2Line() {
		if (autoDisposeAddr2line != null) {
			autoDisposeAddr2line.dispose();
		}
		autoDisposeAddr2line = null;
	}

	private Addr2line getAddr2line() {
		IGnuToolFactory factory = getBinaryParser().getAdapter(IGnuToolFactory.class);
		if (factory != null) {
			return factory.getAddr2line(getPath());
		}
		return null;
	}

	/** @since 8.4 */
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
	protected void loadSymbols(PE64 pe) throws IOException {
		symbolLoadingAddr2line = getAddr2line(false);
		symbolLoadingCPPFilt = getCPPFilt();
		try {
			super.loadSymbols(pe);
		} finally {
			if (symbolLoadingAddr2line != null) {
				symbolLoadingAddr2line.dispose();
				symbolLoadingAddr2line = null;
			}
			if (symbolLoadingCPPFilt != null) {
				symbolLoadingCPPFilt.dispose();
				symbolLoadingCPPFilt = null;
			}
		}
	}

	@Override
	protected void addSymbols(Coff64.Symbol[] peSyms, byte[] table, List<Symbol> list) {
		for (Coff64.Symbol element : peSyms) {
			if ((element.n_sclass != Coff64.Symbol.SC_EXTERNAL) || (element.n_scnum <= 0)) {
				continue; // ignore non-external symbol
			}
			String name = element.toString();
			if (symbolLoadingCPPFilt != null) {
				try {
					name = symbolLoadingCPPFilt.getFunction(name);
				} catch (IOException e1) {
					symbolLoadingCPPFilt.dispose();
					symbolLoadingCPPFilt = null;
				}
			}
			IAddress addr = new Addr32(element.n_value);
			long size = element.getSize();
			int type = element.isFunction() ? ISymbol.FUNCTION : ISymbol.VARIABLE;
			if (symbolLoadingAddr2line != null) {
				try {
					String filename = symbolLoadingAddr2line.getFileName(addr);
					// addr2line returns "??" when it can not find the file
					IPath file = (filename != null && !filename.equals("??")) ? new Path(filename) : Path.EMPTY; //$NON-NLS-1$
					int startLine = symbolLoadingAddr2line.getLineNumber(addr);
					int endLine = symbolLoadingAddr2line.getLineNumber(addr.add(size - 1));
					list.add(new GNUPESymbol64(this, name, type, addr, size, file, startLine, endLine));
				} catch (IOException e) {
					symbolLoadingAddr2line.dispose();
					symbolLoadingAddr2line = null;
					// the symbol still needs to be added
					list.add(new GNUPESymbol64(this, name, type, addr, size));
				}
			} else {
				list.add(new GNUPESymbol64(this, name, type, addr, size));
			}
		}
	}

}
