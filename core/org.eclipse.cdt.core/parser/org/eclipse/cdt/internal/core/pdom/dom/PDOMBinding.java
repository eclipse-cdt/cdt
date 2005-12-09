/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILanguage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMBinding implements IBinding {

	protected final PDOMDatabase pdom;
	protected int record;
	
	private static final int STRING_REC_OFFSET =  0; // size 4
	private static final int FIRST_DECL_OFFSET =  4; // size 4
	private static final int FIRST_DEF_OFFSET  =  8; // size 4
	private static final int FIRST_REF_OFFSET  = 12; // size 4
	private static final int LANGUAGE_OFFSET   = 16; // size 2
	private static final int TYPE_OFFSET       = 18; // size 2
	
	protected int getRecordSize() {
		return 20;
	}

	public static class Comparator implements IBTreeComparator {
	
		private Database db;
	
		public Comparator(Database db) {
			this.db = db;
		}
		
		public int compare(int record1, int record2) throws CoreException {
			int string1 = db.getInt(record1 + STRING_REC_OFFSET);
			int string2 = db.getInt(record2 + STRING_REC_OFFSET);
			// Need to deal with language and type
			
			return db.stringCompare(string1, string2);
		}
		
	}
	
	public abstract static class Visitor implements IBTreeVisitor {
	
		private Database db;
		private char[] key;
		
		public Visitor(Database db, char[] key, int language, int type) {
			this.db = db;
			this.key = key;
		}
		
		public int compare(int record1) throws CoreException {
			int string1 = db.getInt(record1 + STRING_REC_OFFSET);
			// Need to deal with language and type
			
			return db.stringCompare(string1, key);
		}

	}

	public static class FindVisitor extends Visitor {
		
		private int record;
		
		public FindVisitor(Database db, char[] stringKey, int language, int type) {
			super(db, stringKey, language, type);
		}
		
		public boolean visit(int record) throws CoreException {
			this.record = record;
			return false;
		}
		
		public int findIn(BTree btree) throws CoreException {
			btree.visit(this);
			return record;
		}

	}
	
	public PDOMBinding(PDOMDatabase pdom, IASTName name, int language, int type) throws CoreException {
		this.pdom = pdom;

		char[] namechars = name.toCharArray();
			
		BTree index = pdom.getBindingIndex();
		record = new FindVisitor(pdom.getDB(), namechars, language, type).findIn(index);
			
		if (record == 0) {
			Database db = pdom.getDB();
			record = db.malloc(getRecordSize());
			
			db.putChar(record + LANGUAGE_OFFSET, (char)language);
			db.putChar(record + TYPE_OFFSET, (char)type);

			int stringRecord = db.malloc((namechars.length + 1) * Database.CHAR_SIZE);
			db.putChars(stringRecord, namechars);
				
			db.putInt(record + STRING_REC_OFFSET, stringRecord);
			pdom.getBindingIndex().insert(record, new Comparator(db));
		}
	}

	public PDOMBinding(PDOMDatabase pdom, int bindingRecord) {
		this.pdom = pdom;
		this.record = bindingRecord;
	}
	
	public int getRecord() {
		return record;
	}

	public ILanguage getLanguage() throws CoreException {
		return pdom.getLanguage(pdom.getDB().getChar(record + LANGUAGE_OFFSET));
	}
	
	public int getBindingType() throws CoreException {
		return pdom.getDB().getChar(record + TYPE_OFFSET);
	}
	
	public boolean hasDeclarations() throws CoreException {
		Database db = pdom.getDB();
		return db.getInt(record + FIRST_DECL_OFFSET) != 0
			|| db.getInt(record + FIRST_DEF_OFFSET) != 0;
	}
	
	public void addDeclaration(PDOMName name) throws CoreException {
		PDOMName first = getFirstDeclaration();
		if (first != null) {
			first.setPrevInBinding(name);
			name.setNextInBinding(first);
		}
		setFirstDeclaration(name);
	}
	
	public void addDefinition(PDOMName name) throws CoreException {
		PDOMName first = getFirstDefinition();
		if (first != null) {
			first.setPrevInBinding(name);
			name.setNextInBinding(first);
		}
		setFirstDefinition(name);
	}
	
	public void addReference(PDOMName name) throws CoreException {
		PDOMName first = getFirstReference();
		if (first != null) {
			first.setPrevInBinding(name);
			name.setNextInBinding(first);
		}
		setFirstReference(name);
	}
	
	public PDOMName getFirstDeclaration() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_DECL_OFFSET);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}
	
	public void setFirstDeclaration(PDOMName name) throws CoreException {
		int namerec = name != null ? name.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_DECL_OFFSET, namerec);
	}
	
	public PDOMName getFirstDefinition() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_DEF_OFFSET);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}
	
	public void setFirstDefinition(PDOMName name) throws CoreException {
		int namerec = name != null ? name.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_DEF_OFFSET, namerec);
	}
	
	public PDOMName getFirstReference() throws CoreException {
		int namerec = pdom.getDB().getInt(record + FIRST_REF_OFFSET);
		return namerec != 0 ? new PDOMName(pdom, namerec) : null;
	}
	
	public void setFirstReference(PDOMName name) throws CoreException {
		int namerec = name != null ? name.getRecord() : 0;
		pdom.getDB().putInt(record + FIRST_REF_OFFSET, namerec);
	}
	
	public String getName() {
		try {
			Database db = pdom.getDB();
			int stringRecord = db.getInt(record + STRING_REC_OFFSET);
			return db.getString(stringRecord);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return "";
	}

	public char[] getNameCharArray() {
		try {
			Database db = pdom.getDB();
			int stringRecord = db.getInt(record + STRING_REC_OFFSET);
			return db.getChars(stringRecord);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return new char[0];
	}

	public IScope getScope() throws DOMException {
		// TODO implement this
		return null;
	}

	public static PDOMBinding find(PDOMDatabase pdom, char[] name, int language, int type) throws CoreException {
		BTree index = pdom.getBindingIndex();
		int bindingRecord = new FindVisitor(pdom.getDB(), name, language, type).findIn(index);
		if (bindingRecord != 0)
			return new PDOMBinding(pdom, bindingRecord);
		else
			return null;
	}

}
