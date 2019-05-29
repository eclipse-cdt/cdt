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
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

public class QuickFixAddDefaultSwitch extends AbstractAstRewriteQuickFix {

	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixAddDefaultSwitch_add_default_to_switch;
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
		IASTNode astNode = null;
		if (isCodanProblem(marker)) {
			astNode = getASTNodeFromMarker(marker, ast);
		}
		if (astNode == null || !(astNode instanceof IASTSwitchStatement)) {
			return;
		}
		ASTRewrite r = ASTRewrite.create(ast);
		INodeFactory factory = ast.getASTNodeFactory();
		IASTDefaultStatement defStatement = factory.newDefaultStatement();
		IASTBreakStatement breakStatement = factory.newBreakStatement();
		IASTNode[] children = astNode.getChildren();
		IASTCompoundStatement compound = null;
		IASTNullStatement nullStatement = null;
		for (int i = 0; i < children.length; ++i) {
			if (children[i] instanceof IASTCompoundStatement) {
				compound = (IASTCompoundStatement) children[i];
				break;
			} else if (children[i] instanceof IASTNullStatement)
				nullStatement = (IASTNullStatement) children[i];
		}
		if (compound == null && nullStatement != null) {
			compound = factory.newCompoundStatement();
			compound.addStatement(defStatement);
			compound.addStatement(breakStatement);
			r.replace(nullStatement, compound, null);
		} else if (compound != null) {
			r.insertBefore(compound, null, defStatement, null);
			r.insertBefore(compound, null, breakStatement, null);
		} else
			return;
		Change c = r.rewriteAST();
		try {
			c.perform(new NullProgressMonitor());
			marker.delete();
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		}
	}
}
