/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Patrick Hofer [bug 315528]
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import java.util.HashSet;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;

/**
 * Checker to find that class has virtual method and non virtual destructor
 * 
 * @author Alena Laskavaia
 */
public class NonVirtualDestructor extends AbstractIndexAstChecker {
	public static final String PROBLEM_ID = "org.eclipse.cdt.codan.internal.checkers.NonVirtualDestructorProblem"; //$NON-NLS-1$
	
	// Prevent stack overflow in case: class A: public A {};
	private static HashSet<ICPPClassType> checkedClassTypes = new HashSet<ICPPClassType>();

	@Override
	public void processAst(IASTTranslationUnit ast) {
		// Traverse the AST using the visitor pattern.
		ast.accept(new OnEachClass());
	}

	private static ICPPMethod getDestructor(ICPPClassType classType) {
		for (ICPPMethod method : classType.getDeclaredMethods()) {
			if (method.isDestructor()) {
				return method;
			}
		}
		return null;
	}

	private static boolean hasVirtualDestructor(ICPPClassType classType) {
		checkedClassTypes.add(classType);
		ICPPMethod destructor = getDestructor(classType);
		if (destructor != null && destructor.isVirtual()) {
			return true;
		}
		ICPPBase[] bases = classType.getBases();   
		for (ICPPBase base : bases) {
			IBinding baseClass = base.getBaseClass();
			if (baseClass instanceof ICPPClassType) {
				ICPPClassType cppClassType = (ICPPClassType) baseClass;
				if (!checkedClassTypes.contains(cppClassType) && hasVirtualDestructor(cppClassType)) {
					return true;
				}
			}
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
				ICPPClassType classType = (ICPPClassType) binding;
				boolean hasVirtualDestructor = hasVirtualDestructor(classType);
				checkedClassTypes.clear();
				if (hasVirtualDestructor) {
					return PROCESS_SKIP;
				}
				ICPPMethod virtualMethod = null;
				for (ICPPMethod method : classType.getAllDeclaredMethods()) {
					if (!method.isDestructor() && method.isVirtual()) {
						virtualMethod = method;
					}
				}
				if (virtualMethod == null) {
					return PROCESS_SKIP;
				}
				ICPPMethod destructor = getDestructor(classType);
				if (destructor != null &&
						destructor.getVisibility() != ICPPASTVisibilityLabel.v_public &&
						classType.getFriends().length == 0) {
					// No error if the destructor is protected or private and there are no friends.
					return PROCESS_SKIP;
				}

				IASTNode node = decl;
				if (destructor instanceof ICPPInternalBinding) {
					IASTNode[] decls = ((ICPPInternalBinding) destructor).getDeclarations();
					if (decls != null && decls.length > 0) {
						node = decls[0];
					}
				}
				reportProblem(PROBLEM_ID, node, className.getSimpleID().toString(),
						virtualMethod.getName());
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}
	}
}
