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
 *     QNX - Initial API and implementation
 *     IBM Corporation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCPPField extends PDOMCPPVariable implements ICPPField {
	protected static final int FIELD_POSITION_OFFSET = PDOMCPPVariable.RECORD_SIZE; // 2 bytes 1-based

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = FIELD_POSITION_OFFSET + 2;

	public PDOMCPPField(PDOMCPPLinkage linkage, PDOMNode parent, ICPPField field, boolean setTypeAndValue)
			throws CoreException {
		super(linkage, parent, field, setTypeAndValue);
		setFieldPosition(field);
	}

	public PDOMCPPField(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		super.update(linkage, newBinding);
		if (newBinding instanceof ICPPField) {
			setFieldPosition((ICPPField) newBinding);
		}
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
		return PDOMCPPAnnotations.getVisibility(getAnnotations());
	}

	@Override
	public boolean isMutable() {
		return PDOMCPPAnnotations.isMutable(getAnnotations());
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
	public int getFieldPosition() {
		return Short.toUnsignedInt(getShort(record + FIELD_POSITION_OFFSET)) - 1;
	}

	private void setFieldPosition(ICPPField field) throws CoreException {
		int shiftedPos = field.getFieldPosition() + 1;
		if ((shiftedPos & 0xFFFF0000) != 0) {
			CCorePlugin.log(new IllegalArgumentException("Invalid field position " + field.getFieldPosition())); //$NON-NLS-1$
			shiftedPos = 0;
		}
		getDB().putShort(record + FIELD_POSITION_OFFSET, (short) shiftedPos);
	}
}
