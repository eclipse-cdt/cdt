/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.debug.stabs;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.utils.debug.DebugArrayType;
import org.eclipse.cdt.utils.debug.DebugBaseType;
import org.eclipse.cdt.utils.debug.DebugCrossRefType;
import org.eclipse.cdt.utils.debug.DebugEnumField;
import org.eclipse.cdt.utils.debug.DebugEnumType;
import org.eclipse.cdt.utils.debug.DebugField;
import org.eclipse.cdt.utils.debug.DebugFunctionType;
import org.eclipse.cdt.utils.debug.DebugParameterKind;
import org.eclipse.cdt.utils.debug.DebugPointerType;
import org.eclipse.cdt.utils.debug.DebugReferenceType;
import org.eclipse.cdt.utils.debug.DebugStructType;
import org.eclipse.cdt.utils.debug.DebugType;
import org.eclipse.cdt.utils.debug.DebugUnknownType;
import org.eclipse.cdt.utils.debug.DebugVariableKind;
import org.eclipse.cdt.utils.debug.IDebugEntryRequestor;
import org.eclipse.cdt.utils.debug.tools.DebugSym;
import org.eclipse.cdt.utils.debug.tools.DebugSymsRequestor;
import org.eclipse.cdt.utils.elf.Elf;

public class Stabs {

	final static String LLLOW = "01000000000000000000000"; //$NON-NLS-1$
	final static String LLHIGH = "0777777777777777777777"; //$NON-NLS-1$
	final static String ULLHIGH = "01777777777777777777777"; //$NON-NLS-1$

	byte[] stabData;
	byte[] stabstrData;
	boolean isLe;

	boolean inCompilationUnit;
	boolean inFunction;
	boolean inInclude;

	int bracket;
	String currentFile;

	Map mapTypes = new HashMap();
	DebugType voidType = new DebugBaseType("void", 0, false); //$NON-NLS-1$

	public Stabs(String file) throws IOException {
		Elf exe = new Elf(file);
		init(exe);
		exe.dispose();
	}

	public Stabs(Elf exe) throws IOException {
		init(exe);
	}

	public Stabs(byte[] stab, byte[] stabstr, boolean le) {
		init(stab, stabstr, le);
	}

	void init(Elf exe) throws IOException {
		byte[] data = null;
		byte[] stabstr = null;
		Elf.Section[] sections = exe.getSections();
		for (int i = 0; i < sections.length; i++) {
			String name = sections[i].toString();
			if (name.equals(".stab")) { //$NON-NLS-1$
				data = sections[i].loadSectionData();
			} else if (name.equals(".stabstr")) { //$NON-NLS-1$
				stabstr = sections[i].loadSectionData();
			}
		}
		Elf.ELFhdr header = exe.getELFhdr();
		boolean isLE = header.e_ident[Elf.ELFhdr.EI_DATA] == Elf.ELFhdr.ELFDATA2LSB;
		if (data != null && stabstr != null) {
			init(data, stabstr, isLE);
		}
	}

	void init(byte[] stab, byte[] stabstr, boolean le) {
		stabData = stab;
		stabstrData = stabstr;
		isLe = le;
	}

	String makeString(long offset) {
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

	int read_4_bytes(byte[] bytes, int offset) {
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

	short read_2_bytes(byte[] bytes, int offset) {
		if (isLe) {
			return (short) (((bytes[offset + 1] & 0xff) << 8) | (bytes[offset] & 0xff));
		}
		return (short) (((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff));
	}

	public void parse(IDebugEntryRequestor requestor) {
		//List list = new ArrayList();
		long nstab = stabData.length / StabConstant.SIZE;
		int i, offset;
		String holder = null;
		long stroff = 0;
		int type = 0;
		int other = 0;
		short desc = 0;
		long value = 0;
		for (bracket = i = offset = 0; i < nstab; i++, offset += StabConstant.SIZE) {

			// get the offset for the string; 4 bytes
			stroff = read_4_bytes(stabData, offset);
			// get the type; 1 byte;
			type = 0xff & stabData[offset + 4];
			// get the other
			other = 0xff & stabData[offset + 5];
			// get the desc
			desc = read_2_bytes(stabData, offset + 6);
			// get the value
			value = read_4_bytes(stabData, offset + 8);

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
			parseStabEntry(requestor, field, type, other, desc, value);
		}
		// Bring closure.
		if (inFunction) {
			requestor.exitFunction(-1);
			inFunction = false;
		}
		if (inInclude) {
			requestor.exitInclude();
			inInclude = false;
		}
		if (inCompilationUnit) {
			requestor.exitCompilationUnit(value);
			inCompilationUnit = false;
			currentFile = null;
		}
	}

	void parseStabEntry(IDebugEntryRequestor requestor, String field, int type, int other, short desc, long value) {
		// Parse the string
		switch (type) {
			case StabConstant.N_GSYM :
			case StabConstant.N_LSYM :
			case StabConstant.N_PSYM :
				//accept a new variable
				parseStabString(requestor, field, value);
				break;

			case StabConstant.N_SLINE :
				// New statement line
				requestor.acceptStatement(desc, value);
				break;

			case StabConstant.N_FUN :
				if (inFunction) {
					requestor.exitFunction(value);
					inFunction = false;
				}
				// Start a new Function
				if (field.length() == 0) {
					field = " anon "; //$NON-NLS-1$
				}
				inFunction = true;
				parseStabString(requestor, field, value);
				break;

			case StabConstant.N_LBRAC :
				if (inFunction) {
					requestor.enterCodeBlock(value);
				}
				bracket++;
				break;

			case StabConstant.N_RBRAC :
				requestor.exitCodeBlock(value);
				bracket--;
				break;

			case StabConstant.N_BINCL :
				// Start of an include file
				requestor.enterInclude(field);
				inInclude = true;
				break;

			case StabConstant.N_EINCL :
				// end of the include
				requestor.exitInclude();
				inInclude = false;
				break;

			case StabConstant.N_SOL :
				// if we had an include it means the end.
				if (inInclude) {
					requestor.exitInclude();
					inInclude = false;
				}
				// Start of an include file
				requestor.enterInclude(field);
				inInclude = true;
				break;

			case StabConstant.N_CATCH :
				parseStabString(requestor, field, value);
				break;

			case StabConstant.N_SO :
				// if whitin a function
				if (inFunction) {
					requestor.exitFunction(-1);
					inFunction = false;
				}
				if (inInclude) {
					requestor.exitInclude();
					inInclude = false;
				}
				if (inCompilationUnit) {
					requestor.exitCompilationUnit(value);
					inCompilationUnit = false;
					currentFile = null;
				}
				if (field != null && field.length() > 0) {
					// if it ends with "/" do not call the entering yet
					// we have to concatenate the next one.
					if (field.endsWith("/")) { //$NON-NLS-1$
						currentFile = field;
					} else {
						if (currentFile != null) {
							currentFile += field;
						} else {
							currentFile = field;
						}
						requestor.enterCompilationUnit(currentFile, value);
						inCompilationUnit = true;
						currentFile = null;
					}
				}
				break;
		}
		//System.out.println(" " + i + "\t" + Stab.type2String(type) + "\t" +
		// other + "\t\t" +
		//	desc + "\t" + Long.toHexString(value) + "\t" + + stroff + "\t\t"
		// +name);
	}

	void parseStabString(IDebugEntryRequestor requestor, String field, long value) {

		StringField sf = new StringField(field);

		switch (sf.getSymbolDescriptor()) {
			// C++ nested symbol.
			case ':' :
				break;

				// Parameter pass by reference in register.
			case 'a' :
				{
					String information = sf.getTypeInformation();
					String paramName = sf.getName();
					DebugParameterKind paramKind = DebugParameterKind.REGISTER_REFERENCE;
					DebugType paramType = parseStabType("", information); //$NON-NLS-1$
					requestor.acceptParameter(paramName, paramType, paramKind, value);
				}
				break;

				// Sun Based variable
			case 'b' :
				break;

				// symbol descriptor indicates that this stab represents a
				// constant.
			case 'c' :
				{
					String name = sf.getName();
					String information = sf.getTypeInformation();
					parseStabConstant(requestor, name, information, value);
				}
				break;

				//  Conformant array bound(Pascal).
				// Nave of a caught exception GNU C++
			case 'C' :
				{
					String excName = sf.getName();
					String information = sf.getTypeInformation();
					DebugType excType = parseStabType("", information); //$NON-NLS-1$
					requestor.acceptCaughtException(excName, excType, value);
				}
				break;

				// File scope function.
			case 'f' :
				// Global function.
			case 'F' :
				{
					String funcName = sf.getName();
					String funcInfo = sf.getTypeInformation();
					DebugType funcType = parseStabType("", funcInfo); //$NON-NLS-1$
					boolean funcGlobal = sf.getSymbolDescriptor() == 'F';
					requestor.enterFunction(funcName, funcType, funcGlobal, value);
				}
				break;
				// Global variable
			case 'G' :
				{
					String varName = sf.getName();
					String varInfo = sf.getTypeInformation();
					DebugVariableKind varKind = DebugVariableKind.GLOBAL;
					DebugType varType = parseStabType("", varInfo); //$NON-NLS-1$
					requestor.acceptVariable(varName, varType, varKind, value);
				}
				break;

				// ???
			case 'i' :
				break;
				// Internal(nested) procedure.
			case 'I' :
				break;
				// Internal/nested function.
			case 'J' :
				break;
				// Label name
			case 'L' :
				break;
				// Module
			case 'm' :
				break;

				// Argument list parameter
			case 'p' :
				{
					String paramName = sf.getName();
					String paramInfo = sf.getTypeInformation();
					DebugParameterKind paramKind = DebugParameterKind.STACK;
					DebugType paramType = parseStabType("", paramInfo); //$NON-NLS-1$
					requestor.acceptParameter(paramName, paramType, paramKind, value);
				}
				break;

				// Paramater in floating point register.
			case 'D' :
				// register parameter or prototype f function referenced by the
				// file.
			case 'P' :
				// Register Parameter
			case 'R' :
				{
					String paramName = sf.getName();
					String paramInfo = sf.getTypeInformation();
					DebugParameterKind paramKind = DebugParameterKind.REGISTER;
					DebugType paramType = parseStabType("", paramInfo); //$NON-NLS-1$
					requestor.acceptParameter(paramName, paramType, paramKind, value);
				}
				break;

				// static procedure
			case 'Q' :
				break;

				// Floating point register variable
			case 'd' :
				// Never use, according to the
				// Register variable
			case 'r' :
				{
					String varName = sf.getName();
					String varInfo = sf.getTypeInformation();
					DebugVariableKind varKind = DebugVariableKind.REGISTER;
					DebugType varType = parseStabType("", varInfo); //$NON-NLS-1$
					requestor.acceptVariable(varName, varType, varKind, value);
				}
				break;

				// File scope variable
			case 'S' :
				{
					String varName = sf.getName();
					String varInfo = sf.getTypeInformation();
					DebugVariableKind varKind = DebugVariableKind.STATIC;
					DebugType varType = parseStabType("", varInfo); //$NON-NLS-1$
					requestor.acceptVariable(varName, varType, varKind, value);
				}
				break;

				// Type name
			case 't' :
				{
					String name = sf.getName();
					String infoField = sf.getTypeInformation();
					DebugType type = parseStabType(name, infoField);
					requestor.acceptTypeDef(name, type);
				}
				break;

				// Enumeration, structure or union
			case 'T' :
				{
					String infoField = sf.getTypeInformation();
					// According to the doc 't' can follow the 'T'
					if (infoField.length() > 0 && infoField.charAt(0) == 't') {
						//String s = infoField.substring(1);
						parseStabString(requestor, field, value);
					} else {
						// Just register the type.
						String name = sf.getName();
						parseStabType(name, infoField);
					}
				}
				break;

				// Parameter passed by reference.
			case 'v' :
				{
					String paramName = sf.getName();
					String paramInfo = sf.getTypeInformation();
					DebugParameterKind paramKind = DebugParameterKind.REFERENCE;
					DebugType paramType = parseStabType("", paramInfo); //$NON-NLS-1$
					requestor.acceptParameter(paramName, paramType, paramKind, value);
				}
				break;

				// Procedure scope static variable
			case 'V' :
				{
					String varName = sf.getName();
					String varInfo = sf.getTypeInformation();
					DebugVariableKind varKind = DebugVariableKind.LOCAL_STATIC;
					DebugType varType = parseStabType("", varInfo); //$NON-NLS-1$
					requestor.acceptVariable(varName, varType, varKind, value);
				}
				break;

				// Conformant array
			case 'x' :
				break;

				// Function return variable
			case 'X' :
				// local variable
			case 's' :
				// Variable on the stack
			case '-' :
			default :
				{
					String varName = sf.getName();
					String varInfo = sf.getTypeInformation();
					DebugVariableKind varKind = DebugVariableKind.LOCAL;
					DebugType varType = parseStabType("", varInfo); //$NON-NLS-1$
					requestor.acceptVariable(varName, varType, varKind, value);
				}
				break;
		}
	}

	DebugType parseStabType(String name, String typeInformation) {
		try {
			Reader reader = new StringReader(typeInformation);
			return parseStabType(name, reader);
		} catch (IOException e) {
		}
		return new DebugUnknownType(name);
	}

	DebugType parseStabType(String name, Reader reader) throws IOException {
		return parseStabType(name, null, reader);
	}

	DebugType parseStabType(String name, TypeInformation oldType, Reader reader) throws IOException {

		TypeInformation typeInfo = new TypeInformation(reader);
		DebugType type = null;
		switch (typeInfo.getTypeDescriptor()) {

			// Method (C++)
			case '#' :
				break;

				// Reference (C++)
			case '&' :
				{
					DebugType subType = parseStabType("", reader); //$NON-NLS-1$
					type = new DebugReferenceType(subType);
				}
				break;

				// Member (C++) class and variable
			case '@' :
				break;

				// pointer type.
			case '*' :
				{
					DebugType subType = parseStabType("", reader); //$NON-NLS-1$
					type = new DebugPointerType(subType);
				}
				break;

				// Builtin type define byt Sun stabs
				// Pascal Type
			case 'b' :
				// Builtin floating type
			case 'R' :
				// Wide character
			case 'w' :
				// Builtin flaoting point type.
			case 'g' :
				// Complex buitin type.
			case 'c' :
				{
					char desc = typeInfo.getTypeDescriptor();
					type = parseStabBuiltinType(name, desc, reader);
				}
				break;

				// Array.
			case 'a' :
			case 'A' :
				type = parseStabArrayType(name, reader);
				break;

				// Volatile-qualified type
			case 'B' :
				break;

				// Cobol
			case 'C' :
				break;

				// File type
			case 'd' :
				break;

				// N-dimensional dynamic array.
			case 'D' :
				break;

				// Enumeration type
			case 'e' :
				type = parseStabEnumType(name, reader);
				break;

				// N-dimensional subarray
			case 'E' :
				break;

				// Function type
			case 'f' :
				{
					DebugType subType = parseStabType("", reader); //$NON-NLS-1$
					type = new DebugFunctionType(subType);
				}
				break;

				// Pascal Function parameter
			case 'F' :
				break;

				// COBOL Group
			case 'G' :
				break;

				// Imported type
			case 'i' :
				break;

				// Const-qualified type
			case 'k' :
				break;

				// Cobol file desc.
			case 'K' :
				break;

				// Multiple instance type
			case 'M' :
				break;

				// string type:
			case 'n' :
				break;

				// Stringpt:
			case 'N' :
				break;

				// Opaque type
			case 'o' :
				break;

				// Procedure
			case 'p' :
				break;

				// Packed array
			case 'P' :
				break;

				// Range. example:
			case 'r' :
				type = parseStabRangeType(name, typeInfo.getTypeNumber(), reader);
				break;

				// Structure type
			case 's' :
				type = parseStabStructType(name, typeInfo.getTypeNumber(), false, reader);
				break;

				// Union
			case 'u' :
				type = parseStabStructType(name, typeInfo.getTypeNumber(), true, reader);
				break;

				// Set type
			case 'S' :
				break;

				// variant record.
			case 'v' :
				break;

				// Cross-reference
			case 'x' :
				type = parseStabCrossRefType(name, reader);
				break;

				// ???
			case 'Y' :
				break;

				// gstring
			case 'z' :
				break;

				// Reference to a previously define type.
			case '(' :
			case '-' :
			default :
				if (typeInfo.isTypeDefinition()) {
					type = parseStabType(name, typeInfo, reader);
				} else {
					// check for void
					if (oldType != null && oldType.getTypeNumber().equals(typeInfo.getTypeNumber())) {
						type = voidType;
					} else {
						type = getDebugType(typeInfo.getTypeNumber());
					}
				}
		}

		// register the type.
		if (type != null && typeInfo.isTypeDefinition()) {
			mapTypes.put(typeInfo.getTypeNumber(), type);
		}
		if (type == null) {
			type = new DebugUnknownType(name);
		}
		return type;
	}

	/**
	 * @param name
	 * @param reader
	 * @return
	 */
	DebugType parseStabCrossRefType(String name, Reader reader) throws IOException {
		StringBuffer sb = new StringBuffer();
		int c = reader.read();
		if (c == 's') {
			sb.append("struct "); //$NON-NLS-1$
		} else if (c == 'u') {
			sb.append("union "); //$NON-NLS-1$
		} else if (c == 'e') {
			sb.append("enum "); //$NON-NLS-1$
		} else {
			sb.append((char) c);
		}
		while ((c = reader.read()) != -1) {
			if (c == ':') {
				break;
			}
			sb.append((char) c);
		}
		return new DebugCrossRefType(null, name, sb.toString());
	}

	/**
	 * @param name
	 * @param desc
	 * @param reader
	 * @return
	 */
	private DebugType parseStabBuiltinType(String name, char desc, Reader reader) throws IOException {
		DebugType builtinType = null;
		switch (desc) {
			case 'b' :
				{
					// get the signed
					int signed = reader.read();
					reader.mark(1);
					// get the flag
					int charFlag = reader.read();
					if (charFlag != 'c') {
						reader.reset();
					}
					int c;
					StringBuffer sb = new StringBuffer();

					// get the width
					//int width = 0;
					while ((c = reader.read()) != -1) {
						if (c == ';') {
							break;
						}
						sb.append((char) c);
					}
					//try {
					//	String token = sb.toString();
					//	width = Integer.parseInt(token);
					//} catch (NumberFormatException e) {
					//}

					sb.setLength(0);

					// get the offset
					//int offset = 0;
					while ((c = reader.read()) != -1) {
						if (c == ';') {
							break;
						}
						sb.append((char) c);
					}
					//try {
						//String token = sb.toString();
						//offset = Integer.parseInt(token);
					//} catch (NumberFormatException e) {
					//}

					sb.setLength(0);

					// get the nbits
					int nbits = 0;
					while ((c = reader.read()) != -1) {
						if (c == ';') {
							break;
						}
						sb.append((char) c);
					}
					try {
						String token = sb.toString();
						nbits = Integer.parseInt(token);
					} catch (NumberFormatException e) {
					}
					builtinType = new DebugBaseType(name, nbits / 8, signed == 'u');
				}
				break;

			case 'w' :
				{
					builtinType = new DebugBaseType(name, 8, false);
				}
				break;

			case 'R' :
				{
					int c;
					StringBuffer sb = new StringBuffer();

					// get the fp-Type
					//int fpType = 0;
					while ((c = reader.read()) != -1) {
						if (c == ';') {
							break;
						}
						sb.append((char) c);
					}
					//try {
					//	String token = sb.toString();
						//fpType = Integer.parseInt(token);
					//} catch (NumberFormatException e) {
					//}

					sb.setLength(0);

					// get the bytes
					int bytes = 0;
					while ((c = reader.read()) != -1) {
						if (c == ';') {
							break;
						}
						sb.append((char) c);
					}
					try {
						String token = sb.toString();
						bytes = Integer.parseInt(token);
					} catch (NumberFormatException e) {
					}
					builtinType = new DebugBaseType(name, bytes, false);
				}
				break;

			case 'c' :
			case 'g' :
				{
					//DebugType type = parseStabType(name, reader);
					parseStabType(name, reader);
					int c = reader.read(); // semicolon
					StringBuffer sb = new StringBuffer();
					int nbits = 0;
					while ((c = reader.read()) != -1) {
						sb.append((char) c);
					}
					try {
						String token = sb.toString();
						nbits = Integer.parseInt(token);
					} catch (NumberFormatException e) {
					}
					builtinType = new DebugBaseType(name, nbits / 8, false);
				}
				break;

		}
		return builtinType;
	}

	DebugType parseStabArrayType(String name, Reader reader) throws IOException {
		// Format of an array type:
		// "ar<index type>;lower;upper;<array_contents_type>".

		// we only understand range for an array.
		int c = reader.read();
		if (c == 'r') {
			//DebugType index_type = parseStabType("", reader); //$NON-NLS-1$
			parseStabType("", reader); //$NON-NLS-1$

			c = reader.read();
			// Check ';'
			if (c != ';') {
				// bad array type
				return null;
			}

			StringBuffer sb = new StringBuffer();
			while ((c = reader.read()) != -1) {
				if (c == ';') {
					break;
				}
				sb.append((char) c);
			}
			// Check ';'
			if (c != ';') {
				// bad array type
				return null;
			}

			// lower index
			int lower = 0; // should always be zero for C/C++
			try {
				String token = sb.toString();
				lower = Integer.parseInt(token);
			} catch (NumberFormatException e) {
			}

			sb.setLength(0);

			while ((c = reader.read()) != -1) {
				if (c == ';') {
					break;
				}
				sb.append((char) c);
			}
			// Check ';'
			if (c != ';') {
				// bad array type
				return null;
			}

			int upper = 0;
			// upper index
			try {
				String token = sb.toString();
				upper = Integer.parseInt(token);
			} catch (NumberFormatException e) {
			}

			// Check ';'
			if (c != ';') {
				// bad array type
				return null;
			}

			// The array_content_type
			DebugType subType = parseStabType("", reader); //$NON-NLS-1$

			return new DebugArrayType(subType, upper - lower + 1);
		}
		return new DebugArrayType(new DebugUnknownType(name), 0);
	}

	/**
	 * _Bool:t(0,20)=eFalse:0,True:1,; fruit:T(1,4)=eapple:0,orange:1,;
	 * 
	 * @param name
	 * @param attributes
	 * @return
	 */
	DebugType parseStabEnumType(String name, Reader reader) throws IOException {
		List list = new ArrayList();
		String fieldName = null;
		StringBuffer sb = new StringBuffer();
		int c;
		while ((c = reader.read()) != -1) {
			if (c == ':') {
				fieldName = sb.toString();
				sb.setLength(0);
			} else if (c == ',') {
				if (fieldName != null && fieldName.length() > 0) {
					String value = sb.toString();
					int fieldValue = 0;
					try {
						fieldValue = Integer.decode(value).intValue();
					} catch (NumberFormatException e) {
					}
					list.add(new DebugEnumField(fieldName, fieldValue));
				}
				fieldName = null;
				sb.setLength(0);
			} else if (c == ';') {
				break;
			} else {
				sb.append((char) c);
			}
		}
		DebugEnumField[] fields = new DebugEnumField[list.size()];
		list.toArray(fields);
		return new DebugEnumType(name, fields);
	}

	/**
	 * For C lang -- node:T(1,5)=s12i:(0,1),0,32;j:(0,1),32,32;next:(1,6)=*(1,5),64,32;;
	 * 
	 * @param name
	 * @param typeNumber
	 * @param union
	 * @param reader
	 * @return
	 */
	DebugType parseStabStructType(String name, TypeNumber typeNumber, boolean union, Reader reader) throws IOException {
		int c;
		StringBuffer sb = new StringBuffer();
		while ((c = reader.read()) != -1) {
			if (!Character.isDigit((char) c)) {
				reader.reset();
				break;
			}
			reader.mark(1);
			sb.append((char) c);
		}
		String number = sb.toString();
		int size = 0;
		try {
			size = Integer.decode(number).intValue();
		} catch (NumberFormatException e) {
		}
		DebugStructType structType = new DebugStructType(name, size, union);
		// We have to register it right away, some field may
		// need the tag if they self reference via a pointer.
		mapTypes.put(typeNumber, structType);

		// parse the fields.
		parseStabStructField(structType, reader);
		return structType;
	}

	void parseStabStructField(DebugStructType structType, Reader reader) throws IOException {
		// get the field name.
		StringBuffer sb = new StringBuffer();
		int c;
		while ((c = reader.read()) != -1) {
			if (c != ':') {
				sb.append((char) c);
			} else {
				break;
			}
		}
		// Sanity check: We should have ':' if no bailout
		if (c != ':') {
			return;
		}
		String name = sb.toString();

		// get the type of the field
		DebugType fieldType = parseStabType("", reader); //$NON-NLS-1$

		c = reader.read();
		// Sanity check: we should have ',' here.
		if (c != ',') {
			return;
		}

		// the offset of the struct of the field.
		sb.setLength(0);
		while ((c = reader.read()) != -1 && c != ',') {
			sb.append((char) c);
		}
		// Sanity check: we should have ','
		if (c != ',') {
			return;
		}
		int offset = 0;
		try {
			offset = Integer.decode(sb.toString()).intValue();
		} catch (NumberFormatException e) {
		}

		// the number of bits of the struct of the field.
		sb.setLength(0);
		while ((c = reader.read()) != -1 && c != ';') {
			sb.append((char) c);
		}
		// Check we need ';'
		if (c != ';') {
			return;
		}
		int bits = 0;
		try {
			bits = Integer.decode(sb.toString()).intValue();
		} catch (NumberFormatException e) {
		}

		// add the new field.
		structType.addField(new DebugField(name, fieldType, offset, bits));

		// absorb the trailing ';'
		//reader.read();
		// continue the parsing recursively.
		parseStabStructField(structType, reader);
	}

	DebugType parseStabRangeType(String name, TypeNumber number, Reader reader) throws IOException {
		//  int:t(0,1)=r(0,1);-2147483648;2147483647;
		//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		DebugType rangeType = null;

		// get the index_type
		TypeNumber typeNumber = new TypeNumber(reader);

		int c = reader.read();
		// Sanity we should have a semicolon
		if (c != ';') {
			// bad range type;
			return new DebugUnknownType(name);
		}

		StringBuffer sb = new StringBuffer();

		// read the lowerBound.
		while ((c = reader.read()) != -1) {
			if (c == ';') {
				break;
			}
			sb.append((char) c);
		}
		// Sanity we should have a semicolon
		if (c != ';') {
			// bad range type;
			return new DebugUnknownType(name);
		}

		boolean overflowLowerBound = false;
		String lowerBoundString = sb.toString();
		long lowerBound = 0;
		try {
			lowerBound = Long.decode(lowerBoundString).longValue();
		} catch (NumberFormatException e) {
			overflowLowerBound = true;
		}

		sb.setLength(0);

		// read the upperBound.
		while ((c = reader.read()) != -1) {
			if (c == ';') {
				break;
			}
			sb.append((char) c);
		}
		// Sanity we should have a semicolon
		if (c != ';') {
			// bad range type;
			return new DebugUnknownType(name);
		}

		boolean overflowUpperBound = false;
		long upperBound = 0;
		String upperBoundString = sb.toString();
		try {
			upperBound = Long.decode(upperBoundString).longValue();
		} catch (NumberFormatException e) {
			overflowUpperBound = true;
		}

		if (typeNumber == null) {
			typeNumber = new TypeNumber(0, 0);
		}

		boolean self = typeNumber.equals(number);

		// Probably trying 64 bits range like "long long"
		if (overflowLowerBound || overflowUpperBound) {

			if (lowerBoundString.equals(LLLOW) && upperBoundString.equals(LLHIGH))
				rangeType = new DebugBaseType(name, 8, false);
			if (!overflowLowerBound && lowerBound == 0 && upperBoundString.equals(ULLHIGH))
				rangeType = new DebugBaseType(name, 8, true);
		} else {

			if (lowerBound == 0 && upperBound == -1) {
				// if the lower bound is 0 and the upper bound is -1,
				// it means unsigned int
				if (name.equals("long long int")) { //$NON-NLS-1$
					rangeType = new DebugBaseType(name, 8, true);
				} else if (name.equals("long long unsigned int")) { //$NON-NLS-1$
					rangeType = new DebugBaseType(name, 8, true);
				} else {
					rangeType = new DebugBaseType(name, 4, true);
				}
			} else if (upperBound == 0 && lowerBound > 0) {
				// if The upper bound is 0 and the lower bound is positive
				// it is a floating point and the lower bound is the number of bytes
				rangeType = new DebugBaseType(name, (int) lowerBound, true);
			} else if (lowerBound == -128 && upperBound == 127) {
				// signed char;
				rangeType = new DebugBaseType(name, 1, false);
			} else if (self && lowerBound == 0 && upperBound == 127) {
				// C/C++ specific
				rangeType = new DebugBaseType(name, 1, false);
			} else if (self && lowerBound == 0 && upperBound == 255) {
				// unsigned char
				rangeType = new DebugBaseType(name, 1, true);
			} else if (lowerBound == -32768 && upperBound == 32767) {
				// signed short
				rangeType = new DebugBaseType(name, 2, false);
			} else if (self && lowerBound == 0 && upperBound == 65535) {
				// unsigned short
				rangeType = new DebugBaseType(name, 2, true);
			} else if (lowerBound == -2147483648 && upperBound == 2147483647) {
				// int
				rangeType = new DebugBaseType(name, 4, false);
			}
		}
		return rangeType;
	}

	void parseStabConstant(IDebugEntryRequestor requestor, String name, String field, long value) {
		try {
			parseStabConstant(requestor, name, new StringReader(field), value);
		} catch (IOException e) {
			//
		}
	}

	void parseStabConstant(IDebugEntryRequestor requestor, String name, Reader reader, long value) throws IOException {
		int c = reader.read();
		if (c == '=') {
			c = reader.read();
			switch (c) {
				// Boolean constant.
				// c=bvalue or c=bvalue
				// value is a numeric value: 0 fo false and 1 for true.
				// Not supported by GDB.
				case 'b' :
					break;

					// Character constant.
					// c=cvalue
					// value is the numeric value of the constant.
					// Not supported by GDB.
				case 'c' :
					break;

					// Constant whose value van be represented as integral.
					// c=e type-information, value
					// type-information is the type of the constant.
					// value is the numeric value of the constant.
					// This is usually use for enumeration constants.
				case 'e' :
					{
						int val = 0;
						DebugType type = parseStabType("", reader); //$NON-NLS-1$
						c = reader.read();
						if (c == ',') {
							StringBuffer sb = new StringBuffer();
							while ((c = reader.read()) != -1) {
								sb.append((char) c);
							}
							try {
								String s = sb.toString();
								val = Integer.decode(s).intValue();
							} catch (NumberFormatException e) {
							}
						}
						requestor.acceptTypeConst(name, type, val);
					}
					break;

					// Integer constant.
					// c=ivalue
					// value is the numeric value;
				case 'i' :
					{
						int val = 0;
						StringBuffer sb = new StringBuffer();
						while ((c = reader.read()) != -1) {
							sb.append((char) c);
						}
						try {
							String s = sb.toString();
							val = Integer.decode(s).intValue();
						} catch (NumberFormatException e) {
						} catch (IndexOutOfBoundsException e) {
						}
						requestor.acceptIntegerConst(name, val);
					}
					break;

					// Real constant.
					// c=rvalue
					// value is the real value, which can be INF or QNAN or SNAN
					// preceded
					// by a sign.
				case 'r' :
					{
						double val = 0;
						StringBuffer sb = new StringBuffer();
						while ((c = reader.read()) != -1) {
							sb.append((char) c);
						}
						try {
							String s = sb.toString();
							if (s.equals("-INF")) { //$NON-NLS-1$
								val = Double.NEGATIVE_INFINITY;
							} else if (s.equals("INF")) { //$NON-NLS-1$
								val = Double.POSITIVE_INFINITY;
							} else if (s.equals("QNAN")) { //$NON-NLS-1$
								val = Double.NaN;
							} else if (s.equals("SNAN")) { //$NON-NLS-1$
								val = Double.NaN;
							} else {
								val = Double.parseDouble(s);
							}
						} catch (NumberFormatException e) {
						} catch (IndexOutOfBoundsException e) {
						}
						requestor.acceptFloatConst(name, val);
					}
					break;

					// String constant.
					// c=svalue
					// value is a strinc encosed in either ' in which case ' are
					// escaped or
					// " in which case " are escaped.
					// Not supported by GDB.
				case 's' :
					break;

					// Set constant.
					// C/C++ does not have set
					// Not supported by GDB.
				case 'S' :
					break;
			}
		}
	}

	DebugType getDebugType(TypeNumber tn) {
		return (DebugType) mapTypes.get(tn);
	}

	public static void main(String[] args) {
		try {
			DebugSymsRequestor symreq = new DebugSymsRequestor();				
			Stabs stabs = new Stabs(args[0]);
			stabs.parse(symreq);
			DebugSym[] entries = symreq.getEntries();
			for (int i = 0; i < entries.length; i++) {
				DebugSym entry = entries[i];
				System.out.println(entry);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
