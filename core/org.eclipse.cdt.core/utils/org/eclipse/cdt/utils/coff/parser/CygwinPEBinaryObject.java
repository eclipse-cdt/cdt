/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.utils.coff.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.utils.Addr2line;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.CPPFilt;
import org.eclipse.cdt.utils.CygPath;
import org.eclipse.cdt.utils.ICygwinToolsFactroy;
import org.eclipse.cdt.utils.Objdump;
import org.eclipse.cdt.utils.AR.ARHeader;
import org.eclipse.cdt.utils.coff.Coff;
import org.eclipse.cdt.utils.coff.PE;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/*
 * CygwinPEBinaryObject
 */
public class CygwinPEBinaryObject extends PEBinaryObject {

	private Addr2line autoDisposeAddr2line;
	private Addr2line symbolLoadingAddr2line;
	private CygPath symbolLoadingCygPath;
	private CPPFilt symbolLoadingCPPFilt;
	long starttime;

	/**
	 * @param parser
	 * @param path
	 * @param header
	 */
	public CygwinPEBinaryObject(IBinaryParser parser, IPath path, ARHeader header) {
		super(parser, path, header);
	}
	
	/**
	 * @param parser
	 * @param path
	 * @param executable
	 */
	public CygwinPEBinaryObject(IBinaryParser parser, IPath path, int type) {
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
		ICygwinToolsFactroy factory = (ICygwinToolsFactroy)getAdapter(ICygwinToolsFactroy.class);
		if (factory != null) {
			return factory.getAddr2line(getPath());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getCPPFilt()
	 */
	protected CPPFilt getCPPFilt() {
		ICygwinToolsFactroy factory = (ICygwinToolsFactroy)getAdapter(ICygwinToolsFactroy.class);
		if (factory != null) {
			return factory.getCPPFilt();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.BinaryObjectAdapter#getObjdump()
	 */
	protected Objdump getObjdump() {
		ICygwinToolsFactroy factory = (ICygwinToolsFactroy)getAdapter(ICygwinToolsFactroy.class);
		if (factory != null) {
			return factory.getObjdump(getPath());
		}
		return null;
	}

	/**
	 * @return
	 */
	protected CygPath getCygPath() {
		ICygwinToolsFactroy factory = (ICygwinToolsFactroy)getAdapter(ICygwinToolsFactroy.class);
		if (factory != null) {
			return factory.getCygPath();
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
		}
		if (stream == null) {
			stream = super.getContents();
		}
		return stream;
	}

	protected void loadSymbols(PE pe) throws IOException {
		symbolLoadingAddr2line = getAddr2line(false);
		symbolLoadingCPPFilt = getCPPFilt();
		symbolLoadingCygPath = getCygPath();
		super.loadSymbols(pe);
		if (symbolLoadingAddr2line != null) {
			symbolLoadingAddr2line.dispose();
			symbolLoadingAddr2line = null;
		}
		if (symbolLoadingCPPFilt != null) {
			symbolLoadingCPPFilt.dispose();
			symbolLoadingCPPFilt = null;
		}
		if (symbolLoadingCygPath != null) {
			symbolLoadingCygPath.dispose();
			symbolLoadingCygPath = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.coff.parser.PEBinaryObject#addSymbols(org.eclipse.cdt.utils.coff.Coff.Symbol[],
	 *      byte[], java.util.List)
	 */
	protected void addSymbols(Coff.Symbol[] peSyms, byte[] table, List list) {
		for (int i = 0; i < peSyms.length; i++) {
			if (peSyms[i].isFunction() || peSyms[i].isPointer() || peSyms[i].isArray()) {
				String name = peSyms[i].getName(table);
				if (name == null || name.trim().length() == 0 || !Character.isJavaIdentifierStart(name.charAt(0))) {
					continue;
				}
				int type = peSyms[i].isFunction() ? ISymbol.FUNCTION : ISymbol.VARIABLE;
				IAddress addr = new Addr32(peSyms[i].n_value);
				int size = 4;
				if (symbolLoadingCPPFilt != null) {
					try {
						name = symbolLoadingCPPFilt.getFunction(name);
					} catch (IOException e1) {
						symbolLoadingCPPFilt.dispose();
						symbolLoadingCPPFilt = null;
					}
				}
				if (symbolLoadingAddr2line != null) {
					try {
						String filename = symbolLoadingAddr2line.getFileName(addr);
						// Addr2line returns the funny "??" when it can not find
						// the file.
						if (filename != null && filename.equals("??")) { //$NON-NLS-1$
							filename = null;
						}

						if (filename != null) {
							try {
								if (symbolLoadingCygPath != null) {
									filename = symbolLoadingCygPath.getFileName(filename);
								}
							} catch (IOException e) {
								symbolLoadingCygPath.dispose();
								symbolLoadingCygPath = null;
							}
						}
						IPath file = filename != null ? new Path(filename) : Path.EMPTY;
						int startLine = symbolLoadingAddr2line.getLineNumber(addr);
						int endLine = symbolLoadingAddr2line.getLineNumber(addr.add(BigInteger.valueOf(size - 1)));
						list.add(new CygwinSymbol(this, name, type, addr, size, file, startLine, endLine));
					} catch (IOException e) {
						symbolLoadingAddr2line.dispose();
						symbolLoadingAddr2line = null;
						// the symbol still needs to be added
						list.add(new CygwinSymbol(this, name, type, addr, size));
					}
				} else {
					list.add(new CygwinSymbol(this, name, type, addr, size));
				}

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
		} else if (adapter == CygPath.class) {
			return getCygPath();
		}
		return super.getAdapter(adapter);
	}
}