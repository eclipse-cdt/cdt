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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.utils.debug.DebugParameterKind;
import org.eclipse.cdt.utils.debug.DebugType;
import org.eclipse.cdt.utils.debug.DebugVariableKind;
import org.eclipse.cdt.utils.debug.IDebugEntryRequestor;


/**
 * StabSymsRequestor
 *
 */
public class StabSymsRequestor implements IDebugEntryRequestor {

	StabSym currentCU;
	StabSym currentFunction;

	List list = new ArrayList();

	/**
	 * 
	 */
	public StabSymsRequestor() {
		super();
	}

	public StabSym[] getSortedEntries() {
		StabSym[] syms = getEntries();
		Arrays.sort(syms);
		return syms;
	}

	public StabSym[] getEntries() {
		StabSym[] syms = new StabSym[list.size()];
		list.toArray(syms);
		return syms;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#enterCompilationUnit(java.lang.String, long)
	 */
	public void enterCompilationUnit(String name, long address) {
		StabSym sym = new StabSym();
		sym.name = name;
		sym.addr = address;
		sym.type = "CU";
		sym.filename = name;
		currentCU = sym;
		list.add(sym);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#exitCompilationUnit(long)
	 */
	public void exitCompilationUnit(long address) {
		if (currentCU != null) {
			currentCU.size = address;
		}
		currentCU = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#enterInclude(java.lang.String)
	 */
	public void enterInclude(String name) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#exitInclude()
	 */
	public void exitInclude() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#enterFunction(java.lang.String, int, boolean, long)
	 */
	public void enterFunction(String name, DebugType type, boolean isGlobal, long address) {
		StabSym sym = new StabSym();
		sym.name = name;
		sym.addr = address;
		sym.type = "Func";
		if (currentCU != null) {
			sym.filename = currentCU.filename;
		}
		currentFunction = sym;
		list.add(sym);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#exitFunction(long)
	 */
	public void exitFunction(long address) {
		if (currentFunction != null) {
			currentFunction.size = address;
		}
		currentFunction = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#enterCodeBlock(long)
	 */
	public void enterCodeBlock(long offset) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#exitCodeBlock(long)
	 */
	public void exitCodeBlock(long offset) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#acceptStatement(int, long)
	 */
	public void acceptStatement(int line, long address) {
		StabSym sym = new StabSym();
		sym.name = "";
		sym.addr = address;
		sym.startLine = line;
		sym.type = "SLINE";
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#acceptIntegerConst(java.lang.String, long)
	 */
	public void acceptIntegerConst(String name, int value) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#acceptFloatConst(java.lang.String, double)
	 */
	public void acceptFloatConst(String name, double value) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#acceptTypeConst(java.lang.String, org.eclipse.cdt.utils.debug.DebugType, int)
	 */
	public void acceptTypeConst(String name, DebugType type, int value) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#acceptParameter(java.lang.String, int, int, long)
	 */
	public void acceptParameter(String name, DebugType type, DebugParameterKind kind, long offset) {
		StabSym sym = new StabSym();
		sym.name = name;
		sym.addr = offset;
		sym.type = "PARAM";
		if (currentCU != null) {
			sym.filename = currentCU.filename;
		}
		list.add(sym);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#acceptVariable(java.lang.String, int, int, long)
	 */
	public void acceptVariable(String name, DebugType type, DebugVariableKind kind, long address) {
		StabSym sym = new StabSym();
		sym.name = name;
		sym.addr = address;
		sym.type = "VAR";
		if (currentCU != null) {
			sym.filename = currentCU.filename;
		}
		list.add(sym);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#acceptCaughtException(java.lang.String, org.eclipse.cdt.utils.debug.DebugType, long)
	 */
	public void acceptCaughtException(String name, DebugType type, long address) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.debug.IDebugEntryRequestor#acceptTypeDef(java.lang.String, org.eclipse.cdt.utils.debug.DebugType)
	 */
	public void acceptTypeDef(String name, DebugType type) {		
	}

}
