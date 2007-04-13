/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
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

package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCFunction extends PDOMBinding implements IFunction {
	/**
	 * Offset of total number of function parameters (relative to the
	 * beginning of the record).
	 */
	public static final int NUM_PARAMS = PDOMBinding.RECORD_SIZE + 0;
	
	/**
	 * Offset of total number of function parameters (relative to the
	 * beginning of the record).
	 */
	public static final int FIRST_PARAM = PDOMBinding.RECORD_SIZE + 4;
	
	/**
	 * Offset for return type of this function (relative to
	 * the beginning of the record).
	 */
	private static final int RETURN_TYPE = PDOMBinding.RECORD_SIZE + 8;
	
	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	private static final int ANNOTATIONS = PDOMBinding.RECORD_SIZE + 12; // byte
	
	/**
	 * The size in bytes of a PDOMCPPFunction record in the database.
	 */
	public static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 13;
	
	public PDOMCFunction(PDOM pdom, PDOMNode parent, IFunction function) throws CoreException {
		super(pdom, parent, function.getNameCharArray());
		
		try {
			IFunctionType ft= function.getType();
			IType rt= ft.getReturnType();
			if (rt != null) {
				PDOMNode typeNode = getLinkageImpl().addType(this, rt);
				if (typeNode != null) {
					pdom.getDB().putInt(record + RETURN_TYPE, typeNode.getRecord());
				}
			}
			
			IParameter[] params = function.getParameters();
			pdom.getDB().putInt(record + NUM_PARAMS, params.length);
			for (int i = 0; i < params.length; ++i) {
				setFirstParameter(new PDOMCParameter(pdom, this, params[i]));
			}
			pdom.getDB().putByte(record + ANNOTATIONS, PDOMCAnnotation.encodeAnnotation(function));
		} catch(DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}
	
	public PDOMCParameter getFirstParameter() throws CoreException {
		int rec = pdom.getDB().getInt(record + FIRST_PARAM);
		return rec != 0 ? new PDOMCParameter(pdom, rec) : null;
	}
	
	public void setFirstParameter(PDOMCParameter param) throws CoreException {
		if (param != null)
			param.setNextParameter(getFirstParameter());
		int rec = param != null ? param.getRecord() :  0;
		pdom.getDB().putInt(record + FIRST_PARAM, rec);
	}

	public PDOMCFunction(PDOM pdom, int record) {
		super(pdom, record);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCLinkage.CFUNCTION;
	}

	public IFunctionType getType() throws DOMException {
		/*
		 * CVisitor binding resolution assumes any IBinding which is
		 * also an IType should be converted to a IProblemBinding in a
		 * route through the code that triggers errors here. This means
		 * we can't use the convenient idea of having PDOMCFunction implement
		 * both the IType and IBinding subinterfaces. 
		 */
		return new IFunctionType() {
			public Object clone() { fail(); return null;	}
			public IType[] getParameterTypes() throws DOMException {
				return PDOMCFunction.this.getParameterTypes();
			}
			public IType getReturnType() throws DOMException {
				return PDOMCFunction.this.getReturnType();
			}
			public boolean isSameType(IType type) {
				fail(); return false;
			}
		};
	}

	public boolean isStatic() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.STATIC_OFFSET);
	}

	public boolean isExtern() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.EXTERN_OFFSET);
	}

	public IParameter[] getParameters() throws DOMException {
		try {
			int n = pdom.getDB().getInt(record + NUM_PARAMS);
			IParameter[] params = new IParameter[n];
			PDOMCParameter param = getFirstParameter();
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
	
	public IType[] getParameterTypes() throws DOMException {
		try {
			int n = pdom.getDB().getInt(record + NUM_PARAMS);
			IType[] types = new IType[n];
			PDOMCParameter param = getFirstParameter();
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
	
	public boolean isAuto() throws DOMException {
		// ISO/IEC 9899:TC1 6.9.1.4
		return false;
	}

	public boolean isRegister() throws DOMException {
		// ISO/IEC 9899:TC1 6.9.1.4
		return false;
	}

	public boolean isInline() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.INLINE_OFFSET);
	}

	public boolean takesVarArgs() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.VARARGS_OFFSET);
	}
	
	public IType getReturnType() throws DOMException {
		try {
			PDOMNode node = getLinkageImpl().getNode(pdom.getDB().getInt(record + RETURN_TYPE));
			if (node instanceof IType) {
				return (IType) node;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	public IScope getFunctionScope() throws DOMException {
		return null;
	}
}
