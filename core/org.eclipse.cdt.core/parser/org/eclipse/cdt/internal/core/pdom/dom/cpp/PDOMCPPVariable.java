/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for a C++ variable in the index, serves as a base class for fields.
 */
class PDOMCPPVariable extends PDOMCPPBinding implements ICPPVariable {
	private static final int TYPE_OFFSET = PDOMCPPBinding.RECORD_SIZE;
	private static final int VALUE_OFFSET = TYPE_OFFSET + Database.TYPE_SIZE;
	private static final int ANNOTATIONS = VALUE_OFFSET + Database.VALUE_SIZE; // byte
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = ANNOTATIONS + 1;

	public PDOMCPPVariable(PDOMCPPLinkage linkage, PDOMNode parent, ICPPVariable variable, boolean setTypeAndValue)
			throws CoreException {
		super(linkage, parent, variable.getNameCharArray());

		// Find the type record
		Database db = getDB();
		db.putByte(record + ANNOTATIONS, PDOMCPPAnnotations.encodeVariableAnnotations(variable));
		if (setTypeAndValue) {
			setType(parent.getLinkage(), variable.getType());
			linkage.new ConfigureVariable(variable, this);
		}
	}

	public void initData(IValue initialValue) {
		try {
			setValue(initialValue);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	protected void setValue(IValue value) throws CoreException {
		getLinkage().storeValue(record + VALUE_OFFSET, value);
	}

	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof IVariable) {
			final Database db = getDB();
			ICPPVariable var = (ICPPVariable) newBinding;
			IType newType = var.getType();
			setType(linkage, newType);
			setValue(var.getInitialValue());
			db.putByte(record + ANNOTATIONS, PDOMCPPAnnotations.encodeVariableAnnotations(var));
		}
	}

	protected void setType(final PDOMLinkage linkage, IType newType) throws CoreException {
		linkage.storeType(record + TYPE_OFFSET, newType);
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
			return IntegralValue.UNKNOWN;
		}
	}

	@Override
	public boolean isAuto() {
		byte annotation = getAnnotations();
		return !PDOMCPPAnnotations.isExtern(annotation) && !PDOMCPPAnnotations.isStatic(annotation)
				&& getOwner() instanceof ICPPFunction;
	}

	@Override
	public boolean isExtern() {
		return PDOMCPPAnnotations.isExtern(getAnnotations());
	}

	@Override
	public boolean isExternC() {
		return PDOMCPPAnnotations.isExternC(getAnnotations());
	}

	@Override
	public boolean isRegister() {
		return false; // We don't care whether the parameter has register storage class specifier or not.
	}

	@Override
	public boolean isStatic() {
		return PDOMCPPAnnotations.isStatic(getAnnotations());
	}

	@Override
	public boolean isConstexpr() {
		return PDOMCPPAnnotations.isConstexpr(getAnnotations());
	}

	protected final byte getAnnotations() {
		return getByte(record + ANNOTATIONS);
	}

	@Override
	public int getAdditionalNameFlags(int standardFlags, IASTName name) {
		if ((standardFlags & PDOMName.IS_REFERENCE) == PDOMName.IS_REFERENCE) {
			return CPPVariableReadWriteFlags.getReadWriteFlags(name);
		}
		return 0;
	}
}
