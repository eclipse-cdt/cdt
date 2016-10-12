/*******************************************************************************
 * Copyright (c) 2007, 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bryan Wilkinson (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.ProblemFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPComputableFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for function specialization in the index. 
 */
class PDOMCPPFunctionSpecialization extends PDOMCPPSpecialization
		implements ICPPFunctionSpecialization, ICPPComputableFunction {
	/**
	 * Offset of total number of function parameters (relative to the beginning of the record).
	 */
	private static final int NUM_PARAMS = PDOMCPPSpecialization.RECORD_SIZE;

	/**
	 * Offset of pointer to the first parameter of this function (relative to the beginning
	 * of the record).
	 */
	private static final int FIRST_PARAM = NUM_PARAMS + 4;

	/**
	 * Offset for type of this function (relative to the beginning of the record).
	 */
	private static final int FUNCTION_TYPE = FIRST_PARAM + Database.PTR_SIZE;	

	/**
	 * Offset of start of exception specification
	 */
	protected static final int EXCEPTION_SPEC = FUNCTION_TYPE + Database.TYPE_SIZE; // int

	/**
	 * Offset of annotation information (relative to the beginning of the record).
	 */
	private static final int ANNOTATION = EXCEPTION_SPEC + Database.PTR_SIZE; // short

	/** Offset of the number of the required arguments. */
	private static final int REQUIRED_ARG_COUNT = ANNOTATION + 2; // short

	/** Offset of the function body execution for constexpr functions. */
	private static final int FUNCTION_BODY = REQUIRED_ARG_COUNT + 2; // Database.EXECUTION_SIZE
	
	/**
	 * The size in bytes of a PDOMCPPFunctionSpecialization record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = FUNCTION_BODY + Database.EXECUTION_SIZE;

	private static final short ANNOT_PARAMETER_PACK = 8;
	private static final short ANNOT_IS_DELETED = 9;
	private static final short ANNOT_IS_CONSTEXPR = 10;

	private ICPPFunctionType fType; // No need for volatile, all fields of ICPPFunctionTypes are final.
	private short fAnnotations= -1;
	private int fRequiredArgCount= -1;
	
	public PDOMCPPFunctionSpecialization(PDOMCPPLinkage linkage, PDOMNode parent, ICPPFunction astFunction,
			PDOMBinding specialized, IASTNode point) throws CoreException {
		super(linkage, parent, (ICPPSpecialization) astFunction, specialized);
		
		Database db = getDB();
		ICPPParameter[] astParams= astFunction.getParameters();
		IFunctionType astFt= astFunction.getType();
		if (astFt != null) {
			getLinkage().storeType(record + FUNCTION_TYPE, astFt);
		}

		ICPPFunction origAstFunc= (ICPPFunction) ((ICPPSpecialization) astFunction).getSpecializedBinding();
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
			for (int i= length; --i >= 0;) {
				// There may be fewer or less original parameters, because of parameter packs.
				if (i < origAstParams.length - 1) {
					// Normal case
					origPar= new PDOMCPPParameter(linkage, specialized, origAstParams[i], null);
				} else if (origPar == null) {
					// Use last parameter
					origPar= new PDOMCPPParameter(linkage, specialized, origAstParams[origAstParams.length - 1], null);
				}
				next= new PDOMCPPParameterSpecialization(linkage, this, astParams[i], origPar, next);
			}
			db.putRecPtr(record + FIRST_PARAM, next == null ? 0 : next.getRecord());
		}
		fAnnotations = PDOMCPPAnnotations.encodeFunctionAnnotations(astFunction);
		db.putShort(record + ANNOTATION, fAnnotations);	
		db.putShort(record + REQUIRED_ARG_COUNT , (short) astFunction.getRequiredArgumentCount());
		long typelist= 0;
		if (astFunction instanceof ICPPMethod && ((ICPPMethod) astFunction).isImplicit()) {
			// Don't store the exception specification, it is computed on demand.
		} else {
			typelist = PDOMCPPTypeList.putTypes(this, astFunction.getExceptionSpecification());
		}
		db.putRecPtr(record + EXCEPTION_SPEC, typelist);
		if (!(astFunction instanceof ICPPTemplateInstance)
				|| ((ICPPTemplateInstance) astFunction).isExplicitSpecialization()) {
			linkage.new ConfigureFunctionSpecialization(astFunction, this, point);
		}
	}

	public PDOMCPPFunctionSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	public void initData(ICPPExecution functionBody) {
		if (functionBody == null)
			return;
		try {
			getLinkage().storeExecution(record + FUNCTION_BODY, functionBody);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FUNCTION_SPECIALIZATION;
	}

	@Override
	public boolean isInline() {
		return PDOMCPPAnnotations.isInline(getAnnotations());
	}

	protected final short getAnnotations() {
		if (fAnnotations == -1) {
			try {
				fAnnotations= getDB().getShort(record + ANNOTATION);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fAnnotations= 0;
			}
		}
		return fAnnotations;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public IScope getFunctionScope() {
		return null;
	}

	@Override
	public ICPPParameter[] getParameters() {
		try {
			PDOMLinkage linkage= getLinkage();
			Database db= getDB();
			ICPPFunctionType ft = getType();
			IType[] ptypes= ft == null ? IType.EMPTY_TYPE_ARRAY : ft.getParameterTypes();
			
			int n = db.getInt(record + NUM_PARAMS);
			ICPPParameter[] result = new ICPPParameter[n];
			
			long next = db.getRecPtr(record + FIRST_PARAM);
 			for (int i = 0; i < n && next != 0; i++) {
 				IType type= i < ptypes.length ? ptypes[i] : null;
				final PDOMCPPParameterSpecialization par =
						new PDOMCPPParameterSpecialization(linkage, next, type);
				next= par.getNextPtr();
				result[i]= par;
			}
			return result;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPParameter.EMPTY_CPPPARAMETER_ARRAY;
		}
	}

	@Override
	public ICPPFunctionType getType() {		
		if (fType == null) {
			try {
				fType= (ICPPFunctionType) getLinkage().loadType(record + FUNCTION_TYPE);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fType= new ProblemFunctionType(ISemanticProblem.TYPE_NOT_PERSISTED);
			}
		}
		return fType;
	}

	@Override
	public boolean isAuto() {
		// ISO/IEC 14882:2003 7.1.1.2
		return false; 
	}

	@Override
	public boolean isConstexpr() {
		return getBit(getAnnotations(), ANNOT_IS_CONSTEXPR);
	}

	@Override
	public boolean isExtern() {
		return PDOMCPPAnnotations.isExtern(getAnnotations());
	}

	@Override
	public boolean isExternC() {
		return PDOMCPPAnnotations.isExternC(getAnnotations());
	}

	@Override
	public boolean isRegister() {
		// ISO/IEC 14882:2003 7.1.1.2
		return false; 
	}

	@Override
	public boolean isStatic() {
		return PDOMCPPAnnotations.isStatic(getAnnotations());
	}

	@Override
	public boolean takesVarArgs() {
		return PDOMCPPAnnotations.isVarargsFunction(getAnnotations());
	}

	@Override
	public boolean isNoReturn() {
		return PDOMCPPAnnotations.isNoReturnFunction(getAnnotations());
	}

	@Override
	public int getRequiredArgumentCount() {
		if (fRequiredArgCount == -1) {
			try {
				fRequiredArgCount= getDB().getShort(record + REQUIRED_ARG_COUNT );
			} catch (CoreException e) {
				fRequiredArgCount= 0;
			}
		}
		return fRequiredArgCount;
	}

	@Override
	public boolean hasParameterPack() {
		return getBit(getAnnotations(), ANNOT_PARAMETER_PACK);
	}

	@Override
	public boolean isDeleted() {
		return getBit(getAnnotations(), ANNOT_IS_DELETED);
	}

	@Override
	public int pdomCompareTo(PDOMBinding other) {
		int cmp= super.pdomCompareTo(other);
		return cmp == 0 ? PDOMCPPFunction.compareSignatures(this, other) : cmp;
	}

	@Override
	public IType[] getExceptionSpecification() {
		try {
			final long rec = getPDOM().getDB().getRecPtr(record + EXCEPTION_SPEC);
			return PDOMCPPTypeList.getTypes(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	@Override
	public ICPPExecution getFunctionBodyExecution(IASTNode point) {
		if (!isConstexpr())
			return null;

		try {
			ICPPExecution exec = (ICPPExecution) getLinkage().loadExecution(record + FUNCTION_BODY);
			if (exec == null) {
				exec = CPPTemplates.instantiateFunctionBody(this, point);
			}
			return exec;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
}
