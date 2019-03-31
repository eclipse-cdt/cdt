/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.checkers.CodanCheckersActivator;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IVariable;

public class VariableInitializationChecker extends AbstractIndexAstChecker {
	public static final String STATIC_VAR_ID = "org.eclipse.cdt.codan.internal.checkers.StaticVariableInHeaderProblem"; //$NON-NLS-1$
	public static final String VAR_MULTI_DEC_ID = "org.eclipse.cdt.codan.internal.checkers.MultipleDeclarationsProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simple = (IASTSimpleDeclaration) declaration;
					IASTDeclarator decls[] = ((IASTSimpleDeclaration) declaration).getDeclarators();
					if (simple.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_static
							&& declaration.getTranslationUnit().isHeaderUnit()) {
						if (decls.length > 0) {
							for (IASTDeclarator d : decls) {
								IBinding binding = d.getName().resolveBinding();
								if (binding != null && binding instanceof IVariable) {
									try {
										IScope scope = binding.getScope();
										if (scope == null)
											return PROCESS_CONTINUE;
										if (scope.getKind() == EScopeKind.eGlobal
												|| scope.getKind() == EScopeKind.eNamespace)
											reportProblem(STATIC_VAR_ID, declaration, d.getName());
									} catch (DOMException e) {
										CodanCheckersActivator.log(e);
									}
								}
							}
						}
					}
					if (decls.length == 0)
						return PROCESS_CONTINUE;
					if (decls.length != 1)
						reportProblem(VAR_MULTI_DEC_ID, declaration);
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
