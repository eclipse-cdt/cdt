/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Nathan Ridge
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;

/**
 * Represents a reference to a (member) function (instance), which cannot be resolved because 
 * an argument depends on a template parameter. A compiler would resolve it during instantiation.
 */
public class CPPDeferredFunction extends CPPUnknownBinding implements ICPPDeferredFunction, ICPPComputableFunction {
	private static final ICPPFunctionType FUNCTION_TYPE=
			new CPPFunctionType(ProblemType.UNKNOWN_FOR_EXPRESSION, IType.EMPTY_TYPE_ARRAY);

	/**
	 * Creates a CPPDeferredFunction given a set of overloaded functions
	 * (some of which may be templates) that the function might resolve to.
	 * At least one candidate must be provided.
	 * @param candidates a set of overloaded functions, some of which may be templates
	 * @return the constructed CPPDeferredFunction
	 */
	public static ICPPFunction createForCandidates(ICPPFunction... candidates) {
		if (candidates[0] instanceof ICPPConstructor)
			return new CPPUnknownConstructor(((ICPPConstructor) candidates[0]).getClassOwner(), candidates);
		
		final IBinding owner = candidates[0].getOwner();
		return new CPPDeferredFunction(owner, candidates[0].getNameCharArray(), candidates);
	}
	
	/**
	 * Creates a CPPDeferredFunction given a name. This is for cases where there
	 * are no candidates that could be passed to createForCandidates().
	 * @param name the name of the function
	 * @return the constructed CPPDeferredFunction
	 */
	public static ICPPFunction createForName(char[] name) {
		return new CPPDeferredFunction(null, name, null);
	}

	private final IBinding fOwner;
	private final ICPPFunction[] fCandidates;

	public CPPDeferredFunction(IBinding owner, char[] name, ICPPFunction[] candidates) {
		super(name);
		fOwner= owner;
		fCandidates = candidates;
	}

	@Override
	public ICPPFunction[] getCandidates() {
		return fCandidates;
	}

	@Override
	public IType[] getExceptionSpecification() {
		return null;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public boolean isInline() {
		return false;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public boolean isConstexpr() {
		return false;
	}

	@Override
	public IScope getFunctionScope() {
		return asScope();
	}

	@Override
	public ICPPParameter[] getParameters() {
		return ICPPParameter.EMPTY_CPPPARAMETER_ARRAY;
	}

	@Override
	public ICPPFunctionType getType() {
		return FUNCTION_TYPE;
	}

	@Override
	public boolean isAuto() {
		return false;
	}

	@Override
	public boolean isExtern() {
		return false;
	}

	@Override
	public boolean isRegister() {
		return false;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean takesVarArgs() {
		return false;
	}

	@Override
	public boolean isNoReturn() {
		return false;
	}

	@Override
	public int getRequiredArgumentCount() {
		return 0;
	}

	@Override
	public boolean hasParameterPack() {
		return false;
	}
	
	@Override
	public IBinding getOwner() {
		return fOwner;
	}

	@Override
	public ICPPEvaluation getReturnExpression() {
		return null;
	}
}
