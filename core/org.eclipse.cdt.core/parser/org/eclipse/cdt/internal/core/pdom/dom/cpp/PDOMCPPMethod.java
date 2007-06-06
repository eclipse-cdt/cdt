/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * IBM Corporation
 * Andrew Ferguson (Symbian)
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDelegateCreator;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 * 
 */
class PDOMCPPMethod extends PDOMCPPFunction implements ICPPMethod, ICPPDelegateCreator {

	/**
	 * Offset of remaining annotation information (relative to the beginning of
	 * the record).
	 */
	protected static final int ANNOTATION1 = PDOMCPPFunction.RECORD_SIZE; // byte

	/**
	 * The size in bytes of a PDOMCPPMethod record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPFunction.RECORD_SIZE + 1;

	/**
	 * The bit offset of CV qualifier flags within ANNOTATION1.
	 */
	private static final int CV_OFFSET = PDOMCPPAnnotation.MAX_EXTRA_OFFSET + 1;

	public PDOMCPPMethod(PDOM pdom, PDOMNode parent, ICPPMethod method) throws CoreException {
		super(pdom, parent, method, true);

		Database db = pdom.getDB();

		try {
			byte annotation = 0;
			annotation |= PDOMCPPAnnotation.encodeExtraAnnotation(method);
			db.putByte(record + ANNOTATION1, annotation);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public PDOMCPPMethod(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPPMETHOD;
	}

	public boolean isVirtual() throws DOMException {
		return getBit(getByte(record + ANNOTATION1), PDOMCPPAnnotation.VIRTUAL_OFFSET);
	}

	public boolean isDestructor() throws DOMException {
		return getBit(getByte(record + ANNOTATION1), PDOMCPPAnnotation.DESTRUCTOR_OFFSET);
	}

	public boolean isMutable() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isImplicit() {
		return getBit(getByte(record + ANNOTATION1), PDOMCPPAnnotation.IMPLICIT_METHOD_OFFSET);
	}
	
	public IScope getFunctionScope() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isExtern() throws DOMException {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	public boolean isAuto() throws DOMException {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	public boolean isRegister() throws DOMException {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	public int getVisibility() throws DOMException {
		return PDOMCPPAnnotation.getVisibility(getByte(record + ANNOTATION));
	}

	public ICPPClassType getClassOwner() throws DOMException {
		try {
			return (ICPPClassType) getParentNode();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public Object clone() {
		throw new PDOMNotImplementedError();
	}

	public boolean isConst() {
		return getBit(getByte(record + ANNOTATION1), PDOMCAnnotation.CONST_OFFSET + CV_OFFSET);
	}

	public boolean isVolatile() {
		return getBit(getByte(record + ANNOTATION1), PDOMCAnnotation.VOLATILE_OFFSET + CV_OFFSET);
	}
	
	public ICPPDelegate createDelegate(IASTName name) {
		return new CPPMethod.CPPMethodDelegate(name, this);
	}
	
}