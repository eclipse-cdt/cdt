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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
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
	protected static final int ANNOTATION_OFFSET = EXCEPTION_SPEC + Database.PTR_SIZE; // short
	
	private static final int REQUIRED_ARG_COUNT_OFFSET= ANNOTATION_OFFSET + 2;
	/**
	 * The size in bytes of a PDOMCPPFunction record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = REQUIRED_ARG_COUNT_OFFSET + 4;

	
	private static final short ANNOT_PARAMETER_PACK = 8;
	private static final short ANNOT_IS_DELETED = 9;

	private ICPPFunctionType fType;
	private short fAnnotation= -1;

	private int fRequiredArgCount= -1;
	
	public PDOMCPPFunctionSpecialization(PDOMLinkage linkage, PDOMNode parent, ICPPFunction astFunction, PDOMBinding specialized) throws CoreException {
		super(linkage, parent, (ICPPSpecialization) astFunction, specialized);
		
		Database db = getDB();
		try {
			ICPPParameter[] astParams= astFunction.getParameters();
			IFunctionType astFt= astFunction.getType();
			if (astFt != null) {
				getLinkage().storeType(record + FUNCTION_TYPE, astFt);
			}

			ICPPFunction origAstFunc= (ICPPFunction) ((ICPPSpecialization)astFunction).getSpecializedBinding();
			ICPPParameter[] origAstParams= origAstFunc.getParameters();
			if (origAstParams.length == 0) {
				db.putInt(record + NUM_PARAMS, 0);
				db.putRecPtr(record + FIRST_PARAM, 0);
			} else {
				final int length= astParams.length;
				db.putInt(record + NUM_PARAMS, length);

				db.putRecPtr(record + FIRST_PARAM, 0);
				PDOMCPPParameter origPar= null;
				PDOMCPPParameterSpecialization next= null;
				for (int i= length-1; i >= 0; --i) {
					// There may be fewer or less original parameters, because of parameter packs.
					if (i < origAstParams.length-1) {
						// Normal case
						origPar= new PDOMCPPParameter(linkage, specialized, origAstParams[i], null);
					} else if (origPar == null) {
						// Use last parameter
						origPar= new PDOMCPPParameter(linkage, specialized, origAstParams[origAstParams.length-1], null);
					}
					next= new PDOMCPPParameterSpecialization(linkage, this, astParams[i], origPar, next);
				}
				db.putRecPtr(record + FIRST_PARAM, next == null ? 0 : next.getRecord());
			}
			fAnnotation = getAnnotation(astFunction);
			db.putShort(record + ANNOTATION_OFFSET, fAnnotation);	
			db.putInt(record + REQUIRED_ARG_COUNT_OFFSET, astFunction.getRequiredArgumentCount());
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
		long typelist= 0;
		if (astFunction instanceof ICPPMethod && ((ICPPMethod) astFunction).isImplicit()) {
			// don't store the exception specification, computed it on demand.
		} else {
			typelist = PDOMCPPTypeList.putTypes(this, astFunction.getExceptionSpecification());
		}
		db.putRecPtr(record + EXCEPTION_SPEC, typelist);

	}

	private short getAnnotation(ICPPFunction astFunction) {
		int annot= PDOMCPPAnnotation.encodeAnnotation(astFunction) & 0xff;
		if (astFunction.hasParameterPack()) {
			annot |= (1 << ANNOT_PARAMETER_PACK);
		}
		if (astFunction.isDeleted()) {
			annot |= (1<<ANNOT_IS_DELETED);
		}
		return (short) annot;
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

	public boolean isInline() {
		return getBit(readAnnotation(), PDOMCAnnotation.INLINE_OFFSET);
	}

	private short readAnnotation() {
		if (fAnnotation == -1) {
			try {
				fAnnotation= getDB().getShort(record + ANNOTATION_OFFSET);
			} catch (CoreException e) {
				fAnnotation= 0;
			}
		}
		return fAnnotation;
	}

	public boolean isMutable() {
		return false;
	}

	public IScope getFunctionScope() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public ICPPParameter[] getParameters() throws DOMException {
		try {
			PDOMLinkage linkage= getLinkage();
			Database db= getDB();
			ICPPFunctionType ft = getType();
			IType[] ptypes= ft == null ? IType.EMPTY_TYPE_ARRAY : ft.getParameterTypes();
			
			int n = db.getInt(record + NUM_PARAMS);
			ICPPParameter[] result = new ICPPParameter[n];
			
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
			return ICPPParameter.EMPTY_CPPPARAMETER_ARRAY;
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

	public boolean isAuto() {
		// ISO/IEC 14882:2003 7.1.1.2
		return false; 
	}

	public boolean isExtern() {
		return getBit(readAnnotation(), PDOMCAnnotation.EXTERN_OFFSET);
	}

	public boolean isExternC() {
		return getBit(readAnnotation(), PDOMCPPAnnotation.EXTERN_C_OFFSET);
	}

	public boolean isRegister() {
		// ISO/IEC 14882:2003 7.1.1.2
		return false; 
	}

	public boolean isStatic() {
		return getBit(readAnnotation(), PDOMCAnnotation.STATIC_OFFSET);
	}

	public boolean takesVarArgs() {
		return getBit(readAnnotation(), PDOMCAnnotation.VARARGS_OFFSET);
	}

	
	public int getRequiredArgumentCount() throws DOMException {
		if (fRequiredArgCount == -1) {
			try {
				fRequiredArgCount= getDB().getInt(record + REQUIRED_ARG_COUNT_OFFSET);
			} catch (CoreException e) {
				fRequiredArgCount= 0;
			}
		}
		return fRequiredArgCount;
	}

	public boolean hasParameterPack() {
		return getBit(readAnnotation(), ANNOT_PARAMETER_PACK);
	}

	public boolean isDeleted() {
		return getBit(readAnnotation(), ANNOT_IS_DELETED);
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

	public IType[] getExceptionSpecification() {
		try {
			final long rec = getPDOM().getDB().getRecPtr(record+EXCEPTION_SPEC);
			return PDOMCPPTypeList.getTypes(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
}
