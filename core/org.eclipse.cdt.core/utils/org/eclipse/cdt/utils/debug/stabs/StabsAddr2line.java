/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.utils.debug.stabs;

import java.io.IOException;

import org.eclipse.cdt.utils.elf.Elf;

/**
 * StabsAddr2ine
 * 
 * @author alain
 */
public class StabsAddr2line {

	Stabs stabs;

	public StabsAddr2line(String file) throws IOException {
		stabs = new Stabs(file);
	}

	public StabsAddr2line(Elf elf) throws IOException {
		stabs = new Stabs(elf);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAddr2line#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAddr2line#getStartLine(long)
	 */
	public int getStartLine(long address) throws IOException {
		StabSym entry = stabs.getEntry(address);
		if (entry != null) {
			return entry.startLine;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAddr2line#getEndLine(long)
	 */
	public int getEndLine(long address) throws IOException {
		StabSym entry = stabs.getEntry(address);
		if (entry != null) {
			return entry.endLine;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAddr2line#getFunction(long)
	 */
	public String getFunction(long address) throws IOException {
		StabSym entry = stabs.getEntry(address);
		if (entry != null) {
			return entry.name;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAddr2line#getFileName(long)
	 */
	public String getFileName(long address) throws IOException {
		StabSym entry = stabs.getEntry(address);
		if (entry != null) {
			return entry.filename;
		}
		return null;
	}

	public static void main(String[] args) {
		try {
			StabsAddr2line addr2line = new StabsAddr2line(args[0]);
			long address = Integer.decode(args[1]).longValue();
			int startLine = addr2line.getStartLine(address);
			int endLine = addr2line.getEndLine(address);
			String function = addr2line.getFunction(address);
			String filename = addr2line.getFileName(address);
			System.out.println(Long.toHexString(address));
			System.out.println(filename + ":" + function + ":" + startLine + ":" + endLine);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
