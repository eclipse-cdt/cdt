/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
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
class PDOMCPPFunction extends PDOMCPPBinding implements ICPPFunction, ICPPFunctionType {

	/**
	 * Offset of total number of function parameters (relative to the
	 * beginning of the record).
	 */
	private static final int NUM_PARAMS = PDOMBinding.RECORD_SIZE + 0;

	/**
	 * Offset of pointer to the first parameter of this function (relative to
	 * the beginning of the record).
	 */
	private static final int FIRST_PARAM = PDOMBinding.RECORD_SIZE + 4;

	/**
	 * Offset of hash of parameter information to allow fast comparison
	 */
	private static final int SIGNATURE_MEMENTO = PDOMBinding.RECORD_SIZE + 8;
	
	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	protected static final int ANNOTATION = PDOMBinding.RECORD_SIZE + 12; // byte
	
	/**
	 * The size in bytes of a PDOMCPPFunction record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 13;
	
	public PDOMCPPFunction(PDOM pdom, PDOMNode parent, ICPPFunction function) throws CoreException {
		super(pdom, parent, function.getNameCharArray());
		
		Database db = pdom.getDB();
		IBinding binding = function;
		try {
			IParameter[] params = function.getParameters();
			db.putInt(record + NUM_PARAMS, params.length);
			
			for (int i=0; i<params.length; ++i) {
				setFirstParameter(new PDOMCPPParameter(pdom, this, params[i]));
			}
			db.putByte(record + ANNOTATION, PDOMCPPAnnotation.encodeAnnotation(binding));
			IFunctionType ftype = function.getType();
			db.putInt(record + SIGNATURE_MEMENTO, getSignatureMemento(ftype.getParameterTypes()));
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public PDOMCPPFunction(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	public int getSignatureMemento() throws CoreException {
		return pdom.getDB().getInt(record + SIGNATURE_MEMENTO);
	}
	
	public static int getSignatureMemento(PDOM pdom, int record) throws CoreException {
		return pdom.getDB().getInt(record + SIGNATURE_MEMENTO);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPPFUNCTION;
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
	
	public boolean isInline() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.INLINE_OFFSET);
	}

	public boolean isMutable() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IScope getFunctionScope() throws DOMException {
		throw new PDOMNotImplementedError();
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

	public IFunctionType getType() throws DOMException {
		return this;
	}

	public boolean isAuto() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.2
		return false; 
	}

	public boolean isExtern() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.EXTERN_OFFSET);
	}

	public boolean isRegister() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.2
		return false; 
	}

	public boolean isStatic() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.STATIC_OFFSET);
	}

	public boolean takesVarArgs() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.VARARGS_OFFSET);
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
		// ISO/IEC 14882:2003 9.3.1.3
		// Only applicable to member functions
		return false; 
	}

	public boolean isVolatile() {
		// ISO/IEC 14882:2003 9.3.1.3
		// Only applicable to member functions
		return false; 
	}

	public boolean isSameType(IType type) {
		if (type instanceof PDOMCPPFunction) {
			return record == ((PDOMCPPFunction)type).getRecord();
		} else {
			// TODO check the other type for matching name, params
			return false;
		}
	}

	public Object clone() {
		throw new PDOMNotImplementedError();
	}

	public static int getSignatureMemento(IType[] types) throws DOMException {
		if(types.length==1) {
			if(types[0] instanceof IBasicType) {
				if(((IBasicType)types[0]).getType()==IBasicType.t_void) {
					types = new IType[0];
				}
			}
		}
	
		StringBuffer result = new StringBuffer();
		result.append('(');
		for(int i=0; i<types.length; i++) {
			if (i>0) {
				result.append(',');
			}
			result.append(ASTTypeUtil.getType(types[i]));
		}
		result.append(')');
		return result.toString().hashCode();
	}

	public static int getSignatureMemento(IFunctionType type) throws DOMException {
		IType[] params = type.getParameterTypes();	
		return getSignatureMemento(params);
	}
}
