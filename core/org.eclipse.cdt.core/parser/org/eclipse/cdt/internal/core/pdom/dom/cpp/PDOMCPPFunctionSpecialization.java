/*******************************************************************************
 * Copyright (c) 2007, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bryan Wilkinson (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
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
	/** Offset of total number of function parameters (relative to the beginning of the record). */
	private static final int NUM_PARAMS = PDOMCPPSpecialization.RECORD_SIZE;

	/** Offset of pointer to the first parameter of this function (relative to the beginning of the record). */
	private static final int FIRST_PARAM = NUM_PARAMS + 4;

	/** Offset for type of this function (relative to the beginning of the record). */
	private static final int FUNCTION_TYPE = FIRST_PARAM + Database.PTR_SIZE;

	/** Offset of start of exception specification. */
	protected static final int EXCEPTION_SPEC = FUNCTION_TYPE + Database.TYPE_SIZE; // int

	/** Offset of annotation information (relative to the beginning of the record). */
	private static final int ANNOTATION = EXCEPTION_SPEC + Database.PTR_SIZE; // short

	/** Offset of the number of the required arguments. */
	private static final int REQUIRED_ARG_COUNT = ANNOTATION + 2; // short

	/** Offset of the function body execution for constexpr functions. */
	private static final int FUNCTION_BODY = REQUIRED_ARG_COUNT + 2; // Database.EXECUTION_SIZE

	/** Offset of the function's declared type. */
	private static final int DECLARED_TYPE = FUNCTION_BODY + Database.EXECUTION_SIZE; // Database.TYPE_SIZE

	/**
	 * The size in bytes of a PDOMCPPFunctionSpecialization record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = DECLARED_TYPE + Database.TYPE_SIZE;

	private ICPPFunctionType fType; // No need for volatile, all fields of ICPPFunctionTypes are final.
	private ICPPFunctionType fDeclaredType;
	private short fAnnotations = -1;
	private int fRequiredArgCount = -1;

	public PDOMCPPFunctionSpecialization(PDOMCPPLinkage linkage, PDOMNode parent, ICPPFunction astFunction,
			PDOMBinding specialized) throws CoreException {
		super(linkage, parent, (ICPPSpecialization) astFunction, specialized);

		Database db = getDB();
		fAnnotations = PDOMCPPAnnotations.encodeFunctionAnnotations(astFunction);
		db.putShort(record + ANNOTATION, fAnnotations);
		db.putShort(record + REQUIRED_ARG_COUNT, (short) astFunction.getRequiredArgumentCount());
		linkage.new ConfigureFunctionSpecialization(astFunction, this, specialized);
	}

	public PDOMCPPFunctionSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	public void initData(PDOMBinding specialized, ICPPFunctionType type, ICPPFunctionType declaredType,
			ICPPParameter[] astParams, ICPPParameter[] origAstParams, IType[] exceptionSpec,
			ICPPExecution functionBody) {
		try {
			setType(type);
			setDeclaredType(declaredType);
			setParameters(astParams, origAstParams, specialized);
			storeExceptionSpec(exceptionSpec);
			if (functionBody != null) {
				getLinkage().storeExecution(record + FUNCTION_BODY, functionBody);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	private void setParameters(ICPPParameter[] astParams, ICPPParameter[] origAstParams, PDOMBinding specialized)
			throws CoreException {
		final PDOMCPPLinkage linkage = (PDOMCPPLinkage) getLinkage();
		final Database db = getDB();
		if (origAstParams.length == 0) {
			db.putInt(record + NUM_PARAMS, 0);
			db.putRecPtr(record + FIRST_PARAM, 0);
		} else {
			final int length = astParams.length;
			db.putInt(record + NUM_PARAMS, length);

			db.putRecPtr(record + FIRST_PARAM, 0);
			PDOMCPPParameter origPar = null;
			PDOMCPPParameterSpecialization next = null;
			for (int i = length; --i >= 0;) {
				// There may be fewer or less original parameters, because of parameter packs.
				if (i < origAstParams.length - 1) {
					// Normal case.
					origPar = new PDOMCPPParameter(linkage, specialized, origAstParams[i], null);
				} else if (origPar == null) {
					// Use last parameter.
					origPar = new PDOMCPPParameter(linkage, specialized, origAstParams[origAstParams.length - 1], null);
				}
				next = new PDOMCPPParameterSpecialization(linkage, this, astParams[i], origPar, next);
			}
			db.putRecPtr(record + FIRST_PARAM, next == null ? 0 : next.getRecord());
		}
	}

	private void setType(ICPPFunctionType ft) throws CoreException {
		if (ft != null) {
			fType = null;
			getLinkage().storeType(record + FUNCTION_TYPE, ft);
		}
	}

	private void setDeclaredType(ICPPFunctionType ft) throws CoreException {
		if (ft != null) {
			fType = null;
			getLinkage().storeType(record + DECLARED_TYPE, ft);
		}
	}

	private void storeExceptionSpec(IType[] exceptionSpec) throws CoreException {
		long typelist = 0;
		if (exceptionSpec != null) {
			typelist = PDOMCPPTypeList.putTypes(this, exceptionSpec);
		}
		getDB().putRecPtr(record + EXCEPTION_SPEC, typelist);
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
				fAnnotations = getDB().getShort(record + ANNOTATION);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fAnnotations = 0;
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
			PDOMLinkage linkage = getLinkage();
			Database db = getDB();
			ICPPFunctionType ft = getType();
			IType[] ptypes = ft == null ? IType.EMPTY_TYPE_ARRAY : ft.getParameterTypes();

			int n = db.getInt(record + NUM_PARAMS);
			ICPPParameter[] result = new ICPPParameter[n];

			long next = db.getRecPtr(record + FIRST_PARAM);
			for (int i = 0; i < n && next != 0; i++) {
				IType type = i < ptypes.length ? ptypes[i] : null;
				final PDOMCPPParameterSpecialization par = new PDOMCPPParameterSpecialization(linkage, next, type);
				next = par.getNextPtr();
				result[i] = par;
			}
			return result;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPParameter.EMPTY_CPPPARAMETER_ARRAY;
		}
	}

	@Override
	public ICPPFunctionType getDeclaredType() {
		if (fDeclaredType == null) {
			try {
				fDeclaredType = (ICPPFunctionType) getLinkage().loadType(record + DECLARED_TYPE);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fDeclaredType = new ProblemFunctionType(ISemanticProblem.TYPE_NOT_PERSISTED);
			}
		}
		return fDeclaredType;
	}

	@Override
	public ICPPFunctionType getType() {
		if (fType == null) {
			try {
				fType = (ICPPFunctionType) getLinkage().loadType(record + FUNCTION_TYPE);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fType = new ProblemFunctionType(ISemanticProblem.TYPE_NOT_PERSISTED);
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
		return PDOMCPPAnnotations.isConstexpr(getAnnotations());
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
				fRequiredArgCount = getDB().getShort(record + REQUIRED_ARG_COUNT);
			} catch (CoreException e) {
				fRequiredArgCount = 0;
			}
		}
		return fRequiredArgCount;
	}

	@Override
	public boolean hasParameterPack() {
		return PDOMCPPAnnotations.hasParameterPack(getAnnotations());
	}

	@Override
	public boolean isDeleted() {
		return PDOMCPPAnnotations.isDeletedFunction(getAnnotations());
	}

	@Override
	public int pdomCompareTo(PDOMBinding other) {
		int cmp = super.pdomCompareTo(other);
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
	public ICPPExecution getFunctionBodyExecution() {
		if (!isConstexpr())
			return null;

		try {
			ICPPExecution exec = getLinkage().loadExecution(record + FUNCTION_BODY);
			if (exec == null) {
				exec = CPPTemplates.instantiateFunctionBody(this);
			}
			return exec;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
}
