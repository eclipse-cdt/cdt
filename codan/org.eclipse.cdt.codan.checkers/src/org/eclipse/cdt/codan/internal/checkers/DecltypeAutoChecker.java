/*******************************************************************************
 * Copyright (c) 2017 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;

public class DecltypeAutoChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.DecltypeAutoProblem"; //$NON-NLS-1$

	@Override
	public boolean runInEditor() {
		return true;
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitDeclSpecifiers = true;
			}

			@Override
			public int visit(IASTDeclSpecifier specifier) {
				if (specifier instanceof ICPPASTSimpleDeclSpecifier) {
					if (((ICPPASTSimpleDeclSpecifier) specifier)
							.getType() == ICPPASTSimpleDeclSpecifier.t_decltype_auto) {
						if (specifier.isConst() || specifier.isVolatile()) {
							reportProblem(ERR_ID, specifier);
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
