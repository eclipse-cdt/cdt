/*******************************************************************************
 * Copyright (c) 2017 Institute for Software, HSR Hochschule fuer Technik
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rolf Bislin - Initial implementation
 *     Gil Barash - getStmtBeforeBreak, getStatementAfter
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import java.util.Arrays;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;

abstract public class AbstractCaseBreakQuickFix extends AbstractAstRewriteQuickFix {
	protected void addNewNodeAtMarkedCaseEnd(IASTNode newnode, IASTTranslationUnit ast, IMarker marker) {
		try {
			IASTStatement beforeCaseEnd = getStmtBeforeCaseEnd(marker, ast);
			if (beforeCaseEnd != null && beforeCaseEnd.getParent() instanceof IASTCompoundStatement) {
				IASTCompoundStatement enclosingStatement;
				IASTStatement after;
				if (beforeCaseEnd instanceof IASTCompoundStatement) {
					// Case body is enclosed in braces. Add 'break' as last statement inside braces.
					enclosingStatement = (IASTCompoundStatement) beforeCaseEnd;
					after = null;
				} else {
					enclosingStatement = (IASTCompoundStatement) beforeCaseEnd.getParent();
					after = getNextStatement(beforeCaseEnd);
				}
				ASTRewrite r = ASTRewrite.create(enclosingStatement.getTranslationUnit());
				r.insertBefore(enclosingStatement, after, newnode, null);
				Change c = r.rewriteAST();
				c.perform(new NullProgressMonitor());
			}
		} catch (CoreException | BadLocationException e) {
			CheckersUiActivator.log(e);
		}
	}

	protected IASTStatement getStmtBeforeCaseEnd(IMarker marker, IASTTranslationUnit ast) throws BadLocationException {
		int line = marker.getAttribute(IMarker.LINE_NUMBER, 0) - 1;
		if (line < 0)
			return null;
		IDocument doc = getDocument();
		if (doc == null)
			doc = openDocument(marker);
		IRegion lineInformation = doc.getLineInformation(line);
		IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
		IASTNode containedNode = nodeSelector.findFirstContainedNode(lineInformation.getOffset(), lineInformation.getLength());
		IASTNode beforeCaseEndNode = null;
		if (containedNode != null) {
			beforeCaseEndNode = CxxAstUtils.getEnclosingStatement(containedNode);
		} else {
			beforeCaseEndNode = nodeSelector.findEnclosingNode(lineInformation.getOffset(), lineInformation.getLength());
		}
		if (beforeCaseEndNode instanceof IASTCompoundStatement) {
			while (beforeCaseEndNode != null) {
				if (beforeCaseEndNode.getParent() instanceof IASTCompoundStatement
						&& beforeCaseEndNode.getParent().getParent() instanceof IASTSwitchStatement) {
					return (IASTStatement) beforeCaseEndNode;
				}
				beforeCaseEndNode = beforeCaseEndNode.getParent();
			}
		}
		if (beforeCaseEndNode instanceof IASTStatement)
			return (IASTStatement) beforeCaseEndNode;
		return null;
	}

	protected IASTStatement getNextStatement(IASTStatement beforeStatement) {
		assert(beforeStatement != null);
		IASTNode parent = beforeStatement.getParent();
		if (parent instanceof IASTCompoundStatement) {
			IASTCompoundStatement enclosingStatement = (IASTCompoundStatement) parent;
			IASTStatement[] statements = enclosingStatement.getStatements();
			int indexOfNext = Arrays.asList(statements).indexOf(beforeStatement) + 1;
			if (indexOfNext < statements.length) {
				return statements[indexOfNext];
			}
		}
		return null;
	}
}
