/*******************************************************************************
 * Copyright (c) 2006, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     IBM Corporation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ProblemFunctionType;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCFunction extends PDOMBinding implements IFunction {
	/**
	 * Offset of total number of function parameters (relative to the beginning of the record).
	 */
	public static final int NUM_PARAMS = PDOMBinding.RECORD_SIZE;

	/**
	 * Offset of total number of function parameters (relative to the beginning of the record).
	 */
	public static final int FIRST_PARAM = NUM_PARAMS + 4;

	/**
	 * Offset for the type of this function (relative to the beginning of the record).
	 */
	private static final int FUNCTION_TYPE = FIRST_PARAM + Database.PTR_SIZE;

	/**
	 * Offset of annotation information (relative to the beginning of the record).
	 */
	private static final int ANNOTATIONS = FUNCTION_TYPE + Database.TYPE_SIZE; // byte

	/**
	 * The size in bytes of a PDOMCPPFunction record in the database.
	 */
	@SuppressWarnings("hiding")
	public static final int RECORD_SIZE = ANNOTATIONS + 1;

	public PDOMCFunction(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public PDOMCFunction(PDOMLinkage linkage, PDOMNode parent, IFunction function) throws CoreException {
		super(linkage, parent, function.getNameCharArray());

		IFunctionType type = function.getType();
		setType(getLinkage(), type);
		IParameter[] parameters = function.getParameters();
		setParameters(parameters);
		byte annotations = PDOMCAnnotations.encodeFunctionAnnotations(function);
		getDB().putByte(record + ANNOTATIONS, annotations);
	}

	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (!(newBinding instanceof IFunction))
			return;

		IFunction func = (IFunction) newBinding;
		IFunctionType newType = func.getType();
		setType(linkage, newType);

		PDOMCParameter oldParams = getFirstParameter(null);
		IParameter[] newParams = func.getParameters();
		setParameters(newParams);
		if (oldParams != null) {
			oldParams.delete(linkage);
		}
		byte newAnnotation = PDOMCAnnotations.encodeFunctionAnnotations(func);
		getDB().putByte(record + ANNOTATIONS, newAnnotation);
	}

	private void setType(PDOMLinkage linkage, IFunctionType ft) throws CoreException {
		linkage.storeType(record + FUNCTION_TYPE, ft);
	}

	private void setParameters(IParameter[] params) throws CoreException {
		final PDOMLinkage linkage = getLinkage();
		final Database db = getDB();
		db.putInt(record + NUM_PARAMS, params.length);
		db.putRecPtr(record + FIRST_PARAM, 0);
		PDOMCParameter next = null;
		for (int i = params.length; --i >= 0;) {
			next = new PDOMCParameter(linkage, this, params[i], next);
		}
		db.putRecPtr(record + FIRST_PARAM, next == null ? 0 : next.getRecord());
	}

	public PDOMCParameter getFirstParameter(IType t) throws CoreException {
		long rec = getDB().getRecPtr(record + FIRST_PARAM);
		return rec != 0 ? new PDOMCParameter(getLinkage(), rec, t) : null;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCBindingConstants.CFUNCTION;
	}

	@Override
	public IFunctionType getType() {
		try {
			return (IFunctionType) getLinkage().loadType(record + FUNCTION_TYPE);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ProblemFunctionType(ISemanticProblem.TYPE_NOT_PERSISTED);
		}
	}

	@Override
	public boolean isStatic() {
		return PDOMCAnnotations.isStatic(getAnnotations());
	}

	@Override
	public boolean isExtern() {
		return PDOMCAnnotations.isExtern(getAnnotations());
	}

	@Override
	public IParameter[] getParameters() {
		try {
			PDOMLinkage linkage = getLinkage();
			Database db = getDB();
			IFunctionType ft = getType();
			IType[] ptypes = ft == null ? IType.EMPTY_TYPE_ARRAY : ft.getParameterTypes();

			int n = db.getInt(record + NUM_PARAMS);
			IParameter[] result = new IParameter[n];

			long next = db.getRecPtr(record + FIRST_PARAM);
			for (int i = 0; i < n && next != 0; i++) {
				IType type = i < ptypes.length ? ptypes[i] : null;
				final PDOMCParameter par = new PDOMCParameter(linkage, next, type);
				next = par.getNextPtr();
				result[i] = par;
			}
			return result;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return IParameter.EMPTY_PARAMETER_ARRAY;
		}
	}

	@Override
	public boolean isAuto() {
		// ISO/IEC 9899:TC1 6.9.1.4
		return false;
	}

	@Override
	public boolean isRegister() {
		// ISO/IEC 9899:TC1 6.9.1.4
		return false;
	}

	@Override
	public boolean isInline() {
		return PDOMCAnnotations.isInline(getAnnotations());
	}

	private byte getAnnotations() {
		return getByte(record + ANNOTATIONS);
	}

	@Override
	public boolean takesVarArgs() {
		return PDOMCAnnotations.isVarargsFunction(getAnnotations());
	}

	@Override
	public boolean isNoReturn() {
		return PDOMCAnnotations.isNoReturnFunction(getAnnotations());
	}

	@Override
	public IScope getFunctionScope() {
		return null;
	}
}
