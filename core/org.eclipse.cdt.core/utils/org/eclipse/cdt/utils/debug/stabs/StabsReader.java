/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.debug.stabs;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.eclipse.cdt.core.ISymbolReader;

public class StabsReader implements ISymbolReader {

	byte[] stabData;
	byte[] stabstrData;
	boolean isLe;
	List fileList;
	String[] files = null;
	boolean parsed = false;
	String currentFile;

	public StabsReader(byte[] data, byte[] stabstr, boolean littleEndian) {
		stabData = data;
		stabstrData = stabstr;
		isLe = littleEndian;
		
		fileList = new ArrayList();
	}

	public String[] getSourceFiles() {
		if (!parsed) {
			parse();
			
			parsed = true;

			files = new String[fileList.size()];
			for (int i = 0; i < fileList.size(); i++) {
				files[i] = (String)fileList.get(i);
			}
		}

		return files;
	}

	private String makeString(long offset) {
		StringBuffer buf = new StringBuffer();
		for (; offset < stabstrData.length; offset++) {
			byte b = stabstrData[(int) offset];
			if (b == 0) {
				break;
			}
			buf.append((char) b);
		}
		return buf.toString();
	}

	private int read_4_bytes(byte[] bytes, int offset) {
		if (isLe) {
			return (((bytes[offset + 3] & 0xff) << 24)
				| ((bytes[offset + 2] & 0xff) << 16)
				| ((bytes[offset + 1] & 0xff) << 8)
				| (bytes[offset] & 0xff));
		}
		return (((bytes[offset] & 0xff) << 24)
			| ((bytes[offset + 1] & 0xff) << 16)
			| ((bytes[offset + 2] & 0xff) << 8)
			| (bytes[offset + 3] & 0xff));
	}

	private short read_2_bytes(byte[] bytes, int offset) {
		if (isLe) {
			return (short) (((bytes[offset + 1] & 0xff) << 8) | (bytes[offset] & 0xff));
		}
		return (short) (((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff));
	}

	private String fixUpPath(String path) {
		// some compilers generate extra back slashes
		path = path.replaceAll("\\\\\\\\", "\\\\");
		
		// translate any cygwin drive paths, e.g. //G/System/main.cpp or /cygdrive/c/system/main.c
		if (path.startsWith("/cygdrive/") && ('/' == path.charAt(11))) {
			char driveLetter = path.charAt(10);
			driveLetter = (Character.isLowerCase(driveLetter)) ? Character.toUpperCase(driveLetter) : driveLetter;

			StringBuffer buf = new StringBuffer(path);
			buf.delete(0, 11);
			buf.insert(0, driveLetter);
			buf.insert(1, ':');

			path = buf.toString();
		}

		// translate any cygwin drive paths, e.g. //G/System/main.cpp or /cygdrive/c/system/main.c
		if (path.startsWith("//") && ('/' == path.charAt(3))) {
			char driveLetter = path.charAt(2);
			driveLetter = (Character.isLowerCase(driveLetter)) ? Character.toUpperCase(driveLetter) : driveLetter;

			StringBuffer buf = new StringBuffer(path);
			buf.delete(0, 3);
			buf.insert(0, driveLetter);
			buf.insert(1, ':');

			path = buf.toString();
		}
		
		return path;
	}
	
	private void parse() {
		long nstab = stabData.length / StabConstant.SIZE;
		int i, offset;
		String holder = null;
		long stroff = 0;
		int type = 0;
		int other = 0;
		short desc = 0;
		long value = 0;

		for (i = offset = 0; i < nstab; i++, offset += StabConstant.SIZE) {

			// get the type; 1 byte;
			type = 0xff & stabData[offset + 4];
			
			// ignoring anything other than N_SO and N_SOL because these are source or
			// object file entries
			if (type == StabConstant.N_SO || type == StabConstant.N_SOL) {
				// get the other
				other = 0xff & stabData[offset + 5];
				// get the desc
				desc = read_2_bytes(stabData, offset + 6);
				// get the value
				value = read_4_bytes(stabData, offset + 8);

				// get the offset for the string; 4 bytes
				stroff = read_4_bytes(stabData, offset);

				String field;
				if (stroff > 0) {
					field = makeString(stroff);
				} else {
					field = new String();
				}
				// Check for continuation and if any go to the next stab
				// until we find a string that is not terminated with a
				// continuation line '\\'
				// According to the spec all the other fields are duplicated so we
				// still have the data.
				// From the spec continuation line on AIX is '?'
				if (field.endsWith("\\") || field.endsWith("?")) { //$NON-NLS-1$ //$NON-NLS-2$
					field = field.substring(0, field.length() - 1);
					if (holder == null) {
						holder = field;
					} else {
						holder += field;
					}
					continue;
				} else if (holder != null) {
					field = holder + field;
					holder = null;
				}
				parseStabEntry(field, type, other, desc, value);
			}
		}		
	}

	void parseStabEntry(String field, int type, int other, short desc, long value) {
		// Parse the string
		switch (type) {
			case StabConstant.N_SOL :
				// include file
				if (field != null && field.length() > 0) {
					
					field = fixUpPath(field);

					if (!fileList.contains(field))
						fileList.add(field);					
				}
				break;

			case StabConstant.N_SO :
				// source file
				if (field != null && field.length() > 0) {
					// if it ends with "/" then the next entry will be the rest of the string.
					if (field.endsWith("/")) { //$NON-NLS-1$
						currentFile = field;							
					} else {
						if (currentFile != null) {
							// if this entry is an absolute path then just throw away the existing currentFile
							if (new File(field).isAbsolute()) {
								currentFile = field;
							} else {
								currentFile += field;
							}
						} else {
							currentFile = field;
						}


						currentFile = fixUpPath(currentFile);

						if (!fileList.contains(currentFile))
							fileList.add(currentFile);					

						currentFile = null;
					}
				}
			break;
		}
	}
}
