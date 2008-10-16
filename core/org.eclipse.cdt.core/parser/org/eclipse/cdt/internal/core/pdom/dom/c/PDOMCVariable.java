/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    IBM Corporation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Database representation for c-variables
 */
class PDOMCVariable extends PDOMBinding implements IVariable {

	/**
	 * Offset of pointer to type information for this variable
	 * (relative to the beginning of the record).
	 */
	private static final int TYPE_OFFSET = PDOMBinding.RECORD_SIZE + 0;

	/**
	 * Offset of pointer to value information for this variable
	 * (relative to the beginning of the record).
	 */
	private static final int VALUE_OFFSET = PDOMBinding.RECORD_SIZE + 4;

	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	private static final int ANNOTATIONS = PDOMBinding.RECORD_SIZE + 8;
	
	/**
	 * The size in bytes of a PDOMCVariable record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 9;
	
	public PDOMCVariable(PDOM pdom, PDOMNode parent, IVariable variable) throws CoreException {
		super(pdom, parent, variable.getNameCharArray());

		try {
			final Database db = pdom.getDB();
			setType(parent.getLinkageImpl(), variable.getType());
			db.putByte(record + ANNOTATIONS, PDOMCAnnotation.encodeAnnotation(variable));
			
			setValue(db, variable);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	private void setValue(final Database db, IVariable variable) throws CoreException {
		IValue val= variable.getInitialValue();
		db.putInt(record + VALUE_OFFSET, val == null ? 0 : db.newString(val.getCanonicalRepresentation()).getRecord());
	}
	
	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof IVariable) {
			final Database db = pdom.getDB();
			IVariable var= (IVariable) newBinding;
			IType mytype= getType();
			int valueRec= db.getInt(record + VALUE_OFFSET);
			try {
				IType newType= var.getType();
				setType(linkage, newType);
				db.putByte(record + ANNOTATIONS, PDOMCAnnotation.encodeAnnotation(var));
				setValue(db, var);
				
				if (mytype != null) 
					linkage.deleteType(mytype, record);
				if (valueRec != 0)
					db.getString(valueRec).delete();
			} catch (DOMException e) {
				throw new CoreException(Util.createStatus(e));
			}
		}
	}

	private void setType(final PDOMLinkage linkage, final IType type) throws CoreException {
		final PDOMNode typeNode = linkage.addType(this, type);
		pdom.getDB().putInt(record + TYPE_OFFSET, typeNode != null ? typeNode.getRecord() : 0);
	}

	public PDOMCVariable(PDOM pdom, int record) {
		super(pdom, record);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCBindingConstants.CVARIABLE;
	}
	
	public IType getType() {
		try {
			int typeRec = pdom.getDB().getInt(record + TYPE_OFFSET);
			return (IType)getLinkageImpl().getNode(typeRec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public IValue getInitialValue() {
		try {
			final Database db = pdom.getDB();
			int valRec = db.getInt(record + VALUE_OFFSET);
			if (valRec == 0)
				return null;
			return Value.fromCanonicalRepresentation(db.getString(valRec).getString());
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isStatic() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.STATIC_OFFSET);
	}

	public boolean isExtern() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.EXTERN_OFFSET);
	}

	public boolean isAuto() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.AUTO_OFFSET);
	}

	public boolean isRegister() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.REGISTER_OFFSET);
	}

	@Override
	public int getAdditionalNameFlags(int standardFlags, IASTName name) {
		if ((standardFlags & PDOMName.IS_REFERENCE) == PDOMName.IS_REFERENCE) {
			return CVariableReadWriteFlags.getReadWriteFlags(name);
		}
		return 0;
	}
}
