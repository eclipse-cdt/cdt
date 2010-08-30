/*******************************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * Specialization of a method
 */
class PDOMCPPMethodSpecialization extends PDOMCPPFunctionSpecialization
		implements ICPPMethod {

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
	
	/**
	 * The bit offset of CV qualifier flags within ANNOTATION1.
	 */
	private static final int CV_OFFSET = PDOMCPPAnnotation.MAX_EXTRA_OFFSET + 1;
	
	public PDOMCPPMethodSpecialization(PDOMLinkage linkage, PDOMNode parent, ICPPMethod method, PDOMBinding specialized) throws CoreException {
		super(linkage, parent, method, specialized);		
		Database db = getDB();

		try {
			ICPPFunctionType type = method.getType();
			byte annotation = 0;
			annotation |= PDOMCAnnotation.encodeCVQualifiers(type) << CV_OFFSET;
			annotation |= PDOMCPPAnnotation.encodeExtraAnnotation(method);
			db.putByte(record + ANNOTATION1, annotation);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
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
	
	public boolean isDestructor() {
		return getBit(getByte(record + ANNOTATION1), PDOMCPPAnnotation.DESTRUCTOR_OFFSET);
	}

	public boolean isImplicit() {
		return getBit(getByte(record + ANNOTATION1), PDOMCPPAnnotation.IMPLICIT_METHOD_OFFSET);
	}

	public boolean isExplicit() {
		return getBit(getByte(record + ANNOTATION1), PDOMCPPAnnotation.EXPLICIT_METHOD_OFFSET);
	}

	public boolean isVirtual() {
		return getBit(getByte(record + ANNOTATION1), PDOMCPPAnnotation.VIRTUAL_OFFSET);
	}

	public boolean isPureVirtual() {
		return getBit(getByte(record + ANNOTATION1), PDOMCPPAnnotation.PURE_VIRTUAL_OFFSET);
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

	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	public int getVisibility() {
		return PDOMCPPAnnotation.getVisibility(getByte(record + ANNOTATION_OFFSET));
	}
	
	@Override
	public boolean isConst() {
		return getBit(getByte(record + ANNOTATION1), PDOMCAnnotation.CONST_OFFSET + CV_OFFSET);
	}

	@Override
	public boolean isVolatile() {
		return getBit(getByte(record + ANNOTATION1), PDOMCAnnotation.VOLATILE_OFFSET + CV_OFFSET);
	}
	
	@Override
	public IType[] getExceptionSpecification() {
		if (isImplicit()) {
			return ClassTypeHelper.getInheritedExceptionSpecification(this);
		}
		return super.getExceptionSpecification();
	}
}
