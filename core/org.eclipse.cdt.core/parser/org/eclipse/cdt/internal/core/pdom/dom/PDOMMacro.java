/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.parser.scanner.MacroDefinitionParser;
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
	private static final char[][] UNINITIALIZED= {};

	private final PDOM pdom;
	private final int record;
	
	private char[][] fParameterList= UNINITIALIZED;
	private char[] fName;
	private char[] fExpansion;

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
			
	public char[][] getParameterList() {
		if (fParameterList == UNINITIALIZED) {
			fParameterList= null;
			try {
				byte style= pdom.getDB().getByte(record + MACRO_STYLE);
				if (style == MACROSTYLE_FUNCTION) {
					List<char[]> paramList = new ArrayList<char[]>();
					PDOMMacroParameter param= getFirstParameter();
					while (param != null) {
						paramList.add(param.getName().getChars());
						param = param.getNextParameter();
					}
					fParameterList= paramList.toArray(new char[paramList.size()][]);
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return fParameterList;
	}

	public char[] getExpansionImage() {
		if (fExpansion == null) {
			try {
				fExpansion= getExpansionInDB().getChars();
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fExpansion= new char[] {};
			}
		}
		return fExpansion;
	}

	public char[] getNameCharArray() {
		if (fName == null) {
			try {
				fName= getNameInDB(pdom, record).getChars();
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fName= new char[] { ' ' };
			}
		}
		return fName;
	}
	
	public String getName() {
		return new String(getNameCharArray());
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

	public char[] getExpansion() {
		char[] expansionImage= getExpansionImage();
		return MacroDefinitionParser.getExpansion(expansionImage, 0, expansionImage.length);
	}

	public char[][] getParameterPlaceholderList() {
		char[][] params= getParameterList();
		if (params != null && params.length > 0) {
			char[] lastParam= params[params.length-1];
			if (CharArrayUtils.equals(lastParam, 0, Keywords.cpELLIPSIS.length, Keywords.cpELLIPSIS)) {
				char[][] result= new char[params.length][];
				System.arraycopy(params, 0, result, 0, params.length-1);
				result[params.length-1]= lastParam.length == Keywords.cpELLIPSIS.length ? Keywords.cVA_ARGS : 
					CharArrayUtils.extract(lastParam, Keywords.cpELLIPSIS.length, lastParam.length-Keywords.cpELLIPSIS.length);
				return result;
			}
		}
		return params;
	}

	public boolean isFunctionStyle() {
		return getParameterList() != null;
	}
	
	public boolean isDynamic() {
		return false;
	}

	public ILinkage getLinkage() throws CoreException {
		return Linkage.NO_LINKAGE;
	}

	public IScope getScope() throws DOMException {
		return null;
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}
}
