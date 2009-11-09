/*******************************************************************************
 * Copyright (c) 2007, 2009 QNX Software Systems and others.
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for function specialization in the index. 
 */
class PDOMCPPFunctionSpecialization extends PDOMCPPSpecialization implements ICPPFunction {
	/**
	 * Offset of total number of function parameters (relative to the
	 * beginning of the record).
	 */
	private static final int NUM_PARAMS = PDOMCPPSpecialization.RECORD_SIZE;

	/**
	 * Offset of pointer to the first parameter of this function (relative to
	 * the beginning of the record).
	 */
	private static final int FIRST_PARAM = NUM_PARAMS + 4;

	/**
	 * Offset for type of this function (relative to
	 * the beginning of the record).
	 */
	private static final int FUNCTION_TYPE = FIRST_PARAM + Database.PTR_SIZE;	

	/**
	 * Offset of start of exception specification
	 */
	protected static final int EXCEPTION_SPEC = FUNCTION_TYPE + Database.TYPE_SIZE; // int

	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	protected static final int ANNOTATION = EXCEPTION_SPEC + Database.PTR_SIZE; // byte
	
	/**
	 * The size in bytes of a PDOMCPPFunction record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = ANNOTATION + 1;

	private ICPPFunctionType fType;
	
	public PDOMCPPFunctionSpecialization(PDOMLinkage linkage, PDOMNode parent, ICPPFunction astFunction, PDOMBinding specialized) throws CoreException {
		super(linkage, parent, (ICPPSpecialization) astFunction, specialized);
		
		Database db = getDB();
		try {
			IParameter[] astParams= astFunction.getParameters();
			IFunctionType astFt= astFunction.getType();
			if (astFt != null) {
				getLinkage().storeType(record + FUNCTION_TYPE, astFt);
			}

			ICPPFunction spAstFunc= (ICPPFunction) ((ICPPSpecialization)astFunction).getSpecializedBinding();
			IParameter[] spAstParams= spAstFunc.getParameters();
			
			final int length= Math.min(spAstParams.length, astParams.length);
			db.putInt(record + NUM_PARAMS, length);
			
			db.putRecPtr(record + FIRST_PARAM, 0);
			PDOMCPPParameterSpecialization next= null;
			for (int i= length-1; i >= 0; --i) {
				PDOMCPPParameter par= new PDOMCPPParameter(linkage, specialized, spAstParams[i], null);
				next= new PDOMCPPParameterSpecialization(linkage, this, astParams[i], par, next);
			}
			db.putRecPtr(record + FIRST_PARAM, next == null ? 0 : next.getRecord());
			db.putByte(record + ANNOTATION, PDOMCPPAnnotation.encodeAnnotation(astFunction));			
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
		try {
			long typelist= 0;
			if (astFunction instanceof ICPPMethod && ((ICPPMethod) astFunction).isImplicit()) {
				// don't store the exception specification, computed it on demand.
			} else {
				typelist = PDOMCPPTypeList.putTypes(this, astFunction.getExceptionSpecification());
			}
			db.putRecPtr(record + EXCEPTION_SPEC, typelist);
		} catch (DOMException e) {
			// ignore problems in the exception specification
		}

	}

	public PDOMCPPFunctionSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FUNCTION_SPECIALIZATION;
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
			PDOMLinkage linkage= getLinkage();
			Database db= getDB();
			ICPPFunctionType ft = getType();
			IType[] ptypes= ft == null ? IType.EMPTY_TYPE_ARRAY : ft.getParameterTypes();
			
			int n = db.getInt(record + NUM_PARAMS);
			IParameter[] result = new IParameter[n];
			
			long next = db.getRecPtr(record + FIRST_PARAM);
 			for (int i = 0; i < n && next != 0; i++) {
 				IType type= i<ptypes.length ? ptypes[i] : null;
				final PDOMCPPParameterSpecialization par = new PDOMCPPParameterSpecialization(linkage, next, type);
				next= par.getNextPtr();
				result[i]= par;
			}
			return result;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IParameter[0];
		}
	}

	public ICPPFunctionType getType() throws DOMException {		
		if (fType == null) {
			try {
				fType= (ICPPFunctionType) getLinkage().loadType(record + FUNCTION_TYPE);
			} catch(CoreException ce) {
				CCorePlugin.log(ce);
			}
		}
		return fType;
	}

	public boolean isAuto() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.2
		return false; 
	}

	public boolean isExtern() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.EXTERN_OFFSET);
	}

	public boolean isExternC() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCPPAnnotation.EXTERN_C_OFFSET);
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

	@Override
	public int pdomCompareTo(PDOMBinding other) {
		int cmp= super.pdomCompareTo(other);
		return cmp==0 ? PDOMCPPFunction.compareSignatures(this, other) : cmp;
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
