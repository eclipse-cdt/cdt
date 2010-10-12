/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.dom.parser.c.CVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMValue;
import org.eclipse.core.runtime.CoreException;

/**
 * Database representation for c-variables
 */
class PDOMCVariable extends PDOMBinding implements IVariable {

	/**
	 * Offset of pointer to type information for this variable
	 * (relative to the beginning of the record).
	 */
	private static final int TYPE_OFFSET = PDOMBinding.RECORD_SIZE;

	/**
	 * Offset of pointer to value information for this variable
	 * (relative to the beginning of the record).
	 */
	private static final int VALUE_OFFSET = TYPE_OFFSET + Database.TYPE_SIZE;

	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	private static final int ANNOTATIONS = VALUE_OFFSET + Database.PTR_SIZE;
	
	/**
	 * The size in bytes of a PDOMCVariable record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = ANNOTATIONS + 1;
	
	public PDOMCVariable(PDOMLinkage linkage, PDOMNode parent, IVariable variable) throws CoreException {
		super(linkage, parent, variable.getNameCharArray());

		final Database db = getDB();
		setType(parent.getLinkage(), variable.getType());
		db.putByte(record + ANNOTATIONS, PDOMCAnnotation.encodeAnnotation(variable));
		
		setValue(db, variable);
	}

	private void setValue(final Database db, IVariable variable) throws CoreException {
		IValue val= variable.getInitialValue();
		long valrec= PDOMValue.store(db, getLinkage(), val);
		db.putRecPtr(record + VALUE_OFFSET, valrec);
	}
	
	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof IVariable) {
			final Database db = getDB();
			IVariable var= (IVariable) newBinding;
			long valueRec= db.getRecPtr(record + VALUE_OFFSET);
			IType newType= var.getType();
			setType(linkage, newType);
			db.putByte(record + ANNOTATIONS, PDOMCAnnotation.encodeAnnotation(var));
			setValue(db, var);
			
			PDOMValue.delete(db, valueRec);
		}
	}

	private void setType(final PDOMLinkage linkage, final IType type) throws CoreException {
		linkage.storeType(record + TYPE_OFFSET, type);
	}

	public PDOMCVariable(PDOMLinkage linkage, long record) {
		super(linkage, record);
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
			return getLinkage().loadType(record + TYPE_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public IValue getInitialValue() {
		try {
			final Database db = getDB();
			long valRec = db.getRecPtr(record + VALUE_OFFSET);
			return PDOMValue.restore(db, getLinkage(), valRec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isStatic() {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.STATIC_OFFSET);
	}

	public boolean isExtern() {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.EXTERN_OFFSET);
	}

	public boolean isAuto() {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.AUTO_OFFSET);
	}

	public boolean isRegister() {
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
