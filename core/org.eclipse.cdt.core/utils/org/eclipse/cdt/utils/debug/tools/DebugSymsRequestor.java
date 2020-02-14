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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.utils.debug.DebugParameterKind;
import org.eclipse.cdt.utils.debug.DebugType;
import org.eclipse.cdt.utils.debug.DebugVariableKind;
import org.eclipse.cdt.utils.debug.IDebugEntryRequestor;

/**
 * DebugSymsRequestor
 *
 */
public class DebugSymsRequestor implements IDebugEntryRequestor {

	DebugSym currentCU;
	DebugSym currentFunction;

	List<DebugSym> list = new ArrayList<>();

	/**
	 *
	 */
	public DebugSymsRequestor() {
		super();
	}

	public DebugSym[] getSortedEntries() {
		DebugSym[] syms = getEntries();
		Arrays.sort(syms);
		return syms;
	}

	public DebugSym[] getEntries() {
		DebugSym[] syms = new DebugSym[list.size()];
		list.toArray(syms);
		return syms;
	}

	public DebugSym getEntry(long addr) {
		DebugSym[] entries = getSortedEntries();
		int insertion = Arrays.binarySearch(entries, Long.valueOf(addr));
		if (insertion >= 0) {
			return entries[insertion];
		}
		if (insertion == -1) {
			return null;
		}
		insertion = -insertion - 1;
		DebugSym entry = entries[insertion - 1];
		if (addr < (entry.addr + entry.size)) {
			return entries[insertion - 1];
		}
		return null;
	}

	@Override
	public void enterCompilationUnit(String name, long address) {
		DebugSym sym = new DebugSym();
		sym.name = name;
		sym.addr = address;
		sym.type = "CU"; //$NON-NLS-1$
		sym.filename = name;
		currentCU = sym;
		list.add(sym);
	}

	@Override
	public void exitCompilationUnit(long address) {
		if (currentCU != null) {
			currentCU.size = address;
		}
		currentCU = null;
	}

	@Override
	public void enterInclude(String name) {
	}

	@Override
	public void exitInclude() {
	}

	@Override
	public void enterFunction(String name, DebugType type, boolean isGlobal, long address) {
		DebugSym sym = new DebugSym();
		sym.name = name;
		sym.addr = address;
		sym.type = "Func"; //$NON-NLS-1$
		if (currentCU != null) {
			sym.filename = currentCU.filename;
		}
		currentFunction = sym;
		list.add(sym);
	}

	@Override
	public void exitFunction(long address) {
		if (currentFunction != null) {
			currentFunction.size = address;
		}
		currentFunction = null;
	}

	@Override
	public void enterCodeBlock(long offset) {
	}

	@Override
	public void exitCodeBlock(long offset) {
	}

	@Override
	public void acceptStatement(int line, long address) {
		DebugSym sym = new DebugSym();
		sym.name = ""; //$NON-NLS-1$
		sym.addr = address;
		sym.startLine = line;
		sym.type = "SLINE"; //$NON-NLS-1$
		if (currentFunction != null) {
			if (currentFunction.startLine == 0) {
				currentFunction.startLine = line;
			}
			currentFunction.endLine = line;
		}
		if (currentCU != null) {
			sym.filename = currentCU.filename;
		}
		list.add(sym);
	}

	@Override
	public void acceptIntegerConst(String name, int value) {
	}

	@Override
	public void acceptFloatConst(String name, double value) {
	}

	@Override
	public void acceptTypeConst(String name, DebugType type, int value) {
	}

	@Override
	public void acceptParameter(String name, DebugType type, DebugParameterKind kind, long offset) {
		DebugSym sym = new DebugSym();
		sym.name = name;
		sym.addr = offset;
		sym.type = "PARAM"; //$NON-NLS-1$
		if (currentCU != null) {
			sym.filename = currentCU.filename;
		}
		list.add(sym);
	}

	@Override
	public void acceptVariable(String name, DebugType type, DebugVariableKind kind, long address) {
		DebugSym sym = new DebugSym();
		sym.name = name;
		sym.addr = address;
		sym.type = "VAR"; //$NON-NLS-1$
		if (currentCU != null) {
			sym.filename = currentCU.filename;
		}
		list.add(sym);
	}

	@Override
	public void acceptCaughtException(String name, DebugType type, long address) {
	}

	@Override
	public void acceptTypeDef(String name, DebugType type) {
	}

}
