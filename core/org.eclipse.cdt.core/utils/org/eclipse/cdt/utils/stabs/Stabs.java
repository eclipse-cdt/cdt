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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.utils.elf.Elf;

public class Stabs {

	byte[] stabData;
	byte[] stabstrData;
	boolean isLe;
	List entries;

	public class Entry {
		public long addr;
		public int startLine;
		public String string;

		public Entry(String s) {
			string = s;
		}

		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("Name:").append(string).append("\n");
			buf.append("\taddress:").append(addr).append("\n");
			buf.append("\tstartLine:").append(startLine).append("\n");
			//buf.append("\tName:").append(string).append("\n");
			return buf.toString();
		}
	}

	public abstract class LocatableEntry extends Entry {
		public String filename;

		public LocatableEntry(String s) {
			super(s);
		}
	}

	public class Variable extends LocatableEntry {
		public int kind;
		public Function function;

		public Variable(String s) {
			super(s);
		}

		public String toString() {
			StringBuffer buf = new StringBuffer(super.toString());
			buf.append("\tkind:").append(kind).append("\n");
			buf.append("\tfilename:").append(filename).append("\n");
			buf.append("\tVariable");
			return buf.toString();
		}
	}

	public class Function extends LocatableEntry {
		public int endLine;
		public ArrayList lines;
		public ArrayList variables;

		public Function(String s) {
			super(s);
			variables = new ArrayList();
			lines = new ArrayList();
		}

		public String toString() {
			StringBuffer buf = new StringBuffer(super.toString());
			buf.append("\tendLine:").append(endLine).append("\n");
			buf.append("\tfilename:").append(filename).append("\n");
			buf.append("\tSource code: ");
			for (int i = 0; i < lines.size(); i++) {
				buf.append(" ").append(lines.get(i));
			}
			buf.append("\n");
			buf.append("\tVariables\n");
			for (int i = 0; i < variables.size(); i++) {
				buf.append("\t\t").append(variables.get(i)).append("\n");
			}
			buf.append("\tFunction");
			return buf.toString();
		}
	}

	public class Include extends Entry {
		int index;

		public Include(String s) {
			super(s);
		}

		public String toString() {
			return super.toString() + "\tindex:" + index + "\n";
		}
	}

	public class TypeDef extends Entry {
		String type;

		public TypeDef(String s) {
			super(s);
		}
	}

	public class TypeDefinition extends Entry {
		int typeNumber;
		String name;

		public TypeDefinition(String s) {
			super(s);
		}
	}

	public class StringField extends Entry {
		String name;
		String symbolDescriptor;
		String typeDefinition;
		int typeNumber;
		
		public StringField(String s) {
			super(s);
		}
	}

	public String makeString(long offset) {
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

	public Stabs(byte[] stab, byte[] stabstr, boolean le) {
		stabData = stab;
		stabstrData = stabstr;
		isLe = le;
	}

	public Entry[] getEntries() throws IOException {
		if (entries == null) {
			parse();
		}
		Entry[] array = new Entry[entries.size()];
		entries.toArray(array);
		return array;
	}

	void parse() throws IOException {

		entries = new ArrayList();

		long nstab = stabData.length / StabConstant.SIZE;
		int i, offset, bracket;
		int includeCount = 0;
		Function currentFunction = null;
		String currentFile = "";
		String holder = null;

		for (bracket = i = offset = 0; i < nstab; i++, offset += StabConstant.SIZE) {

			long stroff = 0;
			int type = 0;
			int other = 0;
			short desc = 0;
			long value = 0;
			String name = new String();

			// get the offset for the string; 4 bytes
			if (isLe) {
				stroff =
					(((stabData[offset + 3] & 0xff) << 24)
						+ ((stabData[offset + 2] & 0xff) << 16)
						+ ((stabData[offset + 1] & 0xff) << 8)
						+ (stabData[offset] & 0xff));
			} else {
				stroff =
					(((stabData[offset] & 0xff) << 24)
						+ ((stabData[offset + 1] & 0xff) << 16)
						+ ((stabData[offset + 2] & 0xff) << 8)
						+ (stabData[offset + 3] & 0xff));
			}

			if (stroff > 0) {
				name = makeString(stroff);
			}

			// Check for continuation and if any go to the next stab
			// until we find a string that is not terminated with a continuation line '\\'
			// According to the spec all the other fields are duplicated so we still have the data.
			// From the spec continuation line on AIX is '?'
			if (name.endsWith("\\") || name.endsWith("?")) {
				name = name.substring(0, name.length() - 1);
				if (holder == null) {
					holder = name;
				} else {
					holder += name;
				}
				continue;
			} else if (holder != null) {
				name = holder + name;
				holder = null;
			}

			// get the type; 1 byte;
			type = 0xff & stabData[offset + 4];

			// get the other
			other = 0xff & stabData[offset + 5];

			// get the desc
			if (isLe) {
				int a = stabData[offset + 8] << 8;
				int b = stabData[offset + 7];
				int c = a + b;
				//desc = (short) ((stabData[offset + 7] << 8) + stabData[offset + 6]);
				desc = (short) ((stabData[offset + 8] << 8) + stabData[offset + 7]);
			} else {
				desc = (short) ((stabData[offset + 6] << 8) + stabData[offset + 7]);
			}

			// get the value
			if (isLe) {
				value =
					(((stabData[offset + 11] & 0xff) << 24)
						+ ((stabData[offset + 10] & 0xff) << 16)
						+ ((stabData[offset + 9] & 0xff) << 8)
						+ (stabData[offset + 8] & 0xff));
			} else {
				value =
					(((stabData[offset + 8] & 0xff) << 24)
						+ ((stabData[offset + 9] & 0xff) << 16)
						+ ((stabData[offset + 10] & 0xff) << 8)
						+ (stabData[offset + 11] & 0xff));
			}

			// Parse the string
			switch (type) {
				case StabConstant.N_GSYM :
				case StabConstant.N_LSYM :
				case StabConstant.N_PSYM :
					Variable variable = new Variable(name);
					variable.kind = type;
					variable.addr = value;
					variable.startLine = desc;
					variable.function = currentFunction;
					variable.filename = currentFile;
					entries.add(variable);
					if (currentFunction != null) {
						currentFunction.variables.add(variable);
					}
					break;
				case StabConstant.N_SLINE :
					if (currentFunction != null) {
						currentFunction.endLine = desc;
						currentFunction.lines.add(new Integer(desc));
					}
					break;
				case StabConstant.N_FUN :
					if (name.length() == 0 || desc == 0) {
						currentFunction = null;
					} else {
						currentFunction = new Function(name);
						currentFunction.addr = value;
						currentFunction.startLine = desc;
						currentFunction.filename = currentFile;
						entries.add(currentFunction);
					}
					break;
				case StabConstant.N_LBRAC :
					bracket++;
					break;
				case StabConstant.N_RBRAC :
					bracket--;
					break;
				case StabConstant.N_BINCL :
					Include include = new Include(name);
					include.index = includeCount++;
					entries.add(include);
					break;
				case StabConstant.N_EINCL :
					break;
				case StabConstant.N_SO :
					if (name.length() == 0) {
						currentFile = name;
					} else {
						if (currentFile != null && currentFile.endsWith("/")) {
							currentFile += name;
						} else {
							currentFile = name;
						}
					}
					break;
			}
			//System.out.println(" " + i + "\t" + Stab.type2String(type) + "\t" + other + "\t\t" +
			//	desc + "\t" + Long.toHexString(value) + "\t" + + stroff + "\t\t" +name);
		}
	}

	/**
	 * Format: string_field = name ':' symbol-descriptor type-information type-information = type_number [ '=' ( type_description |
	 * type_reference )] type_number = number | '(' number ',' number ')' type_reference type_descriptor =
	 */
	private void parseString(String s, StringField field) {
		// Some String field may contain format like:
		// "foo::bar::baz:t5=*6" in that case the name is "foo::bar::baz"
		int index = s.lastIndexOf(':');

		if (index > 0) {
			field.name = s.substring(0, index).trim();
			index++;
			// Advance the string.
			s = s.substring(index);
		} else {
			field.name = s;
			s = new String();
		}

		// get the symbol descriptor
		for (index = 0; index < s.length(); index++) {
			char c = s.charAt(index);
			if (!(Character.isLetter(c) || c == ':' || c == '-')) {
				break;
			}
		}
		if (index > 0) {
			field.symbolDescriptor = s.substring(0, index);
			index++;
			// Advance the string.
			s = s.substring(index);
		} else {
			field.symbolDescriptor = new String();
		}

		field.typeDefinition = s;

	}

	private void parseType(String s) {
		int index = s.indexOf(':');
		if (index != -1) {
		}
		// "name:symbol-descriptor type-information"
	}

	public void print() {
		for (int i = 0; i < entries.size(); i++) {
			Entry entry = (Entry) entries.get(i);
			System.out.println(entry);
		}
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
				Stabs stabs = new Stabs(array, strtab, true);
				stabs.parse();
				stabs.print();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
