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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 * 
 * This class represents a collection of symbols that can be linked together at
 * link time. These are generally global symbols specific to a given language.
 */
public abstract class PDOMLinkage extends PDOMNode {

	private static final int ID_OFFSET   = PDOMNode.RECORD_SIZE + 0;
	private static final int NEXT_OFFSET = PDOMNode.RECORD_SIZE + 4;
	private static final int INDEX_OFFSET = PDOMNode.RECORD_SIZE + 8;
	
	protected static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 12;
	
	public PDOMLinkage(PDOMDatabase pdom, int record) {
		super(pdom, record);
	}

	protected PDOMLinkage(PDOMDatabase pdom, String languageId, char[] name) throws CoreException {
		super(pdom, null, name);
		Database db = pdom.getDB();

		// id
		int idrec = db.putString(languageId);
		db.putInt(record + ID_OFFSET, idrec);
		
		pdom.insertLinkage(this);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public static String getId(PDOMDatabase pdom, int record) throws CoreException {
		Database db = pdom.getDB();
		int namerec = db.getInt(record + ID_OFFSET);
		return db.getString(namerec);
	}

	public static int getNextLinkageRecord(PDOMDatabase pdom, int record) throws CoreException {
		return pdom.getDB().getInt(record + NEXT_OFFSET);
	}
	
	public PDOMLinkage getNextLinkage() throws CoreException {
		return pdom.getLinkage(pdom.getDB().getInt(record + NEXT_OFFSET));
	}
	
	public void setNext(int nextrec) throws CoreException {
		pdom.getDB().putInt(record + NEXT_OFFSET, nextrec);
	}
	
	public BTree getIndex() throws CoreException {
		return new BTree(pdom.getDB(), record + INDEX_OFFSET);
	}
	
	public PDOMLinkage getLinkage() throws CoreException {
		return this;
	}

	protected void addChild(PDOMNode child) throws CoreException {
		getIndex().insert(child.getRecord(), child.getIndexComparator());
	}
	
	public abstract PDOMBinding addName(IASTName name) throws CoreException;

	public abstract PDOMBinding adaptBinding(IBinding binding) throws CoreException;
	
	public abstract PDOMBinding getBinding(int record) throws CoreException;
	
}
