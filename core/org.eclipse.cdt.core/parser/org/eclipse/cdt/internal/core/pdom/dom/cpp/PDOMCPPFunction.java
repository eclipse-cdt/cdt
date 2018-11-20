/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     IBM Corporation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.ProblemFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPComputableFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IndexCPPSignatureUtil;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMOverloader;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for c++ functions in the index.
 */
class PDOMCPPFunction extends PDOMCPPBinding implements ICPPFunction, IPDOMOverloader, ICPPComputableFunction {
	/**
	 * Offset of total number of function parameters (relative to the beginning of the record).
	 */
	private static final int NUM_PARAMS = PDOMCPPBinding.RECORD_SIZE;

	/**
	 * Offset of pointer to the first parameter of this function (relative to the beginning of the record).
	 */
	private static final int FIRST_PARAM = NUM_PARAMS + 4;

	/**
	 * Offset of pointer to the function type record of this function (relative to the beginning of the
	 * record).
	 */
	protected static final int FUNCTION_TYPE = FIRST_PARAM + Database.PTR_SIZE;

	/**
	 * Offset of hash of parameter information to allow fast comparison
	 */
	private static final int SIGNATURE_HASH = FUNCTION_TYPE + Database.TYPE_SIZE;

	/**
	 * Offset of start of exception specifications
	 */
	protected static final int EXCEPTION_SPEC = SIGNATURE_HASH + 4; // int

	/**
	 * Offset of annotation information (relative to the beginning of the record).
	 */
	private static final int ANNOTATION = EXCEPTION_SPEC + Database.PTR_SIZE; // short

	/** Offset of the number of the required arguments. */
	private static final int REQUIRED_ARG_COUNT = ANNOTATION + 2; // short

	/** Offset of the function body execution for constexpr functions. */
	private static final int FUNCTION_BODY = REQUIRED_ARG_COUNT + 2; // Database.EXECUTION_SIZE

	/** Offset of the function's declared type. */
	private static final int DECLARED_TYPE = FUNCTION_BODY + Database.EXECUTION_SIZE; // Database.TYPE_SIZE

	/**
	 * The size in bytes of a PDOMCPPFunction record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = DECLARED_TYPE + Database.TYPE_SIZE;

	private short fAnnotations = -1;
	private int fRequiredArgCount = -1;
	private ICPPFunctionType fType; // No need for volatile, all fields of ICPPFunctionTypes are final.
	private ICPPFunctionType fDeclaredType;

	public PDOMCPPFunction(PDOMCPPLinkage linkage, PDOMNode parent, ICPPFunction function, boolean setTypes)
			throws CoreException, DOMException {
		super(linkage, parent, function.getNameCharArray());
		Database db = getDB();
		Integer sigHash = IndexCPPSignatureUtil.getSignatureHash(function);
		getDB().putInt(record + SIGNATURE_HASH, sigHash != null ? sigHash.intValue() : 0);
		db.putShort(record + ANNOTATION, getAnnotations(function));
		db.putShort(record + REQUIRED_ARG_COUNT, (short) function.getRequiredArgumentCount());
		if (setTypes) {
			linkage.new ConfigureFunction(function, this);
		}
	}

	private short getAnnotations(ICPPFunction function) {
		return PDOMCPPAnnotations.encodeFunctionAnnotations(function);
	}

	public void initData(ICPPFunctionType ftype, ICPPFunctionType declaredType, ICPPParameter[] params,
			IType[] exceptionSpec, ICPPExecution functionBody) {
		try {
			setType(ftype);
			setDeclaredType(declaredType);
			setParameters(params);
			storeExceptionSpec(exceptionSpec);
			getLinkage().storeExecution(record + FUNCTION_BODY, functionBody);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (!(newBinding instanceof ICPPFunction))
			return;

		ICPPFunction func = (ICPPFunction) newBinding;
		ICPPFunctionType newType;
		ICPPFunctionType newDeclaredType;
		ICPPParameter[] newParams;
		short newAnnotation;
		int newBindingRequiredArgCount;
		newType = func.getType();
		newDeclaredType = func.getDeclaredType();
		newParams = func.getParameters();
		newAnnotation = getAnnotations(func);
		newBindingRequiredArgCount = func.getRequiredArgumentCount();

		fType = null;
		fDeclaredType = null;
		linkage.storeType(record + FUNCTION_TYPE, newType);
		linkage.storeType(record + DECLARED_TYPE, newDeclaredType);

		PDOMCPPParameter oldParams = getFirstParameter(null);
		int requiredCount;
		if (oldParams != null && hasDeclaration()) {
			int parCount = 0;
			requiredCount = 0;
			for (ICPPParameter newPar : newParams) {
				parCount++;
				if (parCount <= newBindingRequiredArgCount && !oldParams.hasDefaultValue())
					requiredCount = parCount;
				oldParams.update(newPar);
				long next = oldParams.getNextPtr();
				if (next == 0)
					break;
				oldParams = new PDOMCPPParameter(linkage, next, null);
			}
			if (parCount < newBindingRequiredArgCount) {
				requiredCount = newBindingRequiredArgCount;
			}
		} else {
			requiredCount = newBindingRequiredArgCount;
			setParameters(newParams);
			if (oldParams != null) {
				oldParams.delete(linkage);
			}
		}
		final Database db = getDB();
		db.putShort(record + ANNOTATION, newAnnotation);
		fAnnotations = newAnnotation;
		db.putShort(record + REQUIRED_ARG_COUNT, (short) requiredCount);
		fRequiredArgCount = requiredCount;

		long oldRec = db.getRecPtr(record + EXCEPTION_SPEC);
		storeExceptionSpec(extractExceptionSpec(func));
		if (oldRec != 0) {
			PDOMCPPTypeList.clearTypes(this, oldRec);
		}
		linkage.storeExecution(record + FUNCTION_BODY, CPPFunction.getFunctionBodyExecution(func));
	}

	private void storeExceptionSpec(IType[] exceptionSpec) throws CoreException {
		long typelist = PDOMCPPTypeList.putTypes(this, exceptionSpec);
		getDB().putRecPtr(record + EXCEPTION_SPEC, typelist);
	}

	IType[] extractExceptionSpec(ICPPFunction binding) {
		IType[] exceptionSpec;
		if (binding instanceof ICPPMethod && ((ICPPMethod) binding).isImplicit()) {
			// Don't store the exception specification, compute it on demand.
			exceptionSpec = null;
		} else {
			exceptionSpec = binding.getExceptionSpecification();
		}
		return exceptionSpec;
	}

	private void setParameters(ICPPParameter[] params) throws CoreException {
		final PDOMLinkage linkage = getLinkage();
		final Database db = getDB();
		db.putInt(record + NUM_PARAMS, params.length);
		db.putRecPtr(record + FIRST_PARAM, 0);
		PDOMCPPParameter next = null;
		for (int i = params.length; --i >= 0;) {
			next = new PDOMCPPParameter(linkage, this, params[i], next);
		}
		db.putRecPtr(record + FIRST_PARAM, next == null ? 0 : next.getRecord());
	}

	private void setType(ICPPFunctionType ft) throws CoreException {
		fType = null;
		getLinkage().storeType(record + FUNCTION_TYPE, ft);
	}

	private void setDeclaredType(ICPPFunctionType ft) throws CoreException {
		fType = null;
		getLinkage().storeType(record + DECLARED_TYPE, ft);
	}

	@Override
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

	private PDOMCPPParameter getFirstParameter(IType type) throws CoreException {
		long rec = getDB().getRecPtr(record + FIRST_PARAM);
		return rec != 0 ? new PDOMCPPParameter(getLinkage(), rec, type) : null;
	}

	@Override
	public boolean isInline() {
		return PDOMCPPAnnotations.isInline(getAnnotations());
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

	protected final short getAnnotations() {
		if (fAnnotations == -1) {
			try {
				fAnnotations = getDB().getShort(record + ANNOTATION);
			} catch (CoreException e) {
				fAnnotations = 0;
			}
		}
		return fAnnotations;
	}

	@Override
	public boolean isExternC() {
		return PDOMCPPAnnotations.isExternC(getAnnotations());
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
				final PDOMCPPParameter par = new PDOMCPPParameter(linkage, next, type);
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
	public final ICPPFunctionType getType() {
		if (fType == null) {
			try {
				IType type = getLinkage().loadType(record + FUNCTION_TYPE);
				if (type instanceof ICPPFunctionType) {
					fType = (ICPPFunctionType) type;
				} else {
					// Something went wrong while loading the type and we didn't
					// get a function type. Treat it similar to an exception.
					fType = ProblemFunctionType.NOT_PERSISTED;
				}
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fType = ProblemFunctionType.NOT_PERSISTED;
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
	public boolean isDeleted() {
		return PDOMCPPAnnotations.isDeletedFunction(getAnnotations());
	}

	@Override
	public boolean isExtern() {
		return PDOMCPPAnnotations.isExtern(getAnnotations());
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
	public boolean hasParameterPack() {
		return PDOMCPPAnnotations.hasParameterPack(getAnnotations());
	}

	@Override
	public Object clone() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int pdomCompareTo(PDOMBinding other) {
		int cmp = super.pdomCompareTo(other);
		return cmp == 0 ? compareSignatures(this, other) : cmp;
	}

	protected static int compareSignatures(IPDOMOverloader a, Object b) {
		if (b instanceof IPDOMOverloader) {
			IPDOMOverloader bb = (IPDOMOverloader) b;
			try {
				int mySM = a.getSignatureHash();
				int otherSM = bb.getSignatureHash();
				return mySM == otherSM ? 0 : mySM < otherSM ? -1 : 1;
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		} else {
			assert false;
		}
		return 0;
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
			return getLinkage().loadExecution(record + FUNCTION_BODY);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
}
