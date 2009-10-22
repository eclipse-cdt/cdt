/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    IBM Corporation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IndexCPPSignatureUtil;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMOverloader;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for c++ functions in the index.
 */
class PDOMCPPFunction extends PDOMCPPBinding implements ICPPFunction, IPDOMOverloader {

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
	private static final int SIGNATURE_HASH = PDOMCPPBinding.RECORD_SIZE + 12;
		
	/**
	 * Offset of start of exception specifications
	 */
	protected static final int EXCEPTION_SPEC = PDOMCPPBinding.RECORD_SIZE + 16; // int
	
	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	private static final int ANNOTATION = PDOMCPPBinding.RECORD_SIZE + 20; // byte

	/**
	 * The size in bytes of a PDOMCPPFunction record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 21;

	private byte annotation= -1;
	
	public PDOMCPPFunction(PDOMLinkage linkage, PDOMNode parent, ICPPFunction function, boolean setTypes) throws CoreException, DOMException {
		super(linkage, parent, function.getNameCharArray());
		Database db = getDB();		
		Integer sigHash = IndexCPPSignatureUtil.getSignatureHash(function);
		getDB().putInt(record + SIGNATURE_HASH, sigHash != null ? sigHash.intValue() : 0);
		db.putByte(record + ANNOTATION, PDOMCPPAnnotation.encodeAnnotation(function));

		if (setTypes) {
			initData(function.getType(), function.getParameters(), extractExceptionSpec(function));
		}
	}

	public void initData(ICPPFunctionType ftype, IParameter[] params, IType[] exceptionSpec) {
		PDOMCPPFunctionType pft;
		try {
			pft = setType(ftype);
			setParameters(pft, params);
			storeExceptionSpec(exceptionSpec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPFunction) {
			ICPPFunction func= (ICPPFunction) newBinding;
			ICPPFunctionType newType;
			IParameter[] newParams;
			byte newAnnotation;
			try {
				newType= func.getType();
				newParams = func.getParameters();
				newAnnotation = PDOMCPPAnnotation.encodeAnnotation(func);
			} catch (DOMException e) {
				throw new CoreException(Util.createStatus(e));
			}
				
			IFunctionType oldType= getType();
			PDOMCPPParameter oldParams= getFirstParameter();
			PDOMCPPFunctionType pft= setType(newType);
			setParameters(pft, newParams);
			if (oldType != null) {
				linkage.deleteType(oldType, record);
			}
			if (oldParams != null) {
				oldParams.delete(linkage);
			}
			final Database db = getDB();
			db.putByte(record + ANNOTATION, newAnnotation);
			annotation= newAnnotation;
			
			long oldRec = db.getRecPtr(record+EXCEPTION_SPEC);
			storeExceptionSpec(extractExceptionSpec(func));
			if (oldRec != 0) {
				PDOMCPPTypeList.clearTypes(this, oldRec);
			}
		}
	}

	private void storeExceptionSpec(IType[] exceptionSpec) throws CoreException {
		long typelist= PDOMCPPTypeList.putTypes(this, exceptionSpec);
		getDB().putRecPtr(record + EXCEPTION_SPEC, typelist);
	}

	IType[] extractExceptionSpec(ICPPFunction binding) {
		IType[] exceptionSpec;
		if (binding instanceof ICPPMethod && ((ICPPMethod) binding).isImplicit()) {
			// don't store the exception specification, compute it on demand.
			exceptionSpec= null;
		} else {
			try{
				exceptionSpec= binding.getExceptionSpecification();
			} catch (DOMException e) {
				// ignore problems in the exception specification.
				exceptionSpec= null;
			}
		}
		return exceptionSpec;
	}

	private void setParameters(PDOMCPPFunctionType pft, IParameter[] params) throws CoreException {
		final Database db= getDB();
		db.putInt(record + NUM_PARAMS, params.length);
		db.putRecPtr(record + FIRST_PARAM, 0);
		IType[] paramTypes= pft.getParameterTypes();
		for (int i= 0; i < params.length; ++i) {
			long ptRecord= i < paramTypes.length && paramTypes[i] != null ? ((PDOMNode) paramTypes[i]).getRecord() : 0;
			setFirstParameter(new PDOMCPPParameter(getLinkage(), this, params[i], ptRecord));
		}
	}

	private PDOMCPPFunctionType setType(ICPPFunctionType ft) throws CoreException {
		PDOMCPPFunctionType pft = (PDOMCPPFunctionType) getLinkage().addType(this, ft);
		getDB().putRecPtr(record + FUNCTION_TYPE, pft.getRecord());
		getPDOM().putCachedResult(record, pft, true);
		return pft;
	}
	
	public int getSignatureHash() throws CoreException {
		return getDB().getInt(record + SIGNATURE_HASH);
	}
	
	public static int getSignatureHash(PDOMLinkage linkage, long record) throws CoreException {
		return linkage.getDB().getInt(record + SIGNATURE_HASH);
	}
	
	public PDOMCPPFunction(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPFUNCTION;
	}
	
	private PDOMCPPParameter getFirstParameter() throws CoreException {
		long rec = getDB().getRecPtr(record + FIRST_PARAM);
		return rec != 0 ? new PDOMCPPParameter(getLinkage(), rec) : null;
	}

	private void setFirstParameter(PDOMCPPParameter param) throws CoreException {
		if (param != null)
			param.setNextParameter(getFirstParameter());
		long rec = param != null ? param.getRecord() :  0;
		getDB().putRecPtr(record + FIRST_PARAM, rec);
	}
	
	public boolean isInline() throws DOMException {
		return getBit(getAnnotation(), PDOMCAnnotation.INLINE_OFFSET);
	}

	final protected byte getAnnotation() {
		if (annotation == -1)
			annotation= getByte(record + ANNOTATION);
		return annotation;
	}

	public boolean isExternC() throws DOMException {
		return getBit(getAnnotation(), PDOMCPPAnnotation.EXTERN_C_OFFSET);
	}

	public boolean isMutable() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IScope getFunctionScope() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IParameter[] getParameters() throws DOMException {
		try {
			int n = getDB().getInt(record + NUM_PARAMS);
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

	public final ICPPFunctionType getType() {	
		final PDOM pdom= getPDOM();
		ICPPFunctionType ftype= (ICPPFunctionType) pdom.getCachedResult(record);
		if (ftype == null) {
			ftype= readFunctionType();
			pdom.putCachedResult(record, ftype, false);
		}
		return ftype;
	}
	
	private final ICPPFunctionType readFunctionType() {
		try {
			long offset= getDB().getRecPtr(record + FUNCTION_TYPE);
			return offset==0 ? null : new PDOMCPPFunctionType(getLinkage(), offset); 
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
		return getBit(getAnnotation(), PDOMCAnnotation.EXTERN_OFFSET);
	}

	public boolean isRegister() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.2
		return false; 
	}

	public boolean isStatic() throws DOMException {
		return getBit(getAnnotation(), PDOMCAnnotation.STATIC_OFFSET);
	}

	public boolean takesVarArgs() throws DOMException {
		return getBit(getAnnotation(), PDOMCAnnotation.VARARGS_OFFSET);
	}

	@Override
	public Object clone() {
		throw new PDOMNotImplementedError();
	}
	
	@Override
	public int pdomCompareTo(PDOMBinding other) {
		int cmp = super.pdomCompareTo(other);
		return cmp == 0 ? compareSignatures(this, other) : cmp;
	}
		
	protected static int compareSignatures(IPDOMOverloader a, Object b) {
		if (b instanceof IPDOMOverloader) {
			IPDOMOverloader bb= (IPDOMOverloader) b;
			try {
				int mySM = a.getSignatureHash();
				int otherSM = bb.getSignatureHash();
				return mySM == otherSM ? 0 : mySM < otherSM ? -1 : 1;
			} catch(CoreException ce) {
				CCorePlugin.log(ce);
			}
		} else {
			throw new PDOMNotImplementedError(b.getClass().toString());
		}
		return 0;
	}

	public IType[] getExceptionSpecification() throws DOMException {
		try {
			final long rec = getPDOM().getDB().getRecPtr(record+EXCEPTION_SPEC);
			return PDOMCPPTypeList.getTypes(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
}
