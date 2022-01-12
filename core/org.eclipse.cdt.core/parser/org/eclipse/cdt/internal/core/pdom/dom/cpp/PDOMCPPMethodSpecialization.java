/*******************************************************************************
 * Copyright (c) 2007, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bryan Wilkinson (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethodSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Specialization of a method
 */
class PDOMCPPMethodSpecialization extends PDOMCPPFunctionSpecialization implements ICPPMethodSpecialization {
	/**
	 * Offset of remaining annotation information (relative to the beginning of
	 * the record).
	 */
	protected static final int ANNOTATION1 = PDOMCPPFunctionSpecialization.RECORD_SIZE; // byte

	/**
	 * The size in bytes of a PDOMCPPMethodSpecialization record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPFunctionSpecialization.RECORD_SIZE + 1;

	public PDOMCPPMethodSpecialization(PDOMCPPLinkage linkage, PDOMNode parent, ICPPMethod method,
			PDOMBinding specialized) throws CoreException {
		super(linkage, parent, method, specialized);
		Database db = getDB();

		byte annotation = PDOMCPPAnnotations.encodeExtraMethodAnnotations(method);
		db.putByte(record + ANNOTATION1, annotation);
	}

	public PDOMCPPMethodSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_METHOD_SPECIALIZATION;
	}

	@Override
	public boolean isDestructor() {
		return PDOMCPPAnnotations.isDestructor(getByte(record + ANNOTATION1));
	}

	@Override
	public boolean isImplicit() {
		return PDOMCPPAnnotations.isImplicitMethod(getByte(record + ANNOTATION1));
	}

	@Override
	public boolean isExplicit() {
		return PDOMCPPAnnotations.isExplicitMethod(getByte(record + ANNOTATION1));
	}

	@Override
	public boolean isVirtual() {
		return PDOMCPPAnnotations.isVirtualMethod(getByte(record + ANNOTATION1));
	}

	@Override
	public boolean isPureVirtual() {
		return PDOMCPPAnnotations.isPureVirtualMethod(getByte(record + ANNOTATION1));
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
	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	@Override
	public int getVisibility() {
		return PDOMCPPAnnotations.getVisibility(getAnnotations());
	}

	@Override
	public IType[] getExceptionSpecification(IASTNode point) {
		if (isImplicit()) {
			return ClassTypeHelper.getInheritedExceptionSpecification(this);
		}
		return super.getExceptionSpecification();
	}

	@Override
	public boolean isOverride() {
		return false;
	}

	@Override
	public boolean isFinal() {
		return false;
	}
}
