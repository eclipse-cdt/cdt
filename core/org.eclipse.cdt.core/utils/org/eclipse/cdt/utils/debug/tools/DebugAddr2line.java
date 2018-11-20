/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils.debug.tools;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.utils.debug.dwarf.Dwarf;
import org.eclipse.cdt.utils.debug.stabs.Stabs;
import org.eclipse.cdt.utils.elf.Elf;

/**
 * StabsAddr2ine
 *
 * @author alain
 */
public class DebugAddr2line {

	DebugSymsRequestor symreq;

	public DebugAddr2line(String file) throws IOException {
		Elf elf = new Elf(file);
		init(elf);
		elf.dispose();
	}

	public DebugAddr2line(Elf elf) throws IOException {
		init(elf);
	}

	void init(Elf elf) throws IOException {
		symreq = new DebugSymsRequestor();
		Elf.Attribute attribute = elf.getAttributes();
		int type = attribute.getDebugType();
		if (type == Elf.Attribute.DEBUG_TYPE_STABS) {
			Stabs stabs = new Stabs(elf);
			stabs.parse(symreq);
		} else if (type == Elf.Attribute.DEBUG_TYPE_DWARF) {
			Dwarf dwarf = new Dwarf(elf);
			dwarf.parse(symreq);
		} else {
			throw new IOException(CCorePlugin.getResourceString("Util.unknownFormat")); //$NON-NLS-1$
		}
	}

	public void dispose() {
	}

	public int getStartLine(long address) throws IOException {
		DebugSym entry = symreq.getEntry(address);
		if (entry != null) {
			return entry.startLine;
		}
		return 0;
	}

	public int getEndLine(long address) throws IOException {
		DebugSym entry = symreq.getEntry(address);
		if (entry != null) {
			return entry.endLine;
		}
		return 0;
	}

	public String getFunction(long address) throws IOException {
		DebugSym entry = symreq.getEntry(address);
		if (entry != null) {
			return entry.name;
		}
		return null;
	}

	public String getFileName(long address) throws IOException {
		DebugSym entry = symreq.getEntry(address);
		if (entry != null) {
			return entry.filename;
		}
		return null;
	}

	public static void main(String[] args) {
		try {
			DebugAddr2line addr2line = new DebugAddr2line(args[0]);
			long address = Integer.decode(args[1]).longValue();
			int startLine = addr2line.getStartLine(address);
			int endLine = addr2line.getEndLine(address);
			String function = addr2line.getFunction(address);
			String filename = addr2line.getFileName(address);
			System.out.println(Long.toHexString(address));
			System.out.println(filename + ":" + function + ":" + startLine + ":" + endLine); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
