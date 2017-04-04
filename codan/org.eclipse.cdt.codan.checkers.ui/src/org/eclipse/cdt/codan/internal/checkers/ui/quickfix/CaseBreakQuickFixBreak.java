/*******************************************************************************
 * Copyright (c) 2011, 2013 Gil Barash
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gil Barash - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ltk.core.refactoring.Change;

public class CaseBreakQuickFixBreak extends AbstractCaseBreakQuickFix {
	@Override
	public boolean isApplicable(IMarker marker) {
		int line = marker.getAttribute(IMarker.LINE_NUMBER, 0) - 1;
		if (line < 0)
			return false;
		return true;
	}

	@Override
	public String getLabel() {
		return QuickFixMessages.CaseBreakQuickFixBreak_Label;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		try {
			IASTTranslationUnit ast = getTranslationUnitViaEditor(marker).getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			IASTStatement beforeBreak = getStmtBeforeCaseEnd(marker, ast);
			if (beforeBreak != null && beforeBreak.getParent() instanceof IASTCompoundStatement) {
				IASTCompoundStatement enclosingStatement;
				IASTStatement after;
				if (beforeBreak instanceof IASTCompoundStatement) {
					// Case body is enclosed in braces. Add 'break' as last statement inside braces.
					enclosingStatement = (IASTCompoundStatement) beforeBreak;
					after = null;
				} else {
					enclosingStatement = (IASTCompoundStatement) beforeBreak.getParent();
					after = getStatementAfter(beforeBreak);
				}
				ASTRewrite r = ASTRewrite.create(enclosingStatement.getTranslationUnit());
				IASTBreakStatement breakStatement = ast.getASTNodeFactory().newBreakStatement();
				r.insertBefore(enclosingStatement, after, breakStatement, null);
				Change c = r.rewriteAST();
				c.perform(new NullProgressMonitor());
			}
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		} catch (BadLocationException e) {
			CheckersUiActivator.log(e);
		}
	}
}
