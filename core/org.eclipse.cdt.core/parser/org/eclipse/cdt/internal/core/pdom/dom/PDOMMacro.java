/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexMacro;
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
public class PDOMMacro implements IIndexMacro, IASTFileLocation {

	private final PDOM pdom;
	private final int record;
	private IIndexMacro macro;

	private static final byte MACROSTYLE_UNKNOWN = 0; // for reading versions of PDOM <39
	private static final byte MACROSTYLE_OBJECT  = 1;
	private static final byte MACROSTYLE_FUNCTION= 2;
	
	private static final int NAME = 0;
	private static final int FILE = 4;
	private static final int NAME_OFFSET = 8;
	private static final int NAME_LENGTH = 12; // short
	private static final int FIRST_PARAMETER = 14;
	private static final int EXPANSION = 18;
	private static final int NEXT_MACRO = 22;
	private static final int MACRO_STYLE = 26; // byte
	
	private static final int RECORD_SIZE = 27;
	
	public PDOMMacro(PDOM pdom, int record) {
		this.pdom = pdom;
		this.record = record;
	}
	
	public PDOMMacro(PDOM pdom, IASTPreprocessorMacroDefinition macro, PDOMFile file) throws CoreException {
		this.pdom = pdom;
		
		Database db = pdom.getDB();
		this.record = db.malloc(RECORD_SIZE);
		IASTName name = macro.getName();
		db.putInt(record + NAME, db.newString(name.toCharArray()).getRecord());
		db.putInt(record + FILE, file.getRecord());
		IASTFileLocation fileloc = name.getFileLocation();
		db.putInt(record + NAME_OFFSET, fileloc.getNodeOffset());
		db.putShort(record + NAME_LENGTH, (short) fileloc.getNodeLength());
		db.putInt(record + EXPANSION, db.newString(macro.getExpansion()).getRecord());
		setNextMacro(0);
		
		byte macroStyle= MACROSTYLE_OBJECT;
		PDOMMacroParameter last = null;
		if (macro instanceof IASTPreprocessorFunctionStyleMacroDefinition) {
			macroStyle= MACROSTYLE_FUNCTION;
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
		db.putByte(record + MACRO_STYLE, macroStyle);
	}
	
	public int getRecord() {
		return record;
	}
	
	public void delete() throws CoreException {
		getNameInDB(pdom, record).delete();
		getExpansionInDB().delete();
		PDOMMacroParameter param = getFirstParameter();
		if (param != null)
			param.delete();
		pdom.getDB().free(record);
	}
	
	public static IString getNameInDB(PDOM pdom, int record) throws CoreException {
		Database db = pdom.getDB();
		int rec = db.getInt(record + NAME);
		return db.getString(rec);
	}
	
	private IString getExpansionInDB() throws CoreException {
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

	private void setNextMacro(int rec) throws CoreException {
		pdom.getDB().putInt(record + NEXT_MACRO, rec);
	}
	
	private PDOMMacroParameter getFirstParameter() throws CoreException {
		int rec = pdom.getDB().getInt(record + FIRST_PARAMETER);
		return rec != 0 ? new PDOMMacroParameter(pdom, rec) : null;
	}
	
	private class ObjectStylePDOMMacro extends ObjectStyleMacro implements IIndexMacro {
		public ObjectStylePDOMMacro(char[] name) {
			super(name, null);
		}
		public char[] getExpansion() {
			return getMacroExpansion();
		}
		public IASTFileLocation getFileLocation() {
			return PDOMMacro.this;
		}
		public IIndexFile getFile() throws CoreException {
			return PDOMMacro.this.getFile();
		}
		public int getNodeOffset() {
			return PDOMMacro.this.getNodeOffset();
		}
		public int getNodeLength() {
			return PDOMMacro.this.getNodeLength();
		}
		public char[][] getParameterList() {
			return null;
		}
	}
	
	private class FunctionStylePDOMMacro extends FunctionStyleMacro implements IIndexMacro {
		public FunctionStylePDOMMacro(char[] name, char[][] arglist) {
			super(name, null, arglist);
		}
		public char[] getExpansion() {
			return getMacroExpansion();
		}
		public IASTFileLocation getFileLocation() {
			return PDOMMacro.this;
		}
		public IIndexFile getFile() throws CoreException {
			return PDOMMacro.this.getFile();
		}
		public int getNodeOffset() {
			return PDOMMacro.this.getNodeOffset();
		}
		public int getNodeLength() {
			return PDOMMacro.this.getNodeLength();
		}
		public char[][] getParameterList() {
			return getOriginalParameters();
		}
	}
	
	private char[] getMacroExpansion() {
		try {
			return PDOMMacro.this.getExpansionInDB().getChars();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[] { ' ' };
		}
	}

	public IMacro getMacro() throws CoreException {
		rebuildMacro();
		return macro;
	}
	
	private void rebuildMacro() throws CoreException {
		if (macro == null) {
			char[] name = getNameInDB(pdom, record).getChars();
			PDOMMacroParameter param= getFirstParameter();

			byte style= pdom.getDB().getByte(record + MACRO_STYLE);
			if(style == MACROSTYLE_UNKNOWN) {
				/* PDOM formats < 39 do not store MACRO_STYLE (208558) */
				style= param != null ? MACROSTYLE_FUNCTION : MACROSTYLE_OBJECT;
			}

			switch(style) {
			case MACROSTYLE_OBJECT:
				macro= new ObjectStylePDOMMacro(name);
				break;
			case MACROSTYLE_FUNCTION:
				List paramList = new ArrayList();
				while (param != null) {
					paramList.add(param.getName().getChars());
					param = param.getNextParameter();
				}
				char[][] params = (char[][])paramList.toArray(new char[paramList.size()][]);
				macro= new FunctionStylePDOMMacro(name, params);
				break;
			default:
				throw new PDOMNotImplementedError();
			}
		}
	}

	public char[][] getParameterList() {
		try {
			rebuildMacro();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[][]{};
		}
		return macro.getParameterList();
	}

	public char[] getSignature() {
		try {
			rebuildMacro();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[] { ' ' };
		}
		return macro.getSignature();
	}

	public char[] getExpansion() {
		try {
			rebuildMacro();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[] { ' ' };
		}
		return macro.getExpansion();
	}

	public char[] getName() {
		try {
			return getNameInDB(pdom, record).getChars();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[] { ' ' };
		}
	}
	
	public IIndexFile getFile() throws CoreException {
		int filerec = pdom.getDB().getInt(record + FILE);
		return filerec != 0 ? new PDOMFile(pdom, filerec) : null;
	}

	public int getEndingLineNumber() {
		return 0;
	}

	public String getFileName() {
		try {
			PDOMFile file = (PDOMFile) getFile();
			if(file!=null) {
				/*
				 * We need to spec. what this method can return to know
				 * how to implement this. Existing implmentations return
				 * the absolute path, so here we attempt to do the same.
				 */
				URI uri = file.getLocation().getURI();
				if ("file".equals(uri.getScheme())) //$NON-NLS-1$
					return uri.getSchemeSpecificPart();
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public int getStartingLineNumber() {
		return 0;
	}

	public IASTFileLocation asFileLocation() {
		return this;
	}
	
	public IASTFileLocation getFileLocation() {
		return this;
	}

	public int getNodeLength() {
		try {
			return pdom.getDB().getShort(record + NAME_LENGTH);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

	public int getNodeOffset() {
		try {
			return pdom.getDB().getInt(record + NAME_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return 0;
		}
	}

}
