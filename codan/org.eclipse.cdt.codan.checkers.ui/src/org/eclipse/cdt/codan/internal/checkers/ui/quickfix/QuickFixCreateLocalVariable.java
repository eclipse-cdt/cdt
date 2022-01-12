/*******************************************************************************
 * Copyright (c) 2010 Tomasz Wesolowski
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

public class QuickFixCreateLocalVariable extends AbstractAstRewriteQuickFix {
	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixCreateLocalVariable_create_local_variable;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		IASTTranslationUnit ast;
		try {
			ITranslationUnit tu = getTranslationUnitViaEditor(marker);
			ast = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
			return;
		}
		IASTName astName;
		if (isCodanProblem(marker)) {
			astName = getASTNameFromMarker(marker, ast);
		} else {
			astName = getAstNameFromProblemArgument(marker, ast, 0);
		}
		if (astName == null) {
			return;
		}
		ASTRewrite r = ASTRewrite.create(ast);
		INodeFactory factory = ast.getASTNodeFactory();
		IASTDeclaration declaration = CxxAstUtils.createDeclaration(astName, factory, index);
		IASTDeclarationStatement newStatement = factory.newDeclarationStatement(declaration);
		IASTNode targetStatement = CxxAstUtils.getEnclosingStatement(astName);
		if (targetStatement == null) {
			return;
		}
		r.insertBefore(targetStatement.getParent(), targetStatement, newStatement, null);
		Change c = r.rewriteAST();
		try {
			c.perform(new NullProgressMonitor());
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
			return;
		}
		try {
			marker.delete();
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		}
	}

	@Override
	public boolean isApplicable(IMarker marker) {
		if (isCodanProblem(marker)) {
			String problemArgument = getProblemArgument(marker, 1);
			return problemArgument.contains(":func"); //$NON-NLS-1$
		}
		return true; // gcc problem that matched the pattern
	}
}
