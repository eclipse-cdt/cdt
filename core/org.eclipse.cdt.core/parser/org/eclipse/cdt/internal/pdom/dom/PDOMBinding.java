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
package org.eclipse.cdt.internal.pdom.dom;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.PDOMUtils;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

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
		
		public int compare(int record1, int record2) throws IOException {
			int string1 = db.getInt(record1 + STRING_REC_OFFSET);
			int string2 = db.getInt(record2 + STRING_REC_OFFSET);
			
			return PDOMUtils.stringCompare(db, string1, string2);
		}
		
	}
	
	public abstract static class Visitor implements IBTreeVisitor {
	
		private Database db;
		private char[] key;
		
		public Visitor(Database db, char[] key) {
			this.db = db;
			this.key = key;
		}
		
		public int compare(int record1) throws IOException {
			int string1 = db.getInt(record1 + STRING_REC_OFFSET);
			
			return PDOMUtils.stringCompare(db, string1, key);
		}

	}

	public static class FindVisitor extends Visitor {
		
		private int record;
		
		public FindVisitor(Database db, char[] stringKey) {
			super(db, stringKey);
		}
		
		public boolean visit(int record) throws IOException {
			this.record = record;
			return false;
		}
		
		public int findIn(BTree btree) throws IOException {
			btree.visit(this);
			return record;
		}

	}
	
	public PDOMBinding(PDOMDatabase pdom, IASTName name, IBinding binding) throws CoreException {
		try {
			this.pdom = pdom;

			char[] namechars = name.toCharArray();
			
			BTree index = pdom.getBindingIndex();
			record = new FindVisitor(pdom.getDB(), namechars).findIn(index);
			
			if (record == 0) {
				Database db = pdom.getDB();
				record = db.malloc(getRecordSize());

				int stringRecord = db.malloc((namechars.length + 1) * Database.CHAR_SIZE);
				db.putChars(stringRecord, namechars);
				
				db.putInt(record + STRING_REC_OFFSET, stringRecord);
				pdom.getBindingIndex().insert(record, new Comparator(db));
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					CCorePlugin.PLUGIN_ID, 0, "Failed to allocate binding", e));
		}
	}

	public PDOMBinding(PDOMDatabase pdom, int bindingRecord) {
		this.pdom = pdom;
		this.record = bindingRecord;
	}
	
	public int getRecord() {
		return record;
	}

	public void addDeclaration(PDOMName name) throws IOException {
		PDOMName firstDeclaration = getFirstDeclaration();
		if (firstDeclaration != null) {
			firstDeclaration.setPrevInBinding(name);
			name.setNextInBinding(firstDeclaration);
		}
		pdom.getDB().putInt(record + FIRST_DECL_OFFSET, name.getRecord());
	}
	
	public PDOMName getFirstDeclaration() throws IOException {
		int firstDeclRec = pdom.getDB().getInt(record + FIRST_DECL_OFFSET);
		return firstDeclRec != 0 ? new PDOMName(pdom, firstDeclRec) : null;
	}
	
	public String getName() {
		try {
			Database db = pdom.getDB();
			int stringRecord = db.getInt(record + STRING_REC_OFFSET);
			return db.getString(stringRecord);
		} catch (IOException e) {
			CCorePlugin.log(e);
			return "";
		}
	}

	public char[] getNameCharArray() {
		try {
			Database db = pdom.getDB();
			int stringRecord = db.getInt(record + STRING_REC_OFFSET);
			return db.getChars(stringRecord);
		} catch (IOException e) {
			CCorePlugin.log(e);
			return new char[0];
		}
	}

	public IScope getScope() throws DOMException {
		// TODO implement this
		return null;
	}

	public static PDOMBinding find(PDOMDatabase pdom, char[] name) throws IOException {
		BTree index = pdom.getBindingIndex();
		int bindingRecord = new FindVisitor(pdom.getDB(), name).findIn(index);
		if (bindingRecord != 0)
			return new PDOMBinding(pdom, bindingRecord);
		else
			return null;
	}

}
