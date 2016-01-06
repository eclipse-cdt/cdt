/*******************************************************************************
 * Copyright (c) 2005, 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for a c++ variable in the index, serves as a base class for fields.
 */
class PDOMCPPVariable extends PDOMCPPBinding implements ICPPVariable {
	private static final int TYPE_OFFSET = PDOMCPPBinding.RECORD_SIZE;
	private static final int VALUE_OFFSET = TYPE_OFFSET + Database.TYPE_SIZE;
	protected static final int ANNOTATIONS = VALUE_OFFSET + Database.VALUE_SIZE; // byte
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = ANNOTATIONS + 1;

	public PDOMCPPVariable(PDOMLinkage linkage, PDOMNode parent, IVariable variable, boolean setTypeAndValue) throws CoreException {
		super(linkage, parent, variable.getNameCharArray());

		// Find the type record
		Database db = getDB();
		db.putByte(record + ANNOTATIONS, encodeFlags(variable));
		if (setTypeAndValue) {
			setType(parent.getLinkage(), variable.getType());
			setValue(db, variable);
		}
	}

	private void setValue(Database db, IVariable variable) throws CoreException {
		IValue val= variable.getInitialValue();
		getLinkage().storeValue(record + VALUE_OFFSET, val);
	}

	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding, IASTNode point) throws CoreException {
		if (newBinding instanceof IVariable) {
			final Database db = getDB();
			IVariable var= (IVariable) newBinding;
			IType newType= var.getType();
			setType(linkage, newType);
			setValue(db, var);
			db.putByte(record + ANNOTATIONS, encodeFlags(var));
		}
	}

	protected void setType(final PDOMLinkage linkage, IType newType) throws CoreException {
		linkage.storeType(record+TYPE_OFFSET, newType);
	}

	protected byte encodeFlags(IVariable variable) {
		return PDOMCPPAnnotation.encodeAnnotation(variable);
	}

	public PDOMCPPVariable(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPVARIABLE;
	}

	@Override
	public boolean isMutable() {
		// ISO/IEC 14882:2003 7.1.1.8
		return false;
	}

	@Override
	public IType getType() {
		try {
			return getLinkage().loadType(record + TYPE_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	@Override
	public IValue getInitialValue() {
		try {
			return getLinkage().loadValue(record + VALUE_OFFSET);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return Value.UNKNOWN;
		}
	}

	@Override
	public boolean isAuto() {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.AUTO_OFFSET);
	}

	@Override
	public boolean isExtern() {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.EXTERN_OFFSET);
	}

	@Override
	public boolean isExternC() {
		return getBit(getByte(record + ANNOTATIONS), PDOMCPPAnnotation.EXTERN_C_OFFSET);
	}

	@Override
	public boolean isRegister() {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.REGISTER_OFFSET);
	}

	@Override
	public boolean isStatic() {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.STATIC_OFFSET);
	}

	@Override
	public int getAdditionalNameFlags(int standardFlags, IASTName name) {
		if ((standardFlags & PDOMName.IS_REFERENCE) == PDOMName.IS_REFERENCE) {
			return CPPVariableReadWriteFlags.getReadWriteFlags(name);
		}
		return 0;
	}
}
