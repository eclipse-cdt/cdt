/*******************************************************************************
 * Copyright (c) 2017 Rolf Bislin
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rolf Bislin - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.param.RootProblemPreference;
import org.eclipse.cdt.codan.internal.checkers.CaseBreakChecker;
import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeList;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ltk.core.refactoring.Change;

public class CaseBreakQuickFixFallthroughAttribute extends AbstractCaseBreakQuickFix {
	@Override
	public boolean isApplicable(IMarker marker) {
		IProblem problem = getProblem(marker);
		RootProblemPreference map = (RootProblemPreference) problem.getPreference();
		boolean enabled = (boolean) map.getChildValue(CaseBreakChecker.PARAM_ENABLE_FALLTHROUGH_QUICKFIX);
		ITranslationUnit tu = getTranslationUnitViaEditor(marker);
		return enabled && tu.isCXXLanguage();
	}
	@Override
	public String getLabel() {
		return QuickFixMessages.CaseBreakQuickFixFallthroughAttribute_Label;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		try {
			IASTTranslationUnit ast = getTranslationUnitViaEditor(marker).getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
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
					after = getStatementAfter(beforeCaseEnd);
				}
				ASTRewrite r = ASTRewrite.create(enclosingStatement.getTranslationUnit());
				ICPPNodeFactory factory = (ICPPNodeFactory) ast.getASTNodeFactory();
				
				IASTNullStatement nullStatement = factory.newNullStatement();
				nullStatement.addAttributeSpecifier(getFallthroughAttributeList(factory));
				
				r.insertBefore(enclosingStatement, after, nullStatement, null);
				
				Change c = r.rewriteAST();
				c.perform(new NullProgressMonitor());
			}
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		} catch (BadLocationException e) {
			CheckersUiActivator.log(e);
		}
	}

	private IASTAttributeList getFallthroughAttributeList(ICPPNodeFactory factory) {
		IASTAttribute attribute = factory.newAttribute("fallthrough".toCharArray(), null); //$NON-NLS-1$
		
		IASTAttributeList attributeList = factory.newAttributeList();
		attributeList.addAttribute(attribute);
		return attributeList;
	}
}
