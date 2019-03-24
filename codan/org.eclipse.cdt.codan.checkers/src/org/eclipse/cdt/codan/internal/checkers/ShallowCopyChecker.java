/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.SemanticQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * Checker to find that class has pointers but no copy constructor
 */
@SuppressWarnings("restriction")
public class ShallowCopyChecker extends AbstractIndexAstChecker {
	public static final String PROBLEM_ID = "org.eclipse.cdt.codan.internal.checkers.ShallowCopyProblem"; //$NON-NLS-1$
	private static final String OPERATOR_EQ = "operator ="; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new OnEachClass());
	}

	private static boolean hasCopyMethods(ICPPClassType classType) {
		boolean foundCopyConstructor = false;
		boolean foundOperatorEqual = false;
		for (ICPPMethod method : classType.getDeclaredMethods()) {
			if (!foundOperatorEqual && OPERATOR_EQ.equals(method.getName())) {
				foundOperatorEqual = true;
			}
			if (!foundCopyConstructor && method instanceof ICPPConstructor
					&& SemanticQueries.isCopyConstructor((ICPPConstructor) method)) {
				foundCopyConstructor = true;
			}
			if (foundOperatorEqual && foundCopyConstructor)
				return true;
		}
		return false;
	}

	private class OnEachClass extends ASTVisitor {
		OnEachClass() {
			shouldVisitDeclSpecifiers = true;
		}

		@Override
		public int visit(IASTDeclSpecifier decl) {
			if (decl instanceof ICPPASTCompositeTypeSpecifier) {
				ICPPASTCompositeTypeSpecifier spec = (ICPPASTCompositeTypeSpecifier) decl;
				IASTName className = spec.getName();
				IBinding binding = className.resolveBinding();
				if (!(binding instanceof ICPPClassType)) {
					return PROCESS_SKIP;
				}
				try {
					CPPSemantics.pushLookupPoint(className);
					ICPPClassType classType = (ICPPClassType) binding;
					boolean hasCopyConstructor = hasCopyMethods(classType);
					if (hasCopyConstructor) {
						return PROCESS_SKIP;
					}
					ICPPField[] fields = classType.getDeclaredFields();
					boolean hasPointers = false;
					for (ICPPField f : fields) {
						if (f.getType() instanceof IPointerType || f.getType() instanceof ICPPReferenceType) {
							hasPointers = true;
							break;
						}
					}
					if (hasPointers)
						reportProblem(PROBLEM_ID, decl);
					return PROCESS_SKIP;
				} finally {
					CPPSemantics.popLookupPoint();
				}
			}
			return PROCESS_CONTINUE;
		}
	}
}
