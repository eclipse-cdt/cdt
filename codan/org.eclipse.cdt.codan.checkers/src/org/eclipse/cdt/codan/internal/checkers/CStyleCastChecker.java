/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
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
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class CStyleCastChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.CStyleCastProblem"; //$NON-NLS-1$
	public static final String PARAM_MACRO = "checkMacro"; //$NON-NLS-1$
	private boolean checkMacro = true;

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_MACRO, CheckersMessages.CStyleCastCheck_checkInMacro, true);
	}

	private boolean enclosedInMacroExpansion(IASTExpression statement) {
		IASTNodeLocation[] locations = statement.getNodeLocations();
		return locations.length == 1 && locations[0] instanceof IASTMacroExpansionLocation;
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		final IProblem pt = getProblemById(ERR_ID, getFile());
		checkMacro = (boolean) getPreference(pt, PARAM_MACRO);
		if (ast.getLinkage().getLinkageID() == ILinkage.CPP_LINKAGE_ID) {
			ast.accept(new ASTVisitor() {
				{
					shouldVisitExpressions = true;
				}

				@Override
				public int visit(IASTExpression expression) {
					if (expression instanceof IASTCastExpression
							&& (checkMacro || !enclosedInMacroExpansion(expression))) {
						if (((IASTCastExpression) expression).getOperator() == IASTCastExpression.op_cast)
							reportProblem(ERR_ID, expression);
					}
					return PROCESS_CONTINUE;
				}
			});
		}
	}
}
