/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType.UNSPECIFIED_TYPE;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeTraits;

/**
 * Binding for implicit constructors (default and copy constructor).
 */
public class CPPImplicitConstructor extends CPPImplicitMethod implements ICPPConstructor {
	public CPPImplicitConstructor(ICPPClassScope scope, char[] name, ICPPParameter[] params, IASTNode point) {
		// Note: the value passed for the 'isConstexpr' parameter of the CPPImplicitMethod constructor
		// is irrelevant, as CPPImplicitConstructor overrides isConstexpr().
		super(scope, name, createFunctionType(params), params, false);
	}

	private static ICPPFunctionType createFunctionType(IParameter[] params) {
		return CPPVisitor.createImplicitFunctionType(UNSPECIFIED_TYPE, params, false, false);
	}

	/*
	 *	From $12.1 / 5:
	 *	The implicitly-defined default constructor performs the set of initializations of the class that would
	 *	be performed by a user-written default constructor for that class with no ctor-initializer (12.6.2) and
	 *	an empty compound-statement. […] If that user-written default constructor would satisfy the requirements
	 *	of a constexpr constructor (7.1.5), the implicitly-defined default constructor is constexpr.
	 *
	 * Therefore, an implicitly-defined constructor should be considered constexpr if the class type is a literal type.
	*/
	@Override
	public boolean isConstexpr() {
		return TypeTraits.isLiteralClass(getClassOwner());
	}

	@Override
	public ICPPExecution getConstructorChainExecution() {
		return CPPConstructor.getConstructorChainExecution(this);
	}

	@Override
	public ICPPExecution getConstructorChainExecution(IASTNode point) {
		return getConstructorChainExecution();
	}
}
