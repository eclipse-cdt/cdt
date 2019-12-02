/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.utils.debug.DebugBaseType;
import org.eclipse.cdt.utils.debug.DebugParameterKind;
import org.eclipse.cdt.utils.debug.DebugType;
import org.eclipse.cdt.utils.debug.DebugVariableKind;
import org.eclipse.cdt.utils.debug.IDebugEntryRequestor;
import org.eclipse.cdt.utils.debug.dwarf.Dwarf;
import org.eclipse.cdt.utils.debug.stabs.Stabs;
import org.eclipse.cdt.utils.elf.Elf;

/**
 * DebugDump
 *
 */
public class DebugDump implements IDebugEntryRequestor {

	BufferedWriter bwriter;
	int bracket;
	int paramCount = -1;

	String currentCU;

	public DebugDump(OutputStream stream) {
		bwriter = new BufferedWriter(new OutputStreamWriter(stream));
	}

	void parse(String file) throws IOException {
		try (Elf elf = new Elf(file)) {
			parse(elf);
		}
	}

	void parse(Elf elf) throws IOException {
		Elf.Attribute attribute = elf.getAttributes();
		int type = attribute.getDebugType();
		if (type == Elf.Attribute.DEBUG_TYPE_STABS) {
			Stabs stabs = new Stabs(elf);
			stabs.parse(this);
		} else if (type == Elf.Attribute.DEBUG_TYPE_DWARF) {
			Dwarf dwarf = new Dwarf(elf);
			dwarf.parse(this);
		} else {
			throw new IOException(CCorePlugin.getResourceString("Util.unknownFormat")); //$NON-NLS-1$
		}
		bwriter.flush();
	}

	void write(String s) {
		try {
			bwriter.write(s, 0, s.length());
		} catch (IOException e) {
			// ignore.
		}
	}

	void newLine() {
		try {
			bwriter.newLine();
		} catch (IOException e) {
			// ignore
		}
	}

	String printTabs() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bracket; i++) {
			sb.append('\t');
		}
		return sb.toString();
	}

	@Override
	public void enterCompilationUnit(String name, long address) {
		write("/* Enter Compilation Unit " + name + " address " + Long.toHexString(address) + " */"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		newLine();
		currentCU = name;
	}

	@Override
	public void exitCompilationUnit(long address) {
		write("/* Exit Compilation Unit "); //$NON-NLS-1$
		if (currentCU != null) {
			write(currentCU + " address " + Long.toHexString(address)); //$NON-NLS-1$
		}
		write(" */"); //$NON-NLS-1$
		newLine();
		newLine();
		currentCU = null;
	}

	@Override
	public void enterInclude(String name) {
		write("#include \"" + name + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
		write("/* Enter Include */"); //$NON-NLS-1$
		newLine();
	}

	@Override
	public void exitInclude() {
		//write("/* Exit Include */");
		//newLine();newLine();
	}

	@Override
	public void enterFunction(String name, DebugType type, boolean isGlobal, long address) {
		write("/* Func:" + name + " address " + Long.toHexString(address) + " */"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		newLine();
		if (!isGlobal) {
			write("static "); //$NON-NLS-1$
		}
		write(type.toString() + " " + name + "("); //$NON-NLS-1$ //$NON-NLS-2$
		paramCount = 0;
	}

	@Override
	public void exitFunction(long address) {
		if (paramCount > -1) {
			paramCount = -1;
			write(")"); //$NON-NLS-1$
			newLine();
			write("{"); //$NON-NLS-1$
			newLine();
			bracket++;
		}
		for (; bracket > 0; bracket--) {
			write("}"); //$NON-NLS-1$
		}
		write(" /* Exit Func address " + Long.toHexString(address) + " */"); //$NON-NLS-1$ //$NON-NLS-2$
		newLine();
		newLine();
	}

	@Override
	public void enterCodeBlock(long offset) {
		if (paramCount > -1) {
			paramCount = -1;
			write(")"); //$NON-NLS-1$
			newLine();
		}
		write(printTabs() + "{ " + "/* " + offset + " */"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		newLine();
		bracket++;
	}

	@Override
	public void exitCodeBlock(long offset) {
		bracket--;
		write(printTabs() + "} " + "/* " + offset + " */"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		newLine();
	}

	@Override
	public void acceptStatement(int line, long address) {
		if (paramCount > -1) {
			write(")"); //$NON-NLS-1$
			newLine();
			write("{"); //$NON-NLS-1$
			newLine();
			bracket++;
			paramCount = -1;
		}
		write(printTabs() + "/* line " + line + " address " + address + " */"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		newLine();
	}

	@Override
	public void acceptIntegerConst(String name, int value) {
		write("const int " + name + " = " + value + ";"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		newLine();
	}

	@Override
	public void acceptFloatConst(String name, double value) {
		write("const float " + name + " = " + value + ";"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		newLine();
	}

	@Override
	public void acceptTypeConst(String name, DebugType type, int value) {
		write("const " + type.toString() + " " + name + " = " + value + ";"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		newLine();
	}

	@Override
	public void acceptParameter(String name, DebugType type, DebugParameterKind kind, long offset) {
		if (paramCount > 0) {
			write(", "); //$NON-NLS-1$
		}
		paramCount++;
		write(type.toString() + " " + name + "/* " + offset + " */"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public void acceptVariable(String name, DebugType type, DebugVariableKind kind, long address) {
		write(printTabs() + type.toString() + " " + name + ";" + "/* " + Long.toHexString(address) + " */"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		newLine();
	}

	@Override
	public void acceptCaughtException(String name, DebugType type, long address) {
	}

	@Override
	public void acceptTypeDef(String name, DebugType type) {
		if (!name.equals(type.toString())) {
			write("typedef " + type.toString() + " " + name + ";"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			newLine();
		} else if (type instanceof DebugBaseType) {
			DebugBaseType baseType = (DebugBaseType) type;
			write("/* " + name + ": " + baseType.sizeof() + " bytes */"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			newLine();
		} else {
			//int x = 9;
		}
	}

	public static void main(String[] args) {
		try {
			//ByteArrayOutputStream out = new ByteArrayOutputStream();
			DebugDump dump = new DebugDump(System.out);
			dump.parse(args[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
