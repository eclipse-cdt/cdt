/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCPPMethod extends PDOMBinding implements ICPPMethod, ICPPFunctionType {

	public static final int NUM_PARAMS = PDOMBinding.RECORD_SIZE + 0;
	public static final int FIRST_PARAM = PDOMBinding.RECORD_SIZE + 4;
	
	public static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 8;
	
	public PDOMCPPMethod(PDOM pdom, PDOMMemberOwner parent, IASTName name) throws CoreException {
		super(pdom, parent, name);
		IASTNode parentNode = name.getParent();
		if (parentNode instanceof ICPPASTFunctionDeclarator) {
			ICPPASTFunctionDeclarator funcDecl = (ICPPASTFunctionDeclarator)parentNode;
			IASTParameterDeclaration[] params = funcDecl.getParameters();
			pdom.getDB().putInt(record + NUM_PARAMS, params.length);
			for (int i = 0; i < params.length; ++i) {
				ICPPASTParameterDeclaration param = (ICPPASTParameterDeclaration)params[i];
				IASTName paramName = param.getDeclarator().getName();
				IBinding binding = paramName.resolveBinding();
				ICPPParameter paramBinding = (ICPPParameter)binding;
				setFirstParameter(new PDOMCPPParameter(pdom, this, paramName, paramBinding));
			}
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
		throw new PDOMNotImplementedError();
	}

	public boolean isDestructor() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isMutable() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isInline() throws DOMException {
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

	public IScope getFunctionScope() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IFunctionType getType() throws DOMException {
		return this;
	}

	public boolean isStatic() throws DOMException {
		// TODO
		return false;
	}

	public boolean isExtern() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isAuto() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isRegister() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean takesVarArgs() throws DOMException {
		// TODO
		return false;
	}

	public String[] getQualifiedName() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public char[][] getQualifiedNameCharArray() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isGloballyQualified() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public int getVisibility() throws DOMException {
		throw new PDOMNotImplementedError();
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
		return new IType[0];
//		TODO throw new PDOMNotImplementedError();
	}

	public IType getReturnType() throws DOMException {
		return null;
//		TODO throw new PDOMNotImplementedError();
	}

	public boolean isConst() {
		return false;
//		TODO throw new PDOMNotImplementedError();
	}

	public boolean isVolatile() {
		return false;
//		TODO throw new PDOMNotImplementedError();
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