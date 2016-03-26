/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     IBM Corporation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCPPField extends PDOMCPPVariable implements ICPPField {
	protected static final int FIELD_POSITION_OFFSET = ANNOTATIONS + 1; // byte
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = FIELD_POSITION_OFFSET + 1;
	
	public PDOMCPPField(PDOMLinkage linkage, PDOMNode parent, ICPPField field, boolean setTypeAndValue)
			throws CoreException {
		super(linkage, parent, field, setTypeAndValue);
		setFieldPosition(field);
	}

	public PDOMCPPField(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding, IASTNode point) throws CoreException {
		super.update(linkage, newBinding, point);
		if (newBinding instanceof ICPPField) {
			setFieldPosition((ICPPField)newBinding);
		}
	}
	
	private void setFieldPosition(ICPPField field) throws CoreException {
		final Database db = getDB();
		db.putByte(record + FIELD_POSITION_OFFSET, field.getFieldPosition());
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPFIELD;
	}

	@Override
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	@Override
	public int getVisibility() {
		return PDOMCPPAnnotation.getVisibility(getByte(record + ANNOTATIONS));
	}

	@Override
	public boolean isMutable() {
		return getBit(getByte(record + ANNOTATIONS), PDOMCPPAnnotation.MUTABLE_OFFSET);
	}

	@Override
	public boolean isAuto() {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	@Override
	public boolean isExtern() {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public boolean isRegister() {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	@Override
	public ICompositeType getCompositeTypeOwner() {
		return getClassOwner();
	}

	@Override
	public byte getFieldPosition() {
		return getByte(record + FIELD_POSITION_OFFSET);
	}
}
