/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.io.IOException;

import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.core.runtime.IPath;

public class Symbol implements ISymbol, Comparable {

	BinaryObjectAdapter binary;
	long timestamp;
	Addr2line addr2line;

	public IPath filename;
	public int startLine;
	public int endLine;
	public long addr;
	public String name;
	public int type;
	public long size;

	public Symbol(BinaryObjectAdapter bin) {
		binary = bin;
	}
	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.ISymbol#getInitialFilename()
	 */
	public IPath getFilename() {
		return filename;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.ISymbol#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.eclipse.cdt.core.model.IBinaryParser.ISymbol#getType()
	 */
	public int getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getAdress()
	 */
	public long getAddress() {
		return addr;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getEndLine()
	 */
	public int getEndLine() {
		return endLine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getStartLine()
	 */
	public int getStartLine() {
		return startLine;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getLineNumber(long)
	 */
	public int getLineNumber(long offset) {
		int line = -1;
		try {
			Addr2line addressToLine = startAddr2Line();
			if (addressToLine != null) {
				line = addressToLine.getLineNumber(addr + offset);
			}
		} catch (IOException e) {		
		}
		return line;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.IBinaryParser.ISymbol#getSize()
	 */
	public long getSize() {
		return size;
	}

	public int compareTo(Object obj) {
		long thisVal = 0;
		long anotherVal = 0;
		if (obj instanceof Symbol) {
			Symbol sym = (Symbol) obj;
			thisVal = this.addr;
			anotherVal = sym.addr;
		} else if (obj instanceof Long) {
			Long val = (Long) obj;
			anotherVal = val.longValue();
			thisVal = this.addr;
		}
		return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
	}

	synchronized Addr2line startAddr2Line () {
		if (addr2line == null) {
			addr2line = binary.getAddr2line();
			if (addr2line != null) {
				timestamp = System.currentTimeMillis();
				Runnable worker = new Runnable () {
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

}
