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
			buf.append("Name: ").append(string).append("\n");
			buf.append("\taddress:").append("0x").append(Long.toHexString(addr)).append("\n");
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
			StringBuffer buf = new StringBuffer();
			buf.append("Variable: ");
			buf.append(super.toString());
			buf.append("\tkind:").append(kind).append("\n");
			buf.append("\tfilename:").append(filename).append("\n");
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
			StringBuffer buf = new StringBuffer();
			buf.append("Function: ");
			buf.append(super.toString());
			buf.append("\tendLine:").append(endLine).append("\n");
			buf.append("\tfilename:").append(filename).append("\n");
			buf.append("\tSource code: ");
			for (int i = 0; i < lines.size(); i++) {
				buf.append(" ").append(lines.get(i));
			}
			buf.append("\n");
			buf.append("\tVariables\n");
			for (int i = 0; i < variables.size(); i++) {
				buf.append("\t\t").append("[" + i + "]").append("\n");
				buf.append("\t\t\t").append(variables.get(i)).append("\n");
			}
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

	// type-information = type-number | type-definition
	// type-number = type-reference
	// type-reference = number | '(' number ',' number ')'
	// type-definition = type_number '=' (type-descriptor | type-reference)
	public class TypeInformation {
		int typeNumber;
		int fileNumber;
		boolean isTypeDefinition;

		public TypeInformation(String s) {
			parserTypeInformation(s.toCharArray());
		}

		void parserTypeInformation(char[] array) {
		}
	}

	/**
	 * Format: string_field = name ':' symbol-descriptor type-information
	 */
	public class StringField {
		String name;
		char symbolDescriptor;
		String typeInformation;

		public StringField(String s) {
			parseStringField(s.toCharArray());
		}

		/**
		 * Format: string_field = name ':' symbol-descriptor type-information
		 */
		void parseStringField(char[] array) {
			int index = 0;

			// Some String field may contain format like:
			// "foo::bar::baz:t5=*6" in that case the name is "foo::bar::baz"
			char prev = 0;
			for (int i = 0; index < array.length; index++) {
				char c = array[index];
				if (prev != ':') {
					break;
				}
				prev = c;
			}

			if (index < array.length) {
				name = new String(array, 0, index);
			} else {
				name = new String(array);
			}

			// get the symbol descriptor
			if (index < array.length) {
				index++;
				symbolDescriptor = array[index];
			}

			// get the type-information
			if (index < array.length) {
				typeInformation = new String(array, index, array.length);
			} else {
				typeInformation = new String();
			}
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

			/* FIXME: Sometimes the special C++ names start with '.'. */
			if (name.length() > 1 && name.charAt(0) == '$') {
				switch (name.charAt(1)) {
					case 't' :
						name = "this";
						break;
					case 'v' :
						/* Was: name = "vptr"; */
						break;
					case 'e' :
						name = "eh_throw";
						break;
					case '_' :
						/* This was an anonymous type that was never fixed up. */
						break;
					case 'X' :
						/* SunPRO (3.0 at least) static variable encoding. */
						break;
					default :
						name = "unknown C++ encoded name";
						break;
				}
			}

			// get the type; 1 byte;
			type = 0xff & stabData[offset + 4];

			// get the other
			other = 0xff & stabData[offset + 5];

			// get the desc
			if (isLe) {
				desc = (short) (((stabData[offset + 7] & 0xff) << 8) + (stabData[offset + 6] & 0xff));
			} else {
				desc = (short) (((stabData[offset + 6] & 0xff) << 8) + (stabData[offset + 7] & 0xff));
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
						if (currentFunction.startLine == 0) {
							currentFunction.endLine = currentFunction.startLine = desc;
						} else {
							currentFunction.endLine = desc;
						}
						currentFunction.lines.add(new Integer(desc));
					}
					break;
				case StabConstant.N_FUN :
					if (name.length() == 0) {
						name = "anon";
					}
					currentFunction = null;
					currentFunction = new Function(name);
					currentFunction.addr = value;
					currentFunction.startLine = desc;
					currentFunction.filename = currentFile;
					entries.add(currentFunction);
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
