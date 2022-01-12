/*******************************************************************************
 * Copyright (c) 2007, 2016 QNX Software Systems and others.
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

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Template for a method.
 */
class PDOMCPPMethodTemplate extends PDOMCPPFunctionTemplate implements ICPPMethod {
	/** Offset of remaining annotation information (relative to the beginning of the record). */
	private static final int METHOD_ANNOTATION = PDOMCPPFunctionTemplate.RECORD_SIZE; // byte
	/** The size in bytes of a PDOMCPPMethodTemplate record in the database. */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPFunctionTemplate.RECORD_SIZE + 1;

	private byte methodAnnotation = -1;

	public PDOMCPPMethodTemplate(PDOMCPPLinkage linkage, PDOMNode parent, ICPPMethod method)
			throws CoreException, DOMException {
		super(linkage, parent, (ICPPFunctionTemplate) method);
		methodAnnotation = PDOMCPPAnnotations.encodeExtraMethodAnnotations(method);
		getDB().putByte(record + METHOD_ANNOTATION, methodAnnotation);
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

	@Override
	public boolean isDestructor() {
		return PDOMCPPAnnotations.isDestructor(getMethodAnnotation());
	}

	@Override
	public boolean isImplicit() {
		return PDOMCPPAnnotations.isImplicitMethod(getMethodAnnotation());
	}

	@Override
	public boolean isExplicit() {
		return PDOMCPPAnnotations.isExplicitMethod(getMethodAnnotation());
	}

	@Override
	public boolean isVirtual() {
		return PDOMCPPAnnotations.isVirtualMethod(getMethodAnnotation());
	}

	private byte getMethodAnnotation() {
		if (methodAnnotation == -1)
			methodAnnotation = getByte(record + METHOD_ANNOTATION);
		return methodAnnotation;
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

	@Override
	public boolean isPureVirtual() {
		return false;
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
