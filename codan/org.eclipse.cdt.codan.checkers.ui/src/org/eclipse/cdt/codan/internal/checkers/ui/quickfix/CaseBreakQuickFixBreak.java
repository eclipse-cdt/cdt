/*******************************************************************************
 * Copyright (c) 2011, 2013 Gil Barash
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gil Barash - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

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
			IASTTranslationUnit ast = getTranslationUnitViaEditor(marker).getAST(index,
					ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
			IASTBreakStatement breakStatement = ast.getASTNodeFactory().newBreakStatement();
			addNewNodeAtMarkedCaseEnd(breakStatement, ast, marker);
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		}
	}
}
