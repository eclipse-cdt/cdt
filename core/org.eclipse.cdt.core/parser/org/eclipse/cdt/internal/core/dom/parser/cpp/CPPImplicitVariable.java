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

/**
 * Represents a variable implicitly created in C++ code.
 * For example the initializer of a structured binding decomposition [dcl.struct.bind]:
 * <code>auto [first, second] = decomposed;</code>
 *
 * The <code>initializerEvaluation</code> and <code>initializerType</code> have to be supplied.
 * Currently, this class is used for caching the evaluation and type of the initializer to avoid
 * the repetitive resolution for each individual name introduced by a structured binding declaration.
 *
 */
public class CPPImplicitVariable extends CPPVariable {

	private ICPPEvaluation initializerEvaluation;
	private IType initializerType;

	public CPPImplicitVariable(IASTImplicitName name, ICPPEvaluation initializerEvaluation, IType type) {
		super(name);
		this.initializerEvaluation = initializerEvaluation;
		this.initializerType = type;
	}

	@Override
	public ICPPEvaluation getInitializerEvaluation() {
		return initializerEvaluation;
	}

	@Override
	public IType getType() {
		return initializerType;
	}
}
