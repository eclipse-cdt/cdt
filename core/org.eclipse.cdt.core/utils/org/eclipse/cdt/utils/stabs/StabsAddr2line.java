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

package org.eclipse.cdt.utils.stabs;

import java.io.IOException;

import org.eclipse.cdt.utils.elf.Elf;

/**
 * StabsAddr2ine
 * 
 * @author alain
 */
public class StabsAddr2line {

	Stabs stabs;
	long lastAddress;
	Stabs.Entry entry;

	public StabsAddr2line(byte[] stab, byte[] stabstr, boolean le) throws IOException {
		stabs = new Stabs(stab, stabstr, le);
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
		if (address != lastAddress || entry == null) {
			Stabs.Entry[] entries = stabs.getEntries();
			for (int i = 0; i < entries.length; i++) {
				if (entries[i].addr == address) {
					lastAddress = address;
					entry = entries[i];
					break;
				}
			}
		}
		if (address == lastAddress && entry != null) {
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
		if (address != lastAddress || entry == null) {
			Stabs.Entry[] entries = stabs.getEntries();
			for (int i = 0; i < entries.length; i++) {
				if (entries[i].addr == address) {
					lastAddress = address;
					entry = entries[i];
					break;
				}
			}
		}
		if (address == lastAddress && entry != null) {
			if (entry instanceof Stabs.Function) {
				return ((Stabs.Function)entry).endLine;
			}
			return entry.startLine;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAddr2line#getFunction(long)
	 */
	public String getFunction(long address) throws IOException {
		if (address != lastAddress || entry == null) {
			Stabs.Entry[] entries = stabs.getEntries();
			for (int i = 0; i < entries.length; i++) {
				if (entries[i].addr == address) {
					lastAddress = address;
					entry = entries[i];
					break;
				}
			}
		}
		if (address == lastAddress && entry != null) {
			return entry.string;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IAddr2line#getFileName(long)
	 */
	public String getFileName(long address) throws IOException {
		if (address != lastAddress || entry == null) {
			Stabs.Entry[] entries = stabs.getEntries();
			for (int i = 0; i < entries.length; i++) {
				if (entries[i].addr == address) {
					lastAddress = address;
					entry = entries[i];
					break;
				}
			}
		}
		if (address == lastAddress && entry instanceof Stabs.LocatableEntry) {
			return ((Stabs.LocatableEntry)entry).filename;
		}
		return null;
	}

	public static void main(String[] args) {
		try {
			Elf.Section stab = null;
			Elf.Section stabstr = null;
			Elf exe = new Elf(args[0]);
			Elf.Section[] sections = exe.getSections();
			for (int i = 0; i < sections.length; i++) {
				String name = sections[i].toString();
				if (name.equals(".stab")) {
					stab = sections[i];
				} else if (name.equals(".stabstr")) {
					stabstr = sections[i];
				}
			}
			if (stab != null && stabstr != null) {
				long nstab = stab.sh_size / StabConstant.SIZE;
				System.out.println("Number of stabs" + nstab);
				byte[] array = stab.loadSectionData();
				byte[] strtab = stabstr.loadSectionData();
				StabsAddr2line addr2line = new StabsAddr2line(array, strtab, true);
				long address = Integer.decode(args[1]).longValue();
				addr2line.getStartLine(address);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
