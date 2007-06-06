/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * IBM Corporation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDelegateCreator;
import org.eclipse.cdt.internal.core.index.IndexCPPSignatureUtil;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMOverloader;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPFunction extends PDOMCPPBinding implements ICPPFunction, IPDOMOverloader, ICPPDelegateCreator {

	/**
	 * Offset of total number of function parameters (relative to the
	 * beginning of the record).
	 */
	private static final int NUM_PARAMS = PDOMCPPBinding.RECORD_SIZE + 0;

	/**
	 * Offset of pointer to the first parameter of this function (relative to
	 * the beginning of the record).
	 */
	private static final int FIRST_PARAM = PDOMCPPBinding.RECORD_SIZE + 4;
	
	/**
	 * Offset of pointer to the function type record of this function (relative to
	 * the beginning of the record).
	 */
	protected static final int FUNCTION_TYPE= PDOMCPPBinding.RECORD_SIZE + 8;
	
	/**
	 * Offset of hash of parameter information to allow fast comparison
	 */
	private static final int SIGNATURE_MEMENTO = PDOMCPPBinding.RECORD_SIZE + 12;
	
	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	protected static final int ANNOTATION = PDOMCPPBinding.RECORD_SIZE + 16; // byte
	
	/**
	 * The size in bytes of a PDOMCPPFunction record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 17;
	
	public PDOMCPPFunction(PDOM pdom, PDOMNode parent, ICPPFunction function, boolean setTypes) throws CoreException {
		super(pdom, parent, function.getNameCharArray());
		Database db = pdom.getDB();		
		try {
			Integer memento = IndexCPPSignatureUtil.getSignatureMemento(function);
			pdom.getDB().putInt(record + SIGNATURE_MEMENTO, memento != null ? memento.intValue() : 0);
			
			if(setTypes) {
				initData(function);
			}
			db.putByte(record + ANNOTATION, PDOMCPPAnnotation.encodeAnnotation(function));
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public void initData(ICPPFunction function)  throws CoreException, DOMException {
		Database db= pdom.getDB();
		
		ICPPFunctionType ft= (ICPPFunctionType) function.getType();
		PDOMCPPFunctionType pft = (PDOMCPPFunctionType) getLinkageImpl().addType(this, ft);
		db.putInt(record + FUNCTION_TYPE, pft.getRecord());
		
		IParameter[] params= function.getParameters();
		db.putInt(record + NUM_PARAMS, params.length);
		
		IType[] paramTypes= pft.getParameterTypes();
		for (int i=0; i<params.length; ++i) {
			int ptRecord= i<paramTypes.length && paramTypes[i]!=null ? ((PDOMNode) paramTypes[i]).getRecord() : 0;
			setFirstParameter(new PDOMCPPParameter(pdom, this, params[i], ptRecord));
		}	
	}
	
	public int getSignatureMemento() throws CoreException {
		return pdom.getDB().getInt(record + SIGNATURE_MEMENTO);
	}
	
	public static int getSignatureMemento(PDOM pdom, int record) throws CoreException {
		return pdom.getDB().getInt(record + SIGNATURE_MEMENTO);
	}
	
	public PDOMCPPFunction(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
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
		try {
			int offset= pdom.getDB().getInt(record + FUNCTION_TYPE);
			return offset==0 ? null : new PDOMCPPFunctionType(pdom, offset); 
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
			return null;
		}
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

	public Object clone() {
		throw new PDOMNotImplementedError();
	}
	
	public int pdomCompareTo(PDOMBinding other) {
		int cmp= super.pdomCompareTo(other);
		return cmp==0 ? compareSignatures(this, other) : cmp;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		try {
			result.append(getName()+" "+ASTTypeUtil.getParameterTypeString(getType())); //$NON-NLS-1$
			result.append(" "+getNodeType()); //$NON-NLS-1$
		} catch(DOMException de) {
			result.append(de);
		}
		return result.toString();
	}
	
	protected static int compareSignatures(IPDOMOverloader a, Object b) {
		if(b instanceof IPDOMOverloader) {
			IPDOMOverloader bb= (IPDOMOverloader) b;
			try {
				int mySM = a.getSignatureMemento();
				int otherSM = bb.getSignatureMemento();
				return mySM == otherSM ? 0 : mySM < otherSM ? -1 : 1;
			} catch(CoreException ce) {
				CCorePlugin.log(ce);
			}
		} else {
			throw new PDOMNotImplementedError(b.getClass().toString());
		}
		return 0;
	}
	
	public ICPPDelegate createDelegate(IASTName name) {
		return new CPPFunction.CPPFunctionDelegate(name, this);
	}
	
}
