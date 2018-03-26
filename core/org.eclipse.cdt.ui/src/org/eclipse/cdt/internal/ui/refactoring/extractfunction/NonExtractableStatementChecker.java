/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTRangeBasedForStatement;

/**
 * @author Emanuel Graf
 */
class NonExtractableStatementChecker extends AbstractSelectionChecker {
	private List<IASTNode> nodes;
	private boolean successful;

	public NonExtractableStatementChecker(List<IASTNode> nodes) {
		this.nodes = nodes;
	}

	@Override
	public boolean check() {
		successful = true;
		for (IASTNode node : nodes) {
			node.accept(new ASTVisitor() {
				{
					shouldVisitStatements = true;
				}

				@Override
				public int visit(IASTStatement statement) {
					if (statement instanceof IASTContinueStatement) {
						successful = false;
						errorMessage = Messages.ExtractFunctionRefactoring_Error_Continue;
						return PROCESS_SKIP;
					} else if (statement instanceof IASTBreakStatement) {
						successful = false;
						errorMessage = Messages.ExtractFunctionRefactoring_Error_Break;
						return PROCESS_SKIP;
					} else if (statement instanceof IASTForStatement
							|| statement instanceof ICPPASTRangeBasedForStatement
							|| statement instanceof IASTWhileStatement || statement instanceof IASTDoStatement
							|| statement instanceof IASTSwitchStatement ) {
						// Extracting a whole loop or switch statement is
						// allowed.
						return PROCESS_SKIP;
					} else if (statement instanceof IASTCaseStatement) {
						successful = false;
						errorMessage = Messages.ExtractFunctionRefactoring_Error_Case;
						return PROCESS_SKIP;
					} else if (statement instanceof IASTDefaultStatement) {
						successful = false;
						errorMessage = Messages.ExtractFunctionRefactoring_Error_Default;
						return PROCESS_SKIP;
					}
					return PROCESS_CONTINUE;
				}
			});
		}
		return successful;
	}
}
