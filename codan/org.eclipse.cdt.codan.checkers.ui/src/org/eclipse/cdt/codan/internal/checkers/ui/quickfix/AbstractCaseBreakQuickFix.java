/*******************************************************************************
 * Copyright (c) 2017 Rolf Bislin
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rolf Bislin - Initial implementation (pulled up functions from CaseBreakQuickFixBreak)
 *     Gil Barash - getStmtBeforeBreak, getStatementAfter
 *******************************************************************************/ 
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

abstract public class AbstractCaseBreakQuickFix extends AbstractAstRewriteQuickFix {

	protected IASTStatement getStmtBeforeCaseEnd(IMarker marker, IASTTranslationUnit ast) throws BadLocationException {
		int line = marker.getAttribute(IMarker.LINE_NUMBER, 0) - 1;
		if (line < 0)
			return null;
		IDocument doc = getDocument();
		if(doc==null) doc=openDocument(marker);
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

	protected IASTStatement getStatementAfter(IASTStatement beforeBreak) {
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
