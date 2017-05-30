/*******************************************************************************
 * Copyright (c) 2017 Institute for Software, HSR Hochschule fuer Technik
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.param.RootProblemPreference;
import org.eclipse.cdt.codan.internal.checkers.CaseBreakChecker;
import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeList;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.StandardAttributes;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;

public class CaseBreakQuickFixFallthroughAttribute extends AbstractCaseBreakQuickFix {
	@Override
	public boolean isApplicable(IMarker marker) {
		IProblem problem = getProblem(marker);
		RootProblemPreference map = (RootProblemPreference) problem.getPreference();
		boolean enabled = (boolean) map.getChildValue(CaseBreakChecker.PARAM_ENABLE_FALLTHROUGH_QUICKFIX);
		boolean last_case_enabled = (boolean) map.getChildValue(CaseBreakChecker.PARAM_LAST_CASE);
		ITranslationUnit tu = getTranslationUnitViaWorkspace(marker);
		return enabled && tu != null && tu.isCXXLanguage() && (!last_case_enabled || validPositionForFallthrough(tu, marker));
	}

	@Override
	public String getLabel() {
		return QuickFixMessages.CaseBreakQuickFixFallthroughAttribute_Label;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		try {
			IASTTranslationUnit ast = getTranslationUnitViaEditor(marker).getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			ICPPNodeFactory factory = (ICPPNodeFactory) ast.getASTNodeFactory();
			IASTNullStatement nullStatement = factory.newNullStatement();
			nullStatement.addAttributeSpecifier(getFallthroughAttributeList(factory));
			addNewNodeAtMarkedCaseEnd(nullStatement, ast, marker);
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		}
	}

	private boolean validPositionForFallthrough(ITranslationUnit tu, IMarker marker) {
		try {
			IASTTranslationUnit ast = CModelUtil.toWorkingCopy(tu).getAST(null, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			IASTStatement beforeCaseEnd = getStmtBeforeCaseEnd(marker, ast);
			if (getNextStatement(beforeCaseEnd) == null)
				return false;
		} catch (CoreException | BadLocationException e) {
			e.printStackTrace();
		}
		return true;
	}

	private IASTAttributeList getFallthroughAttributeList(ICPPNodeFactory factory) {
		IASTAttribute attribute = factory.newAttribute(StandardAttributes.cFALLTHROUGH, null);
		IASTAttributeList attributeList = factory.newAttributeList();
		attributeList.addAttribute(attribute);
		return attributeList;
	}
}
