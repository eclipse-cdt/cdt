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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * Template for a method.
 */
class PDOMCPPMethodTemplate extends PDOMCPPFunctionTemplate implements ICPPMethod {

	/**
	 * Offset of remaining annotation information (relative to the beginning of
	 * the record).
	 */
	private static final int ANNOTATION1 = PDOMCPPFunctionTemplate.RECORD_SIZE; // byte
	
	/**
	 * The size in bytes of a PDOMCPPMethodTemplate record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPFunctionTemplate.RECORD_SIZE + 1;
	
	/**
	 * The bit offset of CV qualifier flags within ANNOTATION1.
	 */
	private static final int CV_OFFSET = PDOMCPPAnnotation.MAX_EXTRA_OFFSET + 1;

	private byte annotation1= -1;
	
	public PDOMCPPMethodTemplate(PDOMCPPLinkage linkage, PDOMNode parent, ICPPMethod method) 
			throws CoreException, DOMException {
		super(linkage, parent, (ICPPFunctionTemplate) method);
		
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
	
	public PDOMCPPMethodTemplate(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_METHOD_TEMPLATE;
	}

	public boolean isDestructor() {
		return getBit(getAnnotation1(), PDOMCPPAnnotation.DESTRUCTOR_OFFSET);
	}

	final protected byte getAnnotation1() {
		if (annotation1 == -1)
			annotation1= getByte(record + ANNOTATION1);
		return annotation1;
	}

	public boolean isImplicit() {
		return getBit(getAnnotation1(), PDOMCPPAnnotation.IMPLICIT_METHOD_OFFSET);
	}

	public boolean isExplicit() {
		return getBit(getAnnotation1(), PDOMCPPAnnotation.EXPLICIT_METHOD_OFFSET);
	}

	public boolean isVirtual() {
		return getBit(getAnnotation1(), PDOMCPPAnnotation.VIRTUAL_OFFSET);
	}

	public ICPPClassType getClassOwner() {
		return (ICPPClassType) getOwner();
	}

	public int getVisibility() {
		return PDOMCPPAnnotation.getVisibility(getAnnotation());
	}

	public boolean isConst() {
		return getBit(getAnnotation1(), PDOMCAnnotation.CONST_OFFSET + CV_OFFSET);
	}

	public boolean isVolatile() {
		return getBit(getAnnotation1(), PDOMCAnnotation.VOLATILE_OFFSET + CV_OFFSET);
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
	public boolean isAuto() {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	@Override
	public boolean isRegister() {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	public boolean isPureVirtual() {
		return false;
	}
}
