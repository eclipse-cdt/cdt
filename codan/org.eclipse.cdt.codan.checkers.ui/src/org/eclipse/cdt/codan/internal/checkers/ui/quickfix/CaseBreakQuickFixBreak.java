/*******************************************************************************
 * Copyright (c) 2011 Gil Barash
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gil Barash  - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;

public class CaseBreakQuickFixBreak extends AbstractAstRewriteQuickFix {
	@Override
	public boolean isApplicable(IMarker marker) {
		int line = marker.getAttribute(IMarker.LINE_NUMBER, -1) - 1;
		if (line < 0)
			return false;
		return true;
	}

	@Override
	public String getLabel() {
		return Messages.CaseBreakQuickFixBreak_Label;
	}

	protected IASTStatement getStmtBeforeBreak(IMarker marker, IASTTranslationUnit ast) throws BadLocationException {
		int line = marker.getAttribute(IMarker.LINE_NUMBER, -1) - 1;
		if (line < 0)
			return null;
		IRegion lineInformation = getDocument().getLineInformation(line);
		IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
		IASTNode containedNode = nodeSelector.findFirstContainedNode(lineInformation.getOffset(), lineInformation.getLength());
		IASTNode beforeBreakNode = null;
		if (containedNode != null)
			beforeBreakNode = CxxAstUtils.getEnclosingStatement(containedNode);
		else
			beforeBreakNode = nodeSelector.findEnclosingNode(lineInformation.getOffset(), lineInformation.getLength());
		if (beforeBreakNode instanceof IASTCompoundStatement) {
			while (beforeBreakNode != null) {
				if (beforeBreakNode.getParent() instanceof IASTCompoundStatement
						&& beforeBreakNode.getParent().getParent() instanceof IASTSwitchStatement) {
					if (beforeBreakNode instanceof IASTCompoundStatement) {
						IASTStatement[] statements = ((IASTCompoundStatement) beforeBreakNode).getStatements();
						return statements[statements.length - 1]; // return last one
					}
					return (IASTStatement) beforeBreakNode;
				}
				beforeBreakNode = beforeBreakNode.getParent();
			}
		}
		if (beforeBreakNode instanceof IASTStatement)
			return (IASTStatement) beforeBreakNode;
		return null;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		try {
			IASTTranslationUnit ast = getTranslationUnitViaEditor(marker).getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			IASTStatement beforeBreak = getStmtBeforeBreak(marker, ast);
			if (beforeBreak.getParent() instanceof IASTCompoundStatement) {
				IASTCompoundStatement enclosingStatement = (IASTCompoundStatement) beforeBreak.getParent();
				IASTStatement after = getAfterStatement(beforeBreak);
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

	private IASTStatement getAfterStatement(IASTStatement beforeBreak) {
		IASTCompoundStatement enclosingStatement = (IASTCompoundStatement) beforeBreak.getParent();
		IASTStatement after = null;
		IASTStatement[] statements = enclosingStatement.getStatements();
		for (int i = 0; i < statements.length; i++) {
			IASTStatement st = statements[i];
			if (st == beforeBreak) {
				if (i < statements.length - 1) {
					after = statements[i + 1];
					break;
				}
			}
		}
		return after;
	}
}
