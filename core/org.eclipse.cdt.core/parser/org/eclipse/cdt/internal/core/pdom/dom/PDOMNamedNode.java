/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    IBM Corporation
 *    Andrew Ferguson (Symbian)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public abstract class PDOMNamedNode extends PDOMNode {
	/**
	 * Offset of pointer to node name (relative to the beginning of the record).
	 */
	private static final int NAME = PDOMNode.RECORD_SIZE + 0;

	/**
	 * The size in bytes of a PDOMNamedNode record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 4;

	private char[] fName;
	
	public PDOMNamedNode(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMNamedNode(PDOM pdom, PDOMNode parent, char[] name) throws CoreException {
		super(pdom, parent);
		
		fName= name;
		Database db = pdom.getDB();
		db.putInt(record + NAME,
				name != null ? db.newString(name).getRecord() : 0);
	}

	@Override
	abstract protected int getRecordSize();

	public IString getDBName() throws CoreException {
		Database db = pdom.getDB();
		int namerec = db.getInt(record + NAME);
		return db.getString(namerec);
	}
	
	public static IString getDBName(PDOM pdom, int record) throws CoreException {
		Database db = pdom.getDB();
		int namerec = db.getInt(record + NAME);
		return db.getString(namerec);
	}
	
	public char[] getNameCharArray() throws CoreException {
		if (fName != null)
			return fName;
		
		return fName= getDBName().getChars();
	}
	
	public boolean hasName(char[] name) throws CoreException {
		if (fName != null)
			return Arrays.equals(fName, name);
			
		return getDBName().equals(name);
	}

	/**
	 * Template parameters need to update their name.
	 * @throws CoreException 
	 */
	protected void updateName(char[] nameCharArray) throws CoreException {
		if (fName != null && CharArrayUtils.equals(fName, nameCharArray))
			return;
		
		IString name= getDBName();
		if (!name.equals(nameCharArray)) {
			name.delete();
			final Database db= pdom.getDB();
			db.putInt(record + NAME, db.newString(nameCharArray).getRecord());
		}
		fName= nameCharArray;
	}


	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		final Database db = pdom.getDB();
		final int namerec= db.getInt(record + NAME);
		if (namerec != 0) {
			db.free(namerec);
		}
		super.delete(linkage);
	}
	
	public boolean mayHaveChildren() {
		return false;
	}
	
	public IIndexFragmentBinding getParentBinding() throws CoreException {
		PDOMNode parent= getParentNode();
		if (parent instanceof IIndexBinding) {
			return (IIndexFragmentBinding) parent;
		}
		return null;
	}
	
	public final IIndexFragmentBinding getOwner() {
		try {
			return getParentBinding();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
}
