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
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public abstract class PDOMBinding extends PDOMNode implements IBinding {

	private static final int BINDING_TYPE_OFFSET = PDOMNode.RECORD_SIZE +  4; // size 4
	private static final int FIRST_DECL_OFFSET   = PDOMNode.RECORD_SIZE +  8; // size 4
	private static final int FIRST_DEF_OFFSET    = PDOMNode.RECORD_SIZE + 12; // size 4
	private static final int FIRST_REF_OFFSET    = PDOMNode.RECORD_SIZE + 16; // size 4
	
	protected static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 20;
	
	protected PDOMBinding(PDOM pdom, PDOMNode parent, IASTName name, int type) throws CoreException {
		super(pdom, parent, name.toCharArray());
		Database db = pdom.getDB();
		
		// Binding type
		db.putInt(record + BINDING_TYPE_OFFSET, type);
	}
	
	public PDOMBinding(PDOM pdom, int record) {
		super(pdom, record);
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter == PDOMBinding.class)
			return this;
		else
			return null;
	}
	
	public static int getBindingType(PDOM pdom, int record) throws CoreException {
		return pdom.getDB().getInt(record + BINDING_TYPE_OFFSET);
	}
	
	/**
	 * Is the binding as the record orphaned, i.e., has no declarations
	 * or references.
	 * 
	 * @param pdom
	 * @param record
	 * @return
	 * @throws CoreException
	 */
	public static boolean isOrphaned(PDOM pdom, int record) throws CoreException {
		Database db = pdom.getDB();
		return db.getInt(record + FIRST_DECL_OFFSET) == 0
			&& db.getInt(record + FIRST_DEF_OFFSET) == 0
			&& db.getInt(record + FIRST_REF_OFFSET) == 0;
	}
	
	public int getRecord() {
		return record;
	}

	public int getBindingType() throws CoreException {
		return pdom.getDB().getInt(record + BINDING_TYPE_OFFSET);
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
			return super.getDBName().getString();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return "";
	}

	public char[] getNameCharArray() {
		try {
			return super.getNameCharArray();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return new char[0];
	}

	public boolean hasName(char[] name) throws CoreException {
		return pdom.getDB().stringCompare(getNameRecord(), name) == 0;
	}
	
	public IScope getScope() throws DOMException {
		// TODO implement this
		return null;
	}

}
