/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.internal.core.parser.scanner2.FunctionStyleMacro;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents macros.
 * 
 * @author Doug Schaefer
 */
public class PDOMMacro {

	private final PDOM pdom;
	private final int record;

	private static final int NAME = 0;
	private static final int FIRST_PARAMETER = 4;
	private static final int EXPANSION = 8;
	private static final int NEXT_MACRO = 12;
	
	private static final int RECORD_SIZE = 16;
	
	public PDOMMacro(PDOM pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	public PDOMMacro(PDOM pdom, IASTPreprocessorMacroDefinition macro) throws CoreException {
		this.pdom = pdom;
		
		Database db = pdom.getDB();
		this.record = db.malloc(RECORD_SIZE);
		db.putInt(record + NAME, db.newString(macro.getName().toCharArray()).getRecord());
		db.putInt(record + EXPANSION, db.newString(macro.getExpansion()).getRecord());
		setNextMacro(0);
		
		PDOMMacroParameter last = null;
		if (macro instanceof IASTPreprocessorFunctionStyleMacroDefinition) {
			IASTPreprocessorFunctionStyleMacroDefinition func = (IASTPreprocessorFunctionStyleMacroDefinition)macro;
			IASTFunctionStyleMacroParameter[] params = func.getParameters();
			for (int i = params.length - 1; i >= 0; --i) {
				IASTFunctionStyleMacroParameter param = params[i];
				PDOMMacroParameter pdomParam = new PDOMMacroParameter(pdom, param.getParameter());
				if (last != null)
					pdomParam.setNextParameter(last);
				last = pdomParam;
			}
		}
		db.putInt(record + FIRST_PARAMETER, last != null ? last.getRecord() : 0);
	}
	
	public int getRecord() {
		return record;
	}
	
	public void delete() throws CoreException {
		pdom.getDB().free(record);
	}
	
	public IString getName() throws CoreException {
		Database db = pdom.getDB();
		int rec = db.getInt(record + NAME);
		return db.getString(rec);
	}
	
	public IString getExpansion() throws CoreException {
		Database db = pdom.getDB();
		int rec = db.getInt(record + EXPANSION);
		return db.getString(rec);
	}
	
	public PDOMMacro getNextMacro() throws CoreException {
		int rec = pdom.getDB().getInt(record + NEXT_MACRO);
		return rec != 0 ? new PDOMMacro(pdom, rec) : null;
	}
	
	public void setNextMacro(PDOMMacro macro) throws CoreException {
		setNextMacro(macro != null ? macro.getRecord() : 0);
	}

	public void setNextMacro(int rec) throws CoreException {
		pdom.getDB().putInt(record + NEXT_MACRO, rec);
	}
	
	public PDOMMacroParameter getFirstParameter() throws CoreException {
		int rec = pdom.getDB().getInt(record + FIRST_PARAMETER);
		return rec != 0 ? new PDOMMacroParameter(pdom, rec) : null;
	}
	
	private class ObjectStylePDOMMacro extends ObjectStyleMacro {
		public ObjectStylePDOMMacro(char[] name) {
			super(name, null);
		}
		public char[] getExpansion() {
			return getMacroExpansion();
		}
	}
	
	private class FunctionStylePDOMMacro extends FunctionStyleMacro {
		public FunctionStylePDOMMacro(char[] name, char[][] arglist) {
			super(name, null, arglist);
		}
		public char[] getExpansion() {
			return getMacroExpansion();
		}
	}
	
	private char[] getMacroExpansion() {
		try {
			return PDOMMacro.this.getExpansion().getChars();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[] { ' ' };
		}
	}

	public IMacro getMacro() throws CoreException {
		char[] name = getName().getChars();
		PDOMMacroParameter param = getFirstParameter();
		if (param != null) {
			List paramList = new ArrayList();
			while (param != null) {
				paramList.add(param.getName().getChars());
				param = param.getNextParameter();
			}
			char[][] params = (char[][])paramList.toArray(new char[paramList.size()][]);
			return new FunctionStylePDOMMacro(name, params);
		} else
			return new ObjectStylePDOMMacro(name);
	}
}
