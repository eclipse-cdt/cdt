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
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPMethod extends PDOMCPPBinding implements ICPPMethod, ICPPFunctionType {

	/**
	 * Offset of total number of method parameters (relative to the
	 * beginning of the record).
	 */
	private static final int NUM_PARAMS = PDOMBinding.RECORD_SIZE + 0;
	
	/**
	 * Offset of pointer to the first parameter of this method (relative to
	 * the beginning of the record).
	 */
	private static final int FIRST_PARAM = PDOMBinding.RECORD_SIZE + 4;

	/**
	 * Offset of first byte of annotation information (relative to the
	 * beginning of the record).
	 */
	private static final int ANNOTATION0 = PDOMBinding.RECORD_SIZE + 8; // byte

	/**
	 * Offset of remaining annotation information (relative to the
	 * beginning of the record).
	 */
	private static final int ANNOTATION1 = PDOMBinding.RECORD_SIZE + 9; // byte
	
	/**
	 * The size in bytes of a PDOMCPPMethod record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 10;

	/**
	 * The bit offset of CV qualifier flags within ANNOTATION1.
	 */
	private static final int CV_OFFSET = 2;

	public PDOMCPPMethod(PDOM pdom, PDOMNode parent, ICPPMethod method) throws CoreException {
		super(pdom, parent, method.getNameCharArray());
		
		Database db = pdom.getDB();
		
		try {
			IParameter[] params = method.getParameters();
			db.putInt(record + NUM_PARAMS, params.length);
			
			for (int i=0; i<params.length; ++i) {
				setFirstParameter(new PDOMCPPParameter(pdom, this, params[i]));
			}
			ICPPFunctionType type = (ICPPFunctionType) method.getType();
			byte annotation = 0;
			annotation |= PDOMCAnnotation.encodeCVQualifiers(type) << CV_OFFSET;
			annotation |= PDOMCPPAnnotation.encodeExtraAnnotation(method);
			db.putByte(record + ANNOTATION0, PDOMCPPAnnotation.encodeAnnotation(method));
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
	
	public PDOMCPPParameter getFirstParameter() throws CoreException {
		int rec = pdom.getDB().getInt(record + FIRST_PARAM);
		return rec != 0 ? new PDOMCPPParameter(pdom, rec) : null;
	}

	public void setFirstParameter(PDOMCPPParameter param) throws CoreException {
		if (param != null)
			param.setNextParameter(getFirstParameter());
		int rec = param != null ? param.getRecord() :  0;
		pdom.getDB().putInt(record + FIRST_PARAM, rec);
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

	public boolean isInline() throws DOMException {
		return getBit(getByte(record + ANNOTATION0), PDOMCAnnotation.INLINE_OFFSET);
	}

	public IParameter[] getParameters() throws DOMException {
		try {
			int n = pdom.getDB().getInt(record + NUM_PARAMS);
			IParameter[] params = new IParameter[n];
			PDOMCPPParameter param = getFirstParameter();
			while (param != null) {
				params[--n] = param;
				param = param.getNextParameter();
			}
			return params;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IParameter[0];
		}
	}

	public IScope getFunctionScope() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IFunctionType getType() throws DOMException {
		return this;
	}

	public boolean isStatic() throws DOMException {
		return getBit(getByte(record + ANNOTATION0), PDOMCAnnotation.STATIC_OFFSET);
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

	public boolean takesVarArgs() throws DOMException {
		return getBit(getByte(record + ANNOTATION0), PDOMCAnnotation.VARARGS_OFFSET);
	}

	public int getVisibility() throws DOMException {
		return PDOMCPPAnnotation.getVisibility(getByte(record + ANNOTATION0));
	}

	public ICPPClassType getClassOwner() throws DOMException {
		try {
			return (ICPPClassType)getParentNode();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public Object clone() {
		throw new PDOMNotImplementedError();
	}

	public IType[] getParameterTypes() throws DOMException {
		try {
			int n = pdom.getDB().getInt(record + NUM_PARAMS);
			IType[] types = new IType[n];
			PDOMCPPParameter param = getFirstParameter();
			while (param != null) {
				types[--n] = param.getType();
				param = param.getNextParameter();
			}
			return types;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IType[0];
		}
	}

	public IType getReturnType() throws DOMException {
		return null;
//		TODO throw new PDOMNotImplementedError();
	}

	public boolean isConst() {
		return getBit(getByte(record + ANNOTATION1), PDOMCAnnotation.CONST_OFFSET + CV_OFFSET);
	}

	public boolean isVolatile() {
		return getBit(getByte(record + ANNOTATION1), PDOMCAnnotation.VOLATILE_OFFSET + CV_OFFSET);
	}

	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof PDOMCPPMethod)
			return getRecord() == ((PDOMCPPMethod)type).getRecordSize();
		// TODO further analysis to compare with DOM objects
		return false;
	}
}