/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalFixed;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.ExecConstructorChain;

public class CPPConstructor extends CPPMethod implements ICPPConstructor {
	public CPPConstructor(ICPPASTFunctionDeclarator declarator) {
		super(declarator);
	}

	@Override
	public ICPPExecution getConstructorChainExecution() {
		return getConstructorChainExecution(this);
	}

	@Override
	public ICPPExecution getConstructorChainExecution(IASTNode point) {
		return getConstructorChainExecution();
	}

	private static ICPPEvaluation getMemberEvaluation(ICPPField member,
			ICPPASTConstructorChainInitializer chainInitializer, IASTNode point) {
		final IASTInitializer initializer = chainInitializer.getInitializer();
		if (initializer instanceof ICPPASTInitializerClause) {
			return ((ICPPASTInitializerClause) initializer).getEvaluation();
		} else if (initializer instanceof ICPPASTConstructorInitializer) {
			IBinding constructor = CPPSemantics.findImplicitlyCalledConstructor(chainInitializer);
			if (constructor == null) {
				return new EvalTypeId(member.getType(), point, EvalConstructor.extractArguments(initializer));
			} else if (constructor instanceof IProblemBinding) {
				return EvalFixed.INCOMPLETE;
			}
			return new EvalConstructor(member.getType(), (ICPPConstructor) constructor,
					EvalConstructor.extractArguments(initializer), point);
		}
		return null;
	}

	static ICPPExecution computeConstructorChainExecution(IASTNode def) {
		ICPPASTFunctionDefinition fnDef = getFunctionDefinition(def);
		if (fnDef != null) {
			final ICPPASTConstructorChainInitializer[] ccInitializers = fnDef.getMemberInitializers();
			final Map<IBinding, ICPPEvaluation> resultPairs = new HashMap<>();
			for (ICPPASTConstructorChainInitializer ccInitializer : ccInitializers) {
				final IBinding member = ccInitializer.getMemberInitializerId().resolveBinding();
				if (member instanceof ICPPField) {
					final ICPPField fieldMember = (ICPPField) member;
					final ICPPEvaluation memberEval = getMemberEvaluation(fieldMember, ccInitializer, fnDef);
					resultPairs.put(fieldMember, memberEval);
				} else if (member instanceof ICPPConstructor) {
					final ICPPConstructor ctorMember = (ICPPConstructor) member;
					final IASTInitializer initializer = ccInitializer.getInitializer();
					if (initializer instanceof ICPPASTConstructorInitializer
							|| initializer instanceof ICPPASTInitializerList) {
						final ICPPClassType baseClassType = (ICPPClassType) ctorMember.getOwner();
						EvalConstructor memberEval = new EvalConstructor(baseClassType, ctorMember,
								EvalConstructor.extractArguments(initializer, ctorMember), fnDef);
						resultPairs.put(ctorMember, memberEval);
					}
				}
			}
			return new ExecConstructorChain(resultPairs);
		}
		return null;
	}

	static ICPPExecution getConstructorChainExecution(CPPFunction function) {
		if (!function.isConstexpr())
			return null;
		return computeConstructorChainExecution(function.getDefinition());
	}
}
