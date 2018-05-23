/*******************************************************************************
 * Copyright (c) 2019 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thomas Corbat (IFS) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;

/**
 * Represents a variable implicitly created in C++ code.
 * For example the initializer of a structured binding decomposition [dcl.struct.bind]:
 * <code>auto [first, second] = decomposed;</code>
 *
 * The <code>initializerEvaluation</code> has to be supplied.
 *
 */
public class CPPImplicitVariable extends CPPVariable {

	private ICPPEvaluation initializerEvaluation;

	public CPPImplicitVariable(IASTImplicitName name, ICPPEvaluation initializerEvaluation) {
		super(name);
		this.initializerEvaluation = initializerEvaluation;
	}

	@Override
	public ICPPEvaluation getInitializerEvaluation() {
		return initializerEvaluation;
	}

	@Override
	public IType getType() {
		return initializerEvaluation.getType();
	}

	@Override
	public IValue getInitialValue() {
		return initializerEvaluation.getValue();
	}
}
