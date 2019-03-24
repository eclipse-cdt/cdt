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
 * Contributors:
 *    Marco Stornelli - Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.codan.internal.checkers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.codan.checkers.CodanCheckersActivator;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableInstance;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalVariable;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPGlobalScope;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class SymbolShadowingChecker extends AbstractIndexAstChecker {

	public static final String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.SymbolShadowingProblem"; //$NON-NLS-1$

	private IASTTranslationUnit ast;
	private IIndex index;

	@Override
	public void processAst(IASTTranslationUnit ast) {
		this.ast = ast;
		index = ast.getIndex();
		ast.accept(new VariableDeclarationVisitor());
	}

	/**
	 * This visitor looks for variable declarations.
	 */
	class VariableDeclarationVisitor extends ASTVisitor {

		VariableDeclarationVisitor() {
			shouldVisitDeclarators = true;
		}

		/**
		 * Check if it's the type we want. We check for fields, variables and parameters.
		 * @param binding The binding to be checked
		 * @return True if it's a field or variable, false otherwise
		 */
		private boolean validBinding(IBinding binding) {
			return binding instanceof IField || binding instanceof ICPPInternalVariable
					|| binding instanceof ICPPVariableInstance || binding instanceof IParameter;
		}

		private void report(String id, IASTNode astNode, Set<IProblemLocation> cache, Object... args) {
			IProblemLocation loc = getProblemLocation(astNode);
			if (loc != null && !cache.contains(loc)) {
				reportProblem(id, loc, args);
				cache.add(loc);
			}
		}

		private int getLocation(IASTNode astNode, IASTFileLocation astLocation) {
			if (enclosedInMacroExpansion(astNode) && astNode instanceof IASTName) {
				IASTImageLocation imageLocation = ((IASTName) astNode).getImageLocation();
				if (imageLocation != null) {
					return imageLocation.getNodeOffset();
				}
			}
			return astLocation.getNodeOffset();
		}

		/**
		 * Check if at least one of declNames is declared before our declarator, if they
		 * are all declared after our declarator then there's no shadowing.
		 * @param declarator The declarator in lower scope to be checked
		 * @param declNames The declarator names in upper scope
		 * @return True if at least of upper scopes is declared before declarator, false otherwise
		 */
		private boolean isParentDeclaredBefore(IASTDeclarator declarator, IASTName[] declNames) {
			int start = getLocation(declarator, declarator.getFileLocation());
			for (IASTName n : declNames) {
				if (getLocation(n, n.getFileLocation()) < start)
					return true;
			}
			return false;
		}

		@Override
		public int visit(IASTDeclarator declarator) {
			IBinding binding = declarator.getName().resolveBinding();

			if (binding == null || binding instanceof IProblemBinding)
				return PROCESS_CONTINUE;

			/**
			 * We need a cache here to avoid to report same problem multiple times.
			 */
			Set<IProblemLocation> cache = new HashSet<>();
			IScope scope;
			try {
				scope = binding.getScope();
				if (scope.getKind() != EScopeKind.eLocal)
					return PROCESS_CONTINUE;
				scope = scope.getParent();
				while (scope != null && !(scope instanceof IProblemBinding) && !(scope instanceof PDOMCPPGlobalScope)) {
					IBinding[] scopeBindings = scope.find(declarator.getName().toString(),
							declarator.getTranslationUnit());

					IScope current = scope;
					scope = scope.getParent();
					for (IBinding scopeBinding : scopeBindings) {
						if (scopeBinding != null && validBinding(scopeBinding)) {
							IASTName[] declNames = ast.getDeclarationsInAST(scopeBinding);
							if (declNames != null && declNames.length != 0) {
								if (scope != null && current.getKind() == scope.getKind()
										&& current.getKind() == EScopeKind.eLocal
										&& isParentDeclaredBefore(declarator, declNames)) {
									report(ERR_ID, declarator, cache, declarator.getName());
								} else if (scope == null || current.getKind() != scope.getKind()) {
									report(ERR_ID, declarator, cache, declarator.getName());
								}
							} else {
								IIndexName[] indexNames = index.findDeclarations(scopeBinding);
								if (indexNames != null && indexNames.length != 0)
									report(ERR_ID, declarator, cache, declarator.getName());
							}
							break;
						}
					}
				}
			} catch (DOMException | CoreException e) {
				CodanCheckersActivator.log(e);
			}
			return PROCESS_CONTINUE;
		}
	}
}